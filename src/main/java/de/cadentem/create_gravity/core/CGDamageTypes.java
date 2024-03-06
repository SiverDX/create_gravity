package de.cadentem.create_gravity.core;

import de.cadentem.create_gravity.CreateGravity;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public class CGDamageTypes {
    public static final ResourceKey<DamageType> OUT_OF_OXYGEN = ResourceKey.create(Registries.DAMAGE_TYPE, CreateGravity.location("out_of_oxygen"));

    public static DamageSource outOfOxygen(final Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(OUT_OF_OXYGEN));
    }

    public static void bootstrap(final BootstapContext<DamageType> context) {
        context.register(OUT_OF_OXYGEN, new DamageType("create_gravity.out_of_oxygen", 0.1f));
    }
}
