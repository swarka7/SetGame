package bguspl.set.ex.ai;

import bguspl.set.Util;
import bguspl.set.ex.TableView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mid-skill AI: tries to find a legal set, otherwise falls back to random picks.
 */
public class MediumAiStrategy extends EasyAiStrategy {

    private static final long MIN_DELAY = 150;
    private static final long MAX_DELAY = 350;
    private static final long MIN_TAP = 80;
    private static final long MAX_TAP = 140;

    @Override
    public List<Integer> chooseSlots(TableView view, Util util) {
        if (util != null) {
            List<int[]> sets = util.findSets(view.cardsOnTable(), 3);
            if (sets != null && !sets.isEmpty()) {
                for (int[] set : sets) {
                    List<Integer> slots = view.slotsForCards(set);
                    if (slots.size() == util.cardToFeatures(set[0]).length) {
                        return slots;
                    }
                    if (!slots.isEmpty()) return slots;
                }
            }
        }
        List<Integer> fallback = new ArrayList<>(super.chooseSlots(view, util));
        return fallback;
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
