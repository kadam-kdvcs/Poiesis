package org.kdvcs.poiesis.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.network.ModNetwork;
import org.kdvcs.poiesis.network.packet.SelectAlloyFactoryRecipePacket;
import org.kdvcs.poiesis.network.packet.StartAlloyFactoryPacket;
import org.kdvcs.poiesis.network.packet.ToggleAlloyFactoryAutoPacket;
import org.kdvcs.poiesis.recipe.CountedIngredient;
import org.kdvcs.poiesis.recipe.alloy.AlloyDefinition;
import org.kdvcs.poiesis.screen.base.AbstractMachineScreen;
import org.kdvcs.poiesis.screen.model.AlloyFactoryListModel;
import org.kdvcs.poiesis.screen.model.AlloyFactoryListModel.AlloyFamily;
import org.kdvcs.poiesis.screen.widget.AutoCraftToggleButton;
import org.kdvcs.poiesis.screen.widget.SmeltingButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AlloyFactoryScreen extends AbstractMachineScreen<AlloyFactoryMenu> {
    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 226;
    private static final ResourceLocation TEXTURE = containerTexture("alloy_factory");
    private static final ResourceLocation ENERGY_TEXTURE = containerTexture("widget/energy_bar");
    private static final ResourceLocation SLOT_TEXTURE = containerTexture("widget/slot");
    private static final ResourceLocation SELECTED_SLOT_TEXTURE = containerTexture("widget/slot_selected");
    private static final ResourceLocation MATERIAL_BAR_TEXTURE = containerTexture("widget/alloy_factory_material_bar");
    private static final ResourceLocation PROGRESS_BASE_TEXTURE = containerTexture("widget/alloy_factory_base");
    private static final ResourceLocation PROGRESS_TEXTURE = containerTexture("widget/alloy_factory_progress_arrow");

    private static final int DISPLAY_X = 90;
    private static final int DISPLAY_Y = 18;
    private static final int DISPLAY_WIDTH = 106;
    private static final int DISPLAY_HEIGHT = 108;
    private static final int ROW_Y = 33;
    private static final int ROW_HEIGHT = 18;
    private static final int VISIBLE_ROWS = 5;

    private static final int RESULT_X = 92;
    private static final int MATERIAL_X = 111;
    private static final int PROGRESS_BASE_X = 173;
    private static final int PROGRESS_X = 174;
    private static final int CRAFT_X = 180;
    private static final int AUTO_X = 178;
    private static final int AUTO_Y = 21;

    private static final int ENERGY_X = 10;
    private static final int ENERGY_Y = 33;
    private static final int ENERGY_WIDTH = 12;
    private static final int ENERGY_HEIGHT = 45;
    private static final int TEXT_COLOR = 0xE8E8E8;
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    private final SmeltingButton[] craftButtons = new SmeltingButton[VISIBLE_ROWS];
    private final AlloyFactoryListModel recipeList;
    private AutoCraftToggleButton autoButton;
    private int scrollOffset;
    private ResourceLocation materialScrollRecipeId;
    private int materialScrollOffset;

    public AlloyFactoryScreen(AlloyFactoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, GUI_WIDTH, GUI_HEIGHT);
        recipeList = new AlloyFactoryListModel(menu);
    }

    @Override
    protected void configureLabels() {
        titleLabelX = (imageWidth - font.width(title)) / 2;
        titleLabelY = 6;
        inventoryLabelX = 48;
        inventoryLabelY = 133;
    }

    @Override
    protected void init() {
        super.init();
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            final int visibleRow = row;
            SmeltingButton button = new SmeltingButton(
                    leftPos + CRAFT_X,
                    topPos + ROW_Y + row * ROW_HEIGHT + 2,
                    () -> craftVisible(visibleRow)
            );
            button.setTooltip(Tooltip.create(Component.translatable("gui.poiesis.alloy_factory.craft")));
            button.visible = false;
            craftButtons[row] = addRenderableWidget(button);
        }
        autoButton = addRenderableWidget(new AutoCraftToggleButton(
                leftPos + AUTO_X,
                topPos + AUTO_Y,
                menu.autoCrafting(),
                () -> ModNetwork.sendToServer(new ToggleAlloyFactoryAutoPacket(menu.machinePos()))
        ));
    }

    @Override protected ResourceLocation backgroundTexture() { return TEXTURE; }

    @Override
    protected void renderMachineContents(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawEnergyBarFrame(graphics, ENERGY_X, ENERGY_Y + ENERGY_HEIGHT - 1);
        drawBatterySlot(graphics, 8, 82);
        List<AlloyFamily> families = availableFamilies();
        clampScroll(families.size());
        renderHeader(graphics);
        if (!families.isEmpty()) {
            for (int row = 0; row < VISIBLE_ROWS; row++) {
                int index = scrollOffset + row;
                if (index >= families.size()) break;
                renderRow(graphics, families.get(index), row);
            }
        }
        renderEnergy(graphics);
        updateButtons(families);
    }

    private void renderHeader(GuiGraphics graphics) {
        graphics.drawString(
                font,
                Component.translatable("gui.poiesis.alloy_factory.auto_craft"),
                leftPos + DISPLAY_X + 3,
                topPos + DISPLAY_Y + 3,
                TEXT_COLOR,
                false
        );
    }

    private void renderRow(GuiGraphics graphics, AlloyFamily family, int row) {
        AlloyDefinition recipe = chosen(family);
        int y = topPos + ROW_Y + row * ROW_HEIGHT;
        boolean selected = recipe.recipeId().equals(menu.selectedRecipeId());

        graphics.blit(
                selected ? SELECTED_SLOT_TEXTURE : SLOT_TEXTURE,
                leftPos + RESULT_X,
                y,
                0,
                0,
                18,
                18,
                18,
                18
        );
        graphics.blit(
                MATERIAL_BAR_TEXTURE,
                leftPos + MATERIAL_X,
                y,
                0,
                selected ? 18 : 0,
                61,
                18,
                61,
                36
        );
        graphics.blit(
                PROGRESS_BASE_TEXTURE,
                leftPos + PROGRESS_BASE_X,
                y + 1,
                0,
                0,
                6,
                16,
                6,
                16
        );

        int resultX = leftPos + RESULT_X + 1;
        int resultY = y + 1;
        graphics.renderItem(recipe.result(), resultX, resultY);
        // Keep the per-batch yield visible on the candidate itself. The
        // tooltip below continues to show the inventory-dependent maximum.
        graphics.renderItemDecorations(
                font,
                recipe.result(),
                resultX,
                resultY,
                Integer.toString(recipe.result().getCount())
        );

        int materialOffset = materialOffset(recipe);
        int visibleMaterials = Math.min(3, recipe.inputs().size() - materialOffset);
        for (int i = 0; i < visibleMaterials; i++) {
            CountedIngredient requirement = recipe.inputs().get(materialOffset + i);
            ItemStack icon = displayStack(requirement);
            if (icon.isEmpty()) continue;
            int x = leftPos + MATERIAL_X + 1 + i * 16;
            graphics.renderItem(icon, x, y + 1);
            graphics.renderItemDecorations(
                    font, icon, x, y + 1,
                    requirement.count() > 1 ? Integer.toString(requirement.count()) : null
            );
        }

        if (selected && menu.processing()) {
            renderRowProgress(graphics, y);
        }

    }

    private void renderRowProgress(GuiGraphics graphics, int rowY) {
        int visibleHeight = scale(menu.progress(), menu.maxProgress(), 14);
        if (visibleHeight <= 0) return;
        int sourceY = 14 - visibleHeight;
        int destinationY = rowY + 2 + sourceY;
        graphics.blit(
                PROGRESS_TEXTURE,
                leftPos + PROGRESS_X,
                destinationY,
                0,
                sourceY,
                4,
                visibleHeight,
                4,
                14
        );
    }

    private void renderEnergy(GuiGraphics graphics) {
        int height = scale(menu.energy(), menu.capacity(), ENERGY_HEIGHT);
        if (height <= 0) return;
        int sourceY = ENERGY_HEIGHT - height;
        int destinationY = ENERGY_Y + ENERGY_HEIGHT - height;
        graphics.blit(
                ENERGY_TEXTURE,
                leftPos + ENERGY_X,
                topPos + destinationY,
                0,
                sourceY,
                ENERGY_WIDTH,
                height,
                ENERGY_WIDTH,
                ENERGY_HEIGHT
        );
    }

    private void updateButtons(List<AlloyFamily> families) {
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = scrollOffset + row;
            SmeltingButton button = craftButtons[row];
            button.visible = index < families.size();
            if (!button.visible) {
                button.setSelected(false);
                button.setProcessing(false);
                button.active = false;
                continue;
            }
            AlloyDefinition recipe = chosen(families.get(index));
            button.setSelected(recipe.recipeId().equals(menu.selectedRecipeId()));
            button.setProcessing(menu.processing()
                    && recipe.recipeId().equals(menu.selectedRecipeId()));
            button.active = menu.canStart(recipe);
        }
        autoButton.setToggled(menu.autoCrafting());
    }

    private void craftVisible(int row) {
        AlloyDefinition recipe = visibleRecipe(row);
        if (recipe == null || !menu.canStart(recipe)) return;
        select(recipe);
        ModNetwork.sendToServer(new StartAlloyFactoryPacket(menu.machinePos(), recipe.recipeId()));
    }

    private void select(AlloyDefinition recipe) {
        if (menu.processing()) return;
        menu.setSelectedRecipeId(recipe.recipeId());
        recipeList.choose(recipe);
        ModNetwork.sendToServer(new SelectAlloyFactoryRecipePacket(menu.machinePos(), recipe.recipeId()));
    }

    private void cycleVariant(AlloyFamily family, boolean next) {
        AlloyDefinition current = chosen(family);
        int index = family.variants().indexOf(current);
        int direction = next ? 1 : -1;
        AlloyDefinition target = family.variants().get(
                Math.floorMod(index + direction, family.variants().size())
        );
        select(target);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if ((button == 0 || button == 1) && insideContent(mouseX, mouseY)) {
            int row = ((int) mouseY - topPos - ROW_Y) / ROW_HEIGHT;
            AlloyFamily family = visibleFamily(row);
            if (family != null) {
                int localX = (int) mouseX - leftPos;
                if (family.variants().size() > 1
                        && localX >= RESULT_X && localX < RESULT_X + 18) {
                    cycleVariant(family, button == 0);
                    return true;
                }
                if (button == 0 && !craftButtons[row].isMouseOver(mouseX, mouseY)) {
                    select(chosen(family));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!insideContent(mouseX, mouseY)) return super.mouseScrolled(mouseX, mouseY, delta);
        AlloyFamily materialFamily = hoveredFamily((int) mouseX, (int) mouseY);
        int materialRow = ((int) mouseY - topPos - ROW_Y) / ROW_HEIGHT;
        int materialRowY = ROW_Y + materialRow * ROW_HEIGHT;
        if (materialFamily != null
                && isHovering(MATERIAL_X, materialRowY, 61, ROW_HEIGHT, mouseX, mouseY)) {
            AlloyDefinition recipe = chosen(materialFamily);
            int maxOffset = Math.max(0, recipe.inputs().size() - 3);
            if (maxOffset > 0) {
                if (!recipe.recipeId().equals(materialScrollRecipeId)) {
                    materialScrollRecipeId = recipe.recipeId();
                    materialScrollOffset = 0;
                }
                materialScrollOffset = Math.max(
                        0,
                        Math.min(maxOffset, materialScrollOffset + (delta < 0 ? 1 : -1))
                );
                return true;
            }
        }
        int max = Math.max(0, availableFamilies().size() - VISIBLE_ROWS);
        if (max == 0) return super.mouseScrolled(mouseX, mouseY, delta);
        scrollOffset = Math.max(0, Math.min(max, scrollOffset + (delta < 0 ? 1 : -1)));
        return true;
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(ENERGY_X, ENERGY_Y, ENERGY_WIDTH, ENERGY_HEIGHT, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatable("gui.poiesis.energy", number(menu.energy()), number(menu.capacity())),
                    Component.translatable("gui.poiesis.power", number(menu.activeEnergyPerTick()))
            ), mouseX, mouseY);
            return;
        }

        if (autoButton != null && autoButton.isMouseOver(mouseX, mouseY)) {
            graphics.renderComponentTooltip(
                    font,
                    List.of(
                            Component.translatable("gui.poiesis.alloy_factory.auto_smelting"),
                            Component.translatable(
                                    menu.autoCrafting()
                                            ? "gui.poiesis.module_fabricator.auto_on"
                                            : "gui.poiesis.module_fabricator.auto_off"
                            )
                    ),
                    mouseX,
                    mouseY
            );
            return;
        }

        AlloyFamily family = hoveredFamily(mouseX, mouseY);
        if (family == null) return;
        AlloyDefinition recipe = chosen(family);
        int localX = mouseX - leftPos;
        if (localX >= RESULT_X && localX < RESULT_X + 18) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(recipe.result().getHoverName());
            tooltip.add(Component.translatable(
                     "gui.poiesis.alloy_factory.craftable_count",
                    craftableOutputCount(recipe)
             ));
            if (family.variants().size() > 1) {
                tooltip.add(Component.translatable("gui.poiesis.alloy_factory.switch_variant"));
            }
            graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        } else {
            HoveredMaterial material = hoveredMaterial(mouseX, mouseY, recipe);
            if (material != null) {
                graphics.renderComponentTooltip(font, List.of(
                        material.stack().getHoverName(),
                        Component.translatable(
                                "gui.poiesis.module_fabricator.required_count",
                                material.requirement().count()
                        )
                ), mouseX, mouseY);
            }
        }
    }

    private List<AlloyFamily> availableFamilies() {
        if (minecraft == null) return List.of();
        return recipeList.availableFamilies(minecraft.level);
    }

    private String craftableOutputCount(AlloyDefinition recipe) {
        long batches = recipeList.craftableBatches(recipe);
        long perBatch = recipe.result().getCount();
        return NUMBER_FORMAT.format(batches * perBatch);
    }

    private AlloyDefinition chosen(AlloyFamily family) {
        return recipeList.chosen(family);
    }

    private AlloyFamily visibleFamily(int row) {
        List<AlloyFamily> families = availableFamilies();
        int index = scrollOffset + row;
        return row >= 0 && row < VISIBLE_ROWS && index < families.size() ? families.get(index) : null;
    }

    private AlloyDefinition visibleRecipe(int row) {
        AlloyFamily family = visibleFamily(row);
        return family == null ? null : chosen(family);
    }

    private AlloyFamily hoveredFamily(int mouseX, int mouseY) {
        if (!insideContent(mouseX, mouseY)) return null;
        int row = (mouseY - topPos - ROW_Y) / ROW_HEIGHT;
        return visibleFamily(row);
    }

    private HoveredMaterial hoveredMaterial(
            int mouseX,
            int mouseY,
            AlloyDefinition recipe
    ) {
        int row = (mouseY - topPos - ROW_Y) / ROW_HEIGHT;
        int rowY = ROW_Y + row * ROW_HEIGHT;
        int materialOffset = materialOffset(recipe);
        int visibleMaterials = Math.min(3, recipe.inputs().size() - materialOffset);
        for (int index = 0; index < visibleMaterials; index++) {
            int x = MATERIAL_X + 1 + index * 16;
            if (isHovering(x, rowY + 1, 16, 16, mouseX, mouseY)) {
                CountedIngredient requirement = recipe.inputs().get(materialOffset + index);
                ItemStack stack = displayStack(requirement);
                if (!stack.isEmpty()) {
                    return new HoveredMaterial(stack, requirement);
                }
            }
        }
        return null;
    }

    private int materialOffset(AlloyDefinition recipe) {
        if (!recipe.recipeId().equals(materialScrollRecipeId)) return 0;
        return Math.min(materialScrollOffset, Math.max(0, recipe.inputs().size() - 3));
    }

    private boolean insideContent(double mouseX, double mouseY) {
        double x = mouseX - leftPos;
        double y = mouseY - topPos;
        return x >= DISPLAY_X && x < DISPLAY_X + DISPLAY_WIDTH
                && y >= ROW_Y && y < DISPLAY_Y + DISPLAY_HEIGHT;
    }

    private void clampScroll(int count) {
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, count - VISIBLE_ROWS)));
    }

    private ItemStack displayStack(CountedIngredient requirement) {
        for (int slot = 0; slot < 18; slot++) {
            ItemStack inserted = menu.getSlot(slot).getItem();
            if (!inserted.isEmpty() && requirement.ingredient().test(inserted)) {
                ItemStack result = inserted.copy();
                result.setCount(1);
                return result;
            }
        }
        ItemStack[] examples = requirement.ingredient().getItems();
        if (examples.length == 0) return ItemStack.EMPTY;
        ItemStack result = examples[0].copy();
        result.setCount(1);
        return result;
    }

    private static String number(int value) { return NUMBER_FORMAT.format(Math.max(0, value)); }
    private record HoveredMaterial(ItemStack stack, CountedIngredient requirement) { }
}
