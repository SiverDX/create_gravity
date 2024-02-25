package de.cadentem.create_gravity.client;

import de.cadentem.create_gravity.capability.GravityDataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class ClientProxy {
    public static void handleSyncPlayerData(final CompoundTag tag) {
        GravityDataProvider.getCapability(Minecraft.getInstance().player).ifPresent(data -> data.deserializeNBT(tag));
    }
}
