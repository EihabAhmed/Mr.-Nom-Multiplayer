package com.bbk.mrnommultiplayer;

/**
 * Created by eihab on 23/09/16.
 */

public class BonusStain extends Stain {
    public static final int GREEN_STAIN = 0;
    public static final int RED_STAIN = 1;
    public int bonusType;

    public BonusStain(int x, int y, int type, int bonusType) {
        super(x, y, type);
        this.bonusType = bonusType;
    }
}
