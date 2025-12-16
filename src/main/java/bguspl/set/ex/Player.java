package bguspl.set.ex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import bguspl.set.Env;
import bguspl.set.ex.ai.AiStrategy;
import bguspl.set.ex.ai.AiStrategyFactory;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {
     protected LinkedBlockingQueue<Integer> actionQueue;
    public volatile List<Integer> Token;
    private  boolean penalty;
    public boolean penaltyChanged;
    private final Dealer dealer;
    public volatile boolean run;
    public int decision;
    private volatile boolean pause;
    private final String aiLevel;
    private final AiStrategy aiStrategy;

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate key presses).
     */
    private Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The current score of the player.
     */
    private int score;

    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided manually, via the keyboard).
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human, String aiLevel) {
        this.env = env;
        this.table = table;
        this.id = id;
        this.human = human;
        this.aiLevel = human ? null : aiLevel;
        this.aiStrategy = human ? null : AiStrategyFactory.forLevel(aiLevel);
        this.dealer=dealer;
        this.penalty=false;
        this.run=false;
         this.decision=0;
         this.pause=false;
        this.penaltyChanged=false;
        actionQueue=new LinkedBlockingQueue<Integer>();
        Token = new ArrayList<>();
    }

    /**
     * The main player thread of each player starts here (main loop for the player thread).
     */
    @Override
    public void run() {
        
        run=true;
        playerThread = Thread.currentThread();
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
        if (!human) createArtificialIntelligence(aiStrategy);
        while (!terminate) {
            // TODO implement main player loop
            
            int KeySlot = -1;
            try {
                KeySlot = actionQueue.take();
                if (terminate) {
                    continue;
                }

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block

                e.printStackTrace();
            }

            if (KeySlot == -1) {
                System.out.println("-1");
                return;
            }


            // Check what to do with the next keyslot to add it or to remove it or to do nothing
            if (table.slotToCard[KeySlot] != null) {
                penaltyChanged = false; // allow new selections after any penalty

                if (Token.contains((Object) (KeySlot))) {
                    table.removeToken(id, KeySlot);
                    dealer.Press[id][KeySlot] = false;
                    Token.remove((Object) KeySlot);
                } else if (Token.size() < 3 ) {
                    table.placeToken(id, KeySlot);
                    dealer.Press[id][KeySlot] = true;
                    Token.add(KeySlot);
                }
            }
            
            

            // Check with dealer the current set
            if (!penaltyChanged && Token.size() == 3) {
                try {
                    Thread.sleep(500); // brief display so player can see the third token
                } catch (InterruptedException ignored) {}
                if (Token.size() != 3 || penaltyChanged || terminate) continue;
               Token.add(id);
                pause=true;
                try {
                    dealer.PlayerSet.put(Token);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                synchronized (this) {
                    try {
                        wait();
                        if (terminate) {
                            continue;
                        }
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                    }
                }
                if (decision==1) {
                    point();
                    decision = 0;

                } else if (decision == -1) {
                    penalty();
                    decision = 0;
                }
               pause=false;
                Token.clear();
                if (!human)
                    aiThread.interrupt();
            }

      
        }
        if (!human) try { aiThread.join(); } catch (InterruptedException ignored) {}
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it is not full.
     */
    private void createArtificialIntelligence(AiStrategy strategy) {
        aiThread = new Thread(() -> {
            env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
            ThreadLocalRandom random = ThreadLocalRandom.current();
            while (!terminate) {
                try {
                    Thread.sleep(strategy.actionDelayMs(random));
                } catch (InterruptedException ignored) {
                    if (terminate) break;
                }

                if (pause || terminate) continue;
                TableView view = table.snapshot();
                List<Integer> plan = strategy.chooseSlots(view, env.util);
                if (plan == null || plan.isEmpty()) continue;

                for (int slot : plan) {
                    if (terminate || pause) break;
                    if (Token.contains(slot)) continue;
                    keyPressed(slot);
                    try {
                        Thread.sleep(strategy.tapSpacingMs(random));
                    } catch (InterruptedException ignored) {
                        if (terminate) break;
                    }
                }
            }
            env.logger.info("Thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        aiThread.start();
    }
     /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        // TODO implement
  
        terminate = true;
        synchronized (this) {
            notifyAll();
        }
        
        System.out.println("player " + id + " is terminated");
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {
       synchronized(this){
            if (table.slotToCard[slot] == null) {
                return;
            }
              if (  run && ! dealer.isChecking  && !penalty && dealer.canPress) {
                 try {
                    actionQueue.put(slot);
                    wait(5);
                 } catch (InterruptedException e) {
                }
            }
       }
    
        
}

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {
        
        int ignored = table.countCards(); // Just for demonstration in unit tests
        env.ui.setScore(id, ++score);
        
        penalty = true; // Activate penalty flag
        long pointDuration = env.config.pointFreezeMillis;
         
            while (pointDuration > 0) {
                env.ui.setFreeze(id,pointDuration);
                if (pointDuration < 1000 && pointDuration > 0) { // If the remaining freeze time is negligible (< 1 millisecond but positive), sleep for that time
                    synchronized(Thread.currentThread()){
                    try {
                    
                        Thread.sleep(pointDuration);
                        }
                    
                     catch (InterruptedException e) {
                    }
                    pointDuration=0;
                }
                
                }
        
            
                else
                {
                 synchronized (Thread.currentThread()){
                try {
                    Thread.sleep(1000); // Sleep for 1 second
                } catch (InterruptedException e) {
                }
            }
                pointDuration -= 1000; // Decrease the remaining freeze time by 1 second
            }
            
        }
        env.ui.setFreeze(id, 0); // Reset the freeze indicator
        penalty = false; // Deactivate penalty flag after freeze time is over
        penaltyChanged = false;
    }
    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() {
        penalty = true; // Activate penalty flag
        penaltyChanged=true;
        long penaltyDuration = env.config.penaltyFreezeMillis;
            
            while (penaltyDuration > 0) {
                env.ui.setFreeze(id,penaltyDuration);

                if (penaltyDuration < 1000 && penaltyDuration > 0) { // If the remaining freeze time is negligible (< 1 millisecond but positive), sleep for that time
                    synchronized(Thread.currentThread()){

                    
                    try {
                        
                        Thread.sleep(penaltyDuration);
                    
                    } catch (InterruptedException e) {
                    }
                    
                    penaltyDuration=0;
                   
                }
                
                
                }
                else
                {
                    synchronized(Thread.currentThread()){

                try {
                    Thread.sleep(1000); // Sleep for 1 second
                } catch (InterruptedException e) {
                }
            }
               
            }
            penaltyDuration -= 1000; // Decrease the remaining freeze time by 1 second
            
        }
        env.ui.setFreeze(id, 0); // Reset the freeze indicator
        penalty = false; // Deactivate penalty flag after freeze time is over
    }

    public int score() {
        return score;
    }
    public void RemoveFromSet(int slot) {
        Token.remove((Object)slot);
    }
    public void setPenalty(boolean penalty){
        this.penalty=penalty;

    }
  
}
