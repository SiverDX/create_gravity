package de.cadentem.create_gravity.events;

import com.google.common.cache.CacheBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.cadentem.create_gravity.CreateGravity;
import de.cadentem.create_gravity.capability.GravityDataProvider;
import de.cadentem.create_gravity.config.ServerConfig;
import de.cadentem.create_gravity.data.CGEntityTags;
import de.cadentem.create_gravity.data.CGItemTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber
public class ForgeEvents {
    private static final Map<String, ServerConfig.BiomeConfig> BIOME_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(3, TimeUnit.SECONDS)
            .concurrencyLevel(1)
            .<String, ServerConfig.BiomeConfig>build()
            .asMap();

    private static final UUID LOW_GRAVITY_UUID = UUID.fromString("7871c3e3-1016-4d26-b65d-b154a5399e16");
    private static final DamageSource OUT_OF_OXYGEN = new DamageSource("out_of_oxygen").bypassArmor();
    private static final ResourceLocation ADVANCEMENT = CreateGravity.location("place_banner_in_the_end");
    private static final DecimalFormat FORMAT = new DecimalFormat("00.0");

    private static final int LOW_AIR = /* Avoid vanilla out of air damage / reset (at -20) */ -10;

    @SubscribeEvent
    public static void handleAdvancement(final BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer && serverPlayer.getLevel().dimension() == Level.END && event.getPlacedBlock().is(BlockTags.BANNERS)) {
            Advancement advancement = serverPlayer.getLevel().getServer().getAdvancements().getAdvancement(ADVANCEMENT);

            if (advancement == null) {
                return;
            }

            serverPlayer.getAdvancements().getOrStartProgress(advancement).getRemainingCriteria().forEach(criteria -> serverPlayer.getAdvancements().award(advancement, criteria));
        }
    }

    @SubscribeEvent
    public static void handleLogic(final LivingEvent.LivingTickEvent event) {
        ServerConfig.BiomeConfig config = getBiomeConfig(event.getEntity());
        handleGravity(event.getEntity(), config);

        if (event.getEntity() instanceof Player player) {
            handleOxygen(player, config);
        }
    }

    @SubscribeEvent
    public static void setServer(final ServerStartedEvent event) {
        ServerConfig.server = event.getServer();
        ServerConfig.reloadConfig();
    }

    @SubscribeEvent
    public static void reloadConfiguration(final TagsUpdatedEvent event) {
        if (!ServerConfig.SPEC.isLoaded()) {
            return;
        }

        ServerConfig.reloadConfig();
    }

    @SubscribeEvent
    public static void removeCachedEntry(final EntityLeaveLevelEvent event) {
        removeCachedEntry(event.getEntity());
    }

    @SubscribeEvent
    public static void removeCachedEntry(final PlayerEvent.PlayerChangedDimensionEvent event) {
        removeCachedEntry(event.getEntity());
    }

    @SubscribeEvent
    public static void removeCachedEntry(final EntityEvent.EnteringSection event) {
        if (event.getEntity() instanceof Player) {
            removeCachedEntry(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(CreateGravity.MODID).requires(source -> source.hasPermission(Commands.LEVEL_ADMINS));

        builder.then(Commands.literal("reload_config").executes(context -> {
            int configSize = ServerConfig.reloadConfig(context.getSource().getPlayer());
            context.getSource().sendSuccess(Component.translatable("message.system.create_gravity.reloaded_config", configSize, configSize == 1 ? "entry" : "entries"), false);
            return 1;
        }));

        event.getDispatcher().register(builder);
    }

    public static boolean isInLowOxygenBiome(final LivingEntity entity) {
        return isInLowOxygenBiome(getBiomeConfig(entity));
    }

    private static boolean isInLowOxygenBiome(final @Nullable ServerConfig.BiomeConfig config) {
        return config != null && config.oxygenFactor() > 0;
    }

    private static void handleOxygen(final Player player, final @Nullable ServerConfig.BiomeConfig config) {
        if (!isInLowOxygenBiome(config)) {
            return;
        }

        GravityDataProvider.getCapability(player).ifPresent(data -> {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            CompoundTag tag = chest.getTag();
            int backtankSupply = 0;

            if (tag != null && chest.is(CGItemTags.BACKTANKS)) {
                backtankSupply = tag.getInt("Air");
            } else if (CreateGravity.IS_CURIOS_LOADED) {
                Optional<SlotResult> slot = CuriosApi.getCuriosHelper().findCurios(player, curio -> curio.is(CGItemTags.BACKTANKS)).stream().findFirst();

                if (slot.isPresent()) {
                    tag = slot.get().stack().getTag();

                    if (tag != null) {
                        backtankSupply = tag.getInt("Air");
                    }
                }
            }

            boolean hasDivingHelmet = player.getItemBySlot(EquipmentSlot.HEAD).is(CGItemTags.DIVING_HELMETS);

            if (!hasDivingHelmet && CreateGravity.IS_CURIOS_LOADED) {
                if (!CuriosApi.getCuriosHelper().findCurios(player, curio -> curio.is(CGItemTags.DIVING_HELMETS)).isEmpty()) {
                    hasDivingHelmet = true;
                }
            }

            if (backtankSupply >= 1 && (!ServerConfig.FULL_SET.get() || hasDivingHelmet)) {
                setAirSupply(player, player.getAirSupply() + 1);
                data.resetOxygenDamage();

                int depletionRate = ServerConfig.BACKTANK_DEPLETION_RATE.get();

                if (depletionRate > 0 && player.tickCount % depletionRate == 0) {
                    backtankSupply -= 1;
                    tag.putInt("Air", backtankSupply);
                }

                if (player.getLevel().isClientSide() && player == CreateGravity.PROXY.getLocalPlayer()) {
                    Component component;

                    if (backtankSupply >= 1) {
                        component = Component.translatable("message.action_bar.remaining_oxygen", FORMAT.format(backtankSupply * 0.1d), "dm³");
                    } else {
                        component = Component.empty();
                    }

                    CreateGravity.PROXY.setOverlayMessage(component);
                }
            } else {
                data.damageOxygen(1);
                int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.RESPIRATION, player);
                int rate = config.oxygenFactor() + enchantmentLevel * 10 * (hasDivingHelmet ? 4 : 1);

                if (data.getOxygenDamage() % rate == 0) {
                    setAirSupply(player, player.getAirSupply() - ServerConfig.OXYGEN_DEPLETION_AMOUNT.get());
                }
            }

            if (player.getAirSupply() <= LOW_AIR && data.getOxygenDamage() >= ServerConfig.DAMAGE_TICK.get()) {
                data.resetOxygenDamage();
                player.hurt(OUT_OF_OXYGEN, ServerConfig.OUT_OF_AIR_DAMAGE.get().floatValue());

                if (player.getLevel().isClientSide() && player == CreateGravity.PROXY.getLocalPlayer()) {
                    CreateGravity.PROXY.setOverlayMessage(Component.translatable("message.action_bar.low_oxygen_alert"));
                }
            }
        });
    }

    private static void handleGravity(final LivingEntity entity, final @Nullable ServerConfig.BiomeConfig config) {
        if (entity.getLevel().isClientSide()) {
            return;
        }

        AttributeInstance gravity = entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());

        if (gravity == null) {
            return;
        }

        double gravityFactor = config != null ? config.gravityFactor() : 0;
        boolean shouldApplyLowGravity = gravityFactor < 0 && shouldApplyLowGravity(entity);
        AttributeModifier modifier = new AttributeModifier(LOW_GRAVITY_UUID, "Low Gravity Biome", gravityFactor, AttributeModifier.Operation.MULTIPLY_TOTAL);

        if (shouldApplyLowGravity) {
            if (entity.getDeltaMovement().y() != 0) {
                entity.fallDistance *= 0.925f;
            }

            if (!gravity.hasModifier(modifier)) {
                gravity.addTransientModifier(modifier);
            }
        } else if (gravity.hasModifier(modifier)) {
            gravity.removeModifier(modifier);
        }
    }

    private static boolean shouldApplyLowGravity(final LivingEntity entity) {
        if (/* Elytra check */ entity.isFallFlying() || entity.getType().is(CGEntityTags.LOW_GRAVITY_BLACKLIST) || entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }

        return !entity.getItemBySlot(EquipmentSlot.FEET).is(CGItemTags.ANTI_LOW_GRAVITY_BOOTS);
    }

    private static void setAirSupply(final LivingEntity entity, int airSupply) {
        entity.setAirSupply(Mth.clamp(airSupply, LOW_AIR, entity.getMaxAirSupply()));
    }

    private static ServerConfig.BiomeConfig getBiomeConfig(final LivingEntity entity) {
        if (ServerConfig.CACHE_TIME.get() == 0) {
            return ServerConfig.getBiomeConfig(entity.getLevel().getBiome(entity.blockPosition()));
        }

        return BIOME_CACHE.computeIfAbsent(entity.getStringUUID(), key -> ServerConfig.getBiomeConfig(entity.getLevel().getBiome(entity.blockPosition())));
    }

    private static void removeCachedEntry(final Entity entity) {
        if (ServerConfig.CACHE_TIME.get() == 0) {
            return;
        }

        BIOME_CACHE.remove(entity.getStringUUID());
    }
}
