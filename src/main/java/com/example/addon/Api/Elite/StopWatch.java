package com.example.addon.Api.Elite;

import com.example.addon.Api.util.MathUtils;

public interface StopWatch {
    /**
     * gets a point in time
     *
     * @return - time in long
     */
    long getTimePoint();

    /**
     * sets the point in time we are at
     *
     * @param ms - long in
     */
    void setTimePoint(long ms);

    /**
     * time passed
     *
     * @return - long of time passed since
     */
    default long getPassed() {
        return MathUtils.getPassedTimeSince(getTimePoint());
    }

    /**
     * check if time > input
     *
     * @param ms - input
     * @return - true/false
     */
    default boolean hasBeen(long ms) {
        return MathUtils.isTimePointOlderThan(this.getTimePoint(), ms);
    }

    /**
     * resets time
     */
    default void reset() {
        setTimePoint(MathUtils.getMilli());
    }

    /**
     * when you don't use it in a thread.
     */
    class Single implements StopWatch {
        private long timePoint;

        @Override
        public long getTimePoint() {
            return timePoint;
        }

        @Override
        public void setTimePoint(long ms) {
            this.timePoint = ms;
        }
    }

    /**
     * for multiple threads.
     */
    class Multi implements StopWatch {
        private volatile long timePoint;

        @Override
        public long getTimePoint() {
            return timePoint;
        }

        @Override
        public void setTimePoint(long ms) {
            this.timePoint = ms;
        }
    }
}
