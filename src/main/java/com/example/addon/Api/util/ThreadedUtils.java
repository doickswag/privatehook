package com.example.addon.Api.util;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.entity.LivingEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadedUtils {
    public static ExecutorService aaExecutor;
    public static ExecutorService antiCityExecutor;

    public static void init() {
        aaExecutor = Executors.newSingleThreadExecutor();
        antiCityExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public enum CalcType {
        Place, Break
    }

    public static class AaCalcs implements Runnable {
        public static int placeDelayLeft = 0;
        public static int breakDelayLeft = 0;
        private final CalcType type;

        public AaCalcs(CalcType type) {
            this.type = type;
        }


        @Override
        public void run() {
            switch (this.type) {
                case Place:
                    --placeDelayLeft;
                    if (placeDelayLeft <= 0) {
                    }
                case Break:
                    --breakDelayLeft;
                    if (breakDelayLeft <= 0) {
                    }
            }
        }
    }
}
