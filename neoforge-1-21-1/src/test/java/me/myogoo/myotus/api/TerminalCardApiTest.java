package me.myogoo.myotus.api;

import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.menu.MyoSlotSemantics;
import me.myogoo.myotus.menu.TerminalUpgradeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import static org.mockito.Mockito.when;

class TerminalCardApiTest {
    @Test
    void upgradeAndModuleCardsAreSiblingCardTypes() {
        Item upgrade = mock(Item.class, withSettings().extraInterfaces(ITerminalUpgradeCard.class));
        Item module = moduleCard(true);

        assertTrue(upgrade instanceof ITerminalCard);
        assertTrue(module instanceof ITerminalCard);
        assertFalse(module instanceof ITerminalUpgradeCard);
    }

    @Test
    void legacyUpgradeCallbacksRemainDeclaredDefaultMethods() throws NoSuchMethodException {
        assertLegacyDefault("onTerminalOpen");
        assertLegacyDefault("onTerminalClose");
        assertLegacyDefault("onTerminalTick");
    }

    @Test
    void legacyUpgradeHelperAcceptsModuleCardsAndRejectsUnavailableCards() {
        MEStorageMenu menu = mock(MEStorageMenu.class);
        Slot slot = mock(Slot.class);
        Item module = moduleCard(true);
        when(menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT)).thenReturn(List.of(slot));
        ItemStack moduleStack = stack(module);
        when(slot.getItem()).thenReturn(moduleStack);

        assertTrue(TerminalUpgradeHelper.hasUpgrade(menu, module));

