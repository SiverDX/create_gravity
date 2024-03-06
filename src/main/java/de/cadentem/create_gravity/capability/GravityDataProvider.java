package de.cadentem.create_gravity.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class GravityDataProvider implements ICapabilitySerializable<CompoundTag> {
    public static final Map<Level, Map<Integer, LazyOptional<GravityData>>> CACHE = new IdentityHashMap<>();

    private final GravityData data = new GravityData();
    private final LazyOptional<GravityData> instance = LazyOptional.of(() -> data);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull final Capability<T> capability, @Nullable final Direction side) {
        return capability == CapabilityHandler.GRAVITY_DATA_CAPABILITY ? instance.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.orElseThrow(() -> new IllegalArgumentException("Capability instance was not present")).serializeNBT();
    }

    @Override
    public void deserializeNBT(final CompoundTag tag) {
        instance.orElseThrow(() -> new IllegalArgumentException("Capability instance was not present")).deserializeNBT(tag);
    }

    public static LazyOptional<GravityData> getCapability(final Entity entity) {
        if (entity instanceof Player) {
            Map<Integer, LazyOptional<GravityData>> cache = CACHE.computeIfAbsent(entity.level(), key -> new HashMap<>());
            LazyOptional<GravityData> capability = cache.get(entity.getId());

            if (capability == null) {
                capability = entity.getCapability(CapabilityHandler.GRAVITY_DATA_CAPABILITY);

                if (capability.isPresent()) {
                    cache.put(entity.getId(), capability);
                }
            }

            return capability;
        }

        return LazyOptional.empty();
    }
}