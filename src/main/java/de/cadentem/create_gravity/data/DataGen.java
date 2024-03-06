package de.cadentem.create_gravity.data;

import net.minecraft.data.DataGenerator;
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

        generator.addProvider(event.includeServer(), new CGRegistryProvider(generator.getPackOutput(), event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new CGEntityTags(generator.getPackOutput(), event.getLookupProvider(), helper));
        generator.addProvider(event.includeServer(), new CGItemTags(generator.getPackOutput(), event.getLookupProvider(), helper));
        generator.addProvider(event.includeServer(), new CGDamageTypeTags(generator.getPackOutput(), event.getLookupProvider(), helper));
    }
}