package com.iafenvoy.origins.border;

import com.iafenvoy.origins.Origins;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BorderSyncPacket(int minX, int minZ, int maxX, int maxZ) implements CustomPacketPayload {

    // Уникальный сетевой ID пакета: "origins:border_sync"
    public static final Type<BorderSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Origins.MOD_ID, "border_sync"));

    // Кодек, который упаковывает и распаковывает 4 координаты в сетевой буфер байтов
    public static final StreamCodec<FriendlyByteBuf, BorderSyncPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.minX);
                buf.writeInt(packet.minZ);
                buf.writeInt(packet.maxX);
                buf.writeInt(packet.maxZ);
            },
            buf -> new BorderSyncPacket(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(final BorderSyncPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            BorderSaveData.clientMinX = payload.minX;
            BorderSaveData.clientMinZ = payload.minZ;
            BorderSaveData.clientMaxX = payload.maxX;
            BorderSaveData.clientMaxZ = payload.maxZ;
        });
    }
}
