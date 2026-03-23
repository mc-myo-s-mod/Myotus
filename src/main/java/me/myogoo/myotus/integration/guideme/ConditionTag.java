package me.myogoo.myotus.integration.guideme;

import guideme.compiler.PageCompiler;
import guideme.compiler.TagCompiler;
import guideme.document.block.LytBlockContainer;
import guideme.document.block.LytHeading;
import guideme.document.flow.LytFlowText;
import guideme.libs.mdast.mdx.model.MdxJsxFlowElement;
import guideme.style.TextStyle;
import me.myogoo.myotus.util.mod.ModIntegrationManager;

import java.util.Set;

public class ConditionTag implements TagCompiler {
    @Override
    public void compileBlockContext(PageCompiler compiler, LytBlockContainer parent, MdxJsxFlowElement el) {
        var silently = el.getAttributeString("silent", "false");
        var condition = el.getAttributeString("load", "");
        if (condition.isEmpty()) {
            compiler.compileBlockContext(el, parent);
        } else {
            try {
                var mod = ModIntegrationManager.get(condition);
                if (ModIntegrationManager.isLoaded(mod)) {
                    compiler.compileBlockContext(el, parent);
                } else {
                    if (silently.equals("true")) {
                        return;
                    }
                    var heading = new LytHeading();
                    var frontText = new LytFlowText();
                    frontText.setText("This content is hidden because the mod ");
                    var boldText = new LytFlowText();
                    boldText.setStyle(TextStyle.builder().bold(true).build());
                    boldText.setText(mod.getModName());
                    var endText = new LytFlowText();
                    endText.setText(" is not loaded.");
                    heading.setDepth(2);
                    heading.append(frontText);
                    heading.append(boldText);
                    heading.append(endText);
                    parent.append(heading);
                }
            } catch (IllegalArgumentException e) {
                parent.appendError(compiler, condition + "is not loaded", el);
            }
        }
    }

    @Override
    public Set<String> getTagNames() {
        return Set.of("et:condition");
    }
}
