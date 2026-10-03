package com.faefluffkrist.trimworks;

public record TrimEffectInfo(String translationKey, int maxLevel, int piecesForMax, boolean alwaysMaxLevel) {
    public int levelForPieces(int pieces) {
        if (pieces <= 0) return 0;
        if (alwaysMaxLevel) return maxLevel;
        return Math.min(pieces, maxLevel);
    }
}
