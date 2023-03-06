package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.category.CategoryCreationDto;
import ru.practicum.mainservice.dto.category.CategoryDto;
import ru.practicum.mainservice.dto.compilation.CompilationCreationDto;
import ru.practicum.mainservice.dto.compilation.CompilationDto;
import ru.practicum.mainservice.dto.compilation.UpdateCompilationRequestDto;
import ru.practicum.mainservice.dto.event.EventFullDto;
import ru.practicum.mainservice.dto.event.UpdateEventRequestDto;
import ru.practicum.mainservice.dto.user.UserFullDto;
import ru.practicum.mainservice.model.EventState;
import ru.practicum.mainservice.service.AdminService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin")
public class AdminApiController {

    private final AdminService adminService;

    @PostMapping("/users")
    @ResponseStatus(code = HttpStatus.CREATED)
    public UserFullDto addUser(@RequestBody @Valid UserFullDto userFullDto) {
        return adminService.addUser(userFullDto);
    }

    @GetMapping("/users")
    public List<UserFullDto> getUsers(@RequestParam List<Long> ids,
                                      @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                      @RequestParam(defaultValue = "10") @Positive int size) {
        return adminService.getUsers(ids, from, size);
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @Positive long userId) {
        adminService.deleteUser(userId);
    }

    @PostMapping("/categories")
    @ResponseStatus(code = HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody @Valid CategoryCreationDto categoryCreationDto) {
        return adminService.addCategory(categoryCreationDto);
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable @Positive int catId) {
        adminService.deleteCategory(catId);
    }

    @PatchMapping("/categories/{catId}")
    public CategoryDto updateCategory(@PathVariable @Positive int catId,
                                      @RequestBody @Valid CategoryCreationDto categoryCreationDto) {
        return adminService.updateCategory(catId, categoryCreationDto);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable @Positive long eventId,
                                    @RequestBody @Valid UpdateEventRequestDto updateEventRequestDto) {
        return adminService.updateEvent(eventId, updateEventRequestDto);
    }

    @GetMapping("/events")
    public List<EventFullDto> getEvents(@RequestParam(defaultValue = "") List<Long> users,
                                        @RequestParam(defaultValue = "") List<EventState> states,
                                        @RequestParam(defaultValue = "") List<Integer> categories,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                        @RequestParam(defaultValue = "10") @Positive int size) {
        return adminService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PostMapping("/compilations")
    @ResponseStatus(value = HttpStatus.CREATED)
    public CompilationDto createCompilation(@RequestBody @Valid CompilationCreationDto compilationCreationDto) {
        return adminService.createCompilation(compilationCreationDto);
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable @Positive int compId) {
        adminService.deleteCompilation(compId);
    }

    @PatchMapping("/compilations/{compId}")
    public CompilationDto updateCompilation(@PathVariable @Positive int compId,
                                            @RequestBody UpdateCompilationRequestDto updateCompilationRequestDto) {
        return adminService.updateCompilation(compId, updateCompilationRequestDto);
    }
}
