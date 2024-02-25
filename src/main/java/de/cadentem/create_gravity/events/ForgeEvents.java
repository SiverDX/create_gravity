package de.cadentem.create_gravity.events;

import com.google.common.cache.CacheBuilder;
import de.cadentem.create_gravity.capability.GravityDataProvider;
import de.cadentem.create_gravity.config.ServerConfig;
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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
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
    private static final DecimalFormat FORMAT = new DecimalFormat("##.#");
    private static final DamageSource OUT_OF_OXYGEN = new DamageSource("out_of_oxygen").bypassArmor();

    private static final int LOW_AIR = /* Avoid vanilla out of air damage / reset (at -20) */ -10;

    @SubscribeEvent
    public static void applyGravity(final LivingEvent.LivingTickEvent event) {
        ServerConfig.BiomeConfig config = ServerConfig.getBiomeConfig(getBiome(event.getEntity()));
        handleGravity(event.getEntity(), config);

        if (event.getEntity() instanceof Player player) {
            handleOxygen(player, config);
        }
    }

    @SubscribeEvent
    public static void removeCachedEntry(final EntityLeaveLevelEvent event) {
        BIOME_CACHE.remove(event.getEntity().getStringUUID());
    }

    @SubscribeEvent
    public static void removeCachedEntry(final PlayerEvent.PlayerChangedDimensionEvent event) {
        BIOME_CACHE.remove(event.getEntity().getStringUUID());
    }

    public static boolean isInLowOxygenBiome(final LivingEntity entity) {
        ServerConfig.BiomeConfig config = ServerConfig.getBiomeConfig(getBiome(entity));
        return config != null && config.oxygenFactor() > 0;
    }

    private static void handleOxygen(final Player player, final @Nullable ServerConfig.BiomeConfig config) {
        if (config == null || config.oxygenFactor() == 0) {
            return;
        }

        GravityDataProvider.getCapability(player).ifPresent(data -> {
            data.damageOxygen(1);
            boolean hasDivingHelmet = player.getItemBySlot(EquipmentSlot.HEAD).is(CGItemTags.DIVING_HELMETS);

            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            CompoundTag tag = chest.getTag();
            double backtankSupply = 0;

            if (tag != null && chest.is(CGItemTags.BACKTANKS)) {
                backtankSupply = tag.getDouble("Air");
            }

            if (hasDivingHelmet && backtankSupply >= 1) {
                setAirSupply(player, player.getAirSupply() + 1);
                tag.putDouble("Air", backtankSupply - 1);
                data.resetOxygenDamage();
                player.displayClientMessage(Component.translatable("message.action_bar.remaining_oxygen", FORMAT.format(backtankSupply * 0.1), "dm³"), true);
            } else {
                int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.RESPIRATION, player);
                long chance = Math.round(config.oxygenFactor() + enchantmentLevel * 10) * (hasDivingHelmet ? 2 : 1);

                if (data.getOxygenDamage() % chance == 0) {
                    setAirSupply(player, player.getAirSupply() - 1);
                }
            }

            if (player.getMaxAirSupply() <= LOW_AIR && data.getOxygenDamage() >= ServerConfig.DAMAGE_TICK.get()) {
                data.resetOxygenDamage();
                player.hurt(OUT_OF_OXYGEN, ServerConfig.OUT_OF_AIR_DAMAGE.get().floatValue());
                player.displayClientMessage(Component.translatable("message.action_bar.low_oxygen_alert"), true);
            }
        });
    }

    private static void handleGravity(final LivingEntity entity, final @Nullable ServerConfig.BiomeConfig config) {
        AttributeInstance gravity = entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());

        if (gravity == null) {
            return;
        }

        double gravityFactor = config != null ? config.gravityFactor() : 0;
        boolean shouldApplyLowGravity = gravityFactor < 0 && shouldApplyLowGravity(entity);
        AttributeModifier modifier = new AttributeModifier(LOW_GRAVITY_UUID, "Low Gravity Biome", gravityFactor, AttributeModifier.Operation.MULTIPLY_TOTAL);

        if (shouldApplyLowGravity) {
            if (entity.getDeltaMovement().y() != 0) {
                entity.fallDistance *= 0.9f;
            }

            if (!gravity.hasModifier(modifier)) {
                gravity.addTransientModifier(modifier);
            }
        } else if (gravity.hasModifier(modifier)) {
            gravity.removeModifier(modifier);
        }
    }

    private static boolean shouldApplyLowGravity(final LivingEntity entity) {
        if (entity.getType().is(CGEntityTags.LOW_GRAVITY_BLACKLIST) || entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }

        return !entity.getItemBySlot(EquipmentSlot.FEET).is(CGItemTags.ANTI_LOW_GRAVITY_BOOTS);
    }

    private static void setAirSupply(final LivingEntity entity, int airSupply) {
        entity.setAirSupply(Mth.clamp(airSupply, LOW_AIR, entity.getMaxAirSupply()));
    }

    private static Holder<Biome> getBiome(final LivingEntity entity) {
        return BIOME_CACHE.computeIfAbsent(entity.getStringUUID(), key -> entity.getLevel().getBiome(entity.blockPosition()));
    }
}
