package com.alvaro842.remake.server;

import com.alvaro842.remake.RiftTimeline;
import com.alvaro842.remake.network.RiftSyncPayload;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * La animación no se guarda, solo esta en RAM
 */
public final class RiftEvent {
    private static RiftSyncPayload state = RiftSyncPayload.NONE;

    private RiftEvent() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dedsafio")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("start").executes(ctx -> start(ctx.getSource(), false)))
                .then(Commands.literal("reset").executes(ctx -> start(ctx.getSource(), true)))
                .then(Commands.literal("stop").executes(ctx -> stop(ctx.getSource()))));
    }

    public static void syncTo(ServerPlayer player) {
        ServerPlayNetworking.send(player, state);
    }

    public static void clear() {
        state = RiftSyncPayload.NONE;
    }

    private static int start(CommandSourceStack source, boolean restart) {
        var overworld = source.getServer().overworld();
        long now = overworld.getGameTime();
        if (!restart && RiftTimeline.isRunning(now, state.startTick(), state.stopTick())) return 0;
        state = new RiftSyncPayload(now, -1L, overworld.getRandom().nextInt());
        PlayerLookup.all(source.getServer()).forEach(player -> ServerPlayNetworking.send(player, state));
        source.sendSuccess(() -> Component.literal("Reproduciendo animación..."), false);
        return 1;
    }

    private static int stop(CommandSourceStack source) {
        long now = source.getServer().overworld().getGameTime();
        if (!RiftTimeline.isRunning(now, state.startTick(), state.stopTick())) return 0;
        state = new RiftSyncPayload(state.startTick(), now, state.seed());
        PlayerLookup.all(source.getServer()).forEach(player -> ServerPlayNetworking.send(player, state));
        source.sendSuccess(() -> Component.literal("Animación detenida"), false);
        return 1;
    }
}
