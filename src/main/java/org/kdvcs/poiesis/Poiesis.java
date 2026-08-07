package org.kdvcs.poiesis;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.kdvcs.poiesis.network.ModNetwork;
import org.kdvcs.poiesis.registry.ModRegistries;
import org.slf4j.Logger;

@Mod(Poiesis.MOD_ID)
public final class Poiesis {
    public static final String MOD_ID = "poiesis";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Poiesis() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistries.register(modEventBus);
        ModNetwork.register();
    }
}
