package com.iafenvoy.origins.border;

import com.iafenvoy.origins.border.BorderSaveData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = "origins")
public class BorderCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("fog")
                .requires(source -> source.hasPermission(2)) // Нужна опка
                .then(Commands.literal("set")
                        .then(Commands.argument("x1", IntegerArgumentType.integer())
                                .then(Commands.argument("z1", IntegerArgumentType.integer())
                                        .then(Commands.argument("x2", IntegerArgumentType.integer())
                                                .then(Commands.argument("z2", IntegerArgumentType.integer())
                                                        .executes(context -> {
                                                            int x1 = IntegerArgumentType.getInteger(context, "x1");
                                                            int z1 = IntegerArgumentType.getInteger(context, "z1");
                                                            int x2 = IntegerArgumentType.getInteger(context, "x2");
                                                            int z2 = IntegerArgumentType.getInteger(context, "z2");

                                                            CommandSourceStack source = context.getSource();
                                                            ServerLevel level = source.getLevel();
                                                            BorderSaveData data = BorderSaveData.get(level);
                                                            com.iafenvoy.origins.border.BorderConfig.saveBounds(x1, z1, x2, z2);
                                                            data.updateBounds(x1, z1, x2, z2, level);
                                                            com.iafenvoy.origins.border.BorderSyncPacket updatedPacket = new com.iafenvoy.origins.border.BorderSyncPacket(
                                                                    com.iafenvoy.origins.border.BorderConfig.getMinX(),
                                                                    com.iafenvoy.origins.border.BorderConfig.getMinZ(),
                                                                    com.iafenvoy.origins.border.BorderConfig.getMaxX(),
                                                                    com.iafenvoy.origins.border.BorderConfig.getMaxZ()
                                                            );
                                                            net.neoforged.neoforge.network.PacketDistributor.sendToAllPlayers(updatedPacket);
                                                            source.sendSuccess(() -> Component.literal(
                                                                    String.format("§aГраница успешно установлена! Зона: от [%d, %d] до [%d, %d]",
                                                                            data.minX, data.minZ, data.maxX, data.maxZ)), true);
                                                            return 1;
                                                        })
                                                ))))
                )
        );
    }
}
