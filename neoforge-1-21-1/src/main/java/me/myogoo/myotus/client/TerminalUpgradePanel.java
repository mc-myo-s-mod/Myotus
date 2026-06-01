package me.myogoo.myotus.client;

import java.util.List;
import java.util.function.Consumer;

import appeng.client.gui.Icon;
import appeng.client.gui.style.TextureTransform;
import appeng.menu.slot.AppEngSlot;
import me.myogoo.myotus.config.MyotusConfig;
import me.myogoo.myotus.menu.MyoSlotSemantics;
import me.myogoo.myotus.menu.TerminalUpgradeHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.style.Blitter;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;

/**
 * MEStorageScreen 위에 떠 있는(floating) 드래그 가능한 서브스크린 패널.
 * Mekanism의 GuiWindow 패턴을 참고하여 구현.
 * ViewCell 5개를 세로로 표시합니다.
 */
public class TerminalUpgradePanel implements ICompositeWidget {

    public static final String WIDGET_ID = "myotus_terminal_upgrade_panel";

    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 5;
    private static final int SLOT_COUNT = 5;

    private static final int PANEL_HEIGHT = PADDING + (SLOT_SIZE * SLOT_COUNT) + PADDING;


    // 패널 위치 (screen-relative 좌표, guiLeft/guiTop을 기준)
    private int x;
    private int y;

    // 스크린 원점 (window 좌표)
    private Point screenOrigin = Point.ZERO;

    // 가시성
    private boolean visible = false;
    private boolean slotsHidden = false;

    private final List<Slot> viewCellSlots;
    private final List<Slot> upgradeSlots;

    private final MEStorageMenu menu;
    private final boolean isAe2WtlibScreen;

