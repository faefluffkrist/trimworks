package com.faefluffkrist.trimworks.configscreen;

import com.faefluffkrist.trimworks.config.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import java.util.*;

/** Regular trim progression editor. All edits stay in a draft until Save. */
final class TrimDetailScreen extends ScrollingConfigScreen {
    private final TrimEffectsConfigScreen parent;
    private final String trimId;
    private TrimDefinition draft;
    private int effectIndex;
    private final Map<ConfiguredEffect,Map<String,String>> rawLevels=new IdentityHashMap<>();
    private Component status=Component.empty();
    TrimDetailScreen(TrimEffectsConfigScreen parent,String trimId){
        super(Component.literal(TrimEffectsConfigScreen.pretty(trimId)+" • Main Effects"));this.parent=parent;this.trimId=trimId;
        var root=ConfigUi.readOnly()?TrimEffectsConfigManager.getDisplayConfig():TrimEffectsConfigManager.getServerConfig();
        draft=copy(root.trims.get(trimId));
    }
    private static TrimDefinition copy(TrimDefinition original){
        var result=new TrimDefinition();if(original==null){result.enabled=false;return result;}result.enabled=original.enabled;
        if(original.effects!=null)for(var old:original.effects)if(old!=null){var effect=new ConfiguredEffect();effect.id=old.id;effect.levels=old.levels==null?new LinkedHashMap<>():new LinkedHashMap<>(old.levels);result.effects.add(effect);}
        return result;
    }
    private ConfiguredEffect current(){return draft.effects.isEmpty()?null:draft.effects.get(effectIndex);}
    private Map<String,String> raw(ConfiguredEffect effect){return rawLevels.computeIfAbsent(effect,e->{var m=new LinkedHashMap<String,String>();for(int i=1;i<=4;i++)m.put(Integer.toString(i),Integer.toString(e.levels.getOrDefault(Integer.toString(i),0)));return m;});}
    @Override protected void init(){
        effectIndex=Math.max(0,Math.min(effectIndex,Math.max(0,draft.effects.size()-1)));
        layoutRows(draft.effects.isEmpty()?1:6,48,height-82,54,420);
        for(int row=scrollRow;row<Math.min(draft.effects.isEmpty()?1:6,scrollRow+visibleRows);row++){
            int y=listTop+(row-scrollRow)*rowHeight;
            if(row==0){
                var b=addRenderableWidget(Button.builder(ConfigUi.toggle(draft.enabled),button->{draft.enabled=!draft.enabled;rebuildWidgets();}).bounds(listX+listWidth-70,y+3,70,20).build());b.active=!ConfigUi.readOnly();
            }else if(row==1){
                var effect=current();
                var b=addRenderableWidget(Button.builder(Component.literal("Choose / search effect"),button->minecraft.gui.setScreen(new RegistryPickerScreen(this,false,id->{effect.id=id;status=Component.empty();}))).bounds(listX+listWidth-145,y+3,145,20).build());b.active=!ConfigUi.readOnly();
            }else{
                String key=Integer.toString(row-1);var raw=raw(current());
                var box=addRenderableWidget(new EditBox(font,listX+listWidth-65,y+3,65,20,Component.literal("Effect level with "+key+" matching pieces")));
                box.setMaxLength(3);box.setValue(raw.get(key));box.setEditable(!ConfigUi.readOnly());box.setResponder(value->raw.put(key,value));
            }
        }
        int x=listX,w=listWidth,y=height-53;
        var prev=addRenderableWidget(Button.builder(Component.literal("<"),b->{effectIndex--;scrollRow=0;rebuildWidgets();}).bounds(x,y,25,20).build());prev.active=effectIndex>0;
        var next=addRenderableWidget(Button.builder(Component.literal(">"),b->{effectIndex++;scrollRow=0;rebuildWidgets();}).bounds(x+29,y,25,20).build());next.active=effectIndex<draft.effects.size()-1;
        var add=addRenderableWidget(Button.builder(Component.literal("Add Effect"),b->{draft.effects.add(new ConfiguredEffect("minecraft:strength",0,0,0,0));effectIndex=draft.effects.size()-1;scrollRow=1;rebuildWidgets();}).bounds(x+62,y,(w-70)/2,20).build());add.active=!ConfigUi.readOnly();
        var remove=addRenderableWidget(Button.builder(Component.literal("Remove"),b->{draft.effects.remove(effectIndex);effectIndex=Math.max(0,effectIndex-1);scrollRow=0;rebuildWidgets();}).bounds(x+66+(w-70)/2,y,(w-70)/2,20).build());remove.active=!ConfigUi.readOnly()&&!draft.effects.isEmpty();
        addRenderableWidget(Button.builder(Component.literal("Back"),b->onClose()).bounds(x,height-28,65,20).build());
        var reset=addRenderableWidget(Button.builder(Component.literal("Reset"),b->{draft=copy(TrimEffectsConfig.defaults().trims.get(trimId));rawLevels.clear();effectIndex=0;scrollRow=0;status=Component.literal("Defaults restored to draft; save to keep");rebuildWidgets();}).bounds(x+70,height-28,65,20).build());reset.active=!ConfigUi.readOnly();
        var save=addRenderableWidget(Button.builder(Component.literal("Save & Back"),b->save()).bounds(x+w-105,height-28,105,20).build());save.active=!ConfigUi.readOnly();
    }
    private void save(){
        try{
            for(var effect:draft.effects){
                var id=Identifier.tryParse(effect.id==null?"":effect.id);
                if(id==null||!BuiltInRegistries.MOB_EFFECT.containsKey(id))throw new IllegalArgumentException("Choose a registered status effect");
                for(var entry:raw(effect).entrySet()){
                    int level=Integer.parseInt(entry.getValue().trim());if(level<0||level>255)throw new IllegalArgumentException("Levels must be 0–255; 0 disables that step");effect.levels.put(entry.getKey(),level);
                }
            }
            var root=TrimEffectsConfigManager.getServerConfig();var old=root.trims.put(trimId,draft);
            try{TrimEffectsConfigManager.save();}catch(java.io.IOException e){root.trims.put(trimId,old);throw e;}
            parent.markChangesSaved();minecraft.gui.setScreen(parent);
        }catch(Exception e){status=Component.literal(e instanceof NumberFormatException?"Enter a whole level from 0 to 255":e.getMessage()==null?"Could not save":e.getMessage());}
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        super.extractRenderState(g,mx,my,delta);g.centeredText(font,title,width/2,12,0xFFFFFFFF);
        g.centeredText(font,Component.literal(ConfigUi.readOnly()?"Server configuration • read-only":draft.effects.isEmpty()?"No effects configured • Add Effect to begin":"Current effect: "+EffectGuide.name(current().id,false)),width/2,28,0xFFAAAAAA);
        for(int row=scrollRow;row<Math.min(draft.effects.isEmpty()?1:6,scrollRow+visibleRows);row++){
            int y=listTop+(row-scrollRow)*rowHeight;
            if(row==0){
                g.text(font,Component.literal("Enable this trim’s main effects"),listX+4,y+7,0xFFFFE0A1);
                ConfigUi.text(g,font,"Only equipped armor counts. Full-set bonuses have separate controls.",listX+4,y+29,listWidth-8,2,0xFFA8B8BF);
            }else if(row==1){
                ConfigUi.text(g,font,EffectGuide.name(current().id,false),listX+4,y+7,listWidth-155,2,0xFFFFE0A1);
                ConfigUi.text(g,font,EffectGuide.guide(current().id),listX+4,y+29,listWidth-8,2,0xFFA8B8BF);
            }else{
                int pieces=row-1;g.text(font,Component.literal(pieces+" piece"+(pieces==1?"":"s")+" of the set"),listX+4,y+7,0xFFFFE0A1);
                ConfigUi.text(g,font,"Level of "+EffectGuide.name(current().id,false)+" with exactly "+pieces+" equipped piece"+(pieces==1?"":"s")+". 0 = OFF; 1 = level I.",listX+4,y+29,listWidth-8,2,0xFFA8B8BF);
            }
        }
        drawScrollbar(g);ConfigUi.text(g,font,status.getString().isEmpty()&&!draft.effects.isEmpty()?"Effect "+(effectIndex+1)+" / "+draft.effects.size()+" • Levels 0–255":status.getString(),listX,height-69,listWidth,1,0xFFFFCC77);
    }
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
