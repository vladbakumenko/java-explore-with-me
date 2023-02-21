package ru.practicum.server.repository;

import ru.practicum.dto.StatResponseDto;
import ru.practicum.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatServerRepository {

    void save(EndpointHit endpointHit);

    List<StatResponseDto> findAllStats(List<String> uris, LocalDateTime start, LocalDateTime end, boolean unique);

}
