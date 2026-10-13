package me.myogoo.myotus.client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import appeng.client.Point;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.config.MyotusConfig;
import me.myogoo.myotus.menu.MyoSlotSemantics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TerminalUpgradePanelTest {
    @Test
    void scrollbarAppearsOnlyWhenUpgradeSlotsExceedFive() {
        var spec = MyotusConfig.CLIENT.get();
        var configs = new ConfigTracker();
        boolean configWasLoaded = spec.isLoaded();
        if (!configWasLoaded) {
            var container = mock(ModContainer.class);
            when(container.getModId()).thenReturn("myotus_panel_test");
            configs.registerConfig(ModConfig.Type.SERVER, spec, container);
            configs.loadDefaultServerConfigs();
        }
        boolean originalShowViewCells = MyotusConfig.CLIENT.showViewCellSlots.get();
        try {
            MEStorageMenu menu = mock(MEStorageMenu.class);
            List<Slot> viewCells = IntStream.range(0, 5).mapToObj(i -> mock(Slot.class)).toList();
            when(menu.getSlots(SlotSemantics.VIEW_CELL)).thenReturn(viewCells);
            Point panelMouse = new Point(202, 20);

            for (boolean showViewCells : List.of(true, false)) {
                MyotusConfig.CLIENT.showViewCellSlots.set(showViewCells);

                List<Slot> fiveUpgrades = IntStream.range(0, 5).mapToObj(i -> mock(Slot.class)).toList();
                when(menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT)).thenReturn(fiveUpgrades);
                TerminalUpgradePanel fiveSlotPanel = new TerminalUpgradePanel(menu, 200, false);
                fiveSlotPanel.setVisible(true);
                fiveSlotPanel.updateBeforeRender();
                assertEquals(showViewCells ? 42 : 24, fiveSlotPanel.getPanelWidth());
                assertFalse(fiveSlotPanel.onMouseWheel(panelMouse, -1));
                for (int i = 0; i < fiveUpgrades.size(); i++) {
                    assertEquals(showViewCells ? 219 : 201, fiveUpgrades.get(i).x);
                    assertEquals(6 + i * 18, fiveUpgrades.get(i).y);
                }

                List<Slot> sixUpgrades = IntStream.range(0, 6).mapToObj(i -> mock(Slot.class)).toList();
                when(menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT)).thenReturn(sixUpgrades);
                TerminalUpgradePanel sixSlotPanel = new TerminalUpgradePanel(menu, 200, false);
                sixSlotPanel.setVisible(true);
                sixSlotPanel.updateBeforeRender();
                assertEquals(showViewCells ? 47 : 29, sixSlotPanel.getPanelWidth());
                assertTrue(sixSlotPanel.onMouseWheel(panelMouse, -1));
                sixSlotPanel.updateBeforeRender();
                assertEquals(-10000, sixUpgrades.getFirst().x);
                assertEquals(6, sixUpgrades.get(1).y);
                assertEquals(78, sixUpgrades.get(5).y);
            }
        } finally {
            MyotusConfig.CLIENT.showViewCellSlots.set(originalShowViewCells);
            if (!configWasLoaded) {
                configs.unloadConfigs(ModConfig.Type.SERVER);
            }
        }
    }

    @Test
    void scrollsFiveUpgradeRowsWithoutMovingViewCellsOrExposingHiddenSlots() {
        var spec = MyotusConfig.CLIENT.get();
        var configs = new ConfigTracker();
        boolean configWasLoaded = spec.isLoaded();
        if (!configWasLoaded) {
            var container = mock(ModContainer.class);
            when(container.getModId()).thenReturn("myotus_panel_test");
            configs.registerConfig(ModConfig.Type.SERVER, spec, container);
            configs.loadDefaultServerConfigs();
        }
        boolean originalShowViewCells = MyotusConfig.CLIENT.showViewCellSlots.get();
        MyotusConfig.CLIENT.showViewCellSlots.set(true);
        try {
            MEStorageMenu menu = mock(MEStorageMenu.class);
            List<Slot> upgrades = IntStream.range(0, 27).mapToObj(i -> mock(Slot.class)).toList();
            List<Slot> viewCells = IntStream.range(0, 5).mapToObj(i -> mock(Slot.class)).toList();
            when(menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT)).thenReturn(upgrades);
            when(menu.getSlots(SlotSemantics.VIEW_CELL)).thenReturn(viewCells);

            TerminalUpgradePanel panel = new TerminalUpgradePanel(menu, 200, false);
            Point panelMouse = new Point(202, 20);
            panel.setVisible(true);
            panel.updateBeforeRender();
            assertEquals(47, panel.getPanelWidth());
            assertEquals(201, viewCells.getFirst().x);
            assertEquals(6, upgrades.getFirst().y);
            assertEquals(78, upgrades.get(4).y);
            assertEquals(-10000, upgrades.get(5).x);

            assertFalse(panel.onMouseWheel(Point.ZERO, -1));
            assertTrue(panel.onMouseWheel(panelMouse, -1));
            panel.updateBeforeRender();
            assertEquals(-10000, upgrades.getFirst().x);
            assertEquals(6, upgrades.get(1).y);
            assertEquals(78, upgrades.get(5).y);
            assertEquals(6, viewCells.getFirst().y);

            for (int i = 0; i < 30; i++) {
                panel.onMouseWheel(panelMouse, -1);
            }
            panel.updateBeforeRender();
            assertEquals(-10000, upgrades.get(21).x);
            assertEquals(6, upgrades.get(22).y);
            assertEquals(78, upgrades.get(26).y);

            for (int i = 0; i < 30; i++) {
                panel.onMouseWheel(panelMouse, 1);
            }
            panel.updateBeforeRender();
            assertEquals(6, upgrades.getFirst().y);
            assertEquals(-10000, upgrades.get(5).x);

            panel.setVisible(false);
            assertFalse(panel.onMouseWheel(panelMouse, 1));
            assertEquals(-10000, upgrades.getFirst().x);
        } finally {
            MyotusConfig.CLIENT.showViewCellSlots.set(originalShowViewCells);
            if (!configWasLoaded) {
                configs.unloadConfigs(ModConfig.Type.SERVER);
            }
        }
    }

    @Test
    void exclusionBoundsMatchDrawnPanelForWiredAndWirelessTerminals() {
        var spec = MyotusConfig.CLIENT.get();
        var configs = new ConfigTracker();
        boolean configWasLoaded = spec.isLoaded();
        if (!configWasLoaded) {
            var container = mock(ModContainer.class);
            when(container.getModId()).thenReturn("myotus_panel_test");
            configs.registerConfig(ModConfig.Type.SERVER, spec, container);
            configs.loadDefaultServerConfigs();
        }
        boolean originalShowViewCells = MyotusConfig.CLIENT.showViewCellSlots.get();
        try {
            MEStorageMenu menu = mock(MEStorageMenu.class);
            when(menu.getSlots(SlotSemantics.VIEW_CELL))
                    .thenReturn(IntStream.range(0, 5).mapToObj(i -> mock(Slot.class)).toList());
            for (boolean wireless : List.of(false, true)) {
                for (boolean showViewCells : List.of(false, true)) {
                    MyotusConfig.CLIENT.showViewCellSlots.set(showViewCells);
                    for (int slotCount : new int[] {5, 6}) {
                        List<Slot> upgrades = IntStream.range(0, slotCount)
                                .mapToObj(i -> mock(Slot.class)).toList();
                        when(menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT)).thenReturn(upgrades);
                        TerminalUpgradePanel panel = new TerminalUpgradePanel(menu, 200, wireless);
                        panel.setPosition(new Point(230, 15));
                        panel.setVisible(true);
                        panel.updateBeforeRender();

                        int expectedWidth = (showViewCells ? 42 : 24) + (wireless ? 4 : 0)
                                + (slotCount > 5 ? 5 : 0);
                        assertEquals(expectedWidth, panel.getBounds().getWidth());
                        assertEquals(230 + (wireless ? 5 : 1) + (showViewCells ? 18 : 0),
                                upgrades.getFirst().x);
                        assertEquals(21, upgrades.getFirst().y);
                        assertFalse(panel.onMouseDown(new Point(230 + expectedWidth + 1, 16), 0));

                        List<Rect2i> zones = new ArrayList<>();
                        panel.addExclusionZones(zones, new Rect2i(100, 50, 200, 220));
                        assertEquals(1, zones.size());
                        assertEquals(330, zones.getFirst().getX());
                        assertEquals(65, zones.getFirst().getY());
                        assertEquals(expectedWidth, zones.getFirst().getWidth());
                        assertEquals(100, zones.getFirst().getHeight());
                        panel.setVisible(false);
                        zones.clear();
                        panel.addExclusionZones(zones, new Rect2i(100, 50, 200, 220));
                        assertTrue(zones.isEmpty());
                    }
                }
            }
        } finally {
            MyotusConfig.CLIENT.showViewCellSlots.set(originalShowViewCells);
            if (!configWasLoaded) {
                configs.unloadConfigs(ModConfig.Type.SERVER);
            }
        }
    }
}
