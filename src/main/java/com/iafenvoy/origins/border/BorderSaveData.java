package com.iafenvoy.origins.border;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class BorderSaveData extends SavedData {
    // Координаты углов по умолчанию (от -1000 до 1000)
    public int minX = -1000;
    public int minZ = -1000;
    public int maxX = 1000;
    public int maxZ = 1000;
    // Добавь это поле внутрь класса BorderSaveData для клиентского доступа
    public static int clientMinX = -1000, clientMinZ = -1000, clientMaxX = 1000, clientMaxZ = 1000;

    public BorderSaveData() {}

    public BorderSaveData(CompoundTag tag) {
        this.minX = tag.contains("MinX") ? tag.getInt("MinX") : -1000;
        this.minZ = tag.contains("MinZ") ? tag.getInt("MinZ") : -1000;
        this.maxX = tag.contains("MaxX") ? tag.getInt("MaxX") : 1000;
        this.maxZ = tag.contains("MaxZ") ? tag.getInt("MaxZ") : 1000;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("MinX", this.minX);
        tag.putInt("MinZ", this.minZ);
        tag.putInt("MaxX", this.maxX);
        tag.putInt("MaxZ", this.maxZ);
        return tag;
    }

    public void updateBounds(int x1, int z1, int x2, int z2, ServerLevel level) {
        this.minX = Math.min(x1, x2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxZ = Math.max(z1, z2);

        this.setDirty();

        // Отправляем обновленные координаты ВСЕМ игрокам на сервере
        if (level != null && level.getServer() != null) {
            BorderSyncPacket updatedPacket =
                   new BorderSyncPacket(this.minX, this.minZ, this.maxX, this.maxZ);

            // Рассылка пакета всем подключенным клиентам
            net.neoforged.neoforge.network.PacketDistributor.sendToAllPlayers(updatedPacket);
        }
    }



    public static BorderSaveData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        () -> new BorderSaveData(), // Поставщик для новых данных (Supplier)
                        (tag, registries) -> new BorderSaveData(tag), // Чтение из NBT (BiFunction с поддержкой провайдера)
                        net.minecraft.util.datafix.DataFixTypes.LEVEL
                ),
                "custom_world_border"
        );
    }
}
