package net.paradise_client.ui.screen;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.paradise_client.ParadiseClient;
import net.paradise_client.command.Command;
import net.paradise_client.command.CommandManager;
import net.paradise_client.themes.AbstractThemeRenderer;
import net.paradise_client.themes.Theme;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.Set;

public class ClickGuiScreen extends Screen {
    private final Map<CommandManager.CommandCategory, CategoryPanel> panels = new LinkedHashMap<>();
    private final AbstractThemeRenderer theme = Theme.TRANSPARENT.getRenderer();
    private float animationProgress = 0f;

    private String searchText = "";
    private boolean searchFocused = false;

    public ClickGuiScreen() {
        super(Text.literal("Paradise Click GUI"));
    }

    @Override
    protected void init() {
        int x = 10;
        int y = 35;
        int width = 120;
        int spacing = 10;

        for (CommandManager.CommandCategory category : CommandManager.CommandCategory.values()) {
            CategoryPanel panel = panels.get(category);
            if (panel == null) {
                panel = new CategoryPanel(category, x, y, width);
                panels.put(category, panel);
            } else {
                panel.width = width;
                panel.x = x;
                panel.y = y;
            }
            x += width + spacing;

            if (x + width > this.width) {
                x = 10;
                y += 200;
            }
        }
        animationProgress = 0f;
    }

