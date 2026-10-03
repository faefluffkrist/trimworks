package com.faefluffkrist.trimworks.advancement;

/** Pure calculations shared by event hooks and progression checks. */
public final class AdvancementProgressRules {
    private AdvancementProgressRules() {}
    public record Distance(int centimeters,double remainder) {}
    public static int increment(int current,int amount,int goal) {
        if(amount<=0)return 0;
        return Math.min(Math.max(0,goal-current),amount);
    }
    public static int earnedLevels(int before,int after,int xpAward) {
        return xpAward>0?Math.max(0,after-before):0;
    }
    public static Distance distance(double dx,double dy,double dz,double remainder) {
        double blocks=Math.sqrt(dx*dx+dy*dy+dz*dz);
        if(!Double.isFinite(blocks)||blocks>64)return new Distance(0,0);
        double centimeters=blocks*100+remainder;
        int whole=(int)Math.floor(centimeters+1.0E-9);
        return new Distance(whole,Math.max(0,centimeters-whole));
    }
    public static boolean distantMark(boolean samePlayer,long firstTick,long expiry,long now,
                                      boolean glowing,double distanceSquared) {
        return samePlayer&&firstTick<now&&expiry>now&&glowing&&distanceSquared>=400;
    }
}
