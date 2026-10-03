package com.faefluffkrist.trimworks.configscreen;

import com.faefluffkrist.trimworks.config.TrimDefinition;
import com.faefluffkrist.trimworks.config.TrimEffectsConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TrimEffectsConfigScreen extends Screen {
    private final Screen parent;
    private int scrollRow;
    private int visibleRows;
    private int listTop;
    private int listBottom;
    private int scrollbarX;
    private boolean draggingScrollbar;
    private boolean changesSaved;

    public TrimEffectsConfigScreen(Screen parent) {
        super(Component.literal("Trimworks Configuration"));
        this.parent = parent;
    }

    private boolean remoteReadOnly() {
        return TrimEffectsConfigManager.hasServerSync() && !minecraft.hasSingleplayerServer();
    }

    private List<String> sortedIds() {
        var config = remoteReadOnly() ? TrimEffectsConfigManager.getDisplayConfig() : TrimEffectsConfigManager.getServerConfig();
        List<String> ids = new ArrayList<>(config.trims.keySet());
        ids.sort(Comparator.comparing(TrimEffectsConfigScreen::pretty, String.CASE_INSENSITIVE_ORDER));
        return ids;
    }

    @Override
    protected void init() {
        var shownConfig = remoteReadOnly() ? TrimEffectsConfigManager.getDisplayConfig() : TrimEffectsConfigManager.getServerConfig();
        List<String> ids = sortedIds();

        listTop = remoteReadOnly() ? 52 : 40;
        int footerSpace = 90;
        visibleRows = Math.max(1, Math.min(ids.size(), (height - listTop - footerSpace) / 24));
        int maxScroll = Math.max(0, ids.size() - visibleRows);
        scrollRow = Math.max(0, Math.min(scrollRow, maxScroll));

        int buttonWidth = Math.min(460, width - 46);
        int buttonLeft = width / 2 - buttonWidth / 2;
        scrollbarX = buttonLeft + buttonWidth + 7;
        int y = listTop;
        for (int i = scrollRow; i < Math.min(ids.size(), scrollRow + visibleRows); i++) {
            String id = ids.get(i);
            TrimDefinition def = shownConfig.trims.get(id);
            boolean enabled = def != null && def.enabled;

            // Build this from an unstyled root. If the gold/bold trim component is
            // used as the root, Minecraft lets that style bleed into appended text.
            Component label = Component.empty()
                    .append(Component.literal(pretty(id)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .append(Component.literal(" Settings ").withStyle(ChatFormatting.BOLD))
                    .append(Component.literal("| ").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                    .append(Component.literal("Currently Configured to be").withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC))
                    .append(Component.literal(" ["))
                    .append(Component.literal(enabled ? "ON" : "OFF").withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED))
                    .append(Component.literal("]"));

            addRenderableWidget(Button.builder(label, b -> minecraft.gui.setScreen(new TrimDetailScreen(this, id)))
                    .bounds(buttonLeft, y, buttonWidth, 20).build());
            y += 24;
        }
        listBottom = y;

        addRenderableWidget(Button.builder(Component.literal("Built-In Pattern Bonuses"), b -> minecraft.gui.setScreen(new BuiltInBonusesScreen(this)))
                .bounds(width / 2 - 110, y + 3, 220, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Trim Material Bonuses"), b -> minecraft.gui.setScreen(new MaterialBonusesScreen(this)))
                .bounds(width / 2 - 110, y + 27, 220, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> finish())
                .bounds(width / 2 - 60, y + 51, 120, 20).build());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        List<String> ids = sortedIds();
        int maxScroll = Math.max(0, ids.size() - visibleRows);
        if (maxScroll > 0 && vertical != 0) {
            int old = scrollRow;
            scrollRow = Math.max(0, Math.min(maxScroll, scrollRow + (vertical > 0 ? -1 : 1)));
            if (old != scrollRow) {
                rebuildWidgets();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && hasScrollbar()
                && event.x() >= scrollbarX - 2 && event.x() <= scrollbarX + 8
                && event.y() >= listTop && event.y() <= scrollbarTrackBottom()) {
            draggingScrollbar = true;
            setScrollFromMouse(event.y());
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (draggingScrollbar) {
            setScrollFromMouse(event.y());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    private boolean hasScrollbar() {
        return sortedIds().size() > visibleRows;
    }

    private int scrollbarTrackBottom() {
        return listTop + visibleRows * 24 - 4;
    }

    private int scrollbarThumbHeight() {
        int total = sortedIds().size();
        int trackHeight = Math.max(1, scrollbarTrackBottom() - listTop);
        if (total <= 0) return trackHeight;
        return Math.max(18, Math.min(trackHeight, trackHeight * visibleRows / total));
    }

    private int scrollbarThumbTop() {
        int maxScroll = Math.max(0, sortedIds().size() - visibleRows);
        int trackHeight = Math.max(1, scrollbarTrackBottom() - listTop);
        int thumbHeight = scrollbarThumbHeight();
        int travel = Math.max(0, trackHeight - thumbHeight);
        return maxScroll == 0 ? listTop : listTop + (travel * scrollRow / maxScroll);
    }

    private void setScrollFromMouse(double mouseY) {
        List<String> ids = sortedIds();
        int maxScroll = Math.max(0, ids.size() - visibleRows);
        if (maxScroll == 0) return;

        int trackHeight = Math.max(1, scrollbarTrackBottom() - listTop);
        int thumbHeight = scrollbarThumbHeight();
        int travel = Math.max(1, trackHeight - thumbHeight);
        double relative = mouseY - listTop - thumbHeight / 2.0;
        int newScroll = (int) Math.round((relative / travel) * maxScroll);
        newScroll = Math.max(0, Math.min(maxScroll, newScroll));
        if (newScroll != scrollRow) {
            scrollRow = newScroll;
            rebuildWidgets();
            draggingScrollbar = true;
        }
    }

    void markChangesSaved() {
        changesSaved = true;
    }

    private void finish() {
        if (changesSaved && !remoteReadOnly()) {
            minecraft.gui.setScreen(new RelogNoticeScreen(parent));
        } else {
            minecraft.gui.setScreen(parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, title, width / 2, 14, 0xFFFFFF);

        if (remoteReadOnly()) {
            graphics.centeredText(font, Component.literal("Viewing server configuration (read-only)"), width / 2, 30, 0xAAAAAA);
        }

        if (hasScrollbar()) {
            int trackBottom = scrollbarTrackBottom();
            int thumbTop = scrollbarThumbTop();
            int thumbBottom = thumbTop + scrollbarThumbHeight();
            graphics.fill(scrollbarX, listTop, scrollbarX + 6, trackBottom, 0x66000000);
            graphics.fill(scrollbarX + 1, thumbTop, scrollbarX + 5, thumbBottom, draggingScrollbar ? 0xFFFFFFFF : 0xFFAAAAAA);
        }
    }

    @Override public void onClose() { finish(); }

    static String pretty(String id) {
        String namespace = "minecraft";
        String path = id;
        int colon = id.indexOf(':');
        if (colon >= 0) {
            namespace = id.substring(0, colon);
            path = id.substring(colon + 1);
        }
        String[] parts = path.split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (!out.isEmpty()) out.append(' ');
            if (!part.isEmpty()) out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        if (!"minecraft".equals(namespace)) out.append(" (").append(namespace).append(')');
        return out.toString();
    }
}