    @Override
    public void tick() {
        if (animationProgress < 1f) {
            animationProgress = Math.min(1f, animationProgress + 0.15f);
        }
        theme.update();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        theme.renderBackground(context, width, height);

        float scale = 0.95f + (animationProgress * 0.05f);

        context.getMatrices().push();
        context.getMatrices().translate(width / 2f, height / 2f, 0);
        context.getMatrices().scale(scale, scale, 1);
        context.getMatrices().translate(-width / 2f, -height / 2f, 0);

        int searchW = 200;
        int searchX = (width - searchW) / 2;
        int searchY = 10;

        theme.renderHudPanel(context, searchX - 4, searchY - 4, searchW + 8, 28);
        theme.renderTextField(context, searchX, searchY, searchW, 20, searchFocused);
        String displaySearch = searchText.isEmpty() && !searchFocused ? "Search modules..." : searchText;
        int searchColor = searchText.isEmpty() && !searchFocused ? 0xFF888888 : 0xFFFFFFFF;
        context.drawText(textRenderer, displaySearch + (searchFocused && (System.currentTimeMillis() / 500 % 2 == 0) ? "_" : ""), searchX + 6, searchY + 6, searchColor, false);

        for (CategoryPanel panel : panels.values()) {
            panel.render(context, mouseX, mouseY, delta);
        }

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int searchW = 200;
        int searchX = (width - searchW) / 2;
        int searchY = 10;
        if (isHovered(mouseX, mouseY, searchX, searchY, searchW, 20)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        if (ParadiseClient.NOTIFICATION_MANAGER.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        List<CategoryPanel> reversePanels = new ArrayList<>(panels.values());
        Collections.reverse(reversePanels);
        for (CategoryPanel panel : reversePanels) {
            if (panel.mouseClicked(mouseX, mouseY, button)) {
                panels.remove(panel.category);
                panels.put(panel.category, panel);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : panels.values()) {
            panel.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (CategoryPanel panel : panels.values()) {
            if (panel.mouseScrolled(mouseX, mouseY, verticalAmount)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchText.isEmpty()) {
                    searchText = searchText.substring(0, searchText.length() - 1);
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                return true;
            }
        }

        for (CategoryPanel panel : panels.values()) {
            if (panel.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused) {
            searchText += chr;
            return true;
        }

        for (CategoryPanel panel : panels.values()) {
            if (panel.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }

    private class CategoryPanel {
        private final CommandManager.CommandCategory category;
        private int x;
        private int y;
        private int width;
        private boolean expanded = true;
        private final List<ModuleButton> buttons = new ArrayList<>();
        private double dragX;
        private double dragY;
        private boolean dragging = false;
        private int scrollY = 0;
        private final int maxContentHeight = 250;

        private CategoryPanel(CommandManager.CommandCategory category, int x, int y, int width) {
            this.category = category;
            this.x = x;
            this.y = y;
            this.width = width;

            List<Command> commands = ParadiseClient.COMMAND_MANAGER.getCommandsByCategory(category);
            for (Command cmd : commands) {
                buttons.add(new ModuleButton(cmd, x, 0, width, 16));
            }
        }

        private void render(DrawContext context, int mouseX, int mouseY, float delta) {
            if (dragging) {
                x = (int) (mouseX - dragX);
                y = (int) (mouseY - dragY);
            }

            int titleHeight = 20;
            String title = category.getIcon() + " " + category.getDisplayName();
            theme.renderTitleBar(context, x, y, width, titleHeight, title, textRenderer, true);

            if (!expanded) {
                return;
            }

            List<ModuleButton> filteredButtons = buttons.stream()
                    .filter(button -> searchText.isEmpty() || button.command.getName().toLowerCase(Locale.ROOT).contains(searchText.toLowerCase(Locale.ROOT)))
                    .toList();

            int totalHeight = 0;
            for (ModuleButton button : filteredButtons) {
                totalHeight += button.getHeight();
            }

            int contentHeight = Math.min(totalHeight, maxContentHeight);
            if (scrollY > totalHeight - contentHeight) {
                scrollY = Math.max(0, totalHeight - contentHeight);
            }

            context.enableScissor(x, y + titleHeight, x + width, y + titleHeight + contentHeight);

            int currentY = y + titleHeight - scrollY;
            for (ModuleButton button : filteredButtons) {
                button.x = x;
                button.y = currentY;
                button.width = width;
                button.render(context, mouseX, (int) (mouseY + scrollY), delta);
                currentY += button.getHeight();
            }

            context.disableScissor();

            if (totalHeight > maxContentHeight) {
                int sbX = x + width - 3;
                int sbY = y + titleHeight;
                int sbH = contentHeight;
                theme.renderPanel(context, sbX, sbY, 2, sbH);
                float scrollPercent = (float) scrollY / (totalHeight - contentHeight);
                int thumbH = (int) ((float) contentHeight / totalHeight * sbH);
                int thumbY = sbY + (int) (scrollPercent * (sbH - thumbH));
                theme.renderSelection(context, sbX, thumbY, 2, thumbH);
            }
        }

        private boolean mouseScrolled(double mouseX, double mouseY, double amount) {
            if (expanded && isHovered(mouseX, mouseY, x, y + 20, width, maxContentHeight)) {
                List<ModuleButton> filteredButtons = buttons.stream()
                        .filter(button -> searchText.isEmpty() || button.command.getName().toLowerCase(Locale.ROOT).contains(searchText.toLowerCase(Locale.ROOT)))
                        .toList();
                int totalHeight = 0;
                for (ModuleButton button : filteredButtons) {
                    totalHeight += button.getHeight();
                }

                if (totalHeight > maxContentHeight) {
                    scrollY = (int) Math.max(0, Math.min(totalHeight - maxContentHeight, scrollY - amount * 12));
                    return true;
                }
            }
            return false;
        }

        private boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isHovered(mouseX, mouseY, x, y, width, 20)) {
                if (button == 0) {
                    dragging = true;
                    dragX = mouseX - x;
                    dragY = mouseY - y;
                    return true;
                }
                if (button == 1) {
                    expanded = !expanded;
                    if (!expanded) {
                        scrollY = 0;
                    }
                    return true;
                }
            }

            if (expanded && isHovered(mouseX, mouseY, x, y + 20, width, maxContentHeight)) {
                double scrolledMouseY = mouseY + scrollY;
                for (ModuleButton buttonEntry : buttons) {
                    if (searchText.isEmpty() || buttonEntry.command.getName().toLowerCase(Locale.ROOT).contains(searchText.toLowerCase(Locale.ROOT))) {
                        if (buttonEntry.mouseClicked(mouseX, scrolledMouseY, button)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private void mouseReleased(double mouseX, double mouseY, int button) {
            if (button == 0) {
                dragging = false;
            }
        }

        private boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (expanded) {
                for (ModuleButton button : buttons) {
                    if (button.keyPressed(keyCode, scanCode, modifiers)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean charTyped(char chr, int modifiers) {
            if (expanded) {
                for (ModuleButton button : buttons) {
                    if (button.charTyped(chr, modifiers)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private class ModuleButton {
        private final Command command;
        private int x;
        private int y;
        private int width;
        private final int height;
        private boolean expanded = false;
        private float hoverProgress = 0f;
        private float expansionProgress = 0f;
        private final List<ArgElement> elements = new ArrayList<>();
        private long lastClickTime = 0;
        private String lastCompletionPrefix = "";
        private int lastCompletionIndex = -1;

        private ModuleButton(Command command, int x, int y, int width, int height) {
            this.command = command;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            rebuildElements();
        }

        private void rebuildElements() {
            elements.clear();
            if (command.getNode() != null) {
                explore(command.getNode(), 0);
            }
        }

        private void explore(CommandNode<CommandSource> parent, int depth) {
            for (CommandNode<CommandSource> child : parent.getChildren()) {
                elements.add(new ArgElement(child, parent, depth));
                explore(child, depth + 1);
            }
        }

        private void render(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean hovered = isHovered(mouseX, mouseY, x, y, width, height);
            hoverProgress = hovered ? Math.min(1f, hoverProgress + 0.15f) : Math.max(0f, hoverProgress - 0.15f);
            expansionProgress = expanded ? Math.min(1f, expansionProgress + 0.15f) : Math.max(0f, expansionProgress - 0.15f);

            long timeSinceClick = System.currentTimeMillis() - lastClickTime;
            float flash = timeSinceClick < 300 ? 1.0f - (timeSinceClick / 300f) : 0f;

            if (flash > 0) {
                int flashColor = (int) (flash * 0x88) << 24 | (0x00D4FF & 0xFFFFFF);
                drawRoundedRect(context, x, y, width, height, 3, flashColor);
            } else if (hoverProgress > 0) {
                theme.renderSelection(context, x, y, width, height);
            } else {
                theme.renderPanel(context, x, y, width, height);
            }

            int color = (command.getNode() == null || command.getNode().getChildren().isEmpty()) ? 0xFFBBBBBB : 0xFFFFFFFF;
            context.drawText(textRenderer, command.getName(), x + 6, y + (height - 8) / 2, color, false);

            if (command.getNode() != null && !command.getNode().getChildren().isEmpty()) {
                String indicator = expanded ? "v" : ">";
                int indicatorWidth = textRenderer.getWidth(indicator);
                context.drawText(textRenderer, indicator, x + width - indicatorWidth - 6, y + (height - 8) / 2, 0xFF999999, false);
            }

            if (expansionProgress <= 0f) {
                return;
            }

            int currentY = y + height;
            List<ArgElement> visibleElements = getVisibleElements();

            context.getMatrices().push();
            for (ArgElement element : visibleElements) {
                element.render(context, x, currentY, width, mouseX, mouseY);
                currentY += element.getHeight();
            }

            int runX = x + 4;
            int runY = currentY + 2;
            int runW = width - 8;
            int runH = 14;
            boolean runHovered = isHovered(mouseX, mouseY, runX, runY, runW, runH);
            theme.renderButton(context, runX, runY, runW, runH, runHovered, false, "RUN", textRenderer);
            context.getMatrices().pop();
        }

        private int getHeight() {
            int baseHeight = height;
            if (expansionProgress <= 0f) {
                return baseHeight;
            }

            int extraHeight = 18;
            for (ArgElement element : getVisibleElements()) {
                extraHeight += element.getHeight();
            }
            return (int) (baseHeight + (expansionProgress * extraHeight));
        }

        private boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isHovered(mouseX, mouseY, x, y, width, height)) {
                if (button == 0) {
                    if (command.getNode() == null || command.getNode().getChildren().isEmpty()) {
                        executeCommand();
                    } else {
                        expanded = !expanded;
                    }
                    return true;
                }
                if (button == 1) {
                    expanded = !expanded;
                    return true;
                }
            }

            if (expansionProgress > 0.5f) {
                int currentY = y + height;
                for (ArgElement element : getVisibleElements()) {
                    if (isHovered(mouseX, mouseY, x, currentY, width, element.getHeight())) {
                        if (element.node instanceof LiteralCommandNode) {
                            selectLiteral(element);
                            lastClickTime = System.currentTimeMillis();
                        } else if (isBooleanArgument(element.node)) {
                            element.value = element.value.equalsIgnoreCase("true") ? "false" : "true";
                            lastClickTime = System.currentTimeMillis();
                        } else {
                            element.focused = true;
                            elements.forEach(other -> {
                                if (other != element) {
                                    other.focused = false;
                                }
                            });
                        }
                        return true;
                    }
                    currentY += element.getHeight();
                }

                if (isHovered(mouseX, mouseY, x + 4, currentY + 2, width - 8, 14)) {
                    executeCommand();
                    return true;
                }
            }

            elements.forEach(element -> element.focused = false);
            return false;
        }

        private void executeCommand() {
            StringBuilder commandLine = new StringBuilder(command.getName());
            CommandNode<CommandSource> current = command.getNode();
            Set<CommandNode<CommandSource>> visited = new HashSet<>();

            while (current != null && visited.add(current)) {
                ArgElement selectedLiteral = findSelectedLiteralChild(current);
                if (selectedLiteral != null) {
                    commandLine.append(" ").append(selectedLiteral.node.getName());
                    current = selectedLiteral.node;
                    continue;
                }

                ArgElement argumentChild = findArgumentChild(current);
                if (argumentChild != null) {
                    if (argumentChild.value.isBlank()) {
                        break;
                    }
                    commandLine.append(" ").append(argumentChild.value);
                    current = argumentChild.node;
                    continue;
                }

                break;
            }

            ParadiseClient.COMMAND_MANAGER.dispatch(commandLine.toString());
            lastClickTime = System.currentTimeMillis();
        }

        private List<ArgElement> getVisibleElements() {
            List<ArgElement> visibleElements = new ArrayList<>();
            for (ArgElement element : elements) {
                if (isElementVisible(element)) {
                    visibleElements.add(element);
                }
            }
            return visibleElements;
        }

        private boolean isElementVisible(ArgElement element) {
            if (element.parent == command.getNode()) {
                return true;
            }

            ArgElement parentElement = findElement(element.parent);
            if (parentElement == null || !isElementVisible(parentElement)) {
                return false;
            }

            return !(element.parent instanceof LiteralCommandNode) || parentElement.selected;
        }

        private ArgElement findElement(CommandNode<CommandSource> node) {
            for (ArgElement element : elements) {
                if (element.node == node) {
                    return element;
                }
            }
            return null;
        }

        private ArgElement findSelectedLiteralChild(CommandNode<CommandSource> parent) {
            for (ArgElement element : elements) {
                if (element.parent == parent && element.node instanceof LiteralCommandNode && element.selected && isElementVisible(element)) {
                    return element;
                }
            }
            return null;
        }

        private ArgElement findArgumentChild(CommandNode<CommandSource> parent) {
            for (ArgElement element : elements) {
                if (element.parent == parent && element.node instanceof ArgumentCommandNode && isElementVisible(element)) {
                    return element;
                }
            }
            return null;
        }

        private void selectLiteral(ArgElement selectedElement) {
            for (ArgElement sibling : elements) {
                if (sibling.parent == selectedElement.parent && sibling.node instanceof LiteralCommandNode && sibling != selectedElement) {
                    clearBranch(sibling);
                }
            }
            selectedElement.selected = true;
        }

        private void clearBranch(ArgElement root) {
            root.focused = false;
            if (root.node instanceof LiteralCommandNode) {
                root.selected = false;
            } else {
                root.value = "";
            }

            for (ArgElement child : elements) {
                if (child.parent == root.node) {
                    clearBranch(child);
                }
            }
        }

        private boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            for (ArgElement element : elements) {
                if (element.focused) {
                    if (keyCode == GLFW.GLFW_KEY_TAB) {
                        return completeFocusedArgument(element);
                    }
                    if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                        if (!element.value.isEmpty()) {
                            element.value = element.value.substring(0, element.value.length() - 1);
                        }
                        lastCompletionPrefix = "";
                        lastCompletionIndex = -1;
                        return true;
                    }
                    if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                        executeCommand();
                        element.focused = false;
                        return true;
                    }
                    if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                        element.focused = false;
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean charTyped(char chr, int modifiers) {
            for (ArgElement element : elements) {
                if (element.focused) {
                    element.value += chr;
                    lastCompletionPrefix = "";
                    lastCompletionIndex = -1;
                    return true;
                }
            }
            return false;
        }

        private boolean completeFocusedArgument(ArgElement element) {
            List<String> matches = getCompletionMatches(element);
            if (matches.isEmpty()) {
                return false;
            }

            String prefix = element.value;
            if (!prefix.equalsIgnoreCase(lastCompletionPrefix)) {
                lastCompletionPrefix = prefix;
                lastCompletionIndex = 0;
            } else {
                lastCompletionIndex = (lastCompletionIndex + 1) % matches.size();
            }

            element.value = matches.get(Math.max(0, lastCompletionIndex));
            return true;
        }

        private List<String> getCompletionMatches(ArgElement element) {
            String name = element.node.getName().toLowerCase(Locale.ROOT);
            if (!(element.node instanceof ArgumentCommandNode)) {
                return List.of();
            }
            if (name.equals("user") || name.contains("player")) {
                return getOnlinePlayerMatches(element.value);
            }
            if (name.equals("command") || name.contains("cmd")) {
                return getCommandMatches(element.value);
            }
            return List.of();
        }

        private List<String> getOnlinePlayerMatches(String prefix) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getNetworkHandler() == null) {
                return List.of();
            }

            return client.getNetworkHandler().getPlayerList().stream()
                    .map(PlayerListEntry::getProfile)
                    .map(profile -> profile.getName())
                    .filter(name -> name.regionMatches(true, 0, prefix, 0, prefix.length()))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        }

        private List<String> getCommandMatches(String prefix) {
            TreeSet<String> commands = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

            ParadiseClient.COMMAND_MANAGER.getCommands().stream()
                    .map(Command::getName)
                    .forEach(commands::add);

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getNetworkHandler() != null && client.getNetworkHandler().getCommandDispatcher() != null) {
                client.getNetworkHandler().getCommandDispatcher().getRoot().getChildren().stream()
                        .map(CommandNode::getName)
                        .forEach(commands::add);
            }

            return commands.stream()
                    .filter(name -> name.regionMatches(true, 0, prefix, 0, prefix.length()))
                    .toList();
        }
    }

    private class ArgElement {
        private final CommandNode<CommandSource> node;
        private final CommandNode<CommandSource> parent;
        private final int depth;
        private String value = "";
        private boolean focused = false;
        private boolean selected = false;

        private ArgElement(CommandNode<CommandSource> node, CommandNode<CommandSource> parent, int depth) {
            this.node = node;
            this.parent = parent;
            this.depth = depth;
            if (node instanceof LiteralCommandNode && parent.getChildren().size() == 1) {
                this.selected = true;
            }
        }

        private void render(DrawContext context, int x, int y, int width, int mouseX, int mouseY) {
            int h = getHeight();
            theme.renderPanel(context, x + 2, y, width - 4, h);

            if (isBooleanArgument(node)) {
                theme.renderCheckbox(context, x + width - 18, y + (h - 12) / 2, 12, value.equalsIgnoreCase("true"), isHovered(mouseX, mouseY, x + width - 18, y, 12, h));
            }

            if (focused || selected || isHovered(mouseX, mouseY, x + 2, y, width - 4, h)) {
                theme.renderSelection(context, x + 2, y, width - 4, h);
            }

            int color = node instanceof LiteralCommandNode ? (selected ? 0xFF00D4FF : 0xFFAAAAAA) : 0xFFFFFFFF;
            String prefix = " ".repeat(depth * 2) + "* " + node.getName();

            context.getMatrices().push();
            context.getMatrices().translate(x + 6, y + (h - 8) / 2f, 0);
            context.drawText(textRenderer, prefix, 0, 0, color, false);

            if (node instanceof ArgumentCommandNode) {
                String valueText = ": " + value + (focused && (System.currentTimeMillis() / 500 % 2 == 0) ? "_" : "");
                int nameWidth = textRenderer.getWidth(prefix);
                context.drawText(textRenderer, valueText, nameWidth, 0, 0xFFFFFFFF, false);
            }
            context.getMatrices().pop();
        }

        private int getHeight() {
            return 16;
        }
    }

    private boolean isBooleanArgument(CommandNode<CommandSource> node) {
        String name = node.getName().toLowerCase(Locale.ROOT);
        return node instanceof ArgumentCommandNode && (name.contains("enabled") || name.contains("toggle"));
    }

    private static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + radius, y + height - radius, color);
        context.fill(x + width - radius, y + radius, x + width, y + height - radius, color);

        context.fill(x, y, x + radius, y + radius, color);
        context.fill(x + width - radius, y, x + width, y + radius, color);
        context.fill(x, y + height - radius, x + radius, y + height, color);
        context.fill(x + width - radius, y + height - radius, x + width, y + height, color);
    }

    private static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
