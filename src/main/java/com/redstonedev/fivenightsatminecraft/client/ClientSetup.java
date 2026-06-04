package com.redstonedev.fivenightsatminecraft.client;

import com.redstonedev.fivenightsatminecraft.FiveNightsAtMinecraft;
import com.redstonedev.fivenightsatminecraft.client.overlay.JumpscareOverlay;
import com.redstonedev.fivenightsatminecraft.client.overlay.JumpscareState;
import com.redstonedev.fivenightsatminecraft.client.overlay.ViewLockState;
import com.redstonedev.fivenightsatminecraft.client.renderer.AnimatronicRenderer;
import com.redstonedev.fivenightsatminecraft.init.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntities.FREDDY.get(),
                    c -> new AnimatronicRenderer<>(c, "freddy", "freddy", "freddy", 0.5F));
            EntityRenderers.register(ModEntities.BONNIE.get(),
                    c -> new AnimatronicRenderer<>(c, "bonnie", "bonnie", "bonnie", 0.5F));
            EntityRenderers.register(ModEntities.CHICA.get(),
                    c -> new AnimatronicRenderer<>(c, "chica", "chica", "chica", 0.5F));
            EntityRenderers.register(ModEntities.FOXY.get(),
                    c -> new AnimatronicRenderer<>(c, "foxy", "foxy", "foxy", 0.5F));
            EntityRenderers.register(ModEntities.GOLDEN_FREDDY.get(),
                    c -> new AnimatronicRenderer<>(c, "golden_freddy", "goldenfreddy", "golden_freddy", 0.5F));
        });
    }

    @Mod.EventBusSubscriber(modid = FiveNightsAtMinecraft.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBus {
        @SubscribeEvent
        public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.CHAT_PANEL.id(), "fnam_jumpscare", JumpscareOverlay.INSTANCE);
        }
    }

    @Mod.EventBusSubscriber(modid = FiveNightsAtMinecraft.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBus {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            JumpscareState.clientTick();

            // Golden Freddy stare: force the camera to face him while locked.
            if (ViewLockState.isLocked()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null || mc.level == null) { ViewLockState.set(-1); return; }
                Entity e = mc.level.getEntity(ViewLockState.lockedEntityId);
                if (e == null || !e.isAlive()) { ViewLockState.set(-1); return; }
                double dx = e.getX() - mc.player.getX();
                double dy = (e.getY() + e.getBbHeight() * 0.7D) - mc.player.getEyeY();
                double dz = e.getZ() - mc.player.getZ();
                double horiz = Math.sqrt(dx * dx + dz * dz);
                float yaw = (float) (Math.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
                float pitch = (float) (-(Math.atan2(dy, horiz) * (180D / Math.PI)));
                mc.player.setYRot(yaw);
                mc.player.setXRot(pitch);
                mc.player.setYHeadRot(yaw);
            }
        }

        @SubscribeEvent
        public static void onMovementInput(MovementInputUpdateEvent event) {
            // Freeze player movement while Golden Freddy has them locked.
            if (ViewLockState.isLocked()) {
                event.getInput().leftImpulse = 0.0F;
                event.getInput().forwardImpulse = 0.0F;
                event.getInput().up = false;
                event.getInput().down = false;
                event.getInput().left = false;
                event.getInput().right = false;
                event.getInput().jumping = false;
                event.getInput().shiftKeyDown = false;
            }
        }
    }
}
