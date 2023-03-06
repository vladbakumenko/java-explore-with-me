package ru.practicum.mainservice.dto.compilation;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UpdateCompilationRequestDto {
    private String title;
    private Boolean pinned;
    private Set<Long> events;
}
