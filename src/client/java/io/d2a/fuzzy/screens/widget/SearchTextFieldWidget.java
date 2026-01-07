package io.d2a.fuzzy.screens.widget;

import io.d2a.fuzzy.FuzzyClient;
import io.d2a.fuzzy.screens.FuzzyCommandScreen;
import io.d2a.fuzzy.util.SearchResult;
import io.d2a.fuzzy.util.actions.ShiftAction;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.function.BiConsumer;

public class SearchTextFieldWidget extends TextFieldWidget {

    private final FuzzyCommandScreen fuzzyCommandScreen;

    private BiConsumer<SearchResult, ResultEntry> resultConsumer = null;
    private boolean isShiftDown = false;

    public SearchTextFieldWidget(final FuzzyCommandScreen fuzzyCommandScreen,
                                 final TextRenderer textRenderer,
                                 final int x,
                                 final int y,
                                 final int width,
                                 final int height,
                                 final Text text) {
        super(textRenderer, x, y, width, height, text);
        this.fuzzyCommandScreen = fuzzyCommandScreen;
    }

    @Override
    public boolean charTyped(CharInput input) {
        var modifiers = input.modifiers();
        var chr = input.codepoint();
        if (FuzzyClient.getConfig().enableShiftActions() && (modifiers & GLFW.GLFW_MOD_SHIFT) == GLFW.GLFW_MOD_SHIFT) {
            final ShiftAction action = ShiftAction.fromKeyCode((char) chr);
            if (action != null) {
                // get selected entry
                final ResultEntry entry = this.fuzzyCommandScreen.getResultListWidget().getSelectedOrNull();
                if (entry != null) {
                    if (!action.run(entry, this.fuzzyCommandScreen, this)) {
                        // close screen
                        this.fuzzyCommandScreen.close();
                    }
                }
                return true;
            }
        }
        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        var keyCode = input.key();
        var scanCode = input.scancode();
        var modifiers = input.modifiers();
        // update shift state
        this.isShiftDown = (modifiers & GLFW.GLFW_MOD_SHIFT) == GLFW.GLFW_MOD_SHIFT;

        // control keybindings
        switch (keyCode) {
            // select previous entry
            case GLFW.GLFW_KEY_UP -> {
                this.fuzzyCommandScreen.getResultListWidget().selectNextEntryInDirection(NavigationDirection.UP);
                return true;
            }
            // select next entry
            case GLFW.GLFW_KEY_DOWN -> {
                this.fuzzyCommandScreen.getResultListWidget().selectNextEntryInDirection(NavigationDirection.DOWN);
                return true;
            }
        }

        // apply keybindings
        final SearchResult result = SearchResult.fromKeyCode(keyCode);
        if (result != null) {
            final ResultEntry resultEntry = this.fuzzyCommandScreen.getResultListWidget().getSelectedOrNull();
            if (resultEntry != null) {
                this.resultConsumer.accept(result, resultEntry);
            }
            return true;
        }

        // default
        return super.keyPressed(input);
    }

    @Override
    public boolean keyReleased(KeyInput input) {
        var keyCode = input.key();
        var scanCode = input.scancode();
        var modifiers = input.modifiers();
        // update shift state
        this.isShiftDown = (modifiers & GLFW.GLFW_MOD_SHIFT) == GLFW.GLFW_MOD_SHIFT;
        return super.keyReleased(input);
    }

    public void setResultConsumer(BiConsumer<SearchResult, ResultEntry> resultConsumer) {
        this.resultConsumer = resultConsumer;
    }

    public boolean isShiftDown() {
        return isShiftDown;
    }

}
