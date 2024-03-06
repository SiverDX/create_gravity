package de.cadentem.create_gravity.data;

import de.cadentem.create_gravity.CreateGravity;
import de.cadentem.create_gravity.core.CGDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class CGDamageTypeTags extends TagsProvider<DamageType> {
    public CGDamageTypeTags(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider, @Nullable final ExistingFileHelper helper) {
        super(output, Registries.DAMAGE_TYPE, provider.thenApply(lookup -> CGRegistryProvider.BUILDER.buildPatch(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), lookup)), CreateGravity.MODID, helper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        tag(DamageTypeTags.BYPASSES_ARMOR).add(CGDamageTypes.OUT_OF_OXYGEN);
    }
}
