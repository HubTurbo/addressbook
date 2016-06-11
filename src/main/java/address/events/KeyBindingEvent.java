package address.events;

import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.Optional;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Indicates a key event occurred that is potentially a key binding being used
 */
public class KeyBindingEvent extends BaseEvent{

    /** The key event that triggered this event*/
    public Optional<KeyEvent> keyEvent = Optional.empty();

    /** A key combination that could have triggered this event */
    public Optional<KeyCombination> keyCombination = Optional.empty();

    /** The time that the Key event occurred */
    public long time;

    public KeyBindingEvent(KeyEvent keyEvent){
        this.time = System.nanoTime();
        this.keyEvent = Optional.of(keyEvent);
    }

    public KeyBindingEvent(KeyCombination keyCombination){
        this.time = System.nanoTime();
        this.keyCombination = Optional.of(keyCombination);
    }

    /**
     * Returns the elapsed time between the given two events.
     * @param firstEvent
     * @param secondEvent
     * @return elapsed time in milli seconds.
     */
    public static long elapsedTimeInMilliseconds(KeyBindingEvent firstEvent,
                                                 KeyBindingEvent secondEvent){
        long durationInNanoSeconds = secondEvent.time - firstEvent.time;
        long elapsedTimeInMilliseconds = MILLISECONDS.convert(durationInNanoSeconds, NANOSECONDS);
        assert elapsedTimeInMilliseconds >= 0;
        return elapsedTimeInMilliseconds;
    }

    @Override
    public String toString(){
        final String className = this.getClass().getSimpleName();
        return className + " " + ( keyEvent.isPresent()? keyEvent.get(): keyCombination.get().getDisplayText());
    }

    public boolean isMatching(KeyCombination potentialMatch){
        if (keyEvent.isPresent()){
            return potentialMatch.match(keyEvent.get());
        } else {
            return potentialMatch.equals(keyCombination.get());
        }
    }
}
