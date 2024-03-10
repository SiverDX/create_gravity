package de.cadentem.create_gravity.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class ClientProxy implements Proxy {
    @Override
    public @Nullable Player getLocalPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public void setOverlayMessage(final Component component) {
        Minecraft.getInstance().gui.setOverlayMessage(component, false);
    }
}
