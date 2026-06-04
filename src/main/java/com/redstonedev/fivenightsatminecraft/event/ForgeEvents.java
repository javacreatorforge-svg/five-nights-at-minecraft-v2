package com.redstonedev.fivenightsatminecraft.event;

import com.redstonedev.fivenightsatminecraft.init.ModEntities;
import com.redstonedev.fivenightsatminecraft.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Random;

public class ForgeEvents {

    private static final Random RNG = new Random();
    private int tickCounter = 0;

    // Golden Freddy delayed spawn: noise plays, then ~10s later he appears with a warning.
    private int goldenCountdown = 0;
    private ServerLevel goldenLevel = null;
    private BlockPos goldenPos = null;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer() == null) return;

        if (goldenCountdown > 0) {
            goldenCountdown--;
            if (goldenCountdown == 0 && goldenLevel != null && goldenPos != null) {
                spawnGolden(goldenLevel, goldenPos);
                goldenLevel = null; goldenPos = null;
            }
        }

        tickCounter++;
        if (tickCounter % 100 != 0) return; // ~5s
        for (ServerLevel level : event.getServer().getAllLevels()) {
            ambientLaughs(level);
            trySpawnAll(level);
        }
    }

    private void ambientLaughs(ServerLevel level) {
        List<? extends ServerPlayer> players = level.players();
        if (players.isEmpty()) return;
        for (ServerPlayer player : players) {
            if (RNG.nextInt(45) != 0) continue; // ~every 4 min
            SoundEvent laugh = ModSounds.LAUGHS.get(RNG.nextInt(ModSounds.LAUGHS.size())).get();
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    laugh, SoundSource.HOSTILE, 0.8F, 1.0F);
        }
    }

    private boolean exists(ServerLevel level, EntityType<?> type) {
        return !level.getEntities(type, e -> e.isAlive()).isEmpty();
    }

    private void trySpawnAll(ServerLevel level) {
        List<? extends ServerPlayer> players = level.players();
        if (players.isEmpty()) return;

        for (ServerPlayer player : players) {
            boolean underground = isUnderground(level, player);
            boolean night = !level.isDay();

            if (!exists(level, ModEntities.FREDDY.get())) {
                int c = underground ? 110 : (night ? 300 : 800);
                if (RNG.nextInt(c) == 0) { spawn(level, player, ModEntities.FREDDY.get()); continue; }
            }
            if (!exists(level, ModEntities.BONNIE.get())) {
                int c = !night ? 320 : (underground ? 360 : 600); // mostly day
                if (RNG.nextInt(c) == 0) { spawn(level, player, ModEntities.BONNIE.get()); continue; }
            }
            if (!exists(level, ModEntities.CHICA.get())) {
                int c = !night ? 340 : (underground ? 380 : 620);
                if (RNG.nextInt(c) == 0) { spawn(level, player, ModEntities.CHICA.get()); continue; }
            }
            if (!exists(level, ModEntities.FOXY.get())) {
                int c = !night ? 340 : (underground ? 380 : 620);
                if (RNG.nextInt(c) == 0) { spawn(level, player, ModEntities.FOXY.get()); continue; }
            }
            if (goldenCountdown == 0 && !exists(level, ModEntities.GOLDEN_FREDDY.get())) {
                if (RNG.nextInt(2200) == 0) { // RARE
                    BlockPos pos = pickSpawnPos(level, player);
                    if (pos != null) {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                ModSounds.GOLDEN_NOISE.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
                        goldenCountdown = 200; // 10 seconds
                        goldenLevel = level;
                        goldenPos = pos;
                    }
                }
            }
        }
    }

    private void spawn(ServerLevel level, ServerPlayer player, EntityType<?> type) {
        BlockPos pos = pickSpawnPos(level, player);
        if (pos == null) return;
        Mob mob = (Mob) type.create(level);
        if (mob == null) return;
        mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360F, 0);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        level.addFreshEntity(mob);
    }

    private void spawnGolden(ServerLevel level, BlockPos pos) {
        Mob mob = ModEntities.GOLDEN_FREDDY.get().create(level);
        if (mob == null) return;
        mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360F, 0);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        level.addFreshEntity(mob);
        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                ModSounds.GOLDEN_WARNING.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    private boolean isUnderground(ServerLevel level, ServerPlayer player) {
        BlockPos p = player.blockPosition();
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, p.getX(), p.getZ());
        return player.getY() < surface - 5;
    }

    private BlockPos pickSpawnPos(ServerLevel level, ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < 24; attempt++) {
            int x = origin.getX() + (RNG.nextInt(24) - 12);
            int z = origin.getZ() + (RNG.nextInt(24) - 12);
            int y;
            if (isUnderground(level, player)) y = origin.getY() + (RNG.nextInt(6) - 3);
            else y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            BlockPos c = new BlockPos(x, y, z);
            boolean floor = !level.getBlockState(c.below()).getCollisionShape(level, c.below()).isEmpty();
            if (floor && level.getBlockState(c).getCollisionShape(level, c).isEmpty()
                    && level.getBlockState(c.above()).getCollisionShape(level, c.above()).isEmpty()) {
                double d = c.distSqr(origin);
                if (d > 36 && d < 1024) return c;
            }
        }
        return null;
    }
}