        Item unavailableModule = moduleCard(false);
        ItemStack unavailableStack = stack(unavailableModule);
        when(slot.getItem()).thenReturn(unavailableStack);
        assertFalse(TerminalUpgradeHelper.hasUpgrade(menu, unavailableModule));
    }

    @Test
    void insertionRejectsDuplicatesAndUnavailableCardsButAllowsTheCurrentSlot() {
        Item module = moduleCard(true);
        ItemStack installedModule = stack(module);
        when(installedModule.is(module)).thenReturn(true);
        List<ItemStack> installed = List.of(installedModule);
        ItemStack candidate = stack(module);

        assertFalse(TerminalUpgradeHelper.canInsertUpgrade(installed, 1, candidate));
        assertTrue(TerminalUpgradeHelper.canInsertUpgrade(installed, 0, candidate));
        assertFalse(TerminalUpgradeHelper.canInsertUpgrade(installed, 1, stack(moduleCard(false))));
    }

    @Test
    void newAndLegacyQueryFacadesReturnTheSameResults() {
        MEStorageMenu menu = mock(MEStorageMenu.class);
        Slot slot = mock(Slot.class);
        Item module = moduleCard(true);
        when(menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT)).thenReturn(List.of(slot));
        ItemStack moduleStack = stack(module);
        when(slot.getItem()).thenReturn(moduleStack);
        var cards = MyotusAPI.terminalCards();
        var upgrades = MyotusAPI.terminalUpgrades();

        assertEquals(upgrades.installedUpgrades(menu).stream().map(ItemStack::getItem).toList(),
                cards.installedCards(menu).stream().map(ItemStack::getItem).toList());
        assertEquals(upgrades.installedUpgradeItems(menu), cards.installedCardItems(menu));
        assertEquals(upgrades.availableUpgradeCards(menu).stream().map(ItemStack::getItem).toList(),
                cards.availableCards(menu).stream().map(ItemStack::getItem).toList());
        assertEquals(upgrades.availableUpgradeTooltip(menu), cards.availableCardTooltip(menu));
        assertEquals(upgrades.hasUpgrade(menu, module), cards.hasCard(menu, module));
        ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath("myotus_test", "module");
        assertEquals(upgrades.hasUpgrade(menu, itemId), cards.hasCard(menu, itemId));
        assertEquals(upgrades.countUpgrade(menu, module), cards.countCard(menu, module));
        assertEquals(upgrades.countUpgrade(menu, itemId), cards.countCard(menu, itemId));
        List<ItemStack> installed = List.of(stack(module));
        ItemStack candidate = stack(module);
        assertEquals(upgrades.canInsertUpgrade(installed, 0, candidate),
                cards.canInsertCard(installed, 0, candidate));
    }

    @Test
    void legacyQueriesRejectNonCardsBeforeAccessingTheMenu() {
        var upgrades = MyotusAPI.terminalUpgrades();
        Item ordinaryItem = mock(Item.class);
        assertFalse(upgrades.hasUpgrade(null, (Item) null));
        assertEquals(0, upgrades.countUpgrade(null, (Item) null));
        assertFalse(upgrades.hasUpgrade(null, ordinaryItem));
        assertEquals(0, upgrades.countUpgrade(null, ordinaryItem));
    }

    @Test
    void imyotusApiDefaultAliasesReturnTheStaticCardFacades() {
        IMyotusAPI api = mock(IMyotusAPI.class, CALLS_REAL_METHODS);

        assertSame(MyotusAPI.terminalCards(), api.terminalCards());
        assertSame(MyotusAPI.terminalUpgrades(), api.terminalUpgrades());
    }

    @Test
    void bytecodeCompiledAgainstLegacyInterfaceLoadsAsParentCard() {
        Object legacyClass = new LegacyClassLoader().defineLegacyCard();
        ITerminalUpgradeCard legacy = assertInstanceOf(ITerminalUpgradeCard.class, legacyClass);
        assertInstanceOf(ITerminalCard.class, legacy);

        legacy.onTerminalOpen(null, null);
        legacy.onTerminalClose(null, null);
        legacy.onTerminalTick(null, null);
    }

    @Test
    void legacyClientRegistrationMethodDescriptorRemainsPresent() throws NoSuchMethodException {
        Method method = MyotusAPI.Client.class.getDeclaredMethod("registerTerminalModule", Item.class, BiFunction.class);
        assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()));
    }

    private static void assertLegacyDefault(String methodName) throws NoSuchMethodException {
        Method method = ITerminalUpgradeCard.class.getDeclaredMethod(methodName, MEStorageMenu.class, ItemStack.class);
        assertTrue(method.isDefault());
    }

    private static final class LegacyClassLoader extends ClassLoader {
        private LegacyClassLoader() {
            super(ITerminalUpgradeCard.class.getClassLoader());
        }

        private Object defineLegacyCard() {
            String name = "me/myogoo/myotus/api/LegacyBinaryCardFixture";
            ClassWriter writer = new ClassWriter(0);
            writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, name, null, "java/lang/Object",
                    new String[] { "me/myogoo/myotus/api/ITerminalUpgradeCard" });
            MethodVisitor constructor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            constructor.visitCode();
            constructor.visitVarInsn(Opcodes.ALOAD, 0);
            constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            constructor.visitInsn(Opcodes.RETURN);
            constructor.visitMaxs(1, 1);
            constructor.visitEnd();
            writer.visitEnd();

            byte[] bytecode = writer.toByteArray();
            try {
                Class<?> fixture = defineClass("me.myogoo.myotus.api.LegacyBinaryCardFixture", bytecode, 0,
                        bytecode.length);
                return fixture.getConstructor().newInstance();
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError("Unable to instantiate legacy binary fixture", exception);
            }
        }
    }

    private static Item moduleCard(boolean available) {
        Item item = mock(Item.class, withSettings().extraInterfaces(ITerminalModuleCard.class));
        when(((ITerminalCard) item).isAvailable()).thenReturn(available);
        return item;
    }

    private static ItemStack stack(Item item) {
        ItemStack stack = mock(ItemStack.class);
        when(stack.getItem()).thenReturn(item);
        ItemStack copy = mock(ItemStack.class);
        when(copy.getItem()).thenReturn(item);
        when(stack.copy()).thenReturn(copy);
        when(stack.is(item)).thenReturn(true);
        return stack;
    }
}
