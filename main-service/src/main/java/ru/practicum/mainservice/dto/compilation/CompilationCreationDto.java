package ru.practicum.mainservice.dto.compilation;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@Data
@Builder
public class CompilationCreationDto {

    private Set<Long> events;
    private boolean pinned;

    @NotBlank
    private String title;
}
