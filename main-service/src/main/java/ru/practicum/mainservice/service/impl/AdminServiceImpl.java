package ru.practicum.mainservice.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.constants.Constants;
import ru.practicum.mainservice.dto.category.CategoryCreationDto;
import ru.practicum.mainservice.dto.category.CategoryDto;
import ru.practicum.mainservice.dto.compilation.CompilationCreationDto;
import ru.practicum.mainservice.dto.compilation.CompilationDto;
import ru.practicum.mainservice.dto.compilation.UpdateCompilationRequestDto;
import ru.practicum.mainservice.dto.event.EventFullDto;
import ru.practicum.mainservice.dto.event.UpdateEventRequestDto;
import ru.practicum.mainservice.dto.user.UserFullDto;
import ru.practicum.mainservice.exception.ConflictException;
import ru.practicum.mainservice.exception.NotFoundException;
import ru.practicum.mainservice.mapper.*;
import ru.practicum.mainservice.model.*;
import ru.practicum.mainservice.pagination.OffsetBasedPageRequest;
import ru.practicum.mainservice.repository.CategoryRepository;
import ru.practicum.mainservice.repository.CompilationRepository;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.UserRepository;
import ru.practicum.mainservice.service.AdminService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;

    @Transactional
    @Override
    public UserFullDto addUser(UserFullDto userFullDto) {
        User user = userRepository.save(UserMapper.toUser(userFullDto));

        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserFullDto> getUsers(List<Long> ids, int from, int size) {
        Pageable pageable = new OffsetBasedPageRequest(from, size, Sort.by(Sort.Direction.DESC, "id"));

        return userRepository.findAllByIdIn(ids, pageable).getContent().stream()
                .map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteUser(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id=%d was not found", userId));
        }

        userRepository.deleteById(userId);
    }

    @Transactional
    @Override
    public CategoryDto addCategory(CategoryCreationDto categoryCreationDto) {
        Category category = categoryRepository.save(CategoryMapper.toCategory(categoryCreationDto));

        return CategoryMapper.toCategoryDto(category);
    }

    @Transactional
    @Override
    public void deleteCategory(int catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException(String.format("Category with id=%d was not found", catId));
        }

        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("The category is not empty");
        }

        categoryRepository.deleteById(catId);
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(int catId, CategoryCreationDto categoryCreationDto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", catId)));

        category.setName(categoryCreationDto.getName());

        return CategoryMapper.toCategoryDto(category);
    }

    @Transactional
    @Override
    public EventFullDto updateEvent(long eventId, UpdateEventRequestDto updateEventRequestDto) {
        LocalDateTime eventDate = null;

        if (updateEventRequestDto.getEventDate() != null) {
            eventDate = DateTimeMapper.toLocalDateTime(updateEventRequestDto.getEventDate());
            throwIfEventDateIsNotLaterOneHourAfterNow(eventDate);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        StateAction state = null;
        String stateAction = updateEventRequestDto.getStateAction();

        if (stateAction != null) {
            if (!StateAction.PUBLISH_EVENT.toString().equals(stateAction) && !StateAction.REJECT_EVENT.toString().equals(stateAction)) {
                throw new ConflictException("Field StateAction is incorrect");
            }
            state = StateAction.valueOf(stateAction);

            if (!event.getState().equals(EventState.PENDING) && state.equals(StateAction.PUBLISH_EVENT)) {
                throw new ConflictException("Event must be PENDING state to be published");
            }
            if (event.getState().equals(EventState.PUBLISHED) && state.equals(StateAction.REJECT_EVENT)) {
                throw new ConflictException("Event cannot be canceled if already published");
            }
        }

        Category category = null;
        if (updateEventRequestDto.getCategory() != null) {
            int idCat = updateEventRequestDto.getCategory();
            category = categoryRepository.findById(idCat)
                    .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", idCat)));
        }

        EventMapper.fromUpdateDtoToEvent(updateEventRequestDto, event, category, eventDate, state);

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventFullDto> getEvents(List<Long> users, List<EventState> states, List<Integer> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        List<Event> result;
        Pageable pageable = new OffsetBasedPageRequest(from, size, Constants.SORT_BY_ID_DESC);

        BooleanExpression byUsersId = QEvent.event.initiator.id.in(users);
        BooleanExpression byStates = QEvent.event.state.in(states);
        BooleanExpression byCategory = QEvent.event.category.id.in(categories);

        BooleanExpression byEventDate;
        if (rangeStart == null && rangeEnd == null) {
            result = eventRepository.findAll(byUsersId.and(byStates).and(byCategory), pageable).getContent();
            return EventMapper.toListOfEventFullDto(result);
        } else if (rangeStart == null) {
            byEventDate = QEvent.event.eventDate.before(rangeEnd);
        } else if (rangeEnd == null) {
            byEventDate = QEvent.event.eventDate.after(rangeStart);
        } else {
            byEventDate = QEvent.event.eventDate.between(rangeStart, rangeEnd);
        }

        result = eventRepository.findAll(byUsersId.and(byStates).and(byCategory).and(byEventDate), pageable).getContent();
        return EventMapper.toListOfEventFullDto(result);
    }

    @Transactional
    @Override
    public @Valid CompilationDto createCompilation(CompilationCreationDto compilationCreationDto) {
        Set<Long> idEvents = compilationCreationDto.getEvents();
        List<Event> events = new ArrayList<>();

        if (idEvents != null && !idEvents.isEmpty()) {
            events = eventRepository.findAllByIdIn(idEvents);
        }

        Compilation compilation = CompilationMapper.toCompilation(compilationCreationDto, events);

        compilationRepository.save(compilation);

        return CompilationMapper.toCompilationDto(compilation);
    }

    @Transactional
    @Override
    public void deleteCompilation(int compId) {
        boolean exist = compilationRepository.existsById(compId);
        if (!exist) {
            throw new NotFoundException(String.format("Compilation with id=%d was not found", compId));
        }

        compilationRepository.deleteById(compId);
    }

    @Transactional
    @Override
    public @Valid CompilationDto updateCompilation(int compId, UpdateCompilationRequestDto updateCompilationRequestDto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d was not found", compId)));

        Set<Long> idEvents = updateCompilationRequestDto.getEvents();
        List<Event> events = new ArrayList<>();

        if (idEvents != null && !idEvents.isEmpty()) {
            events = eventRepository.findAllByIdIn(idEvents);
        }

        CompilationMapper.fromUpdateDtoToCompilation(updateCompilationRequestDto, compilation, events);

        return CompilationMapper.toCompilationDto(compilation);
    }
    
    private void throwIfEventDateIsNotLaterOneHourAfterNow(LocalDateTime eventDate) {
        LocalDateTime timestamp = LocalDateTime.now().plusHours(1);
        if (eventDate.isBefore(timestamp)) {
            throw new ConflictException("Event cannot start earlier than 1 hours from now");
        }
    }
}
