package org.kdvcs.poiesis.machine.generator;

import net.minecraft.resources.ResourceLocation;
import org.kdvcs.poiesis.Poiesis;

/** Replaceable electrochemical internals used by the Microbial Fuel Cell. */
public record ElectrochemicalConfiguration(
        ResourceLocation anode,
        ResourceLocation cathode,
        ResourceLocation membrane,
        ResourceLocation microbialConsortium
) {
    public static final ElectrochemicalConfiguration DEFAULT = new ElectrochemicalConfiguration(
            id("default_carbon_anode"),
            id("default_air_cathode"),
            id("default_proton_exchange_membrane"),
            id("default_microbial_consortium")
    );

    public ElectrochemicalConfiguration {
        if (anode == null || cathode == null || membrane == null || microbialConsortium == null) {
            throw new IllegalArgumentException("Electrochemical components cannot be null");
        }
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(Poiesis.MOD_ID, path);
    }
}
