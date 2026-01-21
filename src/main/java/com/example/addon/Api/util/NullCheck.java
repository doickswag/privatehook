package com.example.addon.Api.util;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public final class NullCheck {
    private NullCheck() {}

    public static boolean isPlayerNull() {
        return mc.player == null;
    }

    public static boolean isWorldNull() {
        return mc.world == null;
    }

    public static boolean isFullNull() {
        return mc.player == null || mc.world == null;
    }
}
