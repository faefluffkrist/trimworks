package com.faefluffkrist.trimworks.configscreen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.Consumer;

/** Suggestions resolve translated names or registry IDs to actual registered entries. */
final class RegistryPickerScreen extends ScrollingConfigScreen {
    private final Screen parent;
    private final boolean attribute;
    private final Consumer<String> selected;
    private String query="";
    private boolean refresh;
    private List<String> matches=List.of();
    RegistryPickerScreen(Screen parent,boolean attribute,Consumer<String> selected){
        super(Component.literal(attribute?"Choose an Attribute":"Choose a Status Effect"));this.parent=parent;this.attribute=attribute;this.selected=selected;
    }
    @Override protected void init(){
        int w=Math.min(440,width-48),x=width/2-w/2;
        var search=addRenderableWidget(new EditBox(font,x,43,w,20,Component.literal("Search names or registry IDs")));
        search.setMaxLength(160);search.setValue(query);
        search.setResponder(value->{query=value;scrollRow=0;refresh=true;});setFocused(search);
        var ids=new ArrayList<String>();
        if(attribute)for(var value:BuiltInRegistries.ATTRIBUTE)ids.add(BuiltInRegistries.ATTRIBUTE.getKey(value).toString());
        else for(var value:BuiltInRegistries.MOB_EFFECT)ids.add(BuiltInRegistries.MOB_EFFECT.getKey(value).toString());
        String needle=query.toLowerCase(Locale.ROOT).trim();
        matches=ids.stream().filter(id->id.toLowerCase(Locale.ROOT).contains(needle)||EffectGuide.name(id,attribute).toLowerCase(Locale.ROOT).contains(needle)).sorted(Comparator.comparing(id->EffectGuide.name(id,attribute))).toList();
        layoutRows(matches.size(),82,height-45,43,440);
        for(int i=scrollRow;i<Math.min(matches.size(),scrollRow+visibleRows);i++){
            String id=matches.get(i);int y=listTop+(i-scrollRow)*rowHeight;
            addRenderableWidget(Button.builder(Component.literal(EffectGuide.name(id,attribute)),b->{selected.accept(id);minecraft.gui.setScreen(parent);}).bounds(listX,y,listWidth,20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Back"),b->onClose()).bounds(width/2-50,height-28,100,20).build());
    }
    @Override public void tick(){super.tick();if(refresh){refresh=false;rebuildWidgets();}}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        super.extractRenderState(g,mx,my,delta);g.centeredText(font,title,width/2,12,0xFFFFFFFF);
        g.centeredText(font,Component.literal("Type a name, minecraft:, or a mod namespace"),width/2,29,0xFFAAAAAA);
        if(matches.isEmpty())g.centeredText(font,Component.literal("No matching registered entries"),width/2,90,0xFFAAAAAA);
        for(int i=scrollRow;i<Math.min(matches.size(),scrollRow+visibleRows);i++){
            String id=matches.get(i);int y=listTop+(i-scrollRow)*rowHeight;
            ConfigUi.text(g,font,id,listX+5,y+23,listWidth-10,1,0xFFA8B8BF);
            if(!attribute)ConfigUi.text(g,font,EffectGuide.guide(id),listX+5,y+33,listWidth-10,1,0xFF939393);
        }
        drawScrollbar(g);
    }
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
