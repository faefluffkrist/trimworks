package com.faefluffkrist.trimworks.configscreen;

import com.faefluffkrist.trimworks.config.MaterialBonusesConfig;
import com.faefluffkrist.trimworks.config.TrimEffectsConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.io.IOException;

public final class MaterialBonusesScreen extends Screen {
    private final TrimEffectsConfigScreen parent;
    public MaterialBonusesScreen(TrimEffectsConfigScreen parent) { super(Component.literal("Trim Material Bonuses")); this.parent = parent; }
    private boolean readOnly() { return TrimEffectsConfigManager.hasServerSync() && !minecraft.hasSingleplayerServer(); }
    private MaterialBonusesConfig cfg() {
        var root = readOnly() ? TrimEffectsConfigManager.getDisplayConfig() : TrimEffectsConfigManager.getServerConfig();
        if (root.materialBonuses == null) root.materialBonuses = new MaterialBonusesConfig();
        return root.materialBonuses;
    }
    @Override protected void init() {
        int w = Math.min(500, width - 40), x = width / 2 - w / 2, y = 36;
        addToggle(x,y,w,"Material","Bonuses",true,()->cfg().enabled,v->cfg().enabled=v); y+=27;
        addToggle(x,y,w,"Gold","Piglin Neutrality (4 pieces)",false,()->cfg().goldPiglinNeutrality,v->cfg().goldPiglinNeutrality=v); y+=23;
        addToggle(x,y,w,"Resin","Safe Honey Harvest (4 pieces)",false,()->cfg().amberSafeHoneyHarvest,v->cfg().amberSafeHoneyHarvest=v); y+=23;
        addToggle(x,y,w,"Amethyst","Enchanting Quality +1 (4 pieces)",false,()->cfg().amethystEnchantingBoost,v->cfg().amethystEnchantingBoost=v); y+=23;
        addToggle(x,y,w,"Quartz","Ghast Neutrality (4 pieces)",false,()->cfg().quartzGhastNeutrality,v->cfg().quartzGhastNeutrality=v); y+=23;
        addToggle(x,y,w,"Copper","Attracts Lightning + 50% Damage Resistance (4 pieces)",false,()->cfg().copperLightningResistance,v->cfg().copperLightningResistance=v); y+=23;
        addToggle(x,y,w,"Iron","+0.1 Knockback Resistance (4 pieces)",false,()->cfg().ironKnockbackResistance,v->cfg().ironKnockbackResistance=v); y+=23;
        addToggle(x,y,w,"Redstone","+2.5% Movement Speed (4 pieces)",false,()->cfg().redstoneMovementSpeed,v->cfg().redstoneMovementSpeed=v); y+=23;
        addToggle(x,y,w,"Lapis","+5% XP Gain (4 pieces)",false,()->cfg().lapisExperienceBoost,v->cfg().lapisExperienceBoost=v); y+=23;
        addToggle(x,y,w,"Emerald","Villager Discount (4 pieces)",false,()->cfg().emeraldVillagerDiscount,v->cfg().emeraldVillagerDiscount=v); y+=23;
        addToggle(x,y,w,"Diamond","+1 Armor Toughness (4 pieces)",false,()->cfg().diamondArmorToughness,v->cfg().diamondArmorToughness=v); y+=23;
        addToggle(x,y,w,"Netherite","Trimmed Piece Lava Immunity",false,()->cfg().netheriteFireproofPiece,v->cfg().netheriteFireproofPiece=v); y+=29;
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> minecraft.gui.setScreen(parent)).bounds(width/2-60,y,120,20).build());
    }
    private void addToggle(int x,int y,int w,String material,String bonus,boolean master,BoolGet get,BoolSet set) {
        addRenderableWidget(Button.builder(label(material,bonus,master,get.get()), b->{
            if(readOnly()) return; boolean value=!get.get(); set.set(value); b.setMessage(label(material,bonus,master,value));
            try { TrimEffectsConfigManager.save(); parent.markChangesSaved(); } catch(IOException ignored) {}
        }).bounds(x,y,w,20).build());
    }
    private Component label(String material,String bonus,boolean master,boolean value) {
        MutableComponent text=Component.empty();
        if(master) text.append(Component.literal(material+" ").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD))
                .append(Component.literal(bonus+": ").withStyle(ChatFormatting.WHITE));
        else text.append(Component.literal(material+" ").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD))
                .append(Component.literal("- ").withStyle(ChatFormatting.GRAY,ChatFormatting.BOLD))
                .append(Component.literal(bonus+": ").withStyle(ChatFormatting.WHITE));
        return text.append(Component.literal("[").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(value?"ON":"OFF").withStyle(value?ChatFormatting.GREEN:ChatFormatting.RED))
                .append(Component.literal("]").withStyle(ChatFormatting.GRAY));
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float delta){ super.extractRenderState(graphics,mouseX,mouseY,delta); graphics.centeredText(font,title,width/2,16,0xFFFFFF); if(readOnly()) graphics.centeredText(font,Component.literal("Viewing server configuration (read-only)"),width/2,30,0xAAAAAA); }
    @Override public void onClose(){ minecraft.gui.setScreen(parent); }
    @FunctionalInterface private interface BoolGet{boolean get();} @FunctionalInterface private interface BoolSet{void set(boolean v);}
}
