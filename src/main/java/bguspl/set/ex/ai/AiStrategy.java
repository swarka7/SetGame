package bguspl.set.ex.ai;

import bguspl.set.Util;
import bguspl.set.ex.TableView;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public interface AiStrategy {

    List<Integer> chooseSlots(TableView view, Util util);

    long actionDelayMs(ThreadLocalRandom random);

    long tapSpacingMs(ThreadLocalRandom random);
}
