package org.kdvcs.poiesis.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;

import java.util.function.Consumer;

public final class ModFluids {
    private static final ResourceLocation PRODUCER_GAS_STILL =
            texture("block/fluids/water_still");
    private static final ResourceLocation PRODUCER_GAS_FLOW =
            texture("block/fluids/water_flow");
    private static final ResourceLocation PRODUCER_GAS_OVERLAY =
            texture("block/fluids/water_overlay");

    /**
     * Producer gas is visually represented by a warm, smoky tint applied to
     * the shared greyscale fluid textures.
     */
    private static final int PRODUCER_GAS_TINT = 0xFFB79A72;

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Poiesis.MOD_ID);
    public static final DeferredRegister<net.minecraft.world.level.material.Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, Poiesis.MOD_ID);

    public static final RegistryObject<FluidType> PRODUCER_GAS_TYPE =
            FLUID_TYPES.register(
                    "producer_gas",
                    () -> new FluidType(
                            FluidType.Properties.create()
                                    .density(-1)
                                    .temperature(650)
                                    .viscosity(200)
                                    .canPushEntity(false)
                                    .canSwim(false)
                                    .canDrown(false)
                                    .pathType(null)
                                    .adjacentPathType(null)
                    ) {
                        @Override
                        public void initializeClient(
                                Consumer<IClientFluidTypeExtensions> consumer
                        ) {
                            consumer.accept(new IClientFluidTypeExtensions() {
                                @Override
                                public ResourceLocation getStillTexture() {
                                    return PRODUCER_GAS_STILL;
                                }

                                @Override
                                public ResourceLocation getFlowingTexture() {
                                    return PRODUCER_GAS_FLOW;
                                }

                                @Override
                                public ResourceLocation getOverlayTexture() {
                                    return PRODUCER_GAS_OVERLAY;
                                }

                                @Override
                                public int getTintColor() {
                                    return PRODUCER_GAS_TINT;
                                }

                                @Override
                                public int getTintColor(
                                        FluidState state,
                                        BlockAndTintGetter getter,
                                        BlockPos pos
                                ) {
                                    return PRODUCER_GAS_TINT;
                                }
                            });
                        }
                    }
            );

    public static final RegistryObject<FlowingFluid> PRODUCER_GAS =
            FLUIDS.register(
                    "producer_gas",
                    () -> new ForgeFlowingFluid.Source(producerGasProperties())
            );
    public static final RegistryObject<FlowingFluid> FLOWING_PRODUCER_GAS =
            FLUIDS.register(
                    "flowing_producer_gas",
                    () -> new ForgeFlowingFluid.Flowing(producerGasProperties())
            );

    private static ForgeFlowingFluid.Properties producerGasProperties() {
        // Deliberately has no bucket or liquid block: this gas only exists in
        // tanks, recipes and fluid-capability networks.
        return new ForgeFlowingFluid.Properties(
                PRODUCER_GAS_TYPE,
                PRODUCER_GAS,
                FLOWING_PRODUCER_GAS
        );
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(Poiesis.MOD_ID, path);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }

    private ModFluids() {
    }
}
