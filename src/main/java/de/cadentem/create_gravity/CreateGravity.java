package de.cadentem.create_gravity;

import de.cadentem.create_gravity.capability.GravityData;
import de.cadentem.create_gravity.network.NetworkHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CreateGravity.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateGravity {
    public static final String MODID = "create_gravity";

    public CreateGravity() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.register();
    }

    public static ResourceLocation location(final String path) {
        return new ResourceLocation(MODID, path);
    }

    @SubscribeEvent
    public void registerCapability(final RegisterCapabilitiesEvent event) {
        event.register(GravityData.class);
    }
}
