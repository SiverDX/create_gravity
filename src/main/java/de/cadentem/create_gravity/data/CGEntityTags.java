package de.cadentem.create_gravity.data;

import de.cadentem.create_gravity.CreateGravity;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class CGEntityTags extends EntityTypeTagsProvider {
    public static final TagKey<EntityType<?>> LOW_GRAVITY_BLACKLIST = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, CreateGravity.location("low_gravity_blacklist"));

    public CGEntityTags(final DataGenerator generator, @Nullable final ExistingFileHelper helper) {
        super(generator, CreateGravity.MODID, helper);
    }

    @Override
    protected void addTags() {
        tag(LOW_GRAVITY_BLACKLIST).addTag(Tags.EntityTypes.BOSSES);
    }
}