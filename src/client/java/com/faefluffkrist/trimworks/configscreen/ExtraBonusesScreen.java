package com.faefluffkrist.trimworks.configscreen;

import com.faefluffkrist.trimworks.config.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import java.util.*;

final class ExtraBonusesScreen extends ScrollingConfigScreen {
    private final net.minecraft.client.gui.screens.Screen parent;
    private final TrimEffectsConfigScreen rootScreen;
    private final boolean material;
    private final String trimId;
    private final List<BonusRule> draft=new ArrayList<>();
    private final Map<BonusRule,String> amounts=new IdentityHashMap<>();
    private int index;
    private Component status=Component.empty();
    ExtraBonusesScreen(net.minecraft.client.gui.screens.Screen parent,TrimEffectsConfigScreen rootScreen,boolean material,String trimId){
        super(Component.literal(TrimEffectsConfigScreen.pretty(trimId)+" • Extra Bonuses"));this.parent=parent;this.rootScreen=rootScreen;this.material=material;this.trimId=trimId;
        var old=groups().get(trimId);if(old!=null)for(var rule:old)if(rule!=null){var copy=new BonusRule();copy.enabled=rule.enabled;copy.type=rule.type;copy.id=rule.id;copy.amount=rule.amount;copy.percent=rule.percent;draft.add(copy);}
    }
    private Map<String,List<BonusRule>> groups(){var root=ConfigUi.readOnly()?TrimEffectsConfigManager.getDisplayConfig():TrimEffectsConfigManager.getServerConfig();return material?root.materialBonuses.extraBonuses:root.builtInBonuses.extraBonuses;}
    private BonusRule current(){return draft.get(index);}
    @Override protected void init(){
        index=Math.max(0,Math.min(index,Math.max(0,draft.size()-1)));
        layoutRows(draft.isEmpty()?0:5,48,height-82,62,420);
        for(int row=scrollRow;row<Math.min(draft.isEmpty()?0:5,scrollRow+visibleRows);row++){
            int y=listTop+(row-scrollRow)*rowHeight;var rule=current();
            if(row==0){var b=addRenderableWidget(Button.builder(ConfigUi.toggle(rule.enabled),button->{rule.enabled=!rule.enabled;rebuildWidgets();}).bounds(listX+listWidth-75,y+3,75,20).build());b.active=!ConfigUi.readOnly();}
            if(row==1){var b=addRenderableWidget(Button.builder(Component.literal("attribute".equals(rule.type)?"Attribute":"Effect"),button->{
                rule.type="effect".equals(rule.type)?"attribute":"effect";rule.id="attribute".equals(rule.type)?"minecraft:movement_speed":"minecraft:strength";rule.amount="attribute".equals(rule.type)?10:1;rule.percent="attribute".equals(rule.type);amounts.remove(rule);rebuildWidgets();
            }).bounds(listX+listWidth-100,y+3,100,20).build());b.active=!ConfigUi.readOnly();}
            if(row==2){var b=addRenderableWidget(Button.builder(Component.literal("Choose / search"),button->minecraft.gui.setScreen(new RegistryPickerScreen(this,"attribute".equals(rule.type),id->rule.id=id))).bounds(listX+listWidth-125,y+3,125,20).build());b.active=!ConfigUi.readOnly();}
            if(row==3){var box=addRenderableWidget(new EditBox(font,listX+listWidth-90,y+3,90,20,Component.literal("effect".equals(rule.type)?"Status effect level":"Attribute amount")));
                box.setMaxLength(18);box.setValue(amounts.computeIfAbsent(rule,r->Double.toString(r.amount)));box.setEditable(!ConfigUi.readOnly());box.setResponder(value->amounts.put(rule,value));}
            if(row==4){var b=addRenderableWidget(Button.builder(Component.literal(rule.percent?"Percent (%)":"Flat amount"),button->{rule.percent=!rule.percent;rebuildWidgets();}).bounds(listX+listWidth-110,y+3,110,20).build());b.active=!ConfigUi.readOnly()&&"attribute".equals(rule.type);}
        }
        int x=listX,w=listWidth,y=height-53;
        var prev=addRenderableWidget(Button.builder(Component.literal("<"),b->{index--;scrollRow=0;rebuildWidgets();}).bounds(x,y,25,20).build());prev.active=index>0;
        var next=addRenderableWidget(Button.builder(Component.literal(">"),b->{index++;scrollRow=0;rebuildWidgets();}).bounds(x+29,y,25,20).build());next.active=index<draft.size()-1;
        var add=addRenderableWidget(Button.builder(Component.literal("Add Bonus"),b->{draft.add(new BonusRule());index=draft.size()-1;scrollRow=0;rebuildWidgets();}).bounds(x+62,y,(w-70)/2,20).build());add.active=!ConfigUi.readOnly();
        var remove=addRenderableWidget(Button.builder(Component.literal("Remove"),b->{draft.remove(index);index=Math.max(0,index-1);scrollRow=0;rebuildWidgets();}).bounds(x+66+(w-70)/2,y,(w-70)/2,20).build());remove.active=!ConfigUi.readOnly()&&!draft.isEmpty();
        addRenderableWidget(Button.builder(Component.literal("Back"),b->onClose()).bounds(x,height-28,100,20).build());
        var save=addRenderableWidget(Button.builder(Component.literal("Save & Back"),b->save()).bounds(x+w-120,height-28,120,20).build());save.active=!ConfigUi.readOnly();
    }
    private void save(){
        try{
            for(var rule:draft){
                double amount=Double.parseDouble(amounts.getOrDefault(rule,Double.toString(rule.amount)).trim());
                if(!Double.isFinite(amount)||Math.abs(amount)>1000000)throw new IllegalArgumentException("Enter a finite amount between -1000000 and 1000000");
                if("effect".equals(rule.type)&&(amount<1||amount>255||amount!=Math.rint(amount)))throw new IllegalArgumentException("Effect level must be a whole number from 1 to 255");
                var id=Identifier.tryParse(rule.id);if(id==null||("attribute".equals(rule.type)?!BuiltInRegistries.ATTRIBUTE.containsKey(id):!BuiltInRegistries.MOB_EFFECT.containsKey(id)))throw new IllegalArgumentException("Choose a registered effect or attribute");
                rule.amount=amount;
            }
            var old=groups().put(trimId,draft);
            try{TrimEffectsConfigManager.save();}catch(java.io.IOException e){if(old==null)groups().remove(trimId);else groups().put(trimId,old);throw e;}
            rootScreen.markChangesSaved();minecraft.gui.setScreen(parent);
        }catch(Exception e){status=Component.literal(e.getMessage()==null?"Could not save settings":e.getMessage());}
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        super.extractRenderState(g,mx,my,delta);g.centeredText(font,title,width/2,12,0xFFFFFFFF);
        g.centeredText(font,Component.literal(ConfigUi.readOnly()?"Server settings • read-only":draft.isEmpty()?"No additional bonuses configured":"Current "+("attribute".equals(current().type)?"attribute: ":"effect: ")+EffectGuide.name(current().id,"attribute".equals(current().type))),width/2,28,0xFFAAAAAA);
        if(draft.isEmpty()){
            ConfigUi.text(g,font,"Currently no additional effects or attributes.",listX,61,listWidth,2,0xFFFFE0A1);
            ConfigUi.text(g,font,"Choose Add Bonus to create an effect or an attribute modifier.",listX,90,listWidth,3,0xFFAAAAAA);
        }else for(int row=scrollRow;row<Math.min(5,scrollRow+visibleRows);row++){
            int y=listTop+(row-scrollRow)*rowHeight;var rule=current();boolean attribute="attribute".equals(rule.type);
            String label=switch(row){case 0->"Enable this bonus";case 1->"Bonus type";case 2->EffectGuide.name(rule.id,attribute);case 3->attribute?"Attribute amount":"Status effect level";default->"Attribute units";};
            String help=switch(row){
                case 0->"Requires 4 pieces of the set, sharing the same "+(material?"material.":"pattern.");
                case 1->"Effects apply a status; attributes change a stat. Changing type resets the entry.";
                case 2->attribute?"Search stat names or registry IDs, including installed mods.":EffectGuide.guide(rule.id);
                case 3->attribute?"A negative amount applies a penalty. The selected units control the calculation.":"Use a whole level from 1 to 255. Higher vanilla levels are allowed.";
                default->attribute?"Percent multiplies the base stat; Flat adds directly to the stat.":"Units do not apply to status effects; the number above is their level.";
            };
            ConfigUi.text(g,font,label,listX+4,y+6,listWidth-140,2,0xFFFFE0A1);
            ConfigUi.text(g,font,help,listX+4,y+30,listWidth-8,3,0xFFA8B8BF);
        }
        drawScrollbar(g);ConfigUi.text(g,font,status.getString().isEmpty()&&!draft.isEmpty()?"Bonus "+(index+1)+" / "+draft.size()+" • Requires 4 pieces":status.getString(),listX,height-69,listWidth,1,0xFFFFCC77);
    }
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
