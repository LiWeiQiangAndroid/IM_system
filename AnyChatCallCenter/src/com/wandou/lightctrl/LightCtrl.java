package com.wandou.lightctrl;

public class LightCtrl {
    private int Light;
    protected int LightUpBound = 8;
    protected int LightDownBound = 0;

    public void Down(int minus) {
        if ((Light - minus) <= 0) {
            Light = 0;
        } else {
            Light = Light - minus;
        }

    }

    public int GetLight() {
        return Light;
    }

    public void LightCtrl() {
        Light = 0;
    }

    public void SetLight(int InputLight) {
        if (InputLight <= 8 || InputLight >= 0) {
            Light = InputLight;
        }
    }

    public void Up(int plus) {
        if ((Light + plus) >= 8) {
            Light = 8;
        } else {
            Light = Light + plus;
        }
    }
}