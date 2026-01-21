package com.example.addon.Loader;

import com.example.addon.Loader.impl.Check;
import com.example.addon.Loader.impl.Checkers.ArgsCheck;
import com.example.addon.Loader.impl.Checkers.DumpCheck;
import com.example.addon.Loader.impl.Checkers.MusicChecker;
import com.example.addon.Loader.impl.Checkers.VMCheck;
import java.util.Random;

public final class Loader {

    private static long verificationKey = 0L;

    private static final Check[] CHECKS = new Check[]{
        new ArgsCheck(),
        new DumpCheck(),
        new VMCheck(),
      //  new MusicChecker()
    };

    private Loader() {}

    public static long check() {
        for (Check check : CHECKS) {
            check.run();
        }
        verificationKey = System.nanoTime() ^ new Random().nextLong();
        if (verificationKey == 0L) {
            verificationKey = 1L;
        }
        return verificationKey;
    }
    public static long getKey() {
        return verificationKey;
    }
}
