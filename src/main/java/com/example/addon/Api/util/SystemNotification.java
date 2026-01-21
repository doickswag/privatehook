package com.example.addon.Api.util;

import meteordevelopment.meteorclient.MeteorClient;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Objects;

public final class SystemNotification {

    private static TrayIcon trayIcon;
    private static boolean initialized = false;
    private static boolean supported = false;

    private SystemNotification() {
    }

    private static void initialize() {
        if (initialized) return;

        try {
            if (!SystemTray.isSupported()) {
                return;
            }
            SystemTray tray = SystemTray.getSystemTray();
            Image image = null;
            try (InputStream is = SystemNotification.class.getResourceAsStream("/assets/privatehook/icon.png")) {
                image = ImageIO.read(Objects.requireNonNull(is));
            } catch (Exception e) {
                image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            }

            trayIcon = new TrayIcon(image, "PrivateHook");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
            supported = true;

        } catch (Throwable e) {
            supported = false;
        } finally {
            initialized = true;
        }
    }

    public static void send(String title, String message) {
        send(title, message, TrayIcon.MessageType.NONE);
    }

    public static void send(String title, String message, TrayIcon.MessageType type) {
        if (!initialized) {
            initialize();
        }
        if (supported) {
            trayIcon.displayMessage(title, message, type);
        }
    }
}
