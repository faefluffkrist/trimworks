package com.faefluffkrist.trimworks;

import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

/** Wrap only Trimworks-added lines. Keep numeric value/unit pairs together. */
final class TooltipLayout {
    private TooltipLayout() {}
    static List<String> lines(String text) {
        var result=new ArrayList<String>();
        for(String paragraph:text.split("\\n",-1)){
            if(paragraph.isBlank()){result.add("");continue;}
            String[] words=paragraph.trim().split("\\s+");
            var atoms=new ArrayList<String>();
            for(int i=0;i<words.length;i++){
                String atom=words[i];
                if(atom.matches("[+−-]?[0-9]+(?:\\.[0-9]+)?%?")){
                    // Values stay with their units; trim-piece requirements are one atom.
                    if(i+1<words.length&&words[i+1].equals("matching")){
                        atom+=" "+words[++i];
                        if(i+1<words.length&&words[i+1].equals("material"))atom+=" "+words[++i];
                        if(i+1<words.length)atom+=" "+words[++i];
                    }else if(i+1<words.length&&words[i+1].matches("pieces?:?|levels?|seconds?|ticks?|Knockback"))atom+=" "+words[++i];
                }else if(i+1<words.length&&words[i+1].matches("[IVX]+[),]?")&&atom.matches("[A-Za-z’']+"))atom+=" "+words[++i];
                atoms.add(atom);
            }
            String line="";int count=0;
            for(String atom:atoms){int length=atom.split(" ").length;
                if(!line.isEmpty()&&count+length>6){result.add(line);line="";count=0;}
                line+=(line.isEmpty()?"":" ")+atom;count+=length;
            }
            if(!line.isEmpty())result.add(line);
        }
        for(int i=0;i<result.size();i++)if((i>0||text.startsWith("  "))&&!result.get(i).isEmpty())result.set(i,"  "+result.get(i));
        return result;
    }
    static void wrapAddedLines(List<Component> tooltip,int start) {
        for(int i=tooltip.size()-1;i>=start;i--){
            var original=tooltip.get(i);var lines=lines(original.getString());
            tooltip.remove(i);
            for(int j=0;j<lines.size();j++)tooltip.add(i+j,Component.literal(lines.get(j)).setStyle(original.getStyle()));
        }
    }
}
