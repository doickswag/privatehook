package com.example.addon.Loader.impl.Checkers;

import com.example.addon.Loader.impl.Check;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import java.util.Arrays;
import java.util.List;

public class MusicChecker extends Check {
    private static final List<String> TAPPED_ARTISTS = Arrays.asList("nettspend", "bleood", "wifiskeleton", "zayguapkid", "ksuuvi", "xaviersobased", "osamason", "bladee", "chief keef", "yuke", "jaydes", "dream caster", "woody", "jack frost", "twotimer", "blueface", "lil tjay", "ken carson", "destroy lonley", "playboy carti", "ratbowl", "boolymon", "phreshboyswag", "maxon", "snakechildpain", "fimiguerrero", "brennan jones", "edward skeletrix", "lucki", "lifelessgarments");
    @Override
    public void run() {
        if (!isTappedIn()) {
            System.exit(0);
        }
    }
    private interface User32Extra extends User32 {
        User32Extra INSTANCE = Native.load("user32", User32Extra.class);

        interface WNDENUMPROC extends StdCallCallback {
            boolean callback(HWND hWnd, Pointer arg);
        }
        boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer arg);
        int GetWindowTextA(HWND hWnd, byte[] lpString, int nMaxCount);
    }
    private boolean isTappedIn() {
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) {
            return true;
        }
        final boolean[] foundMatch = {false};
        User32Extra.INSTANCE.EnumWindows((User32Extra.WNDENUMPROC) (hWnd, arg) -> {
            if (User32.INSTANCE.IsWindowVisible(hWnd)) {
                byte[] windowTextBuffer = new byte[512];
                User32Extra.INSTANCE.GetWindowTextA(hWnd, windowTextBuffer, 512);
                String windowTitle = Native.toString(windowTextBuffer).toLowerCase();

                if (!windowTitle.isEmpty()) {
                    for (String artist : TAPPED_ARTISTS) {
                        if (windowTitle.contains(artist.toLowerCase())) {
                            foundMatch[0] = true;
                            return false;
                        }
                    }
                }
            }
            return true;
        }, null);
        return foundMatch[0];
    }
}
