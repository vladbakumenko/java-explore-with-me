package ru.practicum.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitRequestDto;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.server.mapper.StatServerMapper;
import ru.practicum.server.model.EndpointHit;
import ru.practicum.server.repository.StatServerRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class StatServiceImpl implements StatService {
    private final StatServerRepository statServerRepository;

    @Override
    public void addHit(EndpointHitRequestDto requestDto) {
        EndpointHit endpointHit = StatServerMapper.toEndpointHit(requestDto);
        endpointHit.setTimestamp(toLocalDateTime(requestDto.getTimestamp()));

        statServerRepository.save(endpointHit);
    }

    @Override
    public List<StatResponseDto> getStats(String start, String end, List<String> uris, boolean unique) {
        LocalDateTime startDateTime = toLocalDateTime(start);
        LocalDateTime endDateTime = toLocalDateTime(end);

        if (uris.isEmpty()) {
            return Collections.emptyList();
        }

        return statServerRepository.findAllStats(uris, startDateTime, endDateTime, unique);
    }

    private static LocalDateTime toLocalDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
