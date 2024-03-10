package de.cadentem.create_gravity.config;

import de.cadentem.create_gravity.CreateGravity;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerConfig {
    private static final int OXYGEN_FACTOR_DEFAULT = 100;
    private static final double GRAVITY_FACTOR_DEFAULT = -0.8;

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue OUT_OF_AIR_DAMAGE;
    public static final ForgeConfigSpec.IntValue DAMAGE_TICK;
    public static final ForgeConfigSpec.IntValue OXYGEN_DEPLETION_AMOUNT;
    public static final ForgeConfigSpec.IntValue BACKTANK_DEPLETION_RATE;
    public static final ForgeConfigSpec.BooleanValue FULL_SET;

    public static final ForgeConfigSpec.IntValue CACHE_TIME;

    // Needed to get access to the full list of biome tags
    public static @Nullable MinecraftServer server;
    private static @Nullable List<BiomeConfig> BIOME_CONFIGS;

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BIOME_CONFIGS_INTERNAL;

    public record BiomeConfig(@Nullable Holder<Biome> biome, @Nullable TagKey<Biome> tag, int oxygenFactor, double gravityFactor) {
        public static @Nullable BiomeConfig fromString(final @NotNull String data) {
            if (server == null) {
                return null;
            }

            String[] split = data.split(";");
            String biome = getData(split, 0);
            String oxygenFactorRaw = getData(split, 1);
            String gravityFactorRaw = getData(split, 2);

            BiomeConfig config = null;

            if (biome != null) {
                boolean isTag = biome.startsWith("#");
                int oxygenFactor = oxygenFactorRaw != null ? Integer.parseInt(oxygenFactorRaw) : OXYGEN_FACTOR_DEFAULT;
                double gravityFactor = gravityFactorRaw != null ? Double.parseDouble(gravityFactorRaw) : GRAVITY_FACTOR_DEFAULT;
                ResourceLocation location = new ResourceLocation(isTag ? biome.substring(1) : biome);

                Registry<Biome> registry = server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);

                if (isTag) {
                    Optional<TagKey<Biome>> optional = registry.getTagNames().filter(tag -> tag.location().equals(location)).findFirst();
                    config = optional.map(key -> new BiomeConfig(null, key, oxygenFactor, gravityFactor)).orElse(null);
                } else {
                    Optional<Holder.Reference<Biome>> optional = registry.holders().filter(holder -> {
                        Optional<ResourceKey<Biome>> key = holder.unwrapKey();
                        return key.map(biomeResourceKey -> biomeResourceKey.location().equals(location)).orElse(false);
                    }).findFirst();

                    config = optional.map(biomeHolder -> new BiomeConfig(biomeHolder, null, oxygenFactor, gravityFactor)).orElse(null);
                }
            }

            if (config == null) {
                CreateGravity.LOG.warn("Configuration not loaded for [{}]", data);
            }

            return config;
        }
    }

    static {
        OUT_OF_AIR_DAMAGE = BUILDER.comment("Amount of damage the entitiy takes once it has run out of air in low oxygen biomes").defineInRange("out_of_air_damage", 6f, 0, 1024);
        DAMAGE_TICK = BUILDER.comment("Entities take 1 oxygen damage per tick in low oxygen biomes - this determines how many oxygen damage ticks they can take before triggering a damage tick").defineInRange("damage_tick", 60, 0, 1000);
        OXYGEN_DEPLETION_AMOUNT = BUILDER.comment("The amount of oxygen lost per tick").defineInRange("oxygen_depletion_amount", 4, 0, 100);
        BACKTANK_DEPLETION_RATE = BUILDER.comment("The rate at which the backtank air supply is used up (in ticks, 20 = 1 second) (0 disables the depletion)").defineInRange("backtank_depletion_rate", 20, 0, 1000);
        FULL_SET = BUILDER.comment("If enabled then the backtank air supply will only be used if the player is also wearing a diving helmet").define("full_set", false);

        String firstLine = "Syntax: \"<modid:biome>;<oxygen_factor>;<gravity_factor>\"\n";
        String secondLine = "Append # in front of the biome entry if it is a tag, e.g. #minecraft:is_end\n";
        String thirdLine = "Gravity factor needs to be between -1 and 0 (-1 disables gravity entirely) - If it is 0 the effect will be disabled in the biome\n";
        String fourthLine = "Oxygen factor needs to be positive (0 or higher) - If it is 0 the effect will be disabled in the biome\n";
        String lastLine = "The factors are optional (default values will apply (100;-0.8)) - syntax in that case is \"<modid:biome>;;\" or \"<modid:biome>;<oxygen_factor>;\" or \"<modid:biome>;;<gravity_factor>\"";

        BIOME_CONFIGS_INTERNAL = BUILDER.comment(firstLine + secondLine + thirdLine + fourthLine + lastLine).defineList("biome_configs", List.of("#minecraft:is_end;;"), ServerConfig::validateBiomeConfig);
        CACHE_TIME = BUILDER.comment("The amount of seconds the current biome is cached - set it to 0 to disable caching (and therefor remove any update delay)").defineInRange("cache_time", 3, 0, 10);

        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    public static void reloadConfig(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            reloadConfig();
        }
    }

    public static void reloadConfig() {
        if (BIOME_CONFIGS_INTERNAL.get().isEmpty()) {
            CreateGravity.LOG.warn("No configuration entries are present");
            return;
        }

        List<BiomeConfig> newConfigs = new ArrayList<>();
        BIOME_CONFIGS_INTERNAL.get().forEach(data -> newConfigs.add(BiomeConfig.fromString(data)));
        BIOME_CONFIGS = newConfigs.stream().filter(Objects::nonNull).collect(Collectors.toList());
        CreateGravity.LOG.info("Reloaded configuration: [{}]", BIOME_CONFIGS);
    }

    public static int reloadConfig(final ServerPlayer initiator) {
        if (server == null) {
            server = initiator.getLevel().getServer();
            CreateGravity.LOG.warn("Server was not set");
        }

        reloadConfig();

        if (BIOME_CONFIGS == null) {
            throw new IllegalStateException("Biome config is null after reloading configuration");
        }

        return BIOME_CONFIGS.size();
    }

    public static @Nullable BiomeConfig getBiomeConfig(final Holder<Biome> biome) {
        if (BIOME_CONFIGS == null) {
            return null;
        }

        for (BiomeConfig config : BIOME_CONFIGS) {
            if (config.tag() != null) {
                if (biome.is(config.tag())) {
                    return config;
                }
            } else if (config.biome() != null && biome.equals(config.biome())) {
                return config;
            }
        }

        return null;
    }

    private static boolean validateBiomeConfig(final Object object) {
        if (object instanceof String string) {
            String[] data = string.split(";");

            if (string.length() - string.replace(";", "").length() != 2) {
                return false;
            }

            String biome = getData(data, 0);
            String oxygenFactor = getData(data, 1);
            String gravityFactor = getData(data, 2);

            if (biome == null) {
                return false;
            }

            biome = biome.startsWith("#") ? biome.substring(1) : biome;

            if (!ResourceLocation.isValidResourceLocation(biome)) {
                return false;
            }

            if (oxygenFactor != null && isOxygenFactorInvalid(oxygenFactor)) {
                return false;
            }

            return gravityFactor == null || !isGravityFactorInvalid(gravityFactor);
        }

        return false;
    }

    private static @Nullable String getData(final String[] data, int index) {
        if (index >= data.length) {
            return null;
        }

        String element = data[index];
        return !element.isBlank() ? element : null;
    }

    private static boolean isOxygenFactorInvalid(final String oxygenFactor) {
        try {
            return Integer.parseInt(oxygenFactor) < 0;
        } catch (NumberFormatException ignored) {
            return true;
        }
    }

    private static boolean isGravityFactorInvalid(final String gravityFactor) {
        try {
            double factor = Double.parseDouble(gravityFactor);
            return factor > 0 || factor < -1;
        } catch (NumberFormatException ignored) {
            return true;
        }
    }
}
