package ru.practicum.mainservice.service;

import ru.practicum.mainservice.dto.category.CategoryDto;
import ru.practicum.mainservice.dto.compilation.CompilationDto;
import ru.practicum.mainservice.dto.event.EventFullDto;
import ru.practicum.mainservice.dto.event.EventShortDto;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicService {

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(int catId);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilationById(int compId);

    List<EventShortDto> getEvents(String text, List<Integer> categories, Boolean paid, LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd, boolean onlyAvailable, String sort, int from, int size);

    EventFullDto getEventById(long eventId);
}
