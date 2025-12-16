package bguspl.set.ex.ai;

import bguspl.set.Config;
import bguspl.set.Util;
import bguspl.set.UtilImpl;
import bguspl.set.ex.TableView;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiStrategyTest {

    @Test
    void hardChoosesExistingSet() {
        Properties properties = new Properties();
        properties.put("FeatureCount", "4");
        properties.put("FeatureSize", "3");
        Config config = new Config(Logger.getAnonymousLogger(), properties);
        Util util = new UtilImpl(config);

        Integer[] slotToCard = new Integer[]{0, 1, 2, null, null, null, null, null, null, null, null, null};
        Integer[] cardToSlot = new Integer[config.deckSize];
        cardToSlot[0] = 0;
        cardToSlot[1] = 1;
        cardToSlot[2] = 2;

        TableView view = new TableView(slotToCard, cardToSlot);
        HardAiStrategy strategy = new HardAiStrategy();

        List<Integer> slots = strategy.chooseSlots(view, util);

        assertEquals(3, slots.size());
        assertTrue(slots.containsAll(Arrays.asList(0, 1, 2)));
    }

    @Test
    void easyReturnsUpToThreeSlots() {
        Config config = new Config(Logger.getAnonymousLogger(), (String) null);
        Util util = new UtilImpl(config);

        Integer[] slotToCard = new Integer[]{0, 1, 2, 3, 4, null, null, null, null, null, null, null};
        Integer[] cardToSlot = new Integer[config.deckSize];
        for (int i = 0; i < 5; i++) {
            cardToSlot[i] = i;
        }

        TableView view = new TableView(slotToCard, cardToSlot);
        EasyAiStrategy strategy = new EasyAiStrategy();

        List<Integer> slots = strategy.chooseSlots(view, util);
        assertTrue(slots.size() <= 3);
        assertTrue(view.slotsWithCards().containsAll(slots));
    }
}
