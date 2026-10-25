package com.app.gym.dao;

import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.app.gym.dto.GymCurrentUserRoleDto;

@Repository
public class GymCurrentUserRoleDao {

    private static final String FIND_CURRENT_USER_ROLES =
            "SELECT DISTINCT "
            + "r.ID AS ROLE_ID, "
            + "r.ROLE_CODE, "
            + "r.ROLE_NAME "
            + "FROM GYM_MST_USER u "
            + "JOIN GYM_MAP_USER_TO_ROLE ur "
            + "ON ur.USER_ID = u.ID "
            + "JOIN GYM_MST_ROLE r "
            + "ON r.ID = ur.ROLE_ID "
            + "WHERE u.ID = :userId "
            + "AND u.IS_INACTIVE = 0 "
            + "AND u.IS_DELETED = 0 "
            + "AND ur.IS_INACTIVE = 0 "
            + "AND ur.IS_DELETED = 0 "
            + "AND r.IS_INACTIVE = 0 "
            + "AND r.IS_DELETED = 0 "
            + "ORDER BY r.ROLE_NAME, r.ROLE_CODE, r.ID";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public GymCurrentUserRoleDao(
            NamedParameterJdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<GymCurrentUserRoleDto> findActiveRoles(
            Long userId) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "A valid user ID is required");
        }

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("userId", userId);

        return jdbcTemplate.query(
                FIND_CURRENT_USER_ROLES,
                parameters,
                (resultSet, rowNumber) ->
                        new GymCurrentUserRoleDto(
                                resultSet.getLong("ROLE_ID"),
                                resultSet.getString("ROLE_CODE"),
                                resultSet.getString("ROLE_NAME"))
        );
    }
}