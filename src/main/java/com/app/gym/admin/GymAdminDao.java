package com.app.gym.admin;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.app.gym.permission.entity.GymPermissionEntity;

@Repository
public class GymAdminDao {

    @PersistenceContext
    private EntityManager em;

    private final JdbcTemplate jdbc;

    public GymAdminDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /*
     * Call from the service's write transaction.
     * Acquire this lock before locking individual users.
     */
    public void lockAdministration() {

        Integer completed = jdbc.queryForObject(
                "SELECT BOOTSTRAP_DONE "
                        + "FROM GYM_ADMIN_GUARD "
                        + "WHERE ID = 1 FOR UPDATE",
                Integer.class
        );

        if (!Integer.valueOf(1).equals(completed)) {
            throw new IllegalStateException(
                    "Admin bootstrap is incomplete");
        }
    }

    public boolean activeUser(Long userId) {

        Long count = em.createQuery(
                "select count(u.id) "
                        + "from GymUserEntity u "
                        + "where u.id = :id "
                        + "and u.isInactive = 0 "
                        + "and u.isDeleted = 0 "
                        + "and u.loginLocked = 0",
                Long.class
        )
                .setParameter("id", userId)
                .getSingleResult();

        return count > 0;
    }

    public boolean hasRole(
            Long userId,
            String roleCode) {

        Long count = em.createQuery(
                "select count(ur.id) "
                        + "from GymUserRoleEntity ur "
                        + "where ur.user.id = :id "
                        + "and ur.isInactive = 0 "
                        + "and ur.isDeleted = 0 "
                        + "and ur.role.isInactive = 0 "
                        + "and ur.role.isDeleted = 0 "
                        + "and ur.role.roleCode = :code",
                Long.class
        )
                .setParameter("id", userId)
                .setParameter("code", roleCode)
                .getSingleResult();

        return count > 0;
    }

    public long activeAdminCount() {

        return em.createQuery(
                "select count(distinct ur.user.id) "
                        + "from GymUserRoleEntity ur "
                        + "where ur.user.isInactive = 0 "
                        + "and ur.user.isDeleted = 0 "
                        + "and ur.user.loginLocked = 0 "
                        + "and ur.isInactive = 0 "
                        + "and ur.isDeleted = 0 "
                        + "and ur.role.isInactive = 0 "
                        + "and ur.role.isDeleted = 0 "
                        + "and ur.role.roleCode = 'ADMIN'",
                Long.class
        )
                .getSingleResult();
    }

    public List<GymPermissionEntity> catalog() {

        return em.createQuery(
                "select p "
                        + "from GymPermissionEntity p "
                        + "where p.isInactive = 0 "
                        + "and p.isDeleted = 0 "
                        + "order by p.permissionCode",
                GymPermissionEntity.class
        )
                .getResultList();
    }

    public Map<String, String> overrides(Long userId) {

        Map<String, String> result = new TreeMap<>();

        String sql =
                "SELECT p.PERMISSION_CODE, o.EFFECT "
                        + "FROM GYM_USER_PERMISSION_OVERRIDE o "
                        + "JOIN GYM_MST_PERMISSION p "
                        + "ON p.ID = o.PERMISSION_ID "
                        + "WHERE o.USER_ID = ? "
                        + "AND o.IS_INACTIVE = 0 "
                        + "AND o.IS_DELETED = 0 "
                        + "AND p.IS_INACTIVE = 0 "
                        + "AND p.IS_DELETED = 0";

        jdbc.query(
                sql,
                rs -> {
                    result.put(
                            rs.getString("PERMISSION_CODE"),
                            rs.getString("EFFECT")
                    );
                },
                userId
        );

        return result;
    }

    /*
     * Call only after service-level authorization and validation,
     * inside the same transaction as lockAdministration().
     */
    public void setOverride(
            Long userId,
            Long permissionId,
            String effect,
            Long actorUserId) {

        Timestamp now = Timestamp.valueOf(
                LocalDateTime.now(ZoneOffset.UTC));

        if ("INHERIT".equals(effect)) {

            jdbc.update(
                    "UPDATE GYM_USER_PERMISSION_OVERRIDE "
                            + "SET IS_DELETED = 1, "
                            + "UPDATED_AT = ?, "
                            + "UPDATED_BY = ?, "
                            + "VERSION = VERSION + 1 "
                            + "WHERE USER_ID = ? "
                            + "AND PERMISSION_ID = ? "
                            + "AND IS_DELETED = 0",
                    now,
                    actorUserId,
                    userId,
                    permissionId
            );

            return;
        }

        int updatedRows = jdbc.update(
                "UPDATE GYM_USER_PERMISSION_OVERRIDE "
                        + "SET EFFECT = ?, "
                        + "IS_INACTIVE = 0, "
                        + "IS_DELETED = 0, "
                        + "UPDATED_AT = ?, "
                        + "UPDATED_BY = ?, "
                        + "VERSION = VERSION + 1 "
                        + "WHERE USER_ID = ? "
                        + "AND PERMISSION_ID = ?",
                effect,
                now,
                actorUserId,
                userId,
                permissionId
        );

        if (updatedRows == 0) {

            jdbc.update(
                    "INSERT INTO GYM_USER_PERMISSION_OVERRIDE "
                            + "(ID, USER_ID, PERMISSION_ID, EFFECT, "
                            + "IS_INACTIVE, IS_DELETED, "
                            + "CREATED_AT, CREATED_BY, "
                            + "UPDATED_AT, UPDATED_BY, VERSION) "
                            + "VALUES "
                            + "(GYM_ENTITY_SEQ.NEXTVAL, ?, ?, ?, "
                            + "0, 0, ?, ?, ?, ?, 0)",
                    userId,
                    permissionId,
                    effect,
                    now,
                    actorUserId,
                    now,
                    actorUserId
            );
        }
    }
}