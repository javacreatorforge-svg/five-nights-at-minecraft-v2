package com.redstonedev.fivenightsatminecraft.init;

import com.redstonedev.fivenightsatminecraft.FiveNightsAtMinecraft;
import com.redstonedev.fivenightsatminecraft.entity.BonnieEntity;
import com.redstonedev.fivenightsatminecraft.entity.ChicaEntity;
import com.redstonedev.fivenightsatminecraft.entity.FoxyEntity;
import com.redstonedev.fivenightsatminecraft.entity.FreddyEntity;
import com.redstonedev.fivenightsatminecraft.entity.GoldenFreddyEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FiveNightsAtMinecraft.MODID);

    public static final RegistryObject<EntityType<FreddyEntity>> FREDDY =
            ENTITIES.register("freddy", () -> EntityType.Builder
                    .<FreddyEntity>of(FreddyEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.6F).clientTrackingRange(16)
                    .build(new ResourceLocation(FiveNightsAtMinecraft.MODID, "freddy").toString()));

    public static final RegistryObject<EntityType<BonnieEntity>> BONNIE =
            ENTITIES.register("bonnie", () -> EntityType.Builder
                    .<BonnieEntity>of(BonnieEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.6F).clientTrackingRange(16)
                    .build(new ResourceLocation(FiveNightsAtMinecraft.MODID, "bonnie").toString()));

    public static final RegistryObject<EntityType<ChicaEntity>> CHICA =
            ENTITIES.register("chica", () -> EntityType.Builder
                    .<ChicaEntity>of(ChicaEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.3F).clientTrackingRange(16) // slightly shorter
                    .build(new ResourceLocation(FiveNightsAtMinecraft.MODID, "chica").toString()));

    public static final RegistryObject<EntityType<FoxyEntity>> FOXY =
            ENTITIES.register("foxy", () -> EntityType.Builder
                    .<FoxyEntity>of(FoxyEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.6F).clientTrackingRange(16)
                    .build(new ResourceLocation(FiveNightsAtMinecraft.MODID, "foxy").toString()));

    public static final RegistryObject<EntityType<GoldenFreddyEntity>> GOLDEN_FREDDY =
            ENTITIES.register("golden_freddy", () -> EntityType.Builder
                    .<GoldenFreddyEntity>of(GoldenFreddyEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.6F).clientTrackingRange(24)
                    .build(new ResourceLocation(FiveNightsAtMinecraft.MODID, "golden_freddy").toString()));
}
