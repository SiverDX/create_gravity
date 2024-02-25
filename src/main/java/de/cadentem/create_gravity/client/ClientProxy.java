package de.cadentem.create_gravity.client;

import de.cadentem.create_gravity.capability.GravityDataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLLoader;

import javax.annotation.Nullable;
import java.text.DecimalFormat;

public class ClientProxy {
    private static final DecimalFormat FORMAT = new DecimalFormat("00.0");

    public static void handleSyncPlayerData(final CompoundTag tag) {
        GravityDataProvider.getCapability(Minecraft.getInstance().player).ifPresent(data -> data.deserializeNBT(tag));
    }

    public static @Nullable Player getLocalPlayer() {
        if (FMLLoader.getDist().isClient()) {
            return Minecraft.getInstance().player;
        }

        return null;
    }

    public static void displayBacktankSupply(int backtankSupply) {
        Component component;

        if (backtankSupply >= 1) {
            component = Component.translatable("message.action_bar.remaining_oxygen", FORMAT.format(backtankSupply * 0.1d), "dm³");
        } else {
            component = Component.empty();
        }

        Minecraft.getInstance().gui.setOverlayMessage(component, false);
    }

    public static void displayOutOfAir() {
        Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("message.action_bar.low_oxygen_alert"), false);
    }
}
