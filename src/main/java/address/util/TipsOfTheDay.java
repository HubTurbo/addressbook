package address.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * A collection of tips of the day
 */
public class TipsOfTheDay {
    private static final Random RANDOM_GENERATOR = new Random();

    public List<String> tipsOfTheDay = new ArrayList<>();

    public Optional<String> getATipOfTheDay() {
        if (tipsOfTheDay.size() == 0) {
            return Optional.empty();
        }

        return Optional.of(tipsOfTheDay.get(RANDOM_GENERATOR.nextInt(tipsOfTheDay.size())));
    }
}
