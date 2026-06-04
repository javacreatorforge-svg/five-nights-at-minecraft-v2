package com.redstonedev.fivenightsatminecraft.network;

import com.redstonedev.fivenightsatminecraft.FiveNightsAtMinecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

public class PacketHandler {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(FiveNightsAtMinecraft.MODID, "main"),
            () -> VERSION, VERSION::equals, VERSION::equals);
    private static int nextId = 0;

    public static void register() {
        CHANNEL.registerMessage(nextId++, JumpscarePacket.class,
                JumpscarePacket::encode, JumpscarePacket::decode, JumpscarePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(nextId++, LockViewPacket.class,
                LockViewPacket::encode, LockViewPacket::decode, LockViewPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    /** charId: 0 Freddy, 1 Bonnie, 2 Chica, 3 Foxy, 4 Golden Freddy. */
    public static class JumpscarePacket {
        public final int charId;
        public final int ticks;
        public JumpscarePacket(int charId, int ticks) { this.charId = charId; this.ticks = ticks; }
        public static void encode(JumpscarePacket p, FriendlyByteBuf b) { b.writeInt(p.charId); b.writeInt(p.ticks); }
        public static JumpscarePacket decode(FriendlyByteBuf b) { return new JumpscarePacket(b.readInt(), b.readInt()); }
        public static void handle(JumpscarePacket p, Supplier<net.minecraftforge.network.NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
                    net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> () -> com.redstonedev.fivenightsatminecraft.client.overlay.JumpscareState.trigger(p.charId, p.ticks)));
            ctx.get().setPacketHandled(true);
        }
    }

    /** Locks the player's camera onto an entity (Golden Freddy stare). entityId < 0 unlocks. */
    public static class LockViewPacket {
        public final int entityId;
        public LockViewPacket(int entityId) { this.entityId = entityId; }
        public static void encode(LockViewPacket p, FriendlyByteBuf b) { b.writeInt(p.entityId); }
        public static LockViewPacket decode(FriendlyByteBuf b) { return new LockViewPacket(b.readInt()); }
        public static void handle(LockViewPacket p, Supplier<net.minecraftforge.network.NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
                    net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> () -> com.redstonedev.fivenightsatminecraft.client.overlay.ViewLockState.set(p.entityId)));
            ctx.get().setPacketHandled(true);
        }
    }
}
