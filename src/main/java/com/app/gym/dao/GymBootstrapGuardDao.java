package com.app.gym.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GymBootstrapGuardDao {

    private final JdbcTemplate jdbcTemplate;

    public GymBootstrapGuardDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean lockAndCheckCompleted() {

        Integer completed = jdbcTemplate.queryForObject(
                "SELECT BOOTSTRAP_DONE " +
                "FROM GYM_ADMIN_GUARD " +
                "WHERE ID = 1 FOR UPDATE",
                Integer.class);

        return Integer.valueOf(1).equals(completed);
    }

    public void markCompleted() {

        int affectedRows = jdbcTemplate.update(
                "UPDATE GYM_ADMIN_GUARD " +
                "SET BOOTSTRAP_DONE = 1 " +
                "WHERE ID = 1");

        if (affectedRows != 1) {
            throw new IllegalStateException(
                    "Bootstrap guard row is missing");
        }
    }
}