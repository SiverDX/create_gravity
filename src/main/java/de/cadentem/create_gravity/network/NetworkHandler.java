package de.cadentem.create_gravity.network;

import de.cadentem.create_gravity.CreateGravity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    public static final String PROTOCOL_VERSION = "1.0.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(CreateGravity.location("main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void register() {
        CHANNEL.registerMessage(0, SyncGravityData.class, SyncGravityData::encode, SyncGravityData::decode, SyncGravityData::handle);
    }
}