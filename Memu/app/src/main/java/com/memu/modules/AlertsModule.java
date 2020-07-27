package com.memu.modules;

import java.io.Serializable;

/**
 * Created by chanpyaeaung on 5/4/16.
 */
public class AlertsModule implements Serializable {

    private String tittle;

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    private int image;

}
