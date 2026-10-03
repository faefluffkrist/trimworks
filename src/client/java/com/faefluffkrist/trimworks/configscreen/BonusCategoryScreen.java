package com.faefluffkrist.trimworks.configscreen;

import com.faefluffkrist.trimworks.config.TrimEffectsConfigManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.io.IOException;
import java.util.List;

/** A height-aware list of focused editors instead of a wall of long toggle buttons. */
class BonusCategoryScreen extends Screen {
    protected final TrimEffectsConfigScreen parent;
    private final boolean material;
    private int page;
    private Component status = Component.empty();
    private static final List<String> MATERIALS = List.of("amethyst", "copper", "diamond", "emerald", "gold", "iron", "lapis", "netherite", "quartz", "redstone", "resin");
    BonusCategoryScreen(TrimEffectsConfigScreen parent, boolean material) {
        super(Component.literal(material ? "Trim Material Bonuses" : "Built-In Pattern Bonuses"));
        this.parent = parent; this.material = material;
    }
    private boolean readOnly() { return TrimEffectsConfigManager.hasServerSync() && !minecraft.hasSingleplayerServer(); }
    private com.faefluffkrist.trimworks.config.TrimEffectsConfig root() {
        return readOnly() ? TrimEffectsConfigManager.getDisplayConfig() : TrimEffectsConfigManager.getServerConfig();
    }
    @Override public void extractBackground(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        net.minecraft.client.gui.screens.Screen.extractMenuBackgroundTexture(graphics,
                net.minecraft.resources.Identifier.withDefaultNamespace("textures/block/cobbled_deepslate.png"),
                0, 0, 0, 0, width, height);
        graphics.fill(0, 0, width, height, 0x99000000);
    }
    @Override protected void init() {
        int w = Math.min(360, width - 40), x = width / 2 - w / 2;
        boolean enabled = material ? root().materialBonuses.enabled : root().builtInBonuses.enabled;
        var master = addRenderableWidget(Button.builder(Component.literal("All bonuses: " + (enabled ? "ON" : "OFF")), b -> {
            if (readOnly()) return;
            if (material) root().materialBonuses.enabled = !root().materialBonuses.enabled;
            else root().builtInBonuses.enabled = !root().builtInBonuses.enabled;
            try { TrimEffectsConfigManager.save(); parent.markChangesSaved(); status = Component.empty(); }
            catch (IOException e) {
                if (material) root().materialBonuses.enabled = enabled; else root().builtInBonuses.enabled = enabled;
                status = Component.literal("Could not save settings");
            }
            rebuildWidgets();
        }).bounds(x, 42, w, 20).build());
        master.active = !readOnly();
        List<String> ids = material ? MATERIALS.stream().map(s -> "minecraft:" + s).toList()
                : root().trims.keySet().stream().sorted().toList();
        int rows = Math.max(1, (height - 124) / 24);
        int pages = Math.max(1, (ids.size() + rows - 1) / rows);
        page = Math.min(page, pages - 1);
        int y = 72;
        for (int i = page * rows; i < Math.min(ids.size(), (page + 1) * rows); i++) {
            String id = ids.get(i);
            addRenderableWidget(Button.builder(Component.literal(TrimEffectsConfigScreen.pretty(id) + "  >"), b ->
                    minecraft.gui.setScreen(new BonusSettingsScreen(this, parent, material, id)))
                    .bounds(x, y, w, 20).build());
            y += 24;
        }
        var prev = addRenderableWidget(Button.builder(Component.literal("<"), b -> { page--; rebuildWidgets(); }).bounds(x, height - 28, 40, 20).build());
        prev.active = page > 0;
        var next = addRenderableWidget(Button.builder(Component.literal(">"), b -> { page++; rebuildWidgets(); }).bounds(x + w - 40, height - 28, 40, 20).build());
        next.active = page < pages - 1;
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose()).bounds(width/2 - 50, height - 28, 100, 20).build());
        status = status.getString().startsWith("Could") ? status : Component.literal("Page " + (page + 1) + " / " + pages);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        super.extractRenderState(g, mx, my, delta);
        g.centeredText(font, title, width/2, 12, 0xFFFFFFFF);
        g.centeredText(font, Component.literal(readOnly() ? "Server settings • read-only" : "Choose a trim to edit its bonuses"), width/2, 27, 0xFFAAAAAA);
        g.centeredText(font, status, width/2, height - 43, 0xFFAAAAAA);
    }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
}
