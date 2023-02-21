package ru.practicum.server.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.EndpointHitRequestDto;
import ru.practicum.server.model.EndpointHit;

@Component
public class StatServerMapper {

    public static EndpointHit toEndpointHit(EndpointHitRequestDto requestDto) {
        return EndpointHit.builder()
                .app(requestDto.getApp())
                .uri(requestDto.getUri())
                .ip(requestDto.getIp())
                .build();
    }
}
