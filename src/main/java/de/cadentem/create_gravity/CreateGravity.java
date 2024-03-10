package de.cadentem.create_gravity;

import com.mojang.logging.LogUtils;
import de.cadentem.create_gravity.capability.GravityData;
import de.cadentem.create_gravity.config.ServerConfig;
import de.cadentem.create_gravity.network.ClientProxy;
import de.cadentem.create_gravity.network.NetworkHandler;
import de.cadentem.create_gravity.network.Proxy;
import de.cadentem.create_gravity.network.ServerProxy;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CreateGravity.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateGravity {
    public static final String MODID = "create_gravity";
    public static final Logger LOG = LogUtils.getLogger();

    public static final Proxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public CreateGravity() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
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
