package com.ticketmanagement.event;

public final class EventVersionPolicy {

    public static final int MIN_SUPPORTED_VERSION = 1;
    public static final int MAX_SUPPORTED_VERSION = 1;

    private EventVersionPolicy() {
    }

    public static boolean isSupported(int version) {
        return version >= MIN_SUPPORTED_VERSION && version <= MAX_SUPPORTED_VERSION;
    }

    public static int requireSupported(int version) {
        if (!isSupported(version)) {
            throw new IllegalArgumentException("Unsupported event version: " + version);
        }
        return version;
    }
}
