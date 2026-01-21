package com.example.addon.Main.modules.PacketMine.enums;

public enum Swap {
    Normal("Silent"),
    Inv("InvSwap");

    private final String title;

    private Swap(String title) {
        this.title = title;
    }
    @Override
    public String toString() {
        return this.title;
    }
}
