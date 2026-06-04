package com.redstonedev.fivenightsatminecraft.init;

import com.redstonedev.fivenightsatminecraft.FiveNightsAtMinecraft;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FiveNightsAtMinecraft.MODID);

    public static final RegistryObject<ForgeSpawnEggItem> FREDDY_SPAWN_EGG =
            ITEMS.register("freddy_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.FREDDY, 0x502813, 0x56514F, eggProps()));
    public static final RegistryObject<ForgeSpawnEggItem> BONNIE_SPAWN_EGG =
            ITEMS.register("bonnie_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.BONNIE, 0x352A67, 0x191919, eggProps()));
    public static final RegistryObject<ForgeSpawnEggItem> CHICA_SPAWN_EGG =
            ITEMS.register("chica_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.CHICA, 0xB58313, 0x56514F, eggProps()));
    public static final RegistryObject<ForgeSpawnEggItem> FOXY_SPAWN_EGG =
            ITEMS.register("foxy_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.FOXY, 0x802814, 0x56514F, eggProps()));
    public static final RegistryObject<ForgeSpawnEggItem> GOLDEN_FREDDY_SPAWN_EGG =
            ITEMS.register("golden_freddy_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.GOLDEN_FREDDY, 0x7D5308, 0xC8A020, eggProps()));

    private static Item.Properties eggProps() {
        return new Item.Properties().tab(CreativeModeTab.TAB_MISC);
    }
}