    public TerminalUpgradePanel(MEStorageMenu menu, int terminalScreenWidth, boolean isAe2WtlibScreen) {
        this.menu = menu;
        this.isAe2WtlibScreen = isAe2WtlibScreen;
        this.viewCellSlots = menu.getSlots(SlotSemantics.VIEW_CELL);
        this.upgradeSlots = menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT);
        setPosition(new Point(terminalScreenWidth, 0));
        configureUpgradeSlots(menu);
        hideSlots();
    }

    private void configureUpgradeSlots(MEStorageMenu menu) {
        for (Slot slot : upgradeSlots) {
            if (slot instanceof AppEngSlot appEngSlot) {
                appEngSlot.setIcon(Icon.BACKGROUND_UPGRADE);
                appEngSlot.setEmptyTooltip(() -> TerminalUpgradeHelper.getAvailableUpgradeTooltip(menu));
            }
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (!visible) {
            hideSlots(); // 닫힐 때 슬롯 숨김
        } else {
            slotsHidden = false;
        }
    }

    private void hideSlots() {
        if (slotsHidden) {
            return;
        }
        hideViewCellSlots();
        for (Slot slot : upgradeSlots) {
            slot.x = -10000;
            slot.y = -10000;
        }
        slotsHidden = true;
    }

    private void hideViewCellSlots() {
        for (Slot slot : viewCellSlots) {
            slot.x = -10000;
            slot.y = -10000;
        }
    }

    @Override
    public void setPosition(Point position) {
        this.x = position.getX();
        this.y = position.getY();
    }

    @Override
    public void setSize(int width, int height) {
        // 크기는 고정
    }

    @Override
    public Rect2i getBounds() {
        return new Rect2i(x, y, getPanelWidth(), PANEL_HEIGHT);
    }

    @Override
    public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
        this.screenOrigin = Point.fromTopLeft(bounds);
    }

    @Override
    public void updateBeforeRender() {
        if (!visible) {
            hideSlots();
            return;
        }
        slotsHidden = false;

        int slotStartX = this.x + (isAe2WtlibScreen ? 5 : 1);
        int slotStartY = this.y + PADDING;

        boolean showViewCell = MyotusConfig.CLIENT.showViewCellSlots.get();

        if (showViewCell) {
            for (int i = 0; i < Math.min(SLOT_COUNT, viewCellSlots.size()); i++) {
                Slot slot = viewCellSlots.get(i);
                slot.x = slotStartX;
                slot.y = slotStartY + i * MyoSlotBlitter.SLOT + 1;
            }
        } else {
            hideViewCellSlots();
        }

        // 오른쪽 열 (UpgradeSlot) - 틈 없이 바로 붙게 설정
        int rightColX = slotStartX + (showViewCell ? MyoSlotBlitter.SLOT : 0);
        for (int i = 0; i < Math.min(SLOT_COUNT, upgradeSlots.size()); i++) {
            Slot slot = upgradeSlots.get(i);
            slot.x = rightColX;
            slot.y = slotStartY + i * MyoSlotBlitter.SLOT + 1;
        }
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        if (!visible) {
            return;
        }

        guiGraphics.pose().pushPose();

        int xl = screenOrigin.getX() + this.x;
        int slotOriginY = screenOrigin.getY() + this.y;
        boolean showViewCell = MyotusConfig.CLIENT.showViewCellSlots.get();
        for (int i = 0; i < SLOT_COUNT; i++) {
            boolean borderTop = (i == 0);
            boolean borderBottom = (i == SLOT_COUNT - 1);

            int yOffset = slotOriginY + i * MyoSlotBlitter.SLOT;
            int xm = xl + (isAe2WtlibScreen ? MyoSlotBlitter.PADDING : MyoSlotBlitter.LEFT_PADDING);
            int xmg = xm + MyoSlotBlitter.SLOT;
            int xm2 = xmg;
            int xr = (showViewCell ? xm2 : xm) + MyoSlotBlitter.SLOT;
            if (borderTop) {
                MyoSlotBlitter.topLeft(guiGraphics, xl, yOffset, isAe2WtlibScreen);
                MyoSlotBlitter.topMiddle(guiGraphics, xm, yOffset);
                if (showViewCell) {
                    MyoSlotBlitter.gapTop(guiGraphics, xmg, yOffset);
                    MyoSlotBlitter.topMiddle(guiGraphics, xm2, yOffset);
                }
                MyoSlotBlitter.topRight(guiGraphics, xr, yOffset);
            } else if (borderBottom) {
                int y = yOffset + MyoSlotBlitter.SLOT + MyoSlotBlitter.PADDING;
                MyoSlotBlitter.bottomLeft(guiGraphics, xl, y, isAe2WtlibScreen);
                MyoSlotBlitter.bottomMiddle(guiGraphics, xm, y);
                if (showViewCell) {
                    MyoSlotBlitter.gapBottom(guiGraphics, xmg, y);
                    MyoSlotBlitter.bottomMiddle(guiGraphics, xm2, y);
                }
                MyoSlotBlitter.bottomRight(guiGraphics, xr, y);
            }
                int y = yOffset + MyoSlotBlitter.PADDING;
                MyoSlotBlitter.leftMiddle(guiGraphics, xl, y, isAe2WtlibScreen);
                MyoSlotBlitter.slot(guiGraphics, xm, y);
                if (showViewCell) {
                    MyoSlotBlitter.gapMiddle(guiGraphics, xmg, y);
                    MyoSlotBlitter.slot(guiGraphics, xm2, y);
                }
                MyoSlotBlitter.rightMiddle(guiGraphics, xr, y);

        }

        guiGraphics.pose().popPose();
    }

    @Override
    public boolean onMouseDown(Point mousePos, int button) {
        if (!visible || (button != 0 && button != 1)) {
            return false;
        }

        if (mousePos.isIn(getBounds())) {
            if (isOverSlot(mousePos)) {
                return false;
            }
            return true;
        }

        return false;
    }

    private boolean isOverSlot(Point mousePos) {
        if (MyotusConfig.CLIENT.showViewCellSlots.get()) {
            for (Slot slot : viewCellSlots) {
                if (isMouseOverSlot(mousePos, slot))
                    return true;
            }
        }
        for (Slot slot : upgradeSlots) {
            if (isMouseOverSlot(mousePos, slot))
                return true;
        }
        return false;
    }

    private boolean isMouseOverSlot(Point mousePos, Slot slot) {
        return mousePos.getX() >= slot.x && mousePos.getX() < slot.x + 16 &&
                mousePos.getY() >= slot.y && mousePos.getY() < slot.y + 16;
    }

    @Override
    public void addExclusionZones(List<Rect2i> exclusionZones, Rect2i screenBounds) {
        if (visible) {
            exclusionZones.add(new Rect2i(
                    screenBounds.getX() + x,
                    screenBounds.getY() + y,
                    getPanelWidth(),
                    PANEL_HEIGHT));
        }
    }

    public int getPanelWidth() {
        return (MyotusConfig.CLIENT.showViewCellSlots.get() ? (SLOT_SIZE * 2) : SLOT_SIZE) + (PADDING * 2);
    }

    static class MyoSlotBlitter {
        private static final Blitter BACKGROUND = Blitter.texture("guis/extra_panels.png", 128, 128);
        private static final int PADDING = 5;
        private static final int LEFT_PADDING = 1;
        private static final int SLOT = 18;

        static void topRight(GuiGraphics guiGraphics, int x, int y) {
            BACKGROUND.src(17, 0, PADDING, PADDING).dest(x, y).blit(guiGraphics);
        }

        static void topMiddle(GuiGraphics guiGraphics, int x, int y) {
            BACKGROUND.src(LEFT_PADDING, 0, SLOT, PADDING).dest(x, y).blit(guiGraphics);
        }

        static void topLeft(GuiGraphics guiGraphics, int x, int y, boolean isAe2wtlib) {
            if (isAe2wtlib) {
                BACKGROUND.src(17, 0, PADDING, PADDING).copy().transform(TextureTransform.MIRROR_H).dest(x, y).blit(guiGraphics);
            } else {
                BACKGROUND.src(0, 0, 1, PADDING).dest(x, y).blit(guiGraphics);
            }
        }

        static void bottomRight(GuiGraphics guiGraphics, int x, int y) {
            BACKGROUND.src(17, 22, PADDING, PADDING).dest(x, y).blit(guiGraphics);
        }

        static void bottomMiddle(GuiGraphics guiGraphics, int x, int y) {
            BACKGROUND.src(LEFT_PADDING, 22, SLOT, PADDING).dest(x, y).blit(guiGraphics);
        }

        static void bottomLeft(GuiGraphics guiGraphics, int x, int y, boolean isAe2wtlib) {
            if (isAe2wtlib) {
                BACKGROUND.src(17, 22, PADDING, PADDING).copy().transform(TextureTransform.MIRROR_H).dest(x, y).blit(guiGraphics);
            } else {
                BACKGROUND.src(0, 22, 1, PADDING).dest(x, y).blit(guiGraphics);
            }
        }

        static void rightMiddle(GuiGraphics guiGraphics, int x, int y) {
            BACKGROUND.src(17, PADDING, PADDING, SLOT).dest(x, y).blit(guiGraphics);
        }

        static void leftMiddle(GuiGraphics guiGraphics, int x, int y, boolean isAe2wtlib) {
            if (isAe2wtlib) {
                BACKGROUND.src(17, 6, PADDING, SLOT).copy().transform(TextureTransform.MIRROR_H).dest(x, y).blit(guiGraphics);
            } else {
                BACKGROUND.src(0, 5, 1, SLOT).dest(x, y).blit(guiGraphics);
            }
        }

        static void gapTop(GuiGraphics guiGraphics, int x, int y) {
            BACKGROUND.src(17, 0, 1, PADDING).dest(x, y).blit(guiGraphics);
        }

        static void gapBottom(GuiGraphics guiGraphics, int x, int y) {
            BACKGROUND.src(17, 22, 1, PADDING).dest(x, y).blit(guiGraphics);
        }

        static void gapMiddle(GuiGraphics guiGraphics, int x, int y) {
            BACKGROUND.src(17, PADDING, 1, SLOT).dest(x, y).blit(guiGraphics);
        }

        static void slot(GuiGraphics guiGraphics, int x, int y) {
            BACKGROUND.src(LEFT_PADDING, 5, SLOT, SLOT).dest(x, y).blit(guiGraphics);
        }
    }
}
