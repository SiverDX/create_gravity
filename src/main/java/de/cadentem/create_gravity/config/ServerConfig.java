package de.cadentem.create_gravity.config;

import de.cadentem.create_gravity.CreateGravity;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerConfig {
    public static final double OXYGEN_FACTOR_DEFAULT = 100;
    public static final double GRAVITY_FACTOR_DEFAULT = -0.8;

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue OUT_OF_AIR_DAMAGE;
    public static final ForgeConfigSpec.IntValue DAMAGE_TICK;

    private static @Nullable List<BiomeConfig> BIOME_CONFIGS;

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BIOME_CONFIGS_INTERNAL;

    public record BiomeConfig(ResourceLocation biome, boolean isTag, double oxygenFactor, double gravityFactor) {
        public static @Nullable BiomeConfig fromString(final @NotNull String data) {
            String[] split = data.split(";");
            String biome = getData(split, 0);
            String oxygenFactorRaw = getData(split, 1);
            String gravityFactorRaw = getData(split, 2);

            if (biome != null) {
                boolean isTag = biome.startsWith("#");
                double oxygenFactor = oxygenFactorRaw != null ? Math.max(0, Double.parseDouble(oxygenFactorRaw)) : OXYGEN_FACTOR_DEFAULT;
                double gravityFactor = gravityFactorRaw != null ? Mth.clamp(Double.parseDouble(gravityFactorRaw), -1, 0) : GRAVITY_FACTOR_DEFAULT;

                return new BiomeConfig(new ResourceLocation(isTag ? biome.substring(1) : biome), isTag, oxygenFactor, gravityFactor);
            }

            return null;
        }
    }

    static {
        OUT_OF_AIR_DAMAGE = BUILDER.comment("Amount of damage the entitiy takes once it has run out of air in low oxygen biomes").defineInRange("out_of_air_damage", 6f, 0, 1024);
        DAMAGE_TICK = BUILDER.comment("Entities take 1 oxygen damage per tick in low oxygen biomes - this determines how many oxygen damage ticks they can take before triggering a damage tick").defineInRange("damage_tick", 60, 0, 1000);

        String firstLine = "Syntax: \"<modid:biome>;<oxygen_factor>;<gravity_factor>\"\n";
        String secondLine = "Append # in front of the biome entry if it is a tag, e.g. #minecraft:is_end\n";
        String thirdLine = "Gravity factor needs to be between -1 and 0 (-1 disables gravity entirely) - If it is 0 the effect will be disabled in the biome\n";
        String fourthLine = "Oxygen factor needs to be positive (0 or higher) - If it is 0 the effect will be disabled in the biome\n";
        String lastLine = "The factors are optional (default values will apply (100;0.8)) - syntax in that case is \"<modid:biome>;;\" or \"<modid:biome>;<oxygen_factor>;\" or \"<modid:biome>;;<gravity_factor>\"";

        BIOME_CONFIGS_INTERNAL = BUILDER.comment(firstLine + secondLine + thirdLine + fourthLine + lastLine).define("oxygen_factors", List.of("#minecraft:is_end;;"), ServerConfig::validateOxygenFactors);

        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    public static void reloadConfig(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            List<BiomeConfig> newConfigs = new ArrayList<>();
            BIOME_CONFIGS_INTERNAL.get().forEach(data -> newConfigs.add(BiomeConfig.fromString(data)));
            BIOME_CONFIGS = newConfigs;
            CreateGravity.LOG.info("Reloaded configuration");
        }
    }

    public static @Nullable BiomeConfig getBiomeConfig(final Holder<Biome> biome) {
        if (BIOME_CONFIGS == null) {
            return null;
        }

        for (BiomeConfig factor : BIOME_CONFIGS) {
            if (factor.isTag()) {
                if (!biome.tags().filter(tag -> tag.location().equals(factor.biome())).toList().isEmpty()) {
                    return factor;
                }
            } else if (biome.is(factor.biome())) {
                return factor;
            }
        }

        return null;
    }

    private static boolean validateOxygenFactors(final Object object) {
        if (object instanceof String string) {
            String[] data = string.split(";");

            if (data.length != 3) {
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
            return Double.parseDouble(oxygenFactor) < 0;
        } catch (NumberFormatException ignored) {
            return true;
        }
    }

    private static boolean isGravityFactorInvalid(final String gravityFactor) {
        try {
            return Double.parseDouble(gravityFactor) >= 0;
        } catch (NumberFormatException ignored) {
            return true;
        }
    }
}
