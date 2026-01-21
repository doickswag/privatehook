/*
package com.example.addon.Loader.test;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HWIDLogin {

    public static boolean run(String s) {
        try {
            new PrintWriter(socket.getOutputStream(), true).println("hwid=" + s);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            if (sb.toString().contains("Successfully processed HWID!")) {
                String[] split = sb.toString().split("\\(")[1].split(":");
                try {
                    socket.close();
                    return true;
                } catch (Throwable t) {
                    try {
                        socket.close();
                    }
                    catch (Throwable exception) {
                        t.addSuppressed(exception);
                    }
                    throw t;
                }
            }
        }
        catch (IOException ignored) {}
        return false;
    }

    private static void login() {
        if (!run(HWIDGrabber.getHWID())) {
            String hwid = HWIDGrabber.getHWIDWithWin();
            try {
                Thread.sleep(2500L);
            }
            catch (InterruptedException cause) {
                throw new RuntimeException(cause);
            }
            if (hwid != null) {
                run(hwid);
            }
        }
    }
}

 */
