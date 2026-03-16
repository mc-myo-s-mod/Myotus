package me.myogoo.myotus.client;

import java.util.List;
import java.util.function.Consumer;

import me.myogoo.myotus.menu.MyoSlotSemantics;
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
 * ViewCell 5개(왼쪽)와 UpgradeSlot 5개(오른쪽)를 세로로 나란히 표시합니다.
 */
public class SidePanelSubScreen implements ICompositeWidget {

    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 5;
    private static final int SLOT_COUNT = 5;

    // 2열 (ViewCell + UpgradeSlot) + 패딩 (끊김 없는 디자인을 위해 열 사이 간격 제거)
    private static final int PANEL_WIDTH = PADDING + (SLOT_SIZE * 2) + PADDING;
    private static final int PANEL_HEIGHT = PADDING + (SLOT_SIZE * SLOT_COUNT) + PADDING;

    private static final Blitter BACKGROUND = Blitter.texture("guis/extra_panels.png", 128, 128);

    // 패널 위치 (screen-relative 좌표, guiLeft/guiTop을 기준)
    private int x;
    private int y;

    // 스크린 원점 (window 좌표)
    private Point screenOrigin = Point.ZERO;

    // 가시성
    private boolean visible = false;

    // 실제 메뉴 슬롯 참조
    private final List<Slot> viewCellSlots;
    private final List<Slot> upgradeSlots;

    private final MEStorageMenu menu;

