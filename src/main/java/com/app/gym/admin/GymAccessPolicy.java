package com.app.gym.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class GymAccessPolicy {

    private GymAccessPolicy() {
    }

    public static final Set<String> PROTECTED =
            Collections.unmodifiableSet(
                    new HashSet<>(
                            Arrays.asList(
                                    "CAN-CREATE-GYM-USER",
                                    "CAN-ASSIGN-GYM-USER-ROLE",
                                    "CAN-UPDATE-GYM-USER",
                                    "CAN-CHANGE-GYM-USER-STATUS",
                                    "CAN-VIEW-GYM-USER-LIST",
                                    "CAN-DELETE-GYM-USER",
                                    "CAN-MANAGE-GYM-ROLE-PERMISSION",
                                    "CAN-RESET-GYM-USER-PASSWORD"
                            )
                    )
            );

    public static boolean protectedPermission(
            String permissionCode,
            String moduleCode) {

        return PROTECTED.contains(permissionCode)
                || "USER_MANAGEMENT".equals(moduleCode)
                || "ACCESS_MANAGEMENT".equals(moduleCode);
    }

    public static List<String> effective(
            Collection<String> rolePermissions,
            Map<String, String> overrides,
            Set<String> activePermissionCodes,
            Set<String> protectedPermissionCodes,
            boolean admin) {

        Set<String> result = new TreeSet<>(rolePermissions);

        overrides.forEach((permissionCode, effect) -> {

            if ("GRANT".equals(effect)) {
                result.add(permissionCode);

            } else if ("DENY".equals(effect)) {
                result.remove(permissionCode);
            }
        });

        // Inactive/deleted permissions cannot remain effective.
        result.retainAll(activePermissionCodes);

        // Staff-management operations are reserved for Admin.
        if (!admin) {
            result.removeAll(protectedPermissionCodes);
        }

        return new ArrayList<>(result);
    }
}