package de.cadentem.create_gravity.events;

import com.google.common.cache.CacheBuilder;
import de.cadentem.create_gravity.capability.GravityDataProvider;
import de.cadentem.create_gravity.data.CGBiomeTags;
import de.cadentem.create_gravity.data.CGEntityTags;
import de.cadentem.create_gravity.data.CGItemTags;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber
public class ForgeEvents {
    private static final Map<String, Holder<Biome>> BIOME_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .concurrencyLevel(1)
            .<String, Holder<Biome>>build()
            .asMap();

    private static final UUID LOW_GRAVITY_UUID = UUID.fromString("7871c3e3-1016-4d26-b65d-b154a5399e16");
    private static final AttributeModifier LOW_GRAVITY_MODIFIER = new AttributeModifier(LOW_GRAVITY_UUID, "Low Gravity Biome", -0.80, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final DecimalFormat FORMAT = new DecimalFormat("##.#");
    private static final DamageSource OUT_OF_OXYGEN = new DamageSource("out_of_oxygen").bypassArmor();

    @SubscribeEvent
    public static void applyGravity(final LivingEvent.LivingTickEvent event) {
        handleGravity(event.getEntity());

        if (event.getEntity() instanceof Player player) {
            handleOxygen(player);
        }
    }

    @SubscribeEvent
    public static void removeCachedEntry(final EntityLeaveLevelEvent event) {
        BIOME_CACHE.remove(event.getEntity().getStringUUID());
    }

    private static void handleOxygen(final Player player) {
        double oxygenFactor = 100;

        GravityDataProvider.getCapability(player).ifPresent(data -> {
            if (getBiome(player).is(CGBiomeTags.DRAINS_OXYGEN)) {
                data.damageOxygen(1);

                ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
                ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                CompoundTag tag = chest.getTag();
                double airSupply = 0;

                if (tag != null) {
                    airSupply = tag.getDouble("Air");
                }

                if (chest.is(CGItemTags.BACKTANK) && helmet.is(CGItemTags.DIVING_HELMET) && airSupply >= 1) {
                    player.displayClientMessage(Component.translatable("message.action_bar.remaining_oxygen", FORMAT.format(airSupply * 0.1), "dm³"), true);
                    setAirSupply(player, player.getAirSupply() + 1);
                    tag.putDouble("Air", airSupply - 1);
                } else {
                    setAirSupply(player, player.getAirSupply() - 4);
                    int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.RESPIRATION, player);

                    if (data.getOxygenDamage() % Math.round(oxygenFactor + enchantmentLevel * 10) == 0) {
                        setAirSupply(player, player.getAirSupply() - 1);
                    }
                }

                if (player.getMaxAirSupply() < -10 && data.getOxygenDamage() >= 60) {
                    data.resetOxygenDamage();
                    player.hurt(OUT_OF_OXYGEN, 6);
                    player.displayClientMessage(Component.translatable("message.action_bar.low_oxygen_alert"), true);
                }
            }
        });
    }

    private static void handleGravity(final LivingEntity entity) {
        if (entity.getType().is(CGEntityTags.LOW_GRAVITY_BLACKLIST) || entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }

        AttributeInstance gravity = entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());

        if (gravity == null) {
            return;
        }

        boolean shouldApplyLowGravity = !entity.getItemBySlot(EquipmentSlot.FEET).is(CGItemTags.ANTI_LOW_GRAVITY_BOOTS);

        if (shouldApplyLowGravity) {
            if (getBiome(entity).is(CGBiomeTags.LOW_GRAVITY)) {
                if (entity.getDeltaMovement().y() != 0) {
                    entity.fallDistance *= 0.9f;
                }

                if (!gravity.hasModifier(LOW_GRAVITY_MODIFIER)) {
                    gravity.addTransientModifier(LOW_GRAVITY_MODIFIER);
                }
            } else {
                shouldApplyLowGravity = false;
            }
        }

        if (!shouldApplyLowGravity && gravity.hasModifier(LOW_GRAVITY_MODIFIER)) {
            gravity.removeModifier(LOW_GRAVITY_MODIFIER);
        }
    }

    private static void setAirSupply(final LivingEntity entity, int airSupply) {
        entity.setAirSupply(Mth.clamp(airSupply, /* Don't trigger vanilla suffocation */ -10, entity.getMaxAirSupply()));
    }

    private static Holder<Biome> getBiome(final LivingEntity entity) {
        return BIOME_CACHE.computeIfAbsent(entity.getStringUUID(), key -> entity.getLevel().getBiome(entity.blockPosition()));
    }
}
