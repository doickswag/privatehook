package com.example.addon.Api.util;

import java.text.DecimalFormat;

public class CalculationUtil {
    private long startTime;
    private String calculationTime;

    // DecimalFormat instance to format the elapsed time
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public CalculationUtil() {
        // Initialize the timer and calculationTime
        reset();
    }

    /**
     * Starts or restarts the timer.
     */
    public void start() {
        startTime = System.nanoTime();
    }

    /**
     * Calculates the elapsed time in milliseconds since the timer was started.
     * The elapsed time is formatted to two decimal places.
     *
     * @return a formatted string representing the elapsed time in ms.
     */
    public String calculate() {
        double elapsedMillis = (System.nanoTime() - startTime) / 1_000_000.0;
        calculationTime = decimalFormat.format(elapsedMillis) + "ms";
        return calculationTime;
    }

    /**
     * Gets the current formatted calculation time.
     *
     * @return calculationTime in the format "0.00ms".
     */
    public String getCalculationTime() {
        return calculationTime;
    }

    /**
     * Resets the timer and sets the calculation time to "0.00ms".
     */
    public void reset() {
        calculationTime = "0.00ms";
        startTime = System.nanoTime();
    }
}
