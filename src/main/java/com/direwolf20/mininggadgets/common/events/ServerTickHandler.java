package com.direwolf20.mininggadgets.common.events;

import com.direwolf20.mininggadgets.common.network.data.DurabilitySyncPayload;
import com.direwolf20.mininggadgets.common.util.CodecHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;

public class ServerTickHandler {
    private static final Map<ServerLevel, Map<BlockPos,Integer>> updates = new HashMap<>();

    @SubscribeEvent
    public static void handleTickEndEvent(ServerTickEvent.Post event) {
        for (var entry : updates.entrySet()) {
            var level = entry.getKey();
            if (level.getServer() != event.getServer()) continue;
            var payload = new DurabilitySyncPayload(entry.getValue().entrySet().stream()
                    .map(e -> new CodecHelpers.DurabilitySyncData(e.getKey(),e.getValue())).toList());
            for (var player : level.players()) {
                if (player.connection.hasChannel(DurabilitySyncPayload.TYPE))
                    PacketDistributor.sendToPlayer(player,payload);
            }
        }
        updates.clear();
    }

    public static void addToList(BlockPos pos, int durability, Level world) {
        if (world instanceof ServerLevel level)
            updates.computeIfAbsent(level,k -> new LinkedHashMap<>()).put(pos.immutable(),durability);
    }
}
