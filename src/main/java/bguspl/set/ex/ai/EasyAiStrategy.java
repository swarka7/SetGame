package bguspl.set.ex.ai;

import bguspl.set.Util;
import bguspl.set.ex.TableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Low-skill AI: randomly samples up to 3 slots that currently hold cards.
 */
public class EasyAiStrategy implements AiStrategy {

    private static final long MIN_DELAY = 700;
    private static final long MAX_DELAY = 1200;
    private static final long MIN_TAP = 120;
    private static final long MAX_TAP = 240;

    @Override
    public List<Integer> chooseSlots(TableView view, Util util) {
        List<Integer> slots = new ArrayList<>(view.slotsWithCards());
        Collections.shuffle(slots);
        if (slots.size() > 3) {
            slots = new ArrayList<>(slots.subList(0, 3));
        }
        return slots;
    }

    @Override
    public long actionDelayMs(ThreadLocalRandom random) {
        return between(random, MIN_DELAY, MAX_DELAY);
    }

    @Override
    public long tapSpacingMs(ThreadLocalRandom random) {
        return between(random, MIN_TAP, MAX_TAP);
    }

    protected long between(ThreadLocalRandom random, long min, long max) {
        if (max <= min) return min;
        return random.nextLong(min, max + 1);
    }
}
