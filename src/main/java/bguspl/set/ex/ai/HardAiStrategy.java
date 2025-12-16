package bguspl.set.ex.ai;

import bguspl.set.Util;
import bguspl.set.ex.TableView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * High-skill AI: always hunts for an actual legal set and taps quickly.
 */
public class HardAiStrategy extends MediumAiStrategy {

    private static final long MIN_DELAY = 120;
    private static final long MAX_DELAY = 250;
    private static final long MIN_TAP = 50;
    private static final long MAX_TAP = 110;

    @Override
    public List<Integer> chooseSlots(TableView view, Util util) {
        if (util != null) {
            List<int[]> sets = util.findSets(view.cardsOnTable(), Integer.MAX_VALUE);
            if (sets != null && !sets.isEmpty()) {
                // Prefer the set with the lowest aggregate slot numbers (greedy but deterministic).
                int[] best = sets.stream()
                        .min(Comparator.comparingInt(set -> view.slotsForCards(set).stream().mapToInt(Integer::intValue).sum()))
                        .orElse(sets.get(0));
                List<Integer> slots = view.slotsForCards(best);
                if (!slots.isEmpty()) return slots;
            }
        }
        return new ArrayList<>(super.chooseSlots(view, util));
    }

    @Override
    public long actionDelayMs(ThreadLocalRandom random) {
        return between(random, MIN_DELAY, MAX_DELAY);
    }

    @Override
    public long tapSpacingMs(ThreadLocalRandom random) {
        return between(random, MIN_TAP, MAX_TAP);
    }
}
