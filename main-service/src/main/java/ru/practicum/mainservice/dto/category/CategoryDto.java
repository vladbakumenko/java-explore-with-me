package ru.practicum.mainservice.dto.category;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Data
@Builder
public class CategoryDto {
    @Positive
    private int id;
    @NotBlank
    private String name;
}
