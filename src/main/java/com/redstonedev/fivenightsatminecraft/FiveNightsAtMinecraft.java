package com.redstonedev.fivenightsatminecraft;

import com.mojang.logging.LogUtils;
import com.redstonedev.fivenightsatminecraft.client.ClientSetup;
import com.redstonedev.fivenightsatminecraft.entity.BonnieEntity;
import com.redstonedev.fivenightsatminecraft.entity.ChicaEntity;
import com.redstonedev.fivenightsatminecraft.entity.FoxyEntity;
import com.redstonedev.fivenightsatminecraft.entity.FreddyEntity;
import com.redstonedev.fivenightsatminecraft.entity.GoldenFreddyEntity;
import com.redstonedev.fivenightsatminecraft.event.ForgeEvents;
import com.redstonedev.fivenightsatminecraft.init.ModEntities;
import com.redstonedev.fivenightsatminecraft.init.ModItems;
import com.redstonedev.fivenightsatminecraft.init.ModSounds;
import com.redstonedev.fivenightsatminecraft.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib3.GeckoLib;

@Mod(FiveNightsAtMinecraft.MODID)
public class FiveNightsAtMinecraft {
    public static final String MODID = "five_nights_at_minecraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FiveNightsAtMinecraft() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        GeckoLib.initialize();
        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::entityAttributes);
        MinecraftForge.EVENT_BUS.register(new ForgeEvents());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(PacketHandler::register);
        LOGGER.info("Five Nights at Minecraft loaded");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ClientSetup.onClientSetup(event);
    }

    private void entityAttributes(final EntityAttributeCreationEvent event) {
        event.put(ModEntities.FREDDY.get(), FreddyEntity.createAttributes().build());
        event.put(ModEntities.BONNIE.get(), BonnieEntity.createAttributes().build());
        event.put(ModEntities.CHICA.get(), ChicaEntity.createAttributes().build());
        event.put(ModEntities.FOXY.get(), FoxyEntity.createAttributes().build());
        event.put(ModEntities.GOLDEN_FREDDY.get(), GoldenFreddyEntity.createAttributes().build());
    }
}
