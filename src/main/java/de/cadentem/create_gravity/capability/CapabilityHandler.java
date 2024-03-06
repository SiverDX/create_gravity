package de.cadentem.create_gravity.capability;

import de.cadentem.create_gravity.CreateGravity;
import de.cadentem.create_gravity.network.NetworkHandler;
import de.cadentem.create_gravity.network.SyncGravityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;

@Mod.EventBusSubscriber
public class CapabilityHandler {
    public static final Capability<GravityData> GRAVITY_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation GRAVITY_DATA = new ResourceLocation(CreateGravity.MODID, "gravity_data");

    @SubscribeEvent
    public static void attachCapability(final AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            if (player instanceof FakePlayer) {
                return;
            }

            event.addCapability(GRAVITY_DATA, new GravityDataProvider());
        }
    }

    @SubscribeEvent
    public static void handleInitialSync(final EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncGravityData(serverPlayer);
        }
    }

    @SubscribeEvent
    @SuppressWarnings("SuspiciousMethodCalls")
    public static void clearCache(final LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            GravityDataProvider.CACHE.remove(event.getLevel());
        }
    }

    @SubscribeEvent
    public static void removeCachedEntry(final EntityLeaveLevelEvent event) {
        Map<Integer, LazyOptional<GravityData>> cache = GravityDataProvider.CACHE.get(event.getEntity().level());

        if (cache != null) {
            cache.remove(event.getEntity().getId());
        }
    }

    public static void syncGravityData(final ServerPlayer serverPlayer) {
        GravityDataProvider.getCapability(serverPlayer).ifPresent(data -> NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new SyncGravityData(data.serializeNBT())));
    }
}