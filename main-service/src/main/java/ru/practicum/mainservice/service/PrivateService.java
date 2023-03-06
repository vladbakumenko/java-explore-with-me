package ru.practicum.mainservice.service;

import ru.practicum.mainservice.dto.event.EventCreationDto;
import ru.practicum.mainservice.dto.event.EventFullDto;
import ru.practicum.mainservice.dto.event.EventShortDto;
import ru.practicum.mainservice.dto.event.UpdateEventRequestDto;
import ru.practicum.mainservice.dto.request.RequestDto;
import ru.practicum.mainservice.dto.request.RequestStatusUpdateRequestDto;
import ru.practicum.mainservice.dto.request.RequestStatusUpdateResultDto;

import java.util.List;

public interface PrivateService {

    EventFullDto addEvent(long userId, EventCreationDto eventCreationDto);

    List<EventShortDto> getAllUserEvents(long userId, int from, int size);

    EventFullDto getEventByUserAndEventId(long userId, long eventId);

    EventFullDto updateEvent(long userId, long eventId, UpdateEventRequestDto updateEventRequestDto);

    RequestDto createRequest(long userId, long eventId);

    List<RequestDto> getRequestsByEventId(long userId, long eventId);

    List<RequestDto> getUserRequests(long userId);

    RequestDto cancelRequest(long userId, long requestId);

    RequestStatusUpdateResultDto updateRequestsStatus(long userId, long eventId, RequestStatusUpdateRequestDto updateDto);
}
