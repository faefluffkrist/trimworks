package com.faefluffkrist.trimworks.configscreen;

import com.faefluffkrist.trimworks.config.ConfiguredEffect;
import com.faefluffkrist.trimworks.config.TrimDefinition;
import com.faefluffkrist.trimworks.config.TrimEffectsConfig;
import com.faefluffkrist.trimworks.config.TrimEffectsConfigManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;

final class TrimDetailScreen extends Screen {
    private final Screen parent;
    private final String trimId;
    private TrimDefinition definition;
    private int effectIndex;
    private EditBox effectId;
    private final EditBox[] levels = new EditBox[4];
    private Component status = Component.empty();

    TrimDetailScreen(Screen parent, String trimId) {
        super(Component.literal(TrimEffectsConfigScreen.pretty(trimId) + " Settings"));
        this.parent = parent;
        this.trimId = trimId;
        this.definition = currentConfig().trims.get(trimId);
    }

    private boolean remoteReadOnly() {
        return TrimEffectsConfigManager.hasServerSync() && !minecraft.hasSingleplayerServer();
    }

    private TrimEffectsConfig currentConfig() {
        return remoteReadOnly() ? TrimEffectsConfigManager.getDisplayConfig() : TrimEffectsConfigManager.getServerConfig();
    }

    @Override
    protected void init() {
        definition = currentConfig().trims.get(trimId);
        if (definition == null) { onClose(); return; }
        if (definition.effects.isEmpty()) definition.effects.add(new ConfiguredEffect("minecraft:strength", 0,0,0,0));
        effectIndex = Math.max(0, Math.min(effectIndex, definition.effects.size() - 1));
        ConfiguredEffect effect = definition.effects.get(effectIndex);

        addRenderableWidget(Button.builder(Component.literal("Enabled: " + (definition.enabled ? "ON" : "OFF")), b -> {
            definition.enabled = !definition.enabled;
            rebuildWidgets();
        }).bounds(width / 2 - 110, 38, 220, 20).build());

        effectId = addRenderableWidget(new EditBox(font, width / 2 - 110, 86, 220, 20, Component.literal("Effect registry ID")));
        effectId.setMaxLength(128);
        effectId.setValue(effect.id == null ? "" : effect.id);

        int y = 128;
        for (int i = 0; i < 4; i++) {
            levels[i] = addRenderableWidget(new EditBox(font, width / 2 + 25, y, 85, 20, Component.literal("Level for " + (i + 1) + " pieces")));
            levels[i].setMaxLength(3);
            levels[i].setValue(Integer.toString(effect.levels.getOrDefault(Integer.toString(i + 1), 0)));
            y += 25;
        }

        Button prev = addRenderableWidget(Button.builder(Component.literal("< Effect"), b -> { storeFields(); effectIndex--; rebuildWidgets(); })
                .bounds(width / 2 - 110, 232, 70, 20).build());
        Button next = addRenderableWidget(Button.builder(Component.literal("Effect >"), b -> { storeFields(); effectIndex++; rebuildWidgets(); })
                .bounds(width / 2 + 40, 232, 70, 20).build());
        prev.active = effectIndex > 0;
        next.active = effectIndex < definition.effects.size() - 1;

        addRenderableWidget(Button.builder(Component.literal("Add Effect"), b -> {
            storeFields(); definition.effects.add(new ConfiguredEffect("minecraft:strength",0,0,0,0)); effectIndex = definition.effects.size()-1; rebuildWidgets();
        }).bounds(width / 2 - 110, 257, 105, 20).build());
        Button remove = addRenderableWidget(Button.builder(Component.literal("Remove Effect"), b -> {
            definition.effects.remove(effectIndex); if (definition.effects.isEmpty()) definition.effects.add(new ConfiguredEffect("minecraft:strength",0,0,0,0)); effectIndex=Math.min(effectIndex,definition.effects.size()-1); rebuildWidgets();
        }).bounds(width / 2 + 5, 257, 105, 20).build());
        remove.active = definition.effects.size() > 1;

        addRenderableWidget(Button.builder(Component.literal("Reset Trim"), b -> resetTrim())
                .bounds(width / 2 - 110, height - 58, 105, 20).build());
        Button save = addRenderableWidget(Button.builder(Component.literal(remoteReadOnly() ? "Back" : "Save & Back"), b -> {
            if (remoteReadOnly()) onClose(); else saveAndClose();
        }).bounds(width / 2 + 5, height - 58, 105, 20).build());

        if (remoteReadOnly()) {
            for (var child : children()) {
                if (child instanceof Button button && button != save) button.active = false;
                if (child instanceof EditBox box) box.setEditable(false);
            }
        }
    }

    private void storeFields() {
        if (effectId == null || definition.effects.isEmpty()) return;
        ConfiguredEffect effect = definition.effects.get(effectIndex);
        effect.id = effectId.getValue().trim();
        for (int i = 0; i < 4; i++) {
            try { effect.levels.put(Integer.toString(i + 1), Math.max(0, Math.min(255, Integer.parseInt(levels[i].getValue().trim())))); }
            catch (NumberFormatException ignored) { effect.levels.put(Integer.toString(i + 1), 0); }
        }
    }

    private void saveAndClose() {
        storeFields();
        try {
            TrimEffectsConfigManager.save();
            status = Component.literal("Saved");
            if (parent instanceof TrimEffectsConfigScreen configScreen) {
                configScreen.markChangesSaved();
            }
            // Returning to the existing parent instance re-runs init(), but keeps
            // the saved/relog status that was set above.
            minecraft.gui.setScreen(parent);
        } catch (IOException e) {
            status = Component.literal("Could not save config: " + e.getMessage());
        }
    }

    private void resetTrim() {
        if (remoteReadOnly()) return;
        TrimDefinition fresh = TrimEffectsConfig.defaults().trims.get(trimId);
        if (fresh == null) {
            fresh = new TrimDefinition();
            fresh.enabled = false;
        }
        TrimEffectsConfigManager.getServerConfig().trims.put(trimId, fresh);
        definition = fresh; effectIndex = 0; status = Component.literal("Reset to default (save to keep)"); rebuildWidgets();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, title, width / 2, 14, 0xFFFFFF);
        graphics.text(font, Component.literal("Effect registry ID"), width / 2 - 110, 72, 0xAAAAAA);
        graphics.centeredText(font, Component.literal("Effect " + (effectIndex + 1) + " / " + definition.effects.size()), width / 2, 112, 0xAAAAAA);
        for (int i=0;i<4;i++) graphics.text(font, Component.literal((i+1) + " matching piece" + (i==0?"":"s") + ":"), width/2-110, 134+i*25, 0xDDDDDD);
        if (remoteReadOnly()) graphics.centeredText(font, Component.literal("Server configuration is read-only"), width/2, 30, 0xAAAAAA);
        if (!status.getString().isEmpty()) graphics.centeredText(font, status, width/2, height-78, 0xAAAAAA);
    }

    @Override public void onClose() { minecraft.gui.setScreen(parent); }
}
