package com.example.addon.Loader.impl.Checkers;

import com.example.addon.Loader.impl.Check;
import java.lang.management.ManagementFactory;

public class ArgsCheck extends Check {

    public static String[] args = {"-XBootclasspath", "-javaagent", "-Xdebug", "-agentlib", "-Xrunjdwp", "-Xnoagent", "-verbose", "-DproxySet", "-DproxyHost", "-DproxyPort", "Xrunjdwp:", "noverify"};

    public ArgsCheck() {
    }

    @Override
    public void run() {
        try {
            for (String string : args) {
                if (ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains(string)) {
                    System.exit(0);
                }
                return;
            }
        } catch (Throwable throwable) {
            System.exit(0);
        }
    }
}
