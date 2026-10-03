package com.faefluffkrist.trimworks.configscreen;

import com.faefluffkrist.trimworks.config.TrimEffectsConfigManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

public final class TrimEffectsConfigScreen extends ScrollingConfigScreen {
    private final Screen parent;
    private int category;
    private boolean changesSaved;
    private Component status=Component.empty();
    private List<String> ids=List.of();
    private static final String[] TABS={"Main Trims","Full Set Bonus","Materials"};
    private static final String[] DESCRIPTIONS={"Status effects by equipped piece count","Built-in abilities and extra full-set bonuses","Bonuses from matching trim materials"};
    public TrimEffectsConfigScreen(Screen parent){super(Component.literal("Trimworks 1.1.0 Configuration"));this.parent=parent;}
    private com.faefluffkrist.trimworks.config.TrimEffectsConfig cfg(){return ConfigUi.readOnly()?TrimEffectsConfigManager.getDisplayConfig():TrimEffectsConfigManager.getServerConfig();}
    @Override protected void init(){
        var config=cfg();
        ids=category==2?new ArrayList<>(List.of("minecraft:amethyst","minecraft:copper","minecraft:diamond","minecraft:emerald","minecraft:gold","minecraft:iron","minecraft:lapis","minecraft:netherite","minecraft:quartz","minecraft:redstone","minecraft:resin")):new ArrayList<>(config.trims.keySet());
        if(category==2&&config.materialBonuses.extraBonuses!=null)for(var id:config.materialBonuses.extraBonuses.keySet())if(!ids.contains(id))ids.add(id);
        if (category == 2 && minecraft.level != null) minecraft.level.registryAccess().lookup(net.minecraft.core.registries.Registries.TRIM_MATERIAL).ifPresent(registry ->
                registry.listElements().forEach(holder -> { String id = holder.key().identifier().toString(); if (!ids.contains(id)) ids.add(id); }));
        ids.sort(Comparator.comparing(TrimEffectsConfigScreen::pretty,String.CASE_INSENSITIVE_ORDER));
        int w=Math.min(460,width-48),x=width/2-w/2,tab=(w-8)/3;
        for(int i=0;i<3;i++){final int selected=i;
            var b=addRenderableWidget(Button.builder(Component.literal(TABS[i]),button->{category=selected;scrollRow=0;status=Component.empty();rebuildWidgets();}).bounds(x+i*(tab+4),34,tab,20).build());b.active=category!=i;
        }
        if(category!=0){
            boolean on=category==1?config.builtInBonuses.enabled:config.materialBonuses.enabled;
            var master=addRenderableWidget(Button.builder(ConfigUi.toggle(on),b->{
                if(ConfigUi.readOnly())return;
                if(category==1)config.builtInBonuses.enabled=!on;else config.materialBonuses.enabled=!on;
                try{TrimEffectsConfigManager.save();markChangesSaved();status=Component.empty();}
                catch(java.io.IOException e){if(category==1)config.builtInBonuses.enabled=on;else config.materialBonuses.enabled=on;status=Component.literal("Could not save settings");}
                rebuildWidgets();
            }).bounds(x+w-65,78,65,20).build());master.active=!ConfigUi.readOnly();
        }
        layoutRows(ids.size(),category==0?82:116,height-50,32,460);
        for(int i=scrollRow;i<Math.min(ids.size(),scrollRow+visibleRows);i++){
            String id=ids.get(i);int y=listTop+(i-scrollRow)*32;
            if(category==0){
                var definition=config.trims.get(id);boolean on=definition!=null&&definition.enabled;
                var toggle=addRenderableWidget(Button.builder(ConfigUi.toggle(on),b->{
                    if(ConfigUi.readOnly()||definition==null)return;definition.enabled=!on;
                    try{TrimEffectsConfigManager.save();markChangesSaved();}catch(java.io.IOException e){definition.enabled=on;status=Component.literal("Could not save settings");}rebuildWidgets();
                }).bounds(listX+listWidth-125,y+3,55,20).build());toggle.active=!ConfigUi.readOnly();
            }
            addRenderableWidget(Button.builder(Component.literal("Edit"),b->{
                minecraft.gui.setScreen(category==0?new TrimDetailScreen(this,id):new BonusSettingsScreen(this,this,category==2,id));
            }).bounds(listX+listWidth-65,y+3,65,20).build());
        }
        boolean compat = com.faefluffkrist.trimworks.gameplay.MobTrimCompatibility.availableInMenu(config);
        var compatibility = addRenderableWidget(Button.builder(Component.literal("Naturally Trimmed"), b ->
                minecraft.gui.setScreen(new MobCompatibilityScreen(this))).bounds(width/2-144,height-28,178,20).build());
        compatibility.active = compat;
        compatibility.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(compat
                ? "Configure non-player trim bonuses" : "Requires Naturally Trimmed on the server or in singleplayer")));
        addRenderableWidget(Button.builder(Component.literal("Done"),b->finish()).bounds(width/2+42,height-28,102,20).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        super.extractRenderState(g,mx,my,delta);g.centeredText(font,title,width/2,12,0xFFFFFFFF);
        g.centeredText(font,Component.literal(DESCRIPTIONS[category]),width/2,62,0xFFB7C7CC);
        if(category!=0){
            g.text(font,Component.literal(category==1?"Enable full-set pattern bonuses":"Enable material bonuses"),listX,80,0xFFFFFFFF);
            ConfigUi.text(g,font,"Applies to this entire category",listX,94,listWidth-78,1,0xFF999999);
        }
        for(int i=scrollRow;i<Math.min(ids.size(),scrollRow+visibleRows);i++){
            String id=ids.get(i);int y=listTop+(i-scrollRow)*32;
            g.fill(listX,y,listX+listWidth-(category==0?132:72),y+27,0x40333C42);
            ConfigUi.text(g,font,pretty(id),listX+7,y+8,listWidth-(category==0?145:85),1,0xFFFFE0A1);
        }
        drawScrollbar(g);
        g.centeredText(font,status.getString().isEmpty()?Component.literal(ConfigUi.readOnly()?"Server configuration • read-only":"Changes require reopening the world"):status,width/2,height-43,0xFFAAAAAA);
    }
    void markChangesSaved(){changesSaved=true;}
    private void finish(){minecraft.gui.setScreen(changesSaved&&!ConfigUi.readOnly()?new RelogNoticeScreen(parent):parent);}
    @Override public void onClose(){finish();}
    static String pretty(String id){
        int colon=id.indexOf(':');String namespace=colon>=0?id.substring(0,colon):"minecraft",path=colon>=0?id.substring(colon+1):id;
        StringBuilder result=new StringBuilder();for(String word:path.split("[_/]+")){if(!result.isEmpty())result.append(' ');if(!word.isEmpty())result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));}
        if(!namespace.equals("minecraft"))result.append(" (").append(namespace).append(')');return result.toString();
    }
}
