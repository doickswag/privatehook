package com.example.addon.Api.util;

import meteordevelopment.meteorclient.utils.world.TickRate;
import net.minecraft.util.math.Vec3d;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets; // Added for consistent encoding
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class MathUtils {
    private long nanoTime = -1L;
    private static final double numbar = ThreadLocalRandom.current().nextDouble();
    public void reset() {
        nanoTime = System.nanoTime();
    }
    public static final long NANOS_PER_MS = TimeUnit.MILLISECONDS.toNanos(1L);

    public static double normalize(double value, double min, double max) {
        return (value - min) / (max - min);
    }

    public static float rad(final float angle) {
        return (float) (angle * Math.PI / 180);
    }

    public static double angle(Vec3d vec3d, Vec3d other) {
        double lengthSq = vec3d.length() * other.length();

        if (lengthSq < 1.0E-4D) {
            return 0.0;
        }

        double dot = vec3d.dotProduct(other);
        double arg = dot / lengthSq;

        if (arg > 1) {
            return 0.0;
        } else if (arg < -1) {
            return 180.0;
        }

        return Math.acos(arg) * 180.0f / Math.PI;
    }
    public static double square(double input) {
        return input * input;
    }

    public static int clamp(int num, int min, int max) {
        return num < min ? min : Math.min(num, max);
    }

    public static float clamp(float num, float min, float max) {
        return num < min ? min : Math.min(num, max);
    }

    public static double clamp(double num, double min, double max) {
        return num < min ? min : Math.min(num, max);
    }

    public static int getRandomNumber(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    public static int getNumber(int min, int max) {
        return (int) (numbar * (max - min + 1)) + min;
    }
    public static float roundFloat(double number, int scale) {
        BigDecimal bd = BigDecimal.valueOf(number);
        bd = bd.setScale(scale, RoundingMode.FLOOR);
        return bd.floatValue();
    }

    public static float lerp(float current, float target, float lerp) {
        current -= (current - target) * clamp(lerp, 0, 1);
        return current;
    }

    // Set Times
    public void setTicks(long ticks) { nanoTime = System.nanoTime() - convertTicksToNano(ticks); }
    public void setNano(long time) { nanoTime = System.nanoTime() - time; }
    public void setMicro(long time) { nanoTime = System.nanoTime() - convertMicroToNano(time); }
    public void setMillis(long time) { nanoTime = System.nanoTime() - convertMillisToNano(time); }
    public void setSec(long time) { nanoTime = System.nanoTime() - convertSecToNano(time); }


    // Get Times
    public long getTicks() { return convertNanoToTicks(nanoTime); }
    public long getNano() { return nanoTime; }
    public long getMicro() { return convertNanoToMicro(nanoTime); }
    public long getMillis() { return convertNanoToMillis(nanoTime); }
    public long getSec() { return convertNanoToSec(nanoTime); }


    // Passed Time
    public boolean passedTicks(long ticks) { return passedNano(convertTicksToNano(ticks)); }
    public boolean passedNano(long time) { return System.nanoTime() - nanoTime >= time; }
    public boolean passedMicro(long time) { return passedNano(convertMicroToNano(time)); }
    public boolean passedMillis(long time) { return passedNano(convertMillisToNano(time)); }
    public boolean passedSec(long time) { return passedNano(convertSecToNano(time)); }


    // Tick Conversions
    public long convertMillisToTicks(long time) { return time / 50; }
    public long convertTicksToMillis(long ticks) { return ticks * 50; }
    public long convertNanoToTicks(long time) { return convertMillisToTicks(convertNanoToMillis(time)); }
    public long convertTicksToNano(long ticks) { return convertMillisToNano(convertTicksToMillis(ticks)); }


    // All Conversions To Smaller
    public long convertSecToMillis(long time) { return time * 1000L; }
    public long convertSecToMicro(long time) { return convertMillisToMicro(convertSecToMillis(time)); }
    public long convertSecToNano(long time) { return convertMicroToNano(convertMillisToMicro(convertSecToMillis(time))); }

    public long convertMillisToMicro(long time) { return time * 1000L; }
    public long convertMillisToNano(long time) { return convertMicroToNano(convertMillisToMicro(time)); }

    public long convertMicroToNano(long time) { return time * 1000L; }


    // All Conversions To Larger
    public long convertNanoToMicro(long time) { return time / 1000L; }
    public long convertNanoToMillis(long time) { return convertMicroToMillis(convertNanoToMicro(time)); }
    public long convertNanoToSec(long time) { return convertMillisToSec(convertMicroToMillis(convertNanoToMicro(time))); }

    public long convertMicroToMillis(long time) { return time / 1000L; }
    public long convertMicroToSec(long time) { return convertMillisToSec(convertMicroToMillis(time)); }

    public long convertMillisToSec(long time) { return time / 1000L; }

    public static long getMilli() {
        return System.nanoTime() / NANOS_PER_MS;
    }

    public static boolean isTimePointOlderThan(long timePoint, long ms) {
        return getPassedTimeSince(timePoint) >= ms;
    }

    public static long getPassedTimeSince(long timePoint) {
        return getMilli() - timePoint;
    }
    // Syncs
    public static double getTPSMatch(boolean TPSSync) {
        return TPSSync ? (TickRate.INSTANCE.getTickRate() / 20.0) : 1.0;
    }
    public long millisPassed() {
        return convertNanoToMillis(System.nanoTime() - this.nanoTime);
    }
    public void setMs(long time) {
        this.nanoTime = System.nanoTime() - convertMillisToNano(time);
    }

}
