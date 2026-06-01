package me.myogoo.myotus.client.gui.widgets;

import appeng.client.gui.widgets.ITooltip;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * AE2 스타일의 키 바인딩 위젯입니다.
 * <p>
 * 멀티키 콤보(Modifier + 주 키)를 지원하며, Ctrl/Shift/Alt 등의 modifier 키를 조합할 수 있습니다.
 * 레이아웃: 위에 라벨, 아래에 키 박스들이 좌측부터 나열됩니다.
 * </p>
 * <p>
 * label이 등록된 {@link KeyMapping}의 이름과 일치하면, 자동으로 해당 KeyMapping에 키를 저장/로드합니다.
 * </p>
 *
 * <pre>{@code
 * var keyWidget = new KeyBindingButton(
 *         Component.literal("Open Menu"),
 *         keys -> {
 *             save();
 *         });
 * widgets.add("mykey", keyWidget);
 * }</pre>
 */
public class KeyBindingButton extends AbstractButton implements ITooltip {

    private static final int KEY_BOX_HEIGHT = 12;
    private static final int KEY_BOX_PADDING = 6;
    private static final int KEY_GAP = 2;
    private static final int LABEL_KEY_GAP = 3;
    private static final int PLUS_WIDTH = 8;

    // AE2 스타일 색상
    private static final int LABEL_COLOR = 0x404040;
    private static final int LABEL_COLOR_INACTIVE = 0x808080;

    private static final int KEY_BOX_BG = 0xFF2D2D2D;
    private static final int KEY_BOX_BG_HOVER = 0xFF3A3A3A;
    private static final int KEY_BOX_BG_LISTENING = 0xFF4A3A00;
    private static final int KEY_BOX_BORDER = 0xFF555555;
    private static final int KEY_BOX_BORDER_LISTENING = 0xFFFFAA00;
    private static final int KEY_TEXT_COLOR = 0xFFFFFF;
    private static final int KEY_TEXT_LISTENING = 0xFFAA00;
    private static final int KEY_TEXT_NONE = 0x808080;
    private static final int PLUS_COLOR = 0xAAAAAA;

    private final List<InputConstants.Key> keys = new ArrayList<>();
    private final List<InputConstants.Key> pendingModifiers = new ArrayList<>();
    private boolean listening = false;

    @Nullable
    private Consumer<List<InputConstants.Key>> changeListener;

    @Nullable
    private KeyMapping boundKeyMapping;

    /**
     * 키 바인딩 위젯을 생성합니다.
     * label이 등록된 KeyMapping의 이름과 일치하면 자동으로 연동됩니다.
     *
     * @param label          위에 표시될 라벨 텍스트
     * @param changeListener 키가 변경되었을 때 호출되는 콜백
     */
    public KeyBindingButton(Component label, @Nullable Consumer<List<InputConstants.Key>> changeListener) {
        super(0, 0, 120, 26, label);
        this.changeListener = changeListener;
        findAndBindKeyMapping(label.getString());
    }

    /**
     * 키 바인딩 위젯을 생성합니다 (초기 키 지정).
     *
     * @param label          위에 표시될 라벨 텍스트
     * @param initialKey     초기 바인딩 키
     * @param changeListener 키가 변경되었을 때 호출되는 콜백
     */
    public KeyBindingButton(Component label, InputConstants.Key initialKey,
            @Nullable Consumer<List<InputConstants.Key>> changeListener) {
        super(0, 0, 120, 26, label);
        this.changeListener = changeListener;
        findAndBindKeyMapping(label.getString());
        if (keys.isEmpty() && !initialKey.equals(InputConstants.UNKNOWN)) {
            keys.add(initialKey);
        }
    }

