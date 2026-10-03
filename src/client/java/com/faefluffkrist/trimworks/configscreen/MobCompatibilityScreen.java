package com.faefluffkrist.trimworks.configscreen;

import com.faefluffkrist.trimworks.config.MobBonusesConfig;
import com.faefluffkrist.trimworks.config.TrimEffectsConfig;
import com.faefluffkrist.trimworks.config.TrimEffectsConfigManager;
import com.faefluffkrist.trimworks.gameplay.MobTrimCompatibility;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import java.util.*;

/** Non-player policy: separate from the normal player configuration. */
public final class MobCompatibilityScreen extends ScrollingConfigScreen {
    private final TrimEffectsConfigScreen parent;
    private final MobBonusesConfig draft = new MobBonusesConfig();
    private int category;
    private List<String> ids = List.of();
    private String status = "";
    private static final String[] TABS = {"Main Trims", "Full Set Bonus", "Materials"};
    public MobCompatibilityScreen(TrimEffectsConfigScreen parent) {
        super(Component.literal("Naturally Trimmed • Mob Bonuses"));
        this.parent = parent;
        var current = cfg().mobBonuses;
        if (current != null) {
            draft.enabled = current.enabled;
            if (current.mainTrimBlacklist != null) draft.mainTrimBlacklist.addAll(current.mainTrimBlacklist);
            if (current.fullSetBlacklist != null) draft.fullSetBlacklist.addAll(current.fullSetBlacklist);
            if (current.materialBlacklist != null) draft.materialBlacklist.addAll(current.materialBlacklist);
        }
    }
    private TrimEffectsConfig cfg() {
        return ConfigUi.readOnly() ? TrimEffectsConfigManager.getDisplayConfig() : TrimEffectsConfigManager.getServerConfig();
    }
    private Set<String> blacklist() {
        return category == 0 ? draft.mainTrimBlacklist : category == 1 ? draft.fullSetBlacklist : draft.materialBlacklist;
    }
    private boolean editable() { return !ConfigUi.readOnly() && MobTrimCompatibility.availableInMenu(cfg()); }
    @Override protected void init() {
        var found = new LinkedHashSet<String>();
        if (category == 2) {
            found.addAll(List.of("minecraft:amethyst", "minecraft:copper", "minecraft:diamond", "minecraft:emerald", "minecraft:gold", "minecraft:iron", "minecraft:lapis", "minecraft:netherite", "minecraft:quartz", "minecraft:redstone", "minecraft:resin"));
            if (cfg().materialBonuses != null && cfg().materialBonuses.extraBonuses != null) found.addAll(cfg().materialBonuses.extraBonuses.keySet());
            if (minecraft.level != null) minecraft.level.registryAccess().lookup(Registries.TRIM_MATERIAL).ifPresent(registry ->
                    registry.listElements().forEach(holder -> found.add(holder.key().identifier().toString())));
        } else {
            found.addAll(cfg().trims.keySet());
            if (cfg().builtInBonuses != null && cfg().builtInBonuses.extraBonuses != null) found.addAll(cfg().builtInBonuses.extraBonuses.keySet());
            if (minecraft.level != null) minecraft.level.registryAccess().lookup(Registries.TRIM_PATTERN).ifPresent(registry ->
                    registry.listElements().forEach(holder -> found.add(holder.value().assetId().toString())));
        }
        found.addAll(blacklist()); // Preserve rules for temporarily absent mods.
        ids = found.stream().sorted(Comparator.comparing(TrimEffectsConfigScreen::pretty, String.CASE_INSENSITIVE_ORDER)).toList();
        int w = Math.min(460, width - 48), x = width / 2 - w / 2, tab = (w - 8) / 3;
        for (int i = 0; i < 3; i++) {
            final int selected = i;
            var button = addRenderableWidget(Button.builder(Component.literal(TABS[i]), b -> {
                category = selected; scrollRow = 0; rebuildWidgets();
            }).bounds(x + i * (tab + 4), 34, tab, 20).build());
            button.active = category != i;
        }
        var master = addRenderableWidget(Button.builder(ConfigUi.toggle(draft.enabled), b -> {
            draft.enabled = !draft.enabled; rebuildWidgets();
        }).bounds(x + w - 60, 60, 60, 20).build());
        master.active = editable();
        layoutRows(ids.size(), 100, height - 60, 36, 460);
        for (int i = scrollRow; i < Math.min(ids.size(), scrollRow + visibleRows); i++) {
            String id = ids.get(i); int y = listTop + (i - scrollRow) * rowHeight;
            boolean patternBlocked = category == 1 && draft.mainTrimBlacklist.contains(id);
            boolean allowed = !blacklist().contains(id);
            var toggle = addRenderableWidget(Button.builder(Component.literal(patternBlocked ? "Trim off" : allowed ? "Allowed" : "Blocked"), b -> {
                if (allowed) blacklist().add(id); else blacklist().remove(id);
                rebuildWidgets();
            }).bounds(listX + listWidth - 82, y + 5, 82, 20).build());
            toggle.active = editable() && draft.enabled && !patternBlocked;
        }
        var save = addRenderableWidget(Button.builder(Component.literal("Save"), b -> save()).bounds(width / 2 - 114, height - 28, 110, 20).build());
        save.active = editable();
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose()).bounds(width / 2 + 4, height - 28, 110, 20).build());
    }
    private void save() {
        if (!editable()) return;
        var old = cfg().mobBonuses;
        boolean changed = old == null || old.enabled != draft.enabled
                || !Objects.equals(old.mainTrimBlacklist, draft.mainTrimBlacklist)
                || !Objects.equals(old.fullSetBlacklist, draft.fullSetBlacklist)
                || !Objects.equals(old.materialBlacklist, draft.materialBlacklist);
        if (!changed) { onClose(); return; }
        cfg().mobBonuses = draft;
        try { TrimEffectsConfigManager.save(); parent.markChangesSaved(); onClose(); }
        catch (java.io.IOException e) { cfg().mobBonuses = old; status = "Could not save settings"; rebuildWidgets(); }
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        super.extractRenderState(g, mx, my, delta);
        g.centeredText(font, title, width / 2, 12, 0xFFFFFFFF);
        ConfigUi.text(g, font, "Enable bonuses for non-player mobs", listX, 64, listWidth - 70, 1, 0xFFFFFFFF);
        ConfigUi.text(g, font, category == 0 ? "Block this pattern's effects and full-set bonuses."
                : category == 1 ? "Block pattern abilities and added full-set bonuses."
                : "Block matching material bonuses on mobs.", listX, 84, listWidth, 1, 0xFFB7C7CC);
        for (int i = scrollRow; i < Math.min(ids.size(), scrollRow + visibleRows); i++) {
            String id = ids.get(i); int y = listTop + (i - scrollRow) * rowHeight;
            g.fill(listX, y, listX + listWidth - 90, y + 31, 0x40333C42);
            ConfigUi.text(g, font, TrimEffectsConfigScreen.pretty(id), listX + 7, y + 11, listWidth - 104, 1, draft.enabled ? 0xFFFFE0A1 : 0xFF888888);
        }
        drawScrollbar(g);
        String footer = !status.isEmpty() ? status : !MobTrimCompatibility.availableInMenu(cfg()) ? "Naturally Trimmed is not loaded"
                : ConfigUi.readOnly() ? "Server settings • read-only" : "Player bonuses stay unchanged • Back discards edits";
        g.centeredText(font, footer, width / 2, height - 50, 0xFFAAAAAA);
    }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
}
