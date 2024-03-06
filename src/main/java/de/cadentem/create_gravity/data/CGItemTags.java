package de.cadentem.create_gravity.data;

import de.cadentem.create_gravity.CreateGravity;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class CGItemTags extends ItemTagsProvider {
    public static final TagKey<Item> ANTI_LOW_GRAVITY_BOOTS = TagKey.create(Registries.ITEM, CreateGravity.location("anti_low_gravity_boots"));
    public static final TagKey<Item> BACKTANKS = TagKey.create(Registries.ITEM, CreateGravity.location("backtanks"));
    public static final TagKey<Item> DIVING_HELMETS = TagKey.create(Registries.ITEM, CreateGravity.location("diving_helmets"));

    public CGItemTags(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider, final ExistingFileHelper helper) {
        super(output, provider, CompletableFuture.completedFuture(null), CreateGravity.MODID, helper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
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