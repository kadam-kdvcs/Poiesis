package org.kdvcs.poiesis.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.network.ModNetwork;
import org.kdvcs.poiesis.network.packet.SelectModuleFabricationRecipePacket;
import org.kdvcs.poiesis.network.packet.StartModuleFabricationPacket;
import org.kdvcs.poiesis.network.packet.ToggleModuleFabricatorAutoPacket;
import org.kdvcs.poiesis.recipe.CountedIngredient;
import org.kdvcs.poiesis.recipe.ModuleFabricationRecipe;
import org.kdvcs.poiesis.registry.ModRecipeTypes;
import org.kdvcs.poiesis.screen.base.AbstractMachineScreen;
import org.kdvcs.poiesis.screen.widget.AutoCraftToggleButton;
import org.kdvcs.poiesis.screen.widget.FabricationButton;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class ModuleFabricatorScreen
        extends AbstractMachineScreen<ModuleFabricatorMenu> {

    private static final int GUI_WIDTH = 219;
    private static final int GUI_HEIGHT = 223;

    private static final ResourceLocation TEXTURE =
            containerTexture("module_fabricator");

    private static final ResourceLocation ENERGY_TEXTURE =
            containerTexture("widget/energy_bar");

    private static final ResourceLocation PROGRESS_TEXTURE =
            containerTexture("widget/module_fabricator_progress_arrow");

    private static final ResourceLocation SLOT_TEXTURE =
            containerTexture("widget/slot");

    private static final ResourceLocation SELECTED_SLOT_TEXTURE =
            containerTexture("widget/slot_selected");

    private static final ResourceLocation MATERIAL_BAR_TEXTURE =
            containerTexture("widget/material_bar");

    private static final int CONTENT_Y_OFFSET = 3;

    private static final int DISPLAY_X = 64;
    private static final int DISPLAY_Y = 16 + CONTENT_Y_OFFSET;
    private static final int DISPLAY_WIDTH = 138;
    private static final int DISPLAY_HEIGHT = 83;

    private static final int DISPLAY_HEADER_HEIGHT = 13;

    private static final int DISPLAY_CONTENT_X = DISPLAY_X + 1;
    private static final int DISPLAY_CONTENT_Y = DISPLAY_Y + DISPLAY_HEADER_HEIGHT;
    private static final int DISPLAY_CONTENT_WIDTH = DISPLAY_WIDTH - 2;
    private static final int DISPLAY_CONTENT_HEIGHT = DISPLAY_HEIGHT - DISPLAY_HEADER_HEIGHT;

    private static final int ROW_Y = DISPLAY_CONTENT_Y + 1;
    private static final int ROW_HEIGHT = 17;
    private static final int VISIBLE_ROWS = 4;

    private static final int TEMPLATE_FRAME_X = 65;
    private static final int TEMPLATE_FRAME_WIDTH = 18;
    private static final int TEMPLATE_FRAME_HEIGHT = 18;

    private static final int MATERIAL_BAR_X =
            TEMPLATE_FRAME_X + TEMPLATE_FRAME_WIDTH + 1;
    private static final int MATERIAL_BAR_WIDTH = 96;
    private static final int MATERIAL_BAR_HEIGHT = 18;
    private static final int MATERIAL_X = MATERIAL_BAR_X + 1;
    private static final int MATERIAL_Y_OFFSET = 1;
    private static final int MATERIAL_ICON_SIZE = 16;
    private static final int MATERIAL_SPACING = 16;
    private static final int MAX_VISIBLE_MATERIALS = 5;

    private static final int FABRICATE_X =
            MATERIAL_BAR_X + MATERIAL_BAR_WIDTH + 1;
    private static final int FABRICATE_Y_OFFSET =
            (TEMPLATE_FRAME_HEIGHT - FabricationButton.HEIGHT) / 2;

    private static final int AUTO_LABEL_X = DISPLAY_X + 4;
    private static final int AUTO_LABEL_Y = DISPLAY_Y + 2;

    private static final int AUTO_BUTTON_X =
            DISPLAY_X + DISPLAY_WIDTH - 16 - 2;
    private static final int AUTO_BUTTON_Y =
            DISPLAY_Y + (DISPLAY_HEADER_HEIGHT - 8) / 2;

    private static final int ENERGY_X = 16;
    private static final int ENERGY_Y = 25 + CONTENT_Y_OFFSET;
    private static final int ENERGY_WIDTH = 12;
    private static final int ENERGY_HEIGHT = 45;

    private static final int PROGRESS_X = 43;
    private static final int PROGRESS_Y = 48 + CONTENT_Y_OFFSET;
    private static final int PROGRESS_WIDTH = 10;
    private static final int PROGRESS_HEIGHT = 19;

    private static final int TEXT_COLOR = 0xE0E0E0;

    private static final NumberFormat NUMBER_FORMAT =
            NumberFormat.getIntegerInstance(Locale.US);

    private final FabricationButton[] fabricateButtons =
            new FabricationButton[VISIBLE_ROWS];

    private AutoCraftToggleButton autoButton;
    private int scrollOffset;

    public ModuleFabricatorScreen(
            ModuleFabricatorMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    protected void configureLabels() {
        titleLabelX = (imageWidth - font.width(title)) / 2;
        titleLabelY = 7;

        inventoryLabelX = 29;
        inventoryLabelY = 130;
    }

    @Override
    protected void init() {
        super.init();

        for (int row = 0; row < VISIBLE_ROWS; row++) {
            final int buttonRow = row;

            FabricationButton button = new FabricationButton(
                    leftPos + FABRICATE_X,
                    topPos + ROW_Y + row * ROW_HEIGHT
                            + FABRICATE_Y_OFFSET,
                    () -> fabricateVisibleRecipe(buttonRow)
            );

            button.setTooltip(
                    Tooltip.create(
                            Component.translatable(
                                    "gui.poiesis.module_fabricator.craft"
                            )
                    )
            );

            button.visible = false;
            fabricateButtons[row] = addRenderableWidget(button);
        }

        autoButton = addRenderableWidget(
                new AutoCraftToggleButton(
                        leftPos + AUTO_BUTTON_X,
                        topPos + AUTO_BUTTON_Y,
                        menu.autoCrafting(),
                        () -> ModNetwork.sendToServer(
                                new ToggleModuleFabricatorAutoPacket(
                                        menu.machinePos()
                                )
                        )
                )
        );
    }

    @Override
    protected ResourceLocation backgroundTexture() {
        return TEXTURE;
    }

    @Override
    protected void renderMachineContents(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        renderHeader(graphics);

        List<ModuleFabricationRecipe> recipes = availableRecipes();

        if (menu.getSlot(0).getItem().isEmpty()) {
            renderEmptyDisplayHint(graphics);
        } else {
            clampScroll(recipes.size());
            renderRecipeRows(graphics, recipes);
        }

        renderProgress(graphics);
        renderEnergy(graphics);
        updateButtons(recipes);
    }

    private void renderHeader(GuiGraphics graphics) {
        graphics.drawString(
                font,
                Component.translatable(
                        "gui.poiesis.module_fabricator.auto_craft"
                ),
                leftPos + AUTO_LABEL_X,
                topPos + AUTO_LABEL_Y,
                TEXT_COLOR,
                false
        );
    }

    private void renderRecipeRows(
            GuiGraphics graphics,
            List<ModuleFabricationRecipe> recipes
    ) {
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int recipeIndex = scrollOffset + row;
            if (recipeIndex >= recipes.size()) {
                break;
            }
            renderRecipeRow(graphics, recipes.get(recipeIndex), row);
        }
    }

    private void renderRecipeRow(
            GuiGraphics graphics,
            ModuleFabricationRecipe recipe,
            int row
    ) {
        int rowTop = topPos + ROW_Y + row * ROW_HEIGHT;
        boolean selected = recipe.getId().equals(menu.selectedRecipeId());

        renderRecipeRowWidgets(graphics, rowTop, selected);
        renderTemplateIcon(graphics, recipe, rowTop);
        renderMaterialIcons(graphics, recipe, rowTop);
    }

    private void renderRecipeRowWidgets(
            GuiGraphics graphics,
            int rowTop,
            boolean selected
    ) {
        graphics.blit(
                selected ? SELECTED_SLOT_TEXTURE : SLOT_TEXTURE,
                leftPos + TEMPLATE_FRAME_X,
                rowTop,
                0,
                0,
                TEMPLATE_FRAME_WIDTH,
                TEMPLATE_FRAME_HEIGHT,
                TEMPLATE_FRAME_WIDTH,
                TEMPLATE_FRAME_HEIGHT
        );

        graphics.blit(
                MATERIAL_BAR_TEXTURE,
                leftPos + MATERIAL_BAR_X,
                rowTop,
                0,
                selected ? MATERIAL_BAR_HEIGHT : 0,
                MATERIAL_BAR_WIDTH,
                MATERIAL_BAR_HEIGHT,
                MATERIAL_BAR_WIDTH,
                MATERIAL_BAR_HEIGHT * 2
        );
    }

    private void renderTemplateIcon(
            GuiGraphics graphics,
            ModuleFabricationRecipe recipe,
            int rowTop
    ) {
        graphics.renderItem(
                recipe.result(),
                leftPos + TEMPLATE_FRAME_X + 1,
                rowTop + 1
        );
    }

    private void renderMaterialIcons(
            GuiGraphics graphics,
            ModuleFabricationRecipe recipe,
            int rowTop
    ) {
        List<CountedIngredient> materials = recipe.materials();
        int visibleCount = Math.min(materials.size(), MAX_VISIBLE_MATERIALS);

        for (int i = 0; i < visibleCount; i++) {
            CountedIngredient requirement = materials.get(i);
            ItemStack icon = displayStack(requirement);
            if (icon.isEmpty()) {
                continue;
            }

            int x = leftPos + MATERIAL_X + i * MATERIAL_SPACING;
            int y = rowTop + MATERIAL_Y_OFFSET;

            graphics.renderItem(icon, x, y);
            graphics.renderItemDecorations(
                    font,
                    icon,
                    x,
                    y,
                    requirement.count() > 1
                            ? Integer.toString(requirement.count())
                            : null
            );
        }
    }

    private void renderProgress(GuiGraphics graphics) {
        int visibleHeight = scale(
                menu.progress(),
                menu.maxProgress(),
                PROGRESS_HEIGHT
        );

        if (visibleHeight <= 0) {
            return;
        }

        graphics.blit(
                PROGRESS_TEXTURE,
                leftPos + PROGRESS_X,
                topPos + PROGRESS_Y,
                0,
                0,
                PROGRESS_WIDTH,
                visibleHeight,
                PROGRESS_WIDTH,
                PROGRESS_HEIGHT
        );
    }

    private void renderEnergy(GuiGraphics graphics) {
        int visibleHeight = scale(
                menu.energy(),
                menu.capacity(),
                ENERGY_HEIGHT
        );

        if (visibleHeight <= 0) {
            return;
        }

        int sourceY = ENERGY_HEIGHT - visibleHeight;
        int destinationY = ENERGY_Y + ENERGY_HEIGHT - visibleHeight;

        graphics.blit(
                ENERGY_TEXTURE,
                leftPos + ENERGY_X,
                topPos + destinationY,
                0,
                sourceY,
                ENERGY_WIDTH,
                visibleHeight,
                ENERGY_WIDTH,
                ENERGY_HEIGHT
        );
    }

    private void updateButtons(List<ModuleFabricationRecipe> recipes) {
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            FabricationButton button = fabricateButtons[row];
            int recipeIndex = scrollOffset + row;
            boolean hasRecipe =
                    recipeIndex >= 0 && recipeIndex < recipes.size();

            button.visible = hasRecipe;

            if (!hasRecipe) {
                button.setSelected(false);
                button.setProcessing(false);
                button.active = false;
                continue;
            }

            ModuleFabricationRecipe recipe = recipes.get(recipeIndex);
            button.setSelected(
                    recipe.getId().equals(menu.selectedRecipeId())
            );
            button.setProcessing(
                    menu.processing()
                            && recipe.getId().equals(menu.selectedRecipeId())
            );
            button.active = !menu.processing() && menu.canStart(recipe);
        }

        autoButton.setToggled(menu.autoCrafting());
    }

    private void fabricateVisibleRecipe(int row) {
        ModuleFabricationRecipe recipe = visibleRecipeAt(row);
        if (recipe == null || menu.processing() || !menu.canStart(recipe)) {
            return;
        }

        ResourceLocation recipeId = recipe.getId();
        menu.setSelectedRecipeId(recipeId);

        ModNetwork.sendToServer(
                new StartModuleFabricationPacket(
                        menu.machinePos(),
                        recipeId
                )
        );
    }

    private void selectVisibleRecipe(int row) {
        if (menu.processing()) {
            return;
        }
        ModuleFabricationRecipe recipe = visibleRecipeAt(row);
        if (recipe == null) {
            return;
        }

        ResourceLocation id = recipe.getId();
        menu.setSelectedRecipeId(id);

        ModNetwork.sendToServer(
                new SelectModuleFabricationRecipePacket(
                        menu.machinePos(),
                        id
                )
        );
    }

    private ModuleFabricationRecipe visibleRecipeAt(int row) {
        List<ModuleFabricationRecipe> recipes = availableRecipes();
        int index = scrollOffset + row;

        if (row < 0
                || row >= VISIBLE_ROWS
                || index < 0
                || index >= recipes.size()) {
            return null;
        }

        return recipes.get(index);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isInsideDisplayContent(mouseX, mouseY)) {
            int relativeY = (int) mouseY - topPos - ROW_Y;

            if (relativeY >= 0) {
                int row = relativeY / ROW_HEIGHT;

                if (row >= 0 && row < VISIBLE_ROWS) {
                    FabricationButton craftButton = fabricateButtons[row];

                    if (!craftButton.isMouseOver(mouseX, mouseY)) {
                        selectVisibleRecipe(row);
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ) {
        if (!isInsideDisplayContent(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }

        List<ModuleFabricationRecipe> recipes = availableRecipes();
        int max = Math.max(0, recipes.size() - VISIBLE_ROWS);

        if (max <= 0) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }

        scrollOffset = Math.max(
                0,
                Math.min(
                        max,
                        scrollOffset + (delta < 0 ? 1 : -1)
                )
        );

        return true;
    }

    @Override
    protected void renderTooltip(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        super.renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(
                ENERGY_X,
                ENERGY_Y,
                ENERGY_WIDTH,
                ENERGY_HEIGHT,
                mouseX,
                mouseY
        )) {
            renderEnergyTooltip(graphics, mouseX, mouseY);
            return;
        }

        if (autoButton.isMouseOver(mouseX, mouseY)) {
            graphics.renderComponentTooltip(
                    font,
                    List.of(
                            Component.translatable(
                                    "gui.poiesis.module_fabricator.auto_craft"
                            ),
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

        HoveredRecipeIcon hovered = hoveredRecipeIcon(mouseX, mouseY);
        if (hovered == null) {
            return;
        }

        if (hovered.material() == null) {
            graphics.renderTooltip(
                    font,
                    hovered.stack(),
                    mouseX,
                    mouseY
            );
            return;
        }

        graphics.renderComponentTooltip(
                font,
                List.of(
                        hovered.stack().getHoverName(),
                        Component.translatable(
                                "gui.poiesis.module_fabricator.required_count",
                                hovered.material().count()
                        )
                ),
                mouseX,
                mouseY
        );
    }

    private void renderEnergyTooltip(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        graphics.renderComponentTooltip(
                font,
                List.of(
                        Component.translatable(
                                "gui.poiesis.energy",
                                formatNumber(menu.energy()),
                                formatNumber(menu.capacity())
                        ),
                        Component.translatable(
                                "gui.poiesis.power",
                                formatNumber(menu.activeEnergyPerTick())
                        )
                ),
                mouseX,
                mouseY
        );
    }

    private HoveredRecipeIcon hoveredRecipeIcon(int mouseX, int mouseY) {
        List<ModuleFabricationRecipe> recipes = availableRecipes();

        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int recipeIndex = scrollOffset + row;
            if (recipeIndex >= recipes.size()) {
                break;
            }

            ModuleFabricationRecipe recipe = recipes.get(recipeIndex);
            int rowTop = ROW_Y + row * ROW_HEIGHT;

            if (isHovering(
                    TEMPLATE_FRAME_X,
                    rowTop,
                    TEMPLATE_FRAME_WIDTH,
                    TEMPLATE_FRAME_HEIGHT,
                    mouseX,
                    mouseY
            )) {
                return new HoveredRecipeIcon(recipe.result(), null);
            }

            List<CountedIngredient> materials = recipe.materials();
            int visibleCount = Math.min(materials.size(), MAX_VISIBLE_MATERIALS);

            for (int i = 0; i < visibleCount; i++) {
                CountedIngredient requirement = materials.get(i);
                int x = MATERIAL_X + i * MATERIAL_SPACING;
                int y = rowTop + MATERIAL_Y_OFFSET;

                if (isHovering(
                        x,
                        y,
                        MATERIAL_ICON_SIZE,
                        MATERIAL_ICON_SIZE,
                        mouseX,
                        mouseY
                )) {
                    ItemStack stack = displayStack(requirement);
                    if (!stack.isEmpty()) {
                        return new HoveredRecipeIcon(stack, requirement);
                    }
                }
            }
        }

        return null;
    }

    private void renderEmptyDisplayHint(GuiGraphics graphics) {
        Component text = Component.translatable(
                "gui.poiesis.module_fabricator.insert_substrate"
        );

        int maxTextWidth = DISPLAY_CONTENT_WIDTH - 12;
        List<net.minecraft.util.FormattedCharSequence> lines =
                font.split(text, maxTextWidth);

        int totalHeight = lines.size() * font.lineHeight;

        int startY = topPos
                + DISPLAY_CONTENT_Y
                + (DISPLAY_CONTENT_HEIGHT - totalHeight) / 2;

        for (int i = 0; i < lines.size(); i++) {
            net.minecraft.util.FormattedCharSequence line = lines.get(i);
            int lineWidth = font.width(line);

            int x = leftPos
                    + DISPLAY_CONTENT_X
                    + (DISPLAY_CONTENT_WIDTH - lineWidth) / 2;

            int y = startY + i * font.lineHeight;

            graphics.drawString(
                    font,
                    line,
                    x,
                    y,
                    0xFFFFFF,
                    false
            );
        }
    }

    private boolean isInsideDisplayContent(double mouseX, double mouseY) {
        double localX = mouseX - leftPos;
        double localY = mouseY - topPos;

        return localX >= DISPLAY_CONTENT_X
                && localX < DISPLAY_CONTENT_X + DISPLAY_CONTENT_WIDTH
                && localY >= DISPLAY_CONTENT_Y
                && localY < DISPLAY_CONTENT_Y + DISPLAY_CONTENT_HEIGHT;
    }

    private List<ModuleFabricationRecipe> availableRecipes() {
        if (minecraft == null || minecraft.level == null) {
            return List.of();
        }

        ItemStack base = menu.getSlot(0).getItem();
        if (base.isEmpty()) {
            return List.of();
        }

        return minecraft.level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.MODULE_FABRICATION.get())
                .stream()
                .filter(recipe -> recipe.moduleBase().ingredient().test(base))
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .toList();
    }

    private void clampScroll(int recipeCount) {
        scrollOffset = Math.max(
                0,
                Math.min(
                        scrollOffset,
                        Math.max(0, recipeCount - VISIBLE_ROWS)
                )
        );
    }

    private static ItemStack displayStack(CountedIngredient requirement) {
        ItemStack[] examples = requirement.ingredient().getItems();
        if (examples.length == 0) {
            return ItemStack.EMPTY;
        }
        return examples[0].copy();
    }

    private static String formatNumber(int value) {
        return NUMBER_FORMAT.format(Math.max(0, value));
    }

    private record HoveredRecipeIcon(
            ItemStack stack,
            CountedIngredient material
    ) {
    }
}
