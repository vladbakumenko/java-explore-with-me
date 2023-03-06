package ru.practicum.mainservice.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatClient;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.mainservice.constants.Constants;
import ru.practicum.mainservice.dto.category.CategoryDto;
import ru.practicum.mainservice.dto.compilation.CompilationDto;
import ru.practicum.mainservice.dto.event.EventFullDto;
import ru.practicum.mainservice.dto.event.EventShortDto;
import ru.practicum.mainservice.dto.event.EventSortOption;
import ru.practicum.mainservice.exception.BadRequestException;
import ru.practicum.mainservice.exception.NotFoundException;
import ru.practicum.mainservice.exception.StatServerConnectException;
import ru.practicum.mainservice.mapper.CategoryMapper;
import ru.practicum.mainservice.mapper.CompilationMapper;
import ru.practicum.mainservice.mapper.DateTimeMapper;
import ru.practicum.mainservice.mapper.EventMapper;
import ru.practicum.mainservice.model.*;
import ru.practicum.mainservice.pagination.OffsetBasedPageRequest;
import ru.practicum.mainservice.repository.CategoryRepository;
import ru.practicum.mainservice.repository.CompilationRepository;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.service.PublicService;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PublicServiceImpl implements PublicService {

    private final CategoryRepository categoryRepository;
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final StatClient statClient;

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = new OffsetBasedPageRequest(from, size, Sort.by(Sort.Direction.ASC, "id"));
        return categoryRepository.findAll(pageable).getContent().stream()
                .map(CategoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategory(int catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", catId)));
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = new OffsetBasedPageRequest(from, size, Constants.SORT_BY_ID_DESC);
        List<Compilation> compilations = new ArrayList<>();

        if (pinned != null) {
            BooleanExpression byPinned = QCompilation.compilation.pinned.eq(pinned);
            compilations = compilationRepository.findAll(byPinned, pageable).getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        return compilations.stream().map(CompilationMapper::toCompilationDto).collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(int compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d was not found", compId)));

        return CompilationMapper.toCompilationDto(compilation);
    }

    @Transient
    @Override
    public List<EventShortDto> getEvents(String text, List<Integer> categories, Boolean paid, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, boolean onlyAvailable, String sort, int from, int size) {
        List<Event> result;
        Sort sortOption = Constants.SORT_BY_ID_DESC;
        if (sort.equals(EventSortOption.EVENT_DATE.toString())) {
            sortOption = Sort.by(Sort.Direction.ASC, "eventDate");
        } else if (sort.equals(EventSortOption.VIEWS.toString())) {
            sortOption = Sort.by(Sort.Direction.DESC, "views");
        }
        Pageable pageable = new OffsetBasedPageRequest(from, size, sortOption);

        BooleanExpression byPublishState = QEvent.event.state.eq(EventState.PUBLISHED);
        BooleanExpression byText = QEvent.event.annotation.containsIgnoreCase(text)
                .or(QEvent.event.description.containsIgnoreCase(text));
        BooleanExpression byCategories = QEvent.event.category.id.in(categories);
        BooleanExpression byPaid = QEvent.event.paid.eq(paid); // ?

        BooleanExpression byEventDate;
        if (rangeStart == null && rangeEnd == null) {
            LocalDateTime now = LocalDateTime.now();
            byEventDate = QEvent.event.eventDate.after(now);
        } else if (rangeStart == null) {
            byEventDate = QEvent.event.eventDate.before(rangeEnd);
        } else if (rangeEnd == null) {
            byEventDate = QEvent.event.eventDate.after(rangeStart);
        } else {
            byEventDate = QEvent.event.eventDate.between(rangeStart, rangeEnd);
        }

        if (onlyAvailable) {
            BooleanExpression byAvailable = QEvent.event.participantLimit.ne(QEvent.event.confirmedRequests);
            result = eventRepository.findAll(byPublishState.and(byText).and(byCategories)
                    .and(byPaid).and(byEventDate).and(byAvailable), pageable).getContent();
        } else {
            result = eventRepository.findAll(byPublishState.and(byText).and(byCategories).and(byPaid).and(byEventDate), pageable).getContent();
        }

        try {
            Map<Long, Long> hits = getHits(result);
            for (Event event : result) {
                event.setViews(hits.getOrDefault(event.getId(), 0L));
            }
        } catch (StatServerConnectException e) {
            e.getMessage();
        }

        return EventMapper.toListOfEventShortDto(result);
    }

    @Override
    @Transient
    public EventFullDto getEventById(long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new BadRequestException("Event is not available because it has not been published yet");
        }

        try {
            Map<Long, Long> hits = getHits(List.of(event));
            event.setViews(hits.getOrDefault(eventId, 0L));
        } catch (StatServerConnectException e) {
            e.getMessage();
        }

        return EventMapper.toEventFullDto(event);
    }

    private Map<Long, Long> getHits(List<Event> events) {
        List<Long> ids = events.stream().map(Event::getId).collect(Collectors.toList());
        List<String> uris = ids.stream().map(id -> String.format("/events/%d", id)).collect(Collectors.toList());
        List<StatResponseDto> stats = new ArrayList<>();

        try {
            stats = statClient.getStats(DateTimeMapper.fromLocalDateTimeToString(LocalDateTime.now().minusYears(10)),
                    DateTimeMapper.fromLocalDateTimeToString(LocalDateTime.now().plusYears(10)), uris, true);
        } catch (RuntimeException e) {
            throw new StatServerConnectException("Stat-server is not responding.");
        }

        Map<Long, Long> hits = new HashMap<>();
        for (StatResponseDto stat : stats) {
            Long id = Long.valueOf(stat.getUri().substring(8));
            hits.put(id, stat.getHits());
        }

        return hits;
    }
}
