package org.gmalliaris.rental.rooms.util;

import java.util.Optional;
import java.util.UUID;

public final class CommonUtils {

    private CommonUtils() {
        // hide implicit constructor
    }

    public static Optional<UUID> uuidFromString(String uuid) {

        if (uuid == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(uuid));
        }
        catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
