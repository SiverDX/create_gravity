package de.cadentem.create_gravity.network;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public interface Proxy {
    default @Nullable Player getLocalPlayer() {
        return null;
    }

    default void setOverlayMessage(final Component ignored) { /* Nothing to do */ }
}
