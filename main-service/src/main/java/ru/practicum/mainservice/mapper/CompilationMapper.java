package ru.practicum.mainservice.mapper;

import ru.practicum.mainservice.dto.compilation.CompilationCreationDto;
import ru.practicum.mainservice.dto.compilation.CompilationDto;
import ru.practicum.mainservice.dto.compilation.UpdateCompilationRequestDto;
import ru.practicum.mainservice.model.Compilation;
import ru.practicum.mainservice.model.Event;

import java.util.List;
import java.util.stream.Collectors;

public class CompilationMapper {

    public static Compilation toCompilation(CompilationCreationDto compilationCreationDto, List<Event> events) {
        return Compilation.builder()
                .title(compilationCreationDto.getTitle())
                .pinned(compilationCreationDto.isPinned())
                .events(events)
                .build();
    }

    public static CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(compilation.getEvents().stream().map(EventMapper::toEventShortDto).collect(Collectors.toList()))
                .build();
    }

    public static Compilation fromUpdateDtoToCompilation(UpdateCompilationRequestDto updateDto,
                                                         Compilation compilation, List<Event> events) {
        compilation.setTitle(updateDto.getTitle() == null ? compilation.getTitle() : updateDto.getTitle());
        compilation.setPinned(updateDto.getPinned() == null ? compilation.isPinned() : updateDto.getPinned());
        compilation.setEvents(updateDto.getEvents() == null ? compilation.getEvents() : events);

        return compilation;
    }
}
