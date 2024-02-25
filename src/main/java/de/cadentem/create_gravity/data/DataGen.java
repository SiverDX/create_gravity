package de.cadentem.create_gravity.data;

import de.cadentem.create_gravity.CreateGravity;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGen {
    @SubscribeEvent
    public static void configureDataGen(final GatherDataEvent event){
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(),new CGBiomeTags(generator, helper));
        generator.addProvider(event.includeServer(), new CGEntityTags(generator, helper));
        generator.addProvider(event.includeServer(), new CGItemTags(generator, new BlockTagsProvider(generator, CreateGravity.MODID, helper), helper));
    }
}