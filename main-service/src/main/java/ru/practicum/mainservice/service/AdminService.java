package ru.practicum.mainservice.service;

import ru.practicum.mainservice.dto.category.CategoryCreationDto;
import ru.practicum.mainservice.dto.category.CategoryDto;
import ru.practicum.mainservice.dto.compilation.CompilationCreationDto;
import ru.practicum.mainservice.dto.compilation.CompilationDto;
import ru.practicum.mainservice.dto.compilation.UpdateCompilationRequestDto;
import ru.practicum.mainservice.dto.event.EventFullDto;
import ru.practicum.mainservice.dto.event.UpdateEventRequestDto;
import ru.practicum.mainservice.dto.user.UserFullDto;
import ru.practicum.mainservice.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminService {

    UserFullDto addUser(UserFullDto userFullDto);

    List<UserFullDto> getUsers(List<Long> ids, int from, int size);

    void deleteUser(long userId);

    CategoryDto addCategory(CategoryCreationDto categoryCreationDto);

    void deleteCategory(int catId);

    CategoryDto updateCategory(int catId, CategoryCreationDto categoryCreationDto);

    EventFullDto updateEvent(long eventId, UpdateEventRequestDto updateEventRequestDto);

    List<EventFullDto> getEvents(List<Long> users, List<EventState> states, List<Integer> categories,
                                 LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    CompilationDto createCompilation(CompilationCreationDto compilationCreationDto);

    void deleteCompilation(int compId);

    CompilationDto updateCompilation(int compId, UpdateCompilationRequestDto updateCompilationRequestDto);
}
