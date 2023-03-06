package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.constants.Constants;
import ru.practicum.mainservice.dto.event.EventCreationDto;
import ru.practicum.mainservice.dto.event.EventFullDto;
import ru.practicum.mainservice.dto.event.EventShortDto;
import ru.practicum.mainservice.dto.event.UpdateEventRequestDto;
import ru.practicum.mainservice.dto.request.RequestDto;
import ru.practicum.mainservice.dto.request.RequestStatusUpdateRequestDto;
import ru.practicum.mainservice.dto.request.RequestStatusUpdateResultDto;
import ru.practicum.mainservice.dto.request.StatusOfUpdateRequest;
import ru.practicum.mainservice.exception.ConflictException;
import ru.practicum.mainservice.exception.NotFoundException;
import ru.practicum.mainservice.mapper.DateTimeMapper;
import ru.practicum.mainservice.mapper.EventMapper;
import ru.practicum.mainservice.mapper.RequestMapper;
import ru.practicum.mainservice.model.*;
import ru.practicum.mainservice.pagination.OffsetBasedPageRequest;
import ru.practicum.mainservice.repository.CategoryRepository;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.RequestRepository;
import ru.practicum.mainservice.repository.UserRepository;
import ru.practicum.mainservice.service.PrivateService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PrivateServiceImpl implements PrivateService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    @Transactional
    @Override
    public @Valid EventFullDto addEvent(long userId, EventCreationDto eventCreationDto) {
        LocalDateTime eventDate = DateTimeMapper.toLocalDateTime(eventCreationDto.getEventDate());
        throwIfEventDateIsNotLaterTwoHoursAfterNow(eventDate);

        User user = throwIfUserNotFoundOrReturnIfExist(userId);

        int categoryId = eventCreationDto.getCategory();
        Category category = throwIfCategoryNotFoundOrReturnIfExist(categoryId);

        Event event = EventMapper.toEvent(eventCreationDto);
        event.setEventDate(eventDate);
        event.setInitiator(user);
        event.setCategory(category);
        event.setState(EventState.PENDING);

        eventRepository.save(event);

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getAllUserEvents(long userId, int from, int size) {
        Pageable pageable = new OffsetBasedPageRequest(from, size, Constants.SORT_BY_ID_DESC);

        return eventRepository.findAllByInitiatorId(userId, pageable).getContent().stream()
                .map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventByUserAndEventId(long userId, long eventId) {
        Event event = throwIfEventFromCorrectUserNotFoundOrReturnIfExist(eventId, userId);

        return EventMapper.toEventFullDto(event);
    }

    @Transactional
    @Override
    public @Valid EventFullDto updateEvent(long userId, long eventId, UpdateEventRequestDto updateEventRequestDto) {
        LocalDateTime eventDate = null;
        if (updateEventRequestDto.getEventDate() != null) {
            eventDate = DateTimeMapper.toLocalDateTime(updateEventRequestDto.getEventDate());
            throwIfEventDateIsNotLaterTwoHoursAfterNow(eventDate);
        }

        String stateAction = updateEventRequestDto.getStateAction();
        if (!StateAction.SEND_TO_REVIEW.toString().equals(stateAction) && !StateAction.CANCEL_REVIEW.toString().equals(stateAction)) {
            throw new ConflictException("Field StateAction is incorrect");
        }

        StateAction state = StateAction.valueOf(stateAction);

        Category category = null;
        if (updateEventRequestDto.getCategory() != null) {
            category = throwIfCategoryNotFoundOrReturnIfExist(updateEventRequestDto.getCategory());
        }

        Event event = throwIfEventFromCorrectUserNotFoundOrReturnIfExist(eventId, userId);

        EventMapper.fromUpdateDtoToEvent(updateEventRequestDto, event, category, eventDate, state);

        return EventMapper.toEventFullDto(event);
    }

    @Transactional
    @Override
    public RequestDto createRequest(long userId, long eventId) {
        Event event = throwIfEventNotFoundOrReturnIfExist(eventId);
        User user = throwIfUserNotFoundOrReturnIfExist(userId);
        Request request;

        if (userId == event.getInitiator().getId()) {
            throw new ConflictException("Initiator of event cannot be requester");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Сan't participate in an unpublished event");
        }
        if (event.getParticipantLimit() == event.getConfirmedRequests()) {
            throw new ConflictException("Participation limit expired");
        }

        if (!event.isRequestModeration()) {
            request = Request.builder()
                    .event(event)
                    .requester(user)
                    .status(RequestStatus.CONFIRMED)
                    .build();
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        } else {
            request = Request.builder()
                    .event(event)
                    .requester(user)
                    .status(RequestStatus.PENDING)
                    .build();
        }

        requestRepository.save(request);

        return RequestMapper.toRequestDto(request);
    }

    @Override
    public List<RequestDto> getRequestsByEventId(long userId, long eventId) {
        throwIfEventFromCorrectUserNotFoundOrReturnIfExist(eventId, userId);

        List<Request> requests = requestRepository.findAllByEventId(eventId);

        if (requests == null) {
            return Collections.emptyList();
        }

        return RequestMapper.toListOfRequestDto(requests);
    }

    @Override
    public List<RequestDto> getUserRequests(long userId) {
        List<Request> requests = requestRepository.findAllByRequesterId(userId);

        if (requests == null) {
            return Collections.emptyList();
        }

        return RequestMapper.toListOfRequestDto(requests);
    }

    @Transactional
    @Override
    public RequestDto cancelRequest(long userId, long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with id=%d was not found", requestId)));

        request.setStatus(RequestStatus.CANCELED);

        return RequestMapper.toRequestDto(request);
    }

    @Transactional
    @Override
    public RequestStatusUpdateResultDto updateRequestsStatus(long userId, long eventId,
                                                             RequestStatusUpdateRequestDto updateDto) {
        Event event = throwIfEventFromCorrectUserNotFoundOrReturnIfExist(eventId, userId);
        List<Request> requests = requestRepository.findAllByIdIn(updateDto.getRequestIds());

        RequestStatusUpdateResultDto resultDto = RequestStatusUpdateResultDto.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(new ArrayList<>())
                .build();

        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            resultDto.getConfirmedRequests().addAll(RequestMapper.toListOfRequestDto(requests));
            return resultDto;
        }
        if (event.getParticipantLimit() == event.getConfirmedRequests()) {
            throw new ConflictException("Limit of requests for participation is over");
        }
        if (!requests.stream().allMatch(r -> r.getStatus().equals(RequestStatus.PENDING))) {
            throw new ConflictException("All requests should be in status PENDING");
        }
        if (updateDto.getStatus().equals(StatusOfUpdateRequest.REJECTED)) {
            requests.forEach(r -> r.setStatus(RequestStatus.REJECTED));
            resultDto.getRejectedRequests().addAll(RequestMapper.toListOfRequestDto(requests));
            return resultDto;
        }

        int reserve = event.getParticipantLimit() - event.getConfirmedRequests();

        for (Request request : requests) {
            if (reserve > 0) {
                request.setStatus(RequestStatus.CONFIRMED);
                resultDto.getConfirmedRequests().add(RequestMapper.toRequestDto(request));
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                --reserve;
            } else {
                request.setStatus(RequestStatus.REJECTED);
                resultDto.getRejectedRequests().add(RequestMapper.toRequestDto(request));
            }
        }
        return resultDto;
    }

    private void throwIfEventDateIsNotLaterTwoHoursAfterNow(LocalDateTime eventDate) {
        LocalDateTime timestamp = LocalDateTime.now().plusHours(2);
        if (eventDate.isBefore(timestamp)) {
            throw new ConflictException("Event cannot start earlier than 2 hours from now");
        }
    }

    private User throwIfUserNotFoundOrReturnIfExist(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
    }

    private Category throwIfCategoryNotFoundOrReturnIfExist(int categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", categoryId)));
    }

    private Event throwIfEventFromCorrectUserNotFoundOrReturnIfExist(long eventId, long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
    }

    private Event throwIfEventNotFoundOrReturnIfExist(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
    }
}
