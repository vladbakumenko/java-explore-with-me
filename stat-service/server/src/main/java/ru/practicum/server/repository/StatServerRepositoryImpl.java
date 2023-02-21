package ru.practicum.server.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.server.model.EndpointHit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StatServerRepositoryImpl implements StatServerRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(EndpointHit endpointHit) {
        String sql = "INSERT INTO endpoint_hits(app, uri, ip, timestamp) " +
                "VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, endpointHit.getApp());
            stmt.setString(2, endpointHit.getUri());
            stmt.setString(3, endpointHit.getIp());
            stmt.setTimestamp(4, Timestamp.valueOf(endpointHit.getTimestamp()));
            return stmt;
        }, keyHolder);
    }

    @Override
    public List<StatResponseDto> findAllStats(List<String> uris, LocalDateTime start, LocalDateTime end,
                                              boolean unique) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate =
                new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        SqlParameterSource namedParameters = new MapSqlParameterSource(Map.of(
                "uris", uris,
                "start", start,
                "end", end)
        );

        String sql = "select r.app, r.uri, count(*) hits from (select h.ip, h.app, h.uri from endpoint_hits h" +
                " where h.uri in (:uris) and" +
                " cast(h.timestamp as date) between cast((:start) as date) and cast((:end) as date)) as r" +
                " group by r.app, r.uri";

        if (unique) {
            sql = "select r.app, r.uri, count(*) hits from (select distinct h.ip, h.app, h.uri from" +
                    " endpoint_hits h where h.uri in (:uris) and" +
                    " cast(h.timestamp as date) between cast((:start) as date) and cast((:end) as date)) as r" +
                    " group by r.app, r.uri";
        }

        return namedParameterJdbcTemplate.query(sql, namedParameters, (rs, rowNum) -> makeStatResponseDto(rs));
    }

    private StatResponseDto makeStatResponseDto(ResultSet rs) throws SQLException {
        return new StatResponseDto(
                rs.getString("app"),
                rs.getString("uri"),
                rs.getLong("hits")
        );
    }
}
