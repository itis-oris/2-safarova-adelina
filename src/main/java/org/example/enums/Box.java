package org.example.enums;

import java.awt.*;
import java.io.Serializable;

public enum Box implements Serializable {
    ZERO,
    NUM1,
    NUM2,
    NUM3,
    NUM4,
    NUM5,
    NUM6,
    NUM7,
    NUM8,
    BOMB,
    OPENED,
    CLOSED,
    FLAGED,
    BOMBED,
    NOBOMB;

    public Object image; // Основное изображение
    public static Image[] flagImages;


    public Box GetNextNumberBox() {
        return Box.values()[this.ordinal() + 1];
    }

    public int GetNumber() {
        return this.ordinal();
    }
}