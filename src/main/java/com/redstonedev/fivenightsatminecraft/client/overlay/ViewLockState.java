package com.redstonedev.fivenightsatminecraft.client.overlay;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Holds the entity id whose direction the player's camera is locked onto (Golden Freddy stare). */
@OnlyIn(Dist.CLIENT)
public final class ViewLockState {
    private ViewLockState() {}
    public static volatile int lockedEntityId = -1;
    public static void set(int id) { lockedEntityId = id; }
    public static boolean isLocked() { return lockedEntityId >= 0; }
}
