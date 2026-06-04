package com.redstonedev.fivenightsatminecraft.entity;

import net.minecraft.world.damagesource.DamageSource;

public final class ModDamage {
    private ModDamage() {}
    // Anonymous subclass to reach DamageSource's protected constructor; drives the custom death message.
    public static final DamageSource GOLDEN_BITE =
            new DamageSource("golden_freddy_bite") {}.bypassArmor().bypassMagic();
}
