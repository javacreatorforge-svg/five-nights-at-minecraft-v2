package com.redstonedev.fivenightsatminecraft.init;

import com.redstonedev.fivenightsatminecraft.FiveNightsAtMinecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FiveNightsAtMinecraft.MODID);

    // Freddy
    public static final RegistryObject<SoundEvent> FOOTSTEPS = register("footsteps");
    public static final RegistryObject<SoundEvent> SPOTS_YOU = register("spotsyou");
    public static final RegistryObject<SoundEvent> JUMPSCARE = register("jumpscare");
    public static final RegistryObject<SoundEvent> LAUGH     = register("laugh");
    public static final List<RegistryObject<SoundEvent>> LAUGHS = new ArrayList<>();

    // Bonnie
    public static final RegistryObject<SoundEvent> BONNIE_FOOTSTEPS = register("bonnie_footsteps");
    public static final RegistryObject<SoundEvent> BONNIE_JUMPSCARE = register("bonnie_jumpscare");

    // Chica
    public static final RegistryObject<SoundEvent> CHICA_FOOTSTEPS = register("chica_footsteps");
    public static final RegistryObject<SoundEvent> CHICA_JUMPSCARE = register("chica_jumpscare");
    public static final List<RegistryObject<SoundEvent>> CHICA_GROANS = new ArrayList<>();

    // Foxy
    public static final RegistryObject<SoundEvent> FOXY_FOOTSTEPS = register("foxy_footsteps");
    public static final RegistryObject<SoundEvent> FOXY_JUMPSCARE = register("foxy_jumpscare");
    public static final RegistryObject<SoundEvent> FOXY_KNOCKING  = register("foxy_knocking");
    public static final RegistryObject<SoundEvent> FOXY_SINGING   = register("foxy_singing");
    public static final List<RegistryObject<SoundEvent>> FOXY_VOICELINES = new ArrayList<>();

    // Golden Freddy
    public static final RegistryObject<SoundEvent> GOLDEN_JUMPSCARE = register("golden_jumpscare");
    public static final RegistryObject<SoundEvent> GOLDEN_NOISE     = register("golden_noise");
    public static final RegistryObject<SoundEvent> GOLDEN_WARNING   = register("golden_warning");

    static {
        LAUGHS.add(register("laugh1"));
        LAUGHS.add(register("laugh2"));
        LAUGHS.add(register("laugh3"));
        CHICA_GROANS.add(register("chica_groan1"));
        CHICA_GROANS.add(register("chica_groan2"));
        CHICA_GROANS.add(register("chica_groan3"));
        FOXY_VOICELINES.add(register("foxy_voiceline1"));
        FOXY_VOICELINES.add(register("foxy_voiceline2"));
        FOXY_VOICELINES.add(register("foxy_voiceline3"));
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> new SoundEvent(new ResourceLocation(FiveNightsAtMinecraft.MODID, name)));
    }
}
