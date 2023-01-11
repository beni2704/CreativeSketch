package com.binus.creativesketch;

import android.graphics.Path;

public class CustomPath extends Path {
    private int color;
    private float brushThick;

    public CustomPath(int color, float brushThick) {
        this.color = color;
        this.brushThick = brushThick;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setBrushThick(float brushThick) {
        this.brushThick = brushThick;
    }

    public int getColor() {
        return color;
    }

    public float getBrushThick() {
        return brushThick;
    }
}
