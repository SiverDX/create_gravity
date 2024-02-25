package de.cadentem.create_gravity.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import de.cadentem.create_gravity.events.ForgeEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @WrapWithCondition(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setAirSupply(I)V", ordinal = 2))
    private boolean create_gravity$shouldIncrease(final LivingEntity instance, int currentAir) {
        return !(instance instanceof Player) || !ForgeEvents.isInLowOxygenBiome(instance);
    }
}
