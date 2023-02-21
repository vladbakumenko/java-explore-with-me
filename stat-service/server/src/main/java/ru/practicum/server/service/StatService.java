package ru.practicum.server.service;

import ru.practicum.dto.EndpointHitRequestDto;
import ru.practicum.dto.StatResponseDto;

import java.util.List;

public interface StatService {
    void addHit(EndpointHitRequestDto requestDto);

    List<StatResponseDto> getStats(String start, String end, List<String> uris, boolean unique);
}
