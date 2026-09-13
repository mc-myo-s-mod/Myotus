package me.myogoo.myotus.integration.guideme;

import guideme.compiler.PageCompiler;
import guideme.compiler.TagCompiler;
import guideme.document.block.LytBlockContainer;
import guideme.document.block.LytParagraph;
import guideme.document.flow.LytFlowParent;
import guideme.document.flow.LytFlowText;
import guideme.libs.mdast.mdx.model.MdxJsxElementFields;
import guideme.libs.mdast.mdx.model.MdxJsxFlowElement;
import guideme.libs.mdast.mdx.model.MdxJsxTextElement;
import guideme.style.TextStyle;
import net.minecraft.network.chat.Component;

import java.util.Set;

public class TranslateTag implements TagCompiler {
    @Override
    public void compileBlockContext(PageCompiler compiler, LytBlockContainer parent, MdxJsxFlowElement el) {
        var key = el.getAttributeString("key", "");
        if (key.isBlank()) {
            parent.appendError(compiler, "Translate tag requires a non-empty key attribute", el);
            return;
        }

        var paragraph = new LytParagraph();
        appendTranslatedText(paragraph, key, el);
        parent.append(paragraph);
    }

    @Override
    public void compileFlowContext(PageCompiler compiler, LytFlowParent parent, MdxJsxTextElement el) {
        var key = el.getAttributeString("key", "");
        if (key.isBlank()) {
            parent.appendError(compiler, "Translate tag requires a non-empty key attribute", el);
            return;
        }

        appendTranslatedText(parent, key, el);
    }

    private static void appendTranslatedText(LytFlowParent parent, String key, MdxJsxElementFields el) {
        var text = new LytFlowText();
        text.setText(Component.translatable(key).getString());

        var style = styleFromAttributes(el);
        if (style != TextStyle.EMPTY) {
            text.setStyle(style);
        }

        parent.append(text);
    }

    private static TextStyle styleFromAttributes(MdxJsxElementFields el) {
        var builder = TextStyle.builder();
        var hasStyle = false;

        if (getBoolean(el, "bold")) {
            builder.bold(true);
            hasStyle = true;
        }
        if (getBoolean(el, "italic")) {
            builder.italic(true);
            hasStyle = true;
        }
        if (getBoolean(el, "underlined") || getBoolean(el, "underline")) {
            builder.underlined(true);
            hasStyle = true;
        }
        if (getBoolean(el, "strikethrough")) {
            builder.strikethrough(true);
            hasStyle = true;
        }
        if (getBoolean(el, "obfuscated")) {
            builder.obfuscated(true);
            hasStyle = true;
        }

        return hasStyle ? builder.build() : TextStyle.EMPTY;
    }

    private static boolean getBoolean(MdxJsxElementFields el, String attribute) {
        return Boolean.parseBoolean(el.getAttributeString(attribute, "false"));
    }

    @Override
    public Set<String> getTagNames() {
        return Set.of("Translate");
    }
}
