package de.cadentem.create_gravity.network;

import de.cadentem.create_gravity.CreateGravity;
import de.cadentem.create_gravity.capability.GravityDataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncGravityData(CompoundTag tag) {
    public void encode(final FriendlyByteBuf buffer) {
        buffer.writeNbt(tag);
    }

    public static SyncGravityData decode(final FriendlyByteBuf buffer) {
        return new SyncGravityData(buffer.readNbt());
    }

    public static void handle(final SyncGravityData packet, final Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> GravityDataProvider.getCapability(CreateGravity.PROXY.getLocalPlayer()).ifPresent(data -> data.deserializeNBT(packet.tag)));
        }

        context.setPacketHandled(true);
    }
}
