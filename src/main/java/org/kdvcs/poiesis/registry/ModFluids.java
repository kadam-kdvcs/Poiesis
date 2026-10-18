package org.kdvcs.poiesis.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
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
    private static final ResourceLocation BASE_TEXTURE =
            texture("block/fluids/base");

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

    // Pale blue-white refined slurry versus dark green-brown chromium waste.
    private static final int REFINED_ALBICHROME_SLURRY_TINT = 0xFFB8D8E8;
    private static final int CHROMIUM_BEARING_WASTE_TINT = 0xFF586B4A;
    private static final ResourceLocation WASTEWATER_STILL =
            texture("block/fluids/wastewater_still");
    private static final ResourceLocation WASTEWATER_FLOW =
            texture("block/fluids/wastewater_flow");
    private static final ResourceLocation WASTEWATER_OVERLAY =
            texture("block/fluids/wastewater_overlay");
    private static final ResourceLocation NUTRIENT_SOLUTION_STILL = texture("block/fluids/nutrient_solution_still");
    private static final ResourceLocation NUTRIENT_SOLUTION_FLOW = texture("block/fluids/nutrient_solution_flow");
    private static final ResourceLocation NUTRIENT_SOLUTION_OVERLAY = texture("block/fluids/nutrient_solution_overlay");
    private static final ResourceLocation ORGANIC_NUTRIENT_SOLUTION_STILL = texture("block/fluids/organic_nutrient_solution_still");
    private static final ResourceLocation ORGANIC_NUTRIENT_SOLUTION_FLOW = texture("block/fluids/organic_nutrient_solution_flow");
    private static final ResourceLocation ORGANIC_NUTRIENT_SOLUTION_OVERLAY = texture("block/fluids/organic_nutrient_solution_overlay");
    private static final ResourceLocation VITAMIN_SOLUTION_STILL = texture("block/fluids/vitamin_solution_still");
    private static final ResourceLocation VITAMIN_SOLUTION_FLOW = texture("block/fluids/vitamin_solution_flow");
    private static final ResourceLocation VITAMIN_SOLUTION_OVERLAY = texture("block/fluids/vitamin_solution_overlay");

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Poiesis.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
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

    public static final RegistryObject<FluidType> REFINED_ALBICHROME_SLURRY_TYPE =
            registerFluidType("refined_albichrome_slurry", REFINED_ALBICHROME_SLURRY_TINT);
    public static final RegistryObject<FlowingFluid> REFINED_ALBICHROME_SLURRY =
            FLUIDS.register("refined_albichrome_slurry",
                    () -> new ForgeFlowingFluid.Source(refinedAlbichromeSlurryProperties()));
    public static final RegistryObject<FlowingFluid> FLOWING_REFINED_ALBICHROME_SLURRY =
            FLUIDS.register("flowing_refined_albichrome_slurry",
                    () -> new ForgeFlowingFluid.Flowing(refinedAlbichromeSlurryProperties()));

    public static final RegistryObject<FluidType> CHROMIUM_BEARING_WASTE_TYPE =
            registerFluidType("chromium_bearing_waste", CHROMIUM_BEARING_WASTE_TINT);
    public static final RegistryObject<FlowingFluid> CHROMIUM_BEARING_WASTE =
            FLUIDS.register("chromium_bearing_waste",
                    () -> new ForgeFlowingFluid.Source(chromiumBearingWasteProperties()));
    public static final RegistryObject<FlowingFluid> FLOWING_CHROMIUM_BEARING_WASTE =
            FLUIDS.register("flowing_chromium_bearing_waste",
                    () -> new ForgeFlowingFluid.Flowing(chromiumBearingWasteProperties()));

    public static final RegistryObject<FluidType> NUTRIENT_SOLUTION_TYPE =
            registerFluidType("nutrient_solution", 0xFFFFFFFF,
                    NUTRIENT_SOLUTION_STILL, NUTRIENT_SOLUTION_FLOW, NUTRIENT_SOLUTION_OVERLAY);
    public static final RegistryObject<FlowingFluid> NUTRIENT_SOLUTION = FLUIDS.register(
            "nutrient_solution", () -> new ForgeFlowingFluid.Source(nutrientSolutionProperties()));
    public static final RegistryObject<FlowingFluid> FLOWING_NUTRIENT_SOLUTION = FLUIDS.register(
            "flowing_nutrient_solution", () -> new ForgeFlowingFluid.Flowing(nutrientSolutionProperties()));

    public static final RegistryObject<FluidType> ORGANIC_NUTRIENT_SOLUTION_TYPE =
            registerFluidType("organic_nutrient_solution", 0xFFFFFFFF,
                    ORGANIC_NUTRIENT_SOLUTION_STILL, ORGANIC_NUTRIENT_SOLUTION_FLOW, ORGANIC_NUTRIENT_SOLUTION_OVERLAY);
    public static final RegistryObject<FlowingFluid> ORGANIC_NUTRIENT_SOLUTION = FLUIDS.register(
            "organic_nutrient_solution", () -> new ForgeFlowingFluid.Source(organicNutrientSolutionProperties()));
    public static final RegistryObject<FlowingFluid> FLOWING_ORGANIC_NUTRIENT_SOLUTION = FLUIDS.register(
            "flowing_organic_nutrient_solution", () -> new ForgeFlowingFluid.Flowing(organicNutrientSolutionProperties()));

    public static final RegistryObject<FluidType> VITAMIN_SOLUTION_TYPE =
            registerFluidType("vitamin_solution", 0xFFFFFFFF,
                    VITAMIN_SOLUTION_STILL, VITAMIN_SOLUTION_FLOW, VITAMIN_SOLUTION_OVERLAY);
    public static final RegistryObject<FlowingFluid> VITAMIN_SOLUTION = FLUIDS.register(
            "vitamin_solution", () -> new ForgeFlowingFluid.Source(vitaminSolutionProperties()));
    public static final RegistryObject<FlowingFluid> FLOWING_VITAMIN_SOLUTION = FLUIDS.register(
            "flowing_vitamin_solution", () -> new ForgeFlowingFluid.Flowing(vitaminSolutionProperties()));

    /** Murky water produced by the Separator's rinsing process. */
    public static final RegistryObject<FluidType> WASTEWATER_TYPE =
            registerFluidType(
                    "wastewater",
                    0xFFFFFFFF,
                    WASTEWATER_STILL,
                    WASTEWATER_FLOW,
                    WASTEWATER_OVERLAY
            );
    public static final RegistryObject<FlowingFluid> WASTEWATER = FLUIDS.register(
            "wastewater",
            () -> new ForgeFlowingFluid.Source(wastewaterProperties())
    );
    public static final RegistryObject<FlowingFluid> FLOWING_WASTEWATER = FLUIDS.register(
            "flowing_wastewater",
            () -> new ForgeFlowingFluid.Flowing(wastewaterProperties())
    );

    private static RegistryObject<FluidType> registerFluidType(String name, int tint) {
        return registerFluidType(name, tint, BASE_TEXTURE, BASE_TEXTURE, BASE_TEXTURE);
    }

    private static RegistryObject<FluidType> registerFluidType(
            String name, int tint, ResourceLocation stillTexture,
            ResourceLocation flowingTexture, ResourceLocation overlayTexture
    ) {
        return FLUID_TYPES.register(name, () -> new FluidType(
                FluidType.Properties.create()
                        .density(1200)
                        .temperature(300)
                        .viscosity(1200)
                        .canPushEntity(false)
                        .canSwim(false)
                        .canDrown(false)
                        .pathType(null)
                        .adjacentPathType(null)
        ) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        return stillTexture;
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return flowingTexture;
                    }

                    @Override
                    public ResourceLocation getOverlayTexture() {
                        return overlayTexture;
                    }

                    @Override
                    public int getTintColor() {
                        return tint;
                    }

                    @Override
                    public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                        return tint;
                    }
                });
            }
        });
    }

    private static ForgeFlowingFluid.Properties producerGasProperties() {
        // Deliberately has no bucket or liquid block: this gas only exists in
        // tanks, recipes and fluid-capability networks.
        return new ForgeFlowingFluid.Properties(
                PRODUCER_GAS_TYPE,
                PRODUCER_GAS,
                FLOWING_PRODUCER_GAS
        );
    }

    private static ForgeFlowingFluid.Properties refinedAlbichromeSlurryProperties() {
        return new ForgeFlowingFluid.Properties(
                REFINED_ALBICHROME_SLURRY_TYPE,
                REFINED_ALBICHROME_SLURRY,
                FLOWING_REFINED_ALBICHROME_SLURRY
        ).bucket(ModItems.REFINED_ALBICHROME_SLURRY_BUCKET::get)
                .block(() -> ModBlocks.REFINED_ALBICHROME_SLURRY.get());
    }

    private static ForgeFlowingFluid.Properties chromiumBearingWasteProperties() {
        return new ForgeFlowingFluid.Properties(
                CHROMIUM_BEARING_WASTE_TYPE,
                CHROMIUM_BEARING_WASTE,
                FLOWING_CHROMIUM_BEARING_WASTE
        ).bucket(ModItems.CHROMIUM_BEARING_WASTE_BUCKET::get)
                .block(() -> ModBlocks.CHROMIUM_BEARING_WASTE.get());
    }

    private static ForgeFlowingFluid.Properties nutrientSolutionProperties() {
        return new ForgeFlowingFluid.Properties(
                NUTRIENT_SOLUTION_TYPE, NUTRIENT_SOLUTION, FLOWING_NUTRIENT_SOLUTION
        ).bucket(ModItems.NUTRIENT_SOLUTION_BUCKET::get)
                .block(() -> ModBlocks.NUTRIENT_SOLUTION.get());
    }

    private static ForgeFlowingFluid.Properties organicNutrientSolutionProperties() {
        return new ForgeFlowingFluid.Properties(
                ORGANIC_NUTRIENT_SOLUTION_TYPE,
                ORGANIC_NUTRIENT_SOLUTION,
                FLOWING_ORGANIC_NUTRIENT_SOLUTION
        ).bucket(ModItems.ORGANIC_NUTRIENT_SOLUTION_BUCKET::get)
                .block(() -> ModBlocks.ORGANIC_NUTRIENT_SOLUTION.get());
    }

    private static ForgeFlowingFluid.Properties vitaminSolutionProperties() {
        return new ForgeFlowingFluid.Properties(
                VITAMIN_SOLUTION_TYPE,
                VITAMIN_SOLUTION,
                FLOWING_VITAMIN_SOLUTION
        ).bucket(ModItems.VITAMIN_SOLUTION_BUCKET::get)
                .block(() -> ModBlocks.VITAMIN_SOLUTION.get());
    }

    private static ForgeFlowingFluid.Properties wastewaterProperties() {
        return new ForgeFlowingFluid.Properties(
                WASTEWATER_TYPE,
                WASTEWATER,
                FLOWING_WASTEWATER
        ).bucket(ModItems.WASTEWATER_BUCKET::get)
                .block(() -> ModBlocks.WASTEWATER.get());
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