    /**
     * label과 일치하는 KeyMapping을 찾아 바인딩합니다.
     */
    private void findAndBindKeyMapping(String labelText) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.options == null)
            return;

        for (KeyMapping mapping : minecraft.options.keyMappings) {
            String mappingName = Component.translatable(mapping.getName()).getString();
            if (mappingName.equals(labelText)) {
                this.boundKeyMapping = mapping;
                // KeyMapping에서 현재 키 로드 (모디파이어 포함)
                InputConstants.Key boundKey = mapping.getKey();
                if (!boundKey.equals(InputConstants.UNKNOWN)) {
                    keys.clear();
                    // 모디파이어 키가 있으면 먼저 추가
                    KeyModifier modifier = mapping.getKeyModifier();
                    if (modifier != KeyModifier.NONE) {
                        InputConstants.Key modKey = getKeyFromModifier(modifier);
                        if (modKey != null) {
                            keys.add(modKey);
                        }
                    }
                    keys.add(boundKey);
                }
                return;
            }
        }
    }

    public List<InputConstants.Key> getKeys() {
        return Collections.unmodifiableList(keys);
    }

    public void setKeys(List<InputConstants.Key> newKeys) {
        this.keys.clear();
        this.keys.addAll(newKeys);
    }

    public boolean isListening() {
        return listening;
    }

    public void setChangeListener(@Nullable Consumer<List<InputConstants.Key>> listener) {
        this.changeListener = listener;
    }

    @Override
    public void onPress() {
        listening = true;
        pendingModifiers.clear();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!listening) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        if (keyCode == InputConstants.KEY_ESCAPE) {
            // ESC → 입력 취소
            listening = false;
            pendingModifiers.clear();
            return true;
        }

        if (keyCode == InputConstants.KEY_DELETE || keyCode == InputConstants.KEY_BACKSPACE) {
            // DELETE/BACKSPACE → 바인딩 해제
            keys.clear();
            listening = false;
            pendingModifiers.clear();
            applyToKeyMapping();
            notifyChange();
            return true;
        }

        InputConstants.Key pressedKey = InputConstants.getKey(keyCode, scanCode);

        // Modifier 키인지 확인
        if (isModifierKey(keyCode)) {
            if (!pendingModifiers.contains(pressedKey)) {
                pendingModifiers.add(pressedKey);
            }
            return true;
        }

        // 일반 키 → 콤보 확정
        keys.clear();
        keys.addAll(pendingModifiers);
        keys.add(pressedKey);
        listening = false;
        pendingModifiers.clear();
        applyToKeyMapping();
        notifyChange();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (listening && button != 0) {
            // 마우스 버튼 바인딩
            InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(button);
            keys.clear();
            keys.addAll(pendingModifiers);
            keys.add(mouseKey);
            listening = false;
            pendingModifiers.clear();
            applyToKeyMapping();
            notifyChange();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isModifierKey(int keyCode) {
        return keyCode == InputConstants.KEY_LSHIFT || keyCode == InputConstants.KEY_RSHIFT
                || keyCode == InputConstants.KEY_LCONTROL || keyCode == InputConstants.KEY_RCONTROL
                || keyCode == InputConstants.KEY_LALT || keyCode == InputConstants.KEY_RALT;
    }

    /**
     * 바인딩된 KeyMapping에 주 키를 저장합니다.
     */
    private void applyToKeyMapping() {
        if (boundKeyMapping == null)
            return;

        if (keys.isEmpty()) {
            boundKeyMapping.setKeyModifierAndCode(KeyModifier.NONE, InputConstants.UNKNOWN);
        } else {
            // 마지막 키가 주 키
            InputConstants.Key mainKey = keys.get(keys.size() - 1);
            // 모디파이어 결정 (주 키 이전의 키들)
            KeyModifier modifier = KeyModifier.NONE;
            if (keys.size() > 1) {
                InputConstants.Key firstMod = keys.get(0);
                modifier = getKeyModifierFromKey(firstMod);
            }
            boundKeyMapping.setKeyModifierAndCode(modifier, mainKey);
        }
        KeyMapping.resetMapping();
    }

    /**
     * InputConstants.Key에서 해당하는 KeyModifier를 반환합니다.
     */
    private KeyModifier getKeyModifierFromKey(InputConstants.Key key) {
        return switch (key.getValue()) {
            case InputConstants.KEY_LSHIFT, InputConstants.KEY_RSHIFT -> KeyModifier.SHIFT;
            case InputConstants.KEY_LCONTROL, InputConstants.KEY_RCONTROL -> KeyModifier.CONTROL;
            case InputConstants.KEY_LALT, InputConstants.KEY_RALT -> KeyModifier.ALT;
            default -> KeyModifier.NONE;
        };
    }

    /**
     * KeyModifier에서 대표 InputConstants.Key를 반환합니다.
     */
    @Nullable
    private InputConstants.Key getKeyFromModifier(KeyModifier modifier) {
        return switch (modifier) {
            case SHIFT -> InputConstants.getKey(InputConstants.KEY_LSHIFT, 0);
            case CONTROL -> InputConstants.getKey(InputConstants.KEY_LCONTROL, 0);
            case ALT -> InputConstants.getKey(InputConstants.KEY_LALT, 0);
            default -> null;
        };
    }

    private void notifyChange() {
        if (changeListener != null) {
            changeListener.accept(Collections.unmodifiableList(keys));
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var minecraft = Minecraft.getInstance();
        var font = minecraft.font;

        // --- 라벨 (위쪽) ---
        int labelColor = isActive() ? LABEL_COLOR : LABEL_COLOR_INACTIVE;
        guiGraphics.drawString(font, getMessage(), getX(), getY(), labelColor, false);

        // --- 키 박스들 (아래쪽, 좌측부터) ---
        int keyRowY = getY() + font.lineHeight + LABEL_KEY_GAP;
        int currentX = getX();

        // 박스 스타일
        int bgColor;
        int borderColor;
        if (listening) {
            bgColor = KEY_BOX_BG_LISTENING;
            borderColor = KEY_BOX_BORDER_LISTENING;
        } else if (isMouseOver(mouseX, mouseY)) {
            bgColor = KEY_BOX_BG_HOVER;
            borderColor = KEY_BOX_BORDER;
        } else {
            bgColor = KEY_BOX_BG;
            borderColor = KEY_BOX_BORDER;
        }

        if (listening) {
            // listening 상태: pending modifiers + "..."
            List<String> displayKeys = new ArrayList<>();
            for (InputConstants.Key mod : pendingModifiers) {
                displayKeys.add(getShortKeyName(mod));
            }
            displayKeys.add("...");

            for (int i = 0; i < displayKeys.size(); i++) {
                if (i > 0) {
                    // "+" 구분자
                    guiGraphics.drawString(font, "+",
                            currentX + KEY_GAP, keyRowY + (KEY_BOX_HEIGHT - font.lineHeight) / 2 + 1,
                            PLUS_COLOR, false);
                    currentX += PLUS_WIDTH + KEY_GAP;
                }
                String name = displayKeys.get(i);
                int textColor = KEY_TEXT_LISTENING;
                currentX = drawKeyBox(guiGraphics, font, currentX, keyRowY, name, bgColor, borderColor, textColor);
            }
        } else if (keys.isEmpty()) {
            // 바인딩 없음
            drawKeyBox(guiGraphics, font, currentX, keyRowY, "NONE", bgColor, borderColor, KEY_TEXT_NONE);
        } else {
            // 키 콤보 표시
            for (int i = 0; i < keys.size(); i++) {
                if (i > 0) {
                    guiGraphics.drawString(font, "+",
                            currentX + KEY_GAP, keyRowY + (KEY_BOX_HEIGHT - font.lineHeight) / 2 + 1,
                            PLUS_COLOR, false);
                    currentX += PLUS_WIDTH + KEY_GAP;
                }
                String name = getShortKeyName(keys.get(i));
                currentX = drawKeyBox(guiGraphics, font, currentX, keyRowY, name, bgColor, borderColor, KEY_TEXT_COLOR);
            }
        }
    }

    /**
     * 키 박스를 그리고 다음 X 위치를 반환합니다.
     */
    private int drawKeyBox(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font,
            int x, int y, String keyName, int bgColor, int borderColor, int textColor) {
        int textWidth = font.width(keyName);
        int boxWidth = textWidth + KEY_BOX_PADDING * 2;

        // 테두리
        guiGraphics.fill(x - 1, y - 1, x + boxWidth + 1, y + KEY_BOX_HEIGHT + 1, borderColor);
        // 배경
        guiGraphics.fill(x, y, x + boxWidth, y + KEY_BOX_HEIGHT, bgColor);
        // 텍스트 (중앙)
        int textX = x + KEY_BOX_PADDING;
        int textY = y + (KEY_BOX_HEIGHT - font.lineHeight) / 2 + 1;
        guiGraphics.drawString(font, keyName, textX, textY, textColor, false);

        return x + boxWidth;
    }

    /**
     * 키의 짧은 이름을 반환합니다 (Modifier 키는 축약).
     */
    private String getShortKeyName(InputConstants.Key key) {
        String name = key.getDisplayName().getString();
        // 일반적인 modifier 이름 축약
        return switch (key.getValue()) {
            case InputConstants.KEY_LSHIFT, InputConstants.KEY_RSHIFT -> "Shift";
            case InputConstants.KEY_LCONTROL, InputConstants.KEY_RCONTROL -> "Ctrl";
            case InputConstants.KEY_LALT, InputConstants.KEY_RALT -> "Alt";
            default -> name.toUpperCase();
        };
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE,
                        Component.translatable("narration.button.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE,
                        Component.translatable("narration.button.usage.hovered"));
            }
        }
    }

    // --- ITooltip ---

    @Override
    public List<Component> getTooltipMessage() {
        if (listening) {
            return Collections.singletonList(Component.translatable("gui.myotus.keybinding.listening"));
        }
        return Collections.singletonList(getMessage());
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(getX(), getY(), width, height);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return this.visible;
    }
}
