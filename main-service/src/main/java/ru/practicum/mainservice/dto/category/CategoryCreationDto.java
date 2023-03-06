package ru.practicum.mainservice.dto.category;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CategoryCreationDto {
    @NotBlank
    private String name;
}
