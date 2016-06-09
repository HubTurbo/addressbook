package address.events;

import javafx.scene.input.KeyEvent;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Indicates a key event occurred that is potentially a keyboard shortcut
 */
public class PotentialKeyboardShortcutEvent extends BaseEvent{

    /** The key event */
    public KeyEvent keyEvent;

    /** The time that the Key event occurred */
    public long time;

    public PotentialKeyboardShortcutEvent(KeyEvent keyEvent){
        this.time = System.nanoTime();
        this.keyEvent = keyEvent;
    }

    /**
     * Returns the elapsed time between the given two events.
     * @param firstEvent
     * @param secondEvent
     * @return elapsed time in milli seconds.
     */
    public static long elapsedTimeInMilliseconds(PotentialKeyboardShortcutEvent firstEvent,
                                                 PotentialKeyboardShortcutEvent secondEvent){
        long durationInNanoSeconds = secondEvent.time - firstEvent.time;
        long elapsedTimeInMilliseconds = MILLISECONDS.convert(durationInNanoSeconds, NANOSECONDS);
        assert elapsedTimeInMilliseconds >= 0;
        return elapsedTimeInMilliseconds;
    }

    @Override
    public String toString(){
        final String className = this.getClass().getSimpleName();
        return className + " : keyEvent is " + keyEvent.getCode();
    }
}
