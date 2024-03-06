package de.cadentem.create_gravity.data;

import de.cadentem.create_gravity.CreateGravity;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class CGEntityTags extends EntityTypeTagsProvider {
    public static final TagKey<EntityType<?>> LOW_GRAVITY_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, CreateGravity.location("low_gravity_blacklist"));

    public CGEntityTags(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider, @Nullable final ExistingFileHelper helper) {
        super(output, provider, CreateGravity.MODID, helper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        tag(LOW_GRAVITY_BLACKLIST).addTag(Tags.EntityTypes.BOSSES);
    }
}