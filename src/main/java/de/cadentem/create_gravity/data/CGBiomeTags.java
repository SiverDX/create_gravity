package de.cadentem.create_gravity.data;

import de.cadentem.create_gravity.CreateGravity;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class CGBiomeTags extends BiomeTagsProvider {
    public static final TagKey<Biome> LOW_GRAVITY = TagKey.create(Registry.BIOME_REGISTRY, CreateGravity.location("low_gravity"));
    public static final TagKey<Biome> DRAINS_OXYGEN = TagKey.create(Registry.BIOME_REGISTRY, CreateGravity.location("drains_oxygen"));

    public CGBiomeTags(final DataGenerator generator, @Nullable final ExistingFileHelper helper) {
        super(generator, CreateGravity.MODID, helper);
    }

    @Override
    protected void addTags() {
        tag(LOW_GRAVITY).addTag(BiomeTags.IS_END);
        tag(DRAINS_OXYGEN).addTag(BiomeTags.IS_END);
    }
}
