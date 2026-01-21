package com.example.addon.Main.modules.PacketMine.enums;

public enum RenderEnum {
    InAndOut("In And Out"),
    Grow("Grow");

    private final String title;

    private RenderEnum(String title) {
        this.title = title;
    }
    @Override
    public String toString() {
        return this.title;
    }
}
