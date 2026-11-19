package com.iafenvoy.origins;

import com.iafenvoy.origins.border.BorderSyncPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworkEvents {

    public static void registerNetwork(final net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        final net.neoforged.neoforge.network.registration.PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                com.iafenvoy.origins.border.BorderSyncPacket.TYPE,
                com.iafenvoy.origins.border.BorderSyncPacket.CODEC,
                com.iafenvoy.origins.border.BorderSyncPacket::handleClient
        );
    }
}