package com.faefluffkrist.trimworks.configscreen;

import com.faefluffkrist.trimworks.config.BuiltInBonusesConfig;
import com.faefluffkrist.trimworks.config.TrimEffectsConfigManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;

import java.io.IOException;

public final class BuiltInBonusesScreen extends Screen {
    private final TrimEffectsConfigScreen parent;

    public BuiltInBonusesScreen(TrimEffectsConfigScreen parent) {
        super(Component.literal("Built-In Trim Bonuses"));
        this.parent = parent;
    }

    private boolean readOnly() {
        return TrimEffectsConfigManager.hasServerSync() && !minecraft.hasSingleplayerServer();
    }

    private BuiltInBonusesConfig cfg() {
        var root = readOnly() ? TrimEffectsConfigManager.getDisplayConfig() : TrimEffectsConfigManager.getServerConfig();
        if (root.builtInBonuses == null) root.builtInBonuses = new BuiltInBonusesConfig();
        return root.builtInBonuses;
    }

    @Override
    protected void init() {
        int totalW = Math.min(700, width - 30);
        int gap = 10;
        int colW = (totalW - gap) / 2;
        int leftX = width / 2 - totalW / 2;
        int rightX = leftX + colW + gap;
        int y = 42;

        addToggle(leftX, y, totalW, "Built-In", "Bonuses", true, () -> cfg().enabled, v -> cfg().enabled = v);

        int leftY = y + 30;
        addToggle(leftX, leftY, colW, "Ward", "Swift Sneak Progression", false, () -> cfg().wardSwiftSneak, v -> cfg().wardSwiftSneak = v); leftY += 25;
        addToggle(leftX, leftY, colW, "Ward", "Darkness Immunity", false, () -> cfg().wardDarknessImmunity, v -> cfg().wardDarknessImmunity = v); leftY += 25;
        addToggle(leftX, leftY, colW, "Ward", "Deepslate/City Speed I", false, () -> cfg().wardAncientCitySpeed, v -> cfg().wardAncientCitySpeed = v); leftY += 25;
        addToggle(leftX, leftY, colW, "Silence", "Warden Neutrality", false, () -> cfg().silenceWardenNeutrality, v -> cfg().silenceWardenNeutrality = v); leftY += 25;
        addToggle(leftX, leftY, colW, "Silence", "Sculk Speed I", false, () -> cfg().silenceSculkSpeed, v -> cfg().silenceSculkSpeed = v); leftY += 25;
        addToggle(leftX, leftY, colW, "Silence", "Spectral Mark (10s)", false, () -> cfg().silenceSpectralMark, v -> cfg().silenceSpectralMark = v); leftY += 25;
        addToggle(leftX, leftY, colW, "Tide", "Dolphin's Grace", false, () -> cfg().tideDolphinsGrace, v -> cfg().tideDolphinsGrace = v); leftY += 25;
        addToggle(leftX, leftY, colW, "Bolt", "Melee Knockback", false, () -> cfg().boltMeleeKnockback, v -> cfg().boltMeleeKnockback = v); leftY += 25;
        addToggle(leftX, leftY, colW, "Bolt", "Arrow Deflection (25%)", false, () -> cfg().boltProjectileDeflection, v -> cfg().boltProjectileDeflection = v); leftY += 25;
        addToggle(leftX, leftY, colW, "Coast", "Conduit Power", false, () -> cfg().coastConduitPower, v -> cfg().coastConduitPower = v); leftY += 25;

        int rightY = y + 30;
        addToggle(rightX, rightY, colW, "Sentry", "Illager Neutrality", false, () -> cfg().sentryIllagerNeutrality, v -> cfg().sentryIllagerNeutrality = v); rightY += 25;
        addToggle(rightX, rightY, colW, "Vex", "Vex Neutrality", false, () -> cfg().vexNeutrality, v -> cfg().vexNeutrality = v); rightY += 25;
        addToggle(rightX, rightY, colW, "Snout", "Brute & Hoglin Neutrality", false, () -> cfg().snoutBruteHoglinNeutrality, v -> cfg().snoutBruteHoglinNeutrality = v); rightY += 25;
        addToggle(rightX, rightY, colW, "Rib", "Wither Immunity", false, () -> cfg().ribWitherImmunity, v -> cfg().ribWitherImmunity = v); rightY += 25;
        addToggle(rightX, rightY, colW, "Dune", "Terrain Speed II", false, () -> cfg().duneTerrainSpeed, v -> cfg().duneTerrainSpeed = v); rightY += 25;
        addToggle(rightX, rightY, colW, "Wild", "Terrain Speed II", false, () -> cfg().wildTerrainSpeed, v -> cfg().wildTerrainSpeed = v); rightY += 25;
        addToggle(rightX, rightY, colW, "Eye", "Terrain Speed II", false, () -> cfg().eyeTerrainSpeed, v -> cfg().eyeTerrainSpeed = v); rightY += 25;
        addToggle(rightX, rightY, colW, "Eye", "Enderman Gaze Immunity", false, () -> cfg().eyeEndermanGazeImmunity, v -> cfg().eyeEndermanGazeImmunity = v); rightY += 25;

        int backY = Math.max(leftY, rightY) + 5;
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> minecraft.gui.setScreen(parent))
                .bounds(width / 2 - 60, backY, 120, 20).build());
    }

    private void addToggle(int x, int y, int w, String trim, String bonus, boolean master, BoolGet get, BoolSet set) {
        addRenderableWidget(Button.builder(label(trim, bonus, master, get.get()), b -> {
            if (readOnly()) return;
            boolean value = !get.get();
            set.set(value);
            b.setMessage(label(trim, bonus, master, value));
            try {
                TrimEffectsConfigManager.save();
                parent.markChangesSaved();
            } catch (IOException ignored) {}
        }).bounds(x, y, w, 20).build());
    }

    private Component label(String trim, String bonus, boolean master, boolean value) {
        MutableComponent text;
        if (master) {
            text = Component.literal(trim + " ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                    .append(Component.literal(bonus + ": ").withStyle(style -> style.withColor(ChatFormatting.WHITE).withBold(false)));
        } else {
            text = Component.literal(trim + " ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                    .append(Component.literal("- ").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                    .append(Component.literal(bonus + ": ").withStyle(style -> style.withColor(ChatFormatting.WHITE).withBold(false)));
        }
        return text.append(Component.literal("[").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(value ? "ON" : "OFF").withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED))
                .append(Component.literal("]").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, title, width / 2, 14, 0xFFFFFF);
        if (readOnly()) graphics.centeredText(font, Component.literal("Viewing server configuration (read-only)"), width / 2, 28, 0xAAAAAA);
    }

    @Override public void onClose() { minecraft.gui.setScreen(parent); }

    @FunctionalInterface private interface BoolGet { boolean get(); }
    @FunctionalInterface private interface BoolSet { void set(boolean value); }
}