    public SidePanelSubScreen(MEStorageMenu menu) {
        this.menu = menu;
        this.viewCellSlots = menu.getSlots(SlotSemantics.VIEW_CELL);
        this.upgradeSlots = menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT);
        hideSlots();
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (!visible) {
            hideSlots(); // 닫힐 때 슬롯 숨김
        }
    }

    private void hideSlots() {
        menu.hideSlot(SlotSemantics.VIEW_CELL.id());
        menu.hideSlot(MyoSlotSemantics.MYO_UPGRADE_SLOT.id());

        // 즉시 화면 밖으로 이동시켜 잔상 제거
        for (Slot slot : viewCellSlots) {
            slot.x = -10000;
            slot.y = -10000;
        }
        for (Slot slot : upgradeSlots) {
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
        return new Rect2i(x, y, PANEL_WIDTH, PANEL_HEIGHT);
    }

    @Override
    public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
        this.screenOrigin = Point.fromTopLeft(bounds);
    }

    @Override
    public void updateBeforeRender() {
        if (!visible) {
            hideSlots();
        }

        // 패널 내 첫 번째 슬롯 좌표 (screen-relative: guiLeft/top 기준)
        int slotStartX = this.x;
        int slotStartY = this.y + PADDING;

        // AE2의 Slot x, y는 guiLeft, guiTop 등을 기준으로 동작합니다 (screen-relative)
        // 왼쪽 열 (ViewCell)
        for (int i = 0; i < Math.min(SLOT_COUNT, viewCellSlots.size()); i++) {
            Slot slot = viewCellSlots.get(i);
            slot.x = slotStartX + -4;
            slot.y = slotStartY + i * SLOT_SIZE + 1;
        }

        // 오른쪽 열 (UpgradeSlot) - 틈 없이 바로 붙게 설정
        int rightColX = slotStartX + SLOT_SIZE;
        for (int i = 0; i < Math.min(SLOT_COUNT, upgradeSlots.size()); i++) {
            Slot slot = upgradeSlots.get(i);
            slot.x = rightColX + -5;
            slot.y = slotStartY + i * SLOT_SIZE + 1;
        }
    }

    /**
     * 최상단 렌더링을 위해 외부에서 직접 호출.
     * z-index를 높여서 다른 UI 요소 위에 렌더링합니다.
     */
    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        if (!visible) {
            return;
        }

        guiGraphics.pose().pushPose();
        // 배경 레이어 렌더링 시 추가적인 Z-index 이동을 제거하여 슬롯 하이라이트를 가리지 않도록 함.
        // guiGraphics.pose().translate(0, 0, 200);

        // extra_panels.png를 이용하여 바탕화면과 본 스크린이 자연스럽게 이어지도록 그립니다.
        // 드래그 핸들이나 Icon.SLOT_BACKGROUND는 그리지 않아서 슬롯 내장 배경(아이콘)이 보이게 합니다.
        int slotOriginX = screenOrigin.getX() + this.x;
        int slotOriginY = screenOrigin.getY() + this.y + PADDING;

        for (int i = 0; i < SLOT_COUNT; i++) {
            boolean borderTop = (i == 0);
            boolean borderBottom = (i == SLOT_COUNT - 1);

            // 왼쪽 열 (ViewCell) - 안쪽 코너 가림 없이 이음새 처리
            drawSlotPart(guiGraphics, slotOriginX, slotOriginY + i * SLOT_SIZE, true, borderTop, false, borderBottom);
            // 오른쪽 열 보강
            drawSlotGap(guiGraphics, slotOriginX + SLOT_SIZE - PADDING, slotOriginY + i * SLOT_SIZE, borderTop,
                    borderBottom);
            // 오른쪽 열 (UpgradeSlot)
            drawSlotPart(guiGraphics, slotOriginX + SLOT_SIZE + 4, slotOriginY + i * SLOT_SIZE, false, borderTop, true,
                    borderBottom);
        }

        guiGraphics.pose().popPose();
    }

    private static void drawSlotGap(GuiGraphics guiGraphics, int x, int y, boolean borderTop, boolean borderBottom) {
        int srcX = 2;
        int srcY = PADDING;
        int srcWidth = 4;
        int srcHeight = SLOT_SIZE;

        if (borderTop) {
            y -= PADDING;
            srcY = 0;
            srcHeight += PADDING;
        }
        if (borderBottom) {
            srcHeight += PADDING + 2;
        }
        BACKGROUND.src(srcX, srcY, srcWidth, srcHeight).dest(x, y).blit(guiGraphics);
    }

    private static void drawSlotPart(GuiGraphics guiGraphics, int x, int y,
            boolean borderLeft, boolean borderTop, boolean borderRight, boolean borderBottom) {
        int srcX = PADDING;
        int srcY = PADDING;
        int srcWidth = SLOT_SIZE;
        int srcHeight = SLOT_SIZE;

        if (borderLeft) {
            x -= PADDING;
            srcX = 0;
            // srcWidth += PADDING;
        }
        if (borderRight) {
            x -= PADDING;
            srcWidth += PADDING;
        }
        if (borderTop) {
            y -= PADDING;
            srcY = 0;
            srcHeight += PADDING;
        }
        if (borderBottom) {
            srcHeight += PADDING + 2;
        }

        BACKGROUND.src(srcX, srcY, srcWidth, srcHeight).dest(x, y).blit(guiGraphics);
    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        // 현재 단계에서는 추가 foreground 렌더링 없음
    }

    // --- 드래그 핸들링 ---

    @Override
    public boolean onMouseDown(Point mousePos, int button) {
        if (!visible || (button != 0 && button != 1)) {
            return false;
        }

        // 패널 영역 내 클릭인 경우
        if (mousePos.isIn(getBounds())) {
            // 슬롯 영역 위에 있으면 이 이벤트를 소비하지 않고 스크린(Slot 처리기)으로 넘김
            if (isOverSlot(mousePos)) {
                return false;
            }
            // 그 외의 패널 배경 클릭은 소비하여 뒤쪽 GUI 클릭 방지
            return true;
        }

        return false;
    }

    private boolean isOverSlot(Point mousePos) {
        // 실제 슬롯들의 위치를 직접 확인하여 정확한 클릭 판정
        for (Slot slot : viewCellSlots) {
            if (isMouseOverSlot(mousePos, slot))
                return true;
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
                    PANEL_WIDTH,
                    PANEL_HEIGHT));
        }
    }
}
