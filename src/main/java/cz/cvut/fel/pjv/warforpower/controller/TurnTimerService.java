package cz.cvut.fel.pjv.warforpower.controller;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

/**
 * Counts down seconds remaining in the current player's turn.
 * Can be paused during battle and resumed afterward.
 */
public class TurnTimerService extends ScheduledService<Integer> {
    public static final int TURN_SECONDS = 60;

    private int remaining = TURN_SECONDS;
    private boolean paused = false;

    public TurnTimerService() {
        setDelay(Duration.seconds(1));
        setPeriod(Duration.seconds(1));
    }

    /**
     * Resets timer to full duration. Call at the start of each turn.
     */
    public void resetTimer() {
        remaining = TURN_SECONDS;
        paused = false;
    }

    /**
     * Pauses countdown — call when battle starts.
     */
    public void pause() {
        paused = true;
    }

    /**
     * Resumes countdown — call when battle ends.
     */
    public void resume() {
        paused = false;
    }

    public int getRemaining() {
        return remaining;
    }

    @Override
    protected Task<Integer> createTask() {
        return new Task<>() {
            @Override
            protected Integer call() {
                if (!paused && remaining > 0) {
                    remaining--;
                }
                return remaining;
            }
        };
    }
}