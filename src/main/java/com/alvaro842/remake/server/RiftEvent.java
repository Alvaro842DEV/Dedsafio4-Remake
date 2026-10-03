package com.alvaro842.remake.server;

import com.alvaro842.remake.RiftTimeline;
import com.alvaro842.remake.network.RiftSyncPayload;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * La animación no se guarda, solo esta en RAM
 */
public final class RiftEvent {
    private static RiftSyncPayload state = RiftSyncPayload.NONE;

    private RiftEvent() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dedsafio")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("start").executes(ctx -> start(ctx.getSource(), false)))
                .then(Commands.literal("reset").executes(ctx -> start(ctx.getSource(), true)))
                .then(Commands.literal("stop").executes(ctx -> stop(ctx.getSource()))));
    }

    public static void syncTo(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, state);
    }

    public static void clear() {
        state = RiftSyncPayload.NONE;
    }

    private static int start(CommandSourceStack source, boolean restart) {
        var overworld = source.getServer().overworld();
        long now = overworld.getGameTime();
        if (!restart && RiftTimeline.isRunning(now, state.startTick(), state.stopTick())) return 0;
        state = new RiftSyncPayload(now, -1L, overworld.getRandom().nextInt());
        PacketDistributor.sendToAllPlayers(state);
        source.sendSuccess(() -> Component.literal("Reproduciendo animación..."), false);
        return 1;
    }

    private static int stop(CommandSourceStack source) {
        long now = source.getServer().overworld().getGameTime();
        if (!RiftTimeline.isRunning(now, state.startTick(), state.stopTick())) return 0;
        state = new RiftSyncPayload(state.startTick(), now, state.seed());
        PacketDistributor.sendToAllPlayers(state);
        source.sendSuccess(() -> Component.literal("Animación detenida"), false);
        return 1;
    }
}
