package com.faefluffkrist.trimworks.configscreen;

import com.faefluffkrist.trimworks.config.TrimEffectsConfigManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.lang.reflect.Field;
import java.util.*;

final class BonusSettingsScreen extends ScrollingConfigScreen {
    private final Screen parent;
    private final TrimEffectsConfigScreen rootScreen;
    private final boolean material;
    private final String id;
    private final Object config;
    private final List<Field> fields;
    private final Map<Field, String> values = new LinkedHashMap<>();
    private final Map<Field, EditBox> inputs = new LinkedHashMap<>();
    private Component status = Component.empty();
    BonusSettingsScreen(Screen parent, TrimEffectsConfigScreen rootScreen, boolean material, String id) {
        super(Component.literal(TrimEffectsConfigScreen.pretty(id) + (material ? " • Material Bonuses" : " • Full Set Bonus")));
        this.parent = parent; this.rootScreen = rootScreen; this.material = material; this.id = id;
        var root = readOnly() ? TrimEffectsConfigManager.getDisplayConfig() : TrimEffectsConfigManager.getServerConfig();
        config = material ? root.materialBonuses : root.builtInBonuses;
        String path = id.substring(id.indexOf(':') + 1);
        String prefix = path.equals("resin") ? "amber" : path;
        fields = Arrays.stream(config.getClass().getFields()).filter(f -> id.startsWith("minecraft:") && f.getName().startsWith(prefix) && !f.getName().equals("extraBonuses")).toList();
        try { for (Field f : fields) values.put(f, f.getName().endsWith("Chance") ? Double.toString(f.getDouble(config) * 100.0D) : String.valueOf(f.get(config))); }
        catch (IllegalAccessException e) { throw new IllegalStateException(e); }
    }
    private boolean readOnly() { return TrimEffectsConfigManager.hasServerSync() && !net.minecraft.client.Minecraft.getInstance().hasSingleplayerServer(); }
    private void capture() { inputs.forEach((f, box) -> values.put(f, box.getValue())); }
    @Override protected void init() {
        capture(); inputs.clear();
        layoutRows(fields.size(),48,height-82,70,420);
        for(int i=scrollRow;i<Math.min(fields.size(),scrollRow+visibleRows);i++) {
            Field field=fields.get(i); int y=listTop+(i-scrollRow)*rowHeight;
            var info=BonusOptionInfo.forField(field.getName());
            if(field.getType()==boolean.class) {
                var button=addRenderableWidget(Button.builder(ConfigUi.toggle(Boolean.parseBoolean(values.get(field))),b->{
                    capture();values.put(field,Boolean.toString(!Boolean.parseBoolean(values.get(field))));rebuildWidgets();
                }).bounds(listX+listWidth-75,y+3,75,20).build());button.active=!readOnly();
            } else {
                EditBox box=addRenderableWidget(new EditBox(font,listX+listWidth-90,y+3,90,20,Component.literal(info.label())));
                box.setMaxLength(18);box.setValue(values.get(field));box.setEditable(!readOnly());inputs.put(field,box);
            }
        }
        addRenderableWidget(Button.builder(Component.literal("Extra effects & attributes"),b->{capture();minecraft.gui.setScreen(new ExtraBonusesScreen(this,rootScreen,material,id));}).bounds(width/2-120,height-53,240,20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"),b->onClose()).bounds(listX,height-28,100,20).build());
        var save=addRenderableWidget(Button.builder(Component.literal("Save & Back"),b->save()).bounds(listX+listWidth-120,height-28,120,20).build());save.active=!readOnly();
    }
    @Override protected void beforeScroll(){capture();}
    private void save() {
        capture(); var parsed=new LinkedHashMap<Field,Object>(); var old=new LinkedHashMap<Field,Object>();
        try {
            for(Field f:fields) {
                Object value;
                if(f.getType()==boolean.class) value=Boolean.parseBoolean(values.get(f));
                else {
                    double n=Double.parseDouble(values.get(f).trim());
                    String name=f.getName();
                    if(!Double.isFinite(n)||Math.abs(n)>1000000) throw new IllegalArgumentException("Enter a finite amount between -1000000 and 1000000");
                    if(name.endsWith("Level") && (n<1 || n>255)) throw new IllegalArgumentException("Effect levels must be 1–255");
                    if(name.equals("amethystLevelIncrease") && (n < -254 || n > 254)) throw new IllegalArgumentException("Enchantment change must be -254–254");
                    if((name.endsWith("Ticks")||name.endsWith("Interval")) && n<1) throw new IllegalArgumentException("Time values must be positive ticks");
                    if(name.endsWith("Chance")) { if(n<0||n>100) throw new IllegalArgumentException("Chance must be 0–100%"); n /= 100.0D; }
                    if(name.equals("lapisExperiencePercent") && n < -100) throw new IllegalArgumentException("XP penalty cannot exceed 100%");
                    if(f.getType()==int.class) { if(n!=Math.rint(n)) throw new IllegalArgumentException("Use a whole number"); value=(int)n; }
                    else value=n;
                }
                parsed.put(f,value); old.put(f,f.get(config));
            }
            for(var entry:parsed.entrySet()) entry.getKey().set(config,entry.getValue());
            try { TrimEffectsConfigManager.save(); }
            catch(java.io.IOException e) { for(var entry:old.entrySet()) entry.getKey().set(config,entry.getValue()); throw e; }
            rootScreen.markChangesSaved(); minecraft.gui.setScreen(parent);
        } catch(Exception e) { status=Component.literal(e.getMessage()==null?"Could not save":e.getMessage()); }
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
        super.extractRenderState(g,mx,my,delta);g.centeredText(font,title,width/2,12,0xFFFFFFFF);
        g.centeredText(font,Component.literal(readOnly()?"Server settings • read-only":(material ? "Material settings • independent options" : "Full-set settings • independent options")),width/2,28,0xFFAAAAAA);
        if(fields.isEmpty()){
            ConfigUi.text(g,font,material?"Currently no built-in material bonus.":"Currently no built-in full-set bonus.",listX,61,listWidth,2,0xFFFFE0A1);
            ConfigUi.text(g,font,"You can add effects or attributes below.",listX,90,listWidth,2,0xFFAAAAAA);
        }
        for(int i=scrollRow;i<Math.min(fields.size(),scrollRow+visibleRows);i++) {
            var field=fields.get(i);int y=listTop+(i-scrollRow)*rowHeight;var info=BonusOptionInfo.forField(field.getName());
            String label=info.label()+(field.getName().endsWith("Chance")?" (%)":"");
            ConfigUi.text(g,font,label,listX+4,y+6,listWidth-105,2,0xFFFFE0A1);
            ConfigUi.text(g,font,info.explanation(),listX+4,y+30,listWidth-8,3,0xFFA8B8BF);
        }
        drawScrollbar(g);
        ConfigUi.text(g,font,status.getString(),listX,height-69,listWidth,1,0xFFFFCC77);
    }
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
