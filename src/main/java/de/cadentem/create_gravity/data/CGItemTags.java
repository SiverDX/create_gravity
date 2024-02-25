package de.cadentem.create_gravity.data;

import de.cadentem.create_gravity.CreateGravity;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

public class CGItemTags extends ItemTagsProvider {
    public static final TagKey<Item> ANTI_LOW_GRAVITY_BOOTS = TagKey.create(Registry.ITEM_REGISTRY, CreateGravity.location("anti_low_gravity_boots"));
    public static final TagKey<Item> BACKTANKS = TagKey.create(Registry.ITEM_REGISTRY, CreateGravity.location("backtanks"));
    public static final TagKey<Item> DIVING_HELMETS = TagKey.create(Registry.ITEM_REGISTRY, CreateGravity.location("diving_helmets"));

    public CGItemTags(final DataGenerator generator, final BlockTagsProvider provider, final ExistingFileHelper helper) {
        super(generator, provider, CreateGravity.MODID, helper);
    }

    @Override
    protected void addTags() {
        tag(ANTI_LOW_GRAVITY_BOOTS)
                .addOptional(new ResourceLocation("create", "copper_diving_boots"))
                .addOptional(new ResourceLocation("create", "netherite_diving_boots"));

        tag(BACKTANKS)
                .addOptional(new ResourceLocation("create", "copper_backtank"))
                .addOptional(new ResourceLocation("create", "netherite_backtank"));

        tag(DIVING_HELMETS)
                .addOptional(new ResourceLocation("create", "copper_diving_helmet"))
                .addOptional(new ResourceLocation("create", "netherite_diving_helmet"));
    }
}