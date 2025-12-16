package bguspl.set.ex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable snapshot of the table state for AI consumption.
 */
public class TableView {

    private final int[] slotToCard;
    private final int[] cardToSlot;

    public TableView(Integer[] slotToCard, Integer[] cardToSlot) {
        this.slotToCard = new int[slotToCard.length];
        for (int i = 0; i < slotToCard.length; i++) {
            this.slotToCard[i] = slotToCard[i] == null ? -1 : slotToCard[i];
        }
        this.cardToSlot = new int[cardToSlot.length];
        for (int i = 0; i < cardToSlot.length; i++) {
            this.cardToSlot[i] = cardToSlot[i] == null ? -1 : cardToSlot[i];
        }
    }

    public List<Integer> slotsWithCards() {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < slotToCard.length; i++) {
            if (slotToCard[i] >= 0) slots.add(i);
        }
        return slots;
    }

    public List<Integer> cardsOnTable() {
        List<Integer> cards = new ArrayList<>();
        for (int card : slotToCard) {
            if (card >= 0) cards.add(card);
        }
        return cards;
    }

    public int cardAtSlot(int slot) {
        if (slot < 0 || slot >= slotToCard.length) return -1;
        return slotToCard[slot];
    }

    public int slotForCard(int card) {
        if (card < 0 || card >= cardToSlot.length) return -1;
        return cardToSlot[card];
    }

    public int tableSize() {
        return slotToCard.length;
    }

    public List<Integer> slotsForCards(int[] cards) {
        if (cards == null) return Collections.emptyList();
        List<Integer> slots = new ArrayList<>();
        for (int card : cards) {
            int slot = slotForCard(card);
            if (slot >= 0) slots.add(slot);
        }
        return slots;
    }
}
