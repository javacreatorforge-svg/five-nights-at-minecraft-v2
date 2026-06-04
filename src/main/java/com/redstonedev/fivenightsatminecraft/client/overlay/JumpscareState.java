package com.redstonedev.fivenightsatminecraft.client.overlay;

import com.redstonedev.fivenightsatminecraft.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class JumpscareState {
    private JumpscareState() {}
    public static volatile int ticksRemaining = 0;
    public static volatile int charId = 0;

    public static void trigger(int charId, int ticks) {
        JumpscareState.charId = charId;
        ticksRemaining = ticks;
        SoundEvent s;
        if (charId == 1) s = ModSounds.BONNIE_JUMPSCARE.get();
        else if (charId == 2) s = ModSounds.CHICA_JUMPSCARE.get();
        else if (charId == 3) s = ModSounds.FOXY_JUMPSCARE.get();
        else if (charId == 4) s = ModSounds.GOLDEN_JUMPSCARE.get();
        else s = ModSounds.JUMPSCARE.get();
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(s, 1.0F, 1.0F));
    }

    public static void clientTick() {
        if (ticksRemaining > 0) ticksRemaining--;
    }
}
