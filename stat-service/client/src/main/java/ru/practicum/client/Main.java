package ru.practicum.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import ru.practicum.dto.EndpointHitRequestDto;
import ru.practicum.dto.StatResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        StatClient statClient = new StatClient("http://localhost:9090", new RestTemplateBuilder());
        EndpointHitRequestDto endpointHitRequestDto = new EndpointHitRequestDto("ewm-main-service", "/events/1", "192.163.0.1",
                LocalDateTime.parse("2022-09-06 11:00:23"));

        statClient.addHit(endpointHitRequestDto);

        List<StatResponseDto> stats = statClient.getStats("2020-05-05 00:00:00", "2023-05-05 00:00:00", List.of("/events/1"), false);

        System.out.println(stats);
    }
}
