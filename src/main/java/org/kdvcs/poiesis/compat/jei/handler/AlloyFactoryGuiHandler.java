package org.kdvcs.poiesis.compat.jei.handler;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.screen.AlloyFactoryScreen;

import java.util.Collection;
import java.util.stream.IntStream;

/** Opens Alloy Factory recipes from each visible row's progress bar. */
public final class AlloyFactoryGuiHandler
        implements IGuiContainerHandler<AlloyFactoryScreen> {

    private static final int PROGRESS_X = 173;
    private static final int FIRST_PROGRESS_Y = 34;
    private static final int PROGRESS_WIDTH = 6;
    private static final int PROGRESS_HEIGHT = 16;
    private static final int ROW_SPACING = 18;
    private static final int VISIBLE_ROWS = 5;

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
            AlloyFactoryScreen screen,
            double guiMouseX,
            double guiMouseY
    ) {
        return IntStream.range(0, VISIBLE_ROWS)
                .mapToObj(row -> IGuiClickableArea.createBasic(
                        PROGRESS_X,
                        FIRST_PROGRESS_Y + row * ROW_SPACING,
                        PROGRESS_WIDTH,
                        PROGRESS_HEIGHT,
                        PoiesisJeiRecipeTypes.ALLOYING
                ))
                .toList();
    }
}
