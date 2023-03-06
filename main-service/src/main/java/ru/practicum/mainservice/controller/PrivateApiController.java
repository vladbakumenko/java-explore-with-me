package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.event.EventCreationDto;
import ru.practicum.mainservice.dto.event.EventFullDto;
import ru.practicum.mainservice.dto.event.EventShortDto;
import ru.practicum.mainservice.dto.event.UpdateEventRequestDto;
import ru.practicum.mainservice.dto.request.RequestDto;
import ru.practicum.mainservice.dto.request.RequestStatusUpdateRequestDto;
import ru.practicum.mainservice.dto.request.RequestStatusUpdateResultDto;
import ru.practicum.mainservice.service.PrivateService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class PrivateApiController {

    private final PrivateService privateService;

    @PostMapping("/{userId}/events")
    @ResponseStatus(value = HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable @Positive long userId,
                                 @RequestBody @Valid EventCreationDto eventCreationDto) {
        return privateService.addEvent(userId, eventCreationDto);
    }

    @GetMapping("/{userId}/events")
    public List<EventShortDto> getAllUserEvents(@PathVariable @Positive long userId,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                @RequestParam(defaultValue = "10") @Positive int size) {
        return privateService.getAllUserEvents(userId, from, size);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getEventByUserAndEventId(@PathVariable @Positive long userId,
                                                 @PathVariable @Positive long eventId) {
        return privateService.getEventByUserAndEventId(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable @Positive long userId,
                                    @PathVariable @Positive long eventId,
                                    @RequestBody @Valid UpdateEventRequestDto updateEventRequestDto) {
        return privateService.updateEvent(userId, eventId, updateEventRequestDto);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(value = HttpStatus.CREATED)
    public RequestDto createRequest(@PathVariable @Positive long userId,
                                    @RequestParam @Positive long eventId) {
        return privateService.createRequest(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    public List<RequestDto> getUserRequests(@PathVariable @Positive long userId) {
        return privateService.getUserRequests(userId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public RequestDto cancelRequest(@PathVariable @Positive long userId,
                                    @PathVariable @Positive long requestId) {
        return privateService.cancelRequest(userId, requestId);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<RequestDto> getRequestsByEventId(@PathVariable @Positive long userId,
                                                 @PathVariable @Positive long eventId) {
        return privateService.getRequestsByEventId(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public RequestStatusUpdateResultDto updateRequestsStatus(@PathVariable @Positive long userId,
                                                             @PathVariable @Positive long eventId,
                                                             @RequestBody @Valid RequestStatusUpdateRequestDto updateDto) {
        return privateService.updateRequestsStatus(userId, eventId, updateDto);
    }
}
