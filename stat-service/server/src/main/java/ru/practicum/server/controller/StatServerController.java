package ru.practicum.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitRequestDto;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.server.service.StatService;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class StatServerController {

    private final StatService statService;

    @PostMapping("/hit")
    @ResponseStatus(value = HttpStatus.CREATED)
    public void addHit(@RequestBody @Valid EndpointHitRequestDto requestDto) {
        statService.addHit(requestDto);
    }

    @GetMapping("/stats")
    public List<StatResponseDto> getStats(@RequestParam String start,
                                          @RequestParam String end,
                                          @RequestParam List<String> uris,
                                          @RequestParam(defaultValue = "false") boolean unique) {
        return statService.getStats(start, end, uris, unique);
    }
}
