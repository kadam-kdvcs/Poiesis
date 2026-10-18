package org.kdvcs.poiesis.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.kdvcs.poiesis.block.entity.ModuleFabricatorBlockEntity;
import org.kdvcs.poiesis.machine.energy.item.BatterySlotComponent;
import org.kdvcs.poiesis.machine.fabrication.ModuleFabricationProcess;
import org.kdvcs.poiesis.recipe.ModuleFabricationRecipe;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModItems;
import org.kdvcs.poiesis.registry.ModMenuTypes;
import org.kdvcs.poiesis.screen.menu.AbstractMachineMenu;
import org.kdvcs.poiesis.screen.slot.BatterySlot;
import org.kdvcs.poiesis.screen.slot.OutputSlot;

public final class ModuleFabricatorMenu
        extends AbstractMachineMenu {

    /*
     * Menu 中的机器槽索引：
     *
     * 0      substrate
     * 1      output
     * 2~10   materials
     * 11     battery
     */
    private static final int SUBSTRATE_MENU_SLOT = 0;
    private static final int OUTPUT_MENU_SLOT = 1;

    private static final int FIRST_MATERIAL_MENU_SLOT = 2;
    private static final int LAST_MATERIAL_MENU_SLOT = 10;

    private static final int BATTERY_MENU_SLOT = 11;

    private static final int MACHINE_SLOT_COUNT = 12;
    private static final int DATA_COUNT = 9;

    private static final int BATTERY_SLOT_X = 14;
    private static final int BATTERY_SLOT_Y = 77;

    private final ModuleFabricatorBlockEntity blockEntity;

    private ResourceLocation selectedRecipeId;

    public ModuleFabricatorMenu(
            int containerId,
            Inventory playerInventory,
            FriendlyByteBuf buffer
    ) {
        this(
                containerId,
                playerInventory,
                requireBlockEntity(
                        playerInventory,
                        buffer.readBlockPos()
                ),
                new SimpleContainerData(DATA_COUNT),
                readRecipeId(buffer.readUtf())
        );
    }

    public ModuleFabricatorMenu(
            int containerId,
            Inventory playerInventory,
            ModuleFabricatorBlockEntity blockEntity,
            ContainerData data,
            ResourceLocation selectedRecipeId
    ) {
        super(
                ModMenuTypes.MODULE_FABRICATOR.get(),
                containerId,
                playerInventory,
                data,
                ContainerLevelAccess.create(
                        blockEntity.getLevel(),
                        blockEntity.getBlockPos()
                ),
                ModBlocks.MODULE_FABRICATOR.get(),
                MACHINE_SLOT_COUNT
        );

        this.blockEntity = blockEntity;
        this.selectedRecipeId = selectedRecipeId;

        checkContainerDataCount(
                data,
                DATA_COUNT
        );

        addMachineSlots();

        addPlayerInventory(
                29,
                141
        );

        addDataSlots(data);
    }

    private void addMachineSlots() {
        var items =
                blockEntity.runtime()
                        .inventory()
                        .items();

        addSlot(
                new SlotItemHandler(
                        items,
                        ModuleFabricationProcess.MODULE_BASE_SLOT,
                        40,
                        31
                )
        );

        addSlot(
                new OutputSlot(
                        items,
                        ModuleFabricationProcess.OUTPUT_SLOT,
                        40,
                        74
                )
        );

        for (int i = 0;
             i < ModuleFabricationProcess.MATERIAL_SLOT_COUNT;
             i++) {

            addSlot(
                    new SlotItemHandler(
                            items,
                            ModuleFabricationProcess.FIRST_MATERIAL_SLOT + i,
                            29 + i * 18,
                            110
                    )
            );
        }

        blockEntity.runtime()
                .battery()
                .ifPresent(
                        battery ->
                                addSlot(
                                        new BatterySlot(
                                                battery.items(),
                                                0,
                                                BATTERY_SLOT_X,
                                                BATTERY_SLOT_Y
                                        )
                                )
                );
    }

    @Override
    protected boolean movePlayerStackToMachine(
            ItemStack stack
    ) {
        /*
         * 1. 模块基板优先进入 substrate 槽。
         */
        if (stack.is(
                ModItems.SPECIALIZATION_MODULE_SUBSTRATE.get()
        ) || stack.is(
                ModItems.PERFORMANCE_MODULE_SUBSTRATE.get()
        )) {
            return moveToMachineSlot(
                    stack,
                    SUBSTRATE_MENU_SLOT
            );
        }

        /*
         * 2. 能源物品优先进入电池槽。
         *
         * 包括：
         * - Forge ENERGY Capability 物品
         * - ConsumableEnergyRegistry 注册物品
         */
        if (BatterySlotComponent.isValidBatteryItem(stack)) {
            if (moveToMachineSlot(
                    stack,
                    BATTERY_MENU_SLOT
            )) {
                return true;
            }
        }

        /*
         * 3. 剩余物品进入材料槽。
         *
         * 注意这里不要再从 slot 1 开始，
         * 因为菜单 slot 1 是 OutputSlot。
         */
        return moveToMachineSlots(
                stack,
                FIRST_MATERIAL_MENU_SLOT,
                LAST_MATERIAL_MENU_SLOT
        );
    }

    @Override
    public BlockPos machinePos() {
        return blockEntity.getBlockPos();
    }

    @Override
    public boolean hasSideConfiguration() {
        return true;
    }

    public int progress() {
        return dataValue(0);
    }

    public int maxProgress() {
        return dataValue(1);
    }

    public int energy() {
        return int32DataValue(2, 7);
    }

    public int capacity() {
        return int32DataValue(3, 8);
    }

    public int activeEnergyPerTick() {
        return dataValue(4);
    }

    public boolean autoCrafting() {
        return dataValue(5) != 0;
    }

    public boolean processing() {
        return dataValue(6) != 0;
    }

    public ResourceLocation selectedRecipeId() {
        return selectedRecipeId;
    }

    public void setSelectedRecipeId(
            ResourceLocation selectedRecipeId
    ) {
        this.selectedRecipeId =
                selectedRecipeId;
    }

    public boolean canStart(
            ModuleFabricationRecipe recipe
    ) {
        return blockEntity.runtime()
                .process()
                .canStart(recipe);
    }

    private static ModuleFabricatorBlockEntity requireBlockEntity(
            Inventory playerInventory,
            BlockPos pos
    ) {
        if (playerInventory.player
                .level()
                .getBlockEntity(pos)
                instanceof ModuleFabricatorBlockEntity blockEntity) {

            return blockEntity;
        }

        throw new IllegalStateException(
                "Module fabricator block entity is missing at "
                        + pos
        );
    }

    private static ResourceLocation readRecipeId(
            String value
    ) {
        return value == null
                || value.isBlank()
                ? null
                : ResourceLocation.tryParse(value);
    }
}
