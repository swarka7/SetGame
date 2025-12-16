package bguspl.set.ex;

import bguspl.set.Env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {
    public volatile boolean canPress;
    Thread TimerThread;
    public volatile boolean isChecking;
    protected volatile boolean Press[][];
    private List<Integer> FreeSlots;
    public LinkedBlockingQueue< List<Integer>> PlayerSet;
    private Thread[] playersThreads;
    private  boolean TimeToReset;
    private long Timer;
    private volatile List<Integer> DeletedCards;


    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;

    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
        FreeSlots = IntStream.range(0, env.config.tableSize).boxed().collect(Collectors.toList());
        playersThreads=new Thread[players.length];
        this.isChecking=false;
        this.canPress=true;
        this.terminate=false;
        this.TimeToReset=false;
        PlayerSet=new LinkedBlockingQueue<List<Integer>>();
        DeletedCards =new ArrayList<Integer>();
        Timer = env.config.turnTimeoutMillis;
        Press = new boolean[players.length][env.config.tableSize];
        for (int j = 0; j < players.length; j++) {
            for (int i = 0; i < env.config.tableSize; i++) {
                Press[j][i] = false;
            }

        }
        TimerThread=new Thread();

        
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
      
        TimerThread = new Thread(() -> {
            reshuffleTime=System.currentTimeMillis()+env.config.turnTimeoutMillis;
            Timer = reshuffleTime-System.currentTimeMillis();

            env.logger.info("thread Timer starting.");

            while (!terminate) {
                try {
                    synchronized (this) {
                        System.out.println("wait");
                        wait();
                    }
                } catch (InterruptedException e) {
                  
                }
               
               
                while (Timer > 5000) {
                    
                    env.ui.setCountdown(Timer, false);
                    Timer = Timer - 1000;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        
                    }

                    
                }
                while (Timer<=5000&&Timer >= 0) {
                    env.ui.setCountdown(Timer, true);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        
                    }
                    Timer = Timer - 10;

                }
                synchronized (this) {
                    notifyAll();
                }

            }
        });
        TimerThread.start();
        
        initializing();
        
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
        while (!shouldFinish()) {
            placeCardsOnTable();
            updateTimerDisplay();
            if(env.config.hints)
              table.hints();
            timerLoop();
            updateTimerDisplay();
            removeAllCardsFromTable();
            for(Player player:players){
                player.penaltyChanged=false;
            }
        }
        for (int deleted = 0; deleted < env.config.tableSize; deleted++) {
            for (int playerId = 0; playerId < players.length; playerId++){ 
                    table.removeToken(playerId, deleted);}}
                
        env.logger.info("thread Timer terminated.");
        

        announceWinners();
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void timerLoop() {
        while (!terminate && Timer>0)
      {
        
            sleepUntilWokenOrTimeout();
            updateTimerDisplay();
            removeCardsFromTable();
            placeCardsOnTable();
        }
    }

    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        // TODO implement
        for (int i = players.length-1; i >=0 ; i--) {
            players[i].terminate();
        }
        terminate = true;
        System.out.println("dealer is terminated");

    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).size() == 0;
    }

    /**
     * Checks cards should be removed from the table and removes them.
     */
   private void removeCardsFromTable() {
    
        // TODO implement
         if (!PlayerSet.isEmpty()) {
            List<Integer> set=new ArrayList<Integer>();
             try {
                 set = PlayerSet.take();
             } catch (InterruptedException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             int playerId = set.remove(set.size()-1);
             int[] Setarr = ChangeToArr(set);
             int count = 0;
             int[] cards = new int[env.config.featureSize];
             for (int i = 0; i < env.config.featureSize; i++) {
                 if (table.slotToCard[Setarr[i]] != null) {
                     cards[i] = table.slotToCard[Setarr[i]];
                     count++;
                 }
             }
             if (count != env.config.featureSize) {
                 return;
             }
             if (env.util.testSet(cards)) {
                 isChecking = true;
 
               
        for (int i = 0; i < players.length; i++) {
            for (int j = 0; j < env.config.featureSize; j++) {
                if (Press[i][Setarr[j]]) {
                    players[i].RemoveFromSet(Setarr[j]);
                    table.removeToken(i, Setarr[j]);
                    Press[i][Setarr[j]] = false;
                    players[i].penaltyChanged=false;
                }
            }
        }
                boolean ans = SameCardWasPreseed(cards);
                 if (ans) {
                     for (int i = 0; i < env.config.featureSize; i++) {
                         Press[playerId][Setarr[i]] = false;
                     }
                 }
                boolean givePoint = false;
                for (int i = 0; i < env.config.featureSize && !ans; i++) {
                   DeletedCards.add(cards[i]);
                     if (table.cardToSlot[cards[i]] != null)
                         synchronized (table.cardToSlot[cards[i]]) {
                             table.removeCard(Setarr[i]);
                         }
                      for(Player p :players)  {
                        table.removeToken(p.id, Setarr[i]);
                       }
                         
                     FreeSlots.add(Setarr[i]);
                     givePoint = true;
                 }
                 if (givePoint) {
                    PointReset();
                     players[playerId].decision=1;
                 }
                 isChecking =false ;
 
             } else {
                players[playerId].decision=-1;
                for (int i = 0; i < env.config.featureSize; i++) {
                    if (Press[playerId][Setarr[i]]) {
                        players[playerId].RemoveFromSet(Setarr[i]);
                        table.removeToken(playerId, Setarr[i]);
                        Press[playerId][Setarr[i]] = false;
                    }
                }
             }
             players[playerId].playerThread.interrupt();
         }
        
        }
        private void PointReset(){//just if there is legal set
            TimeToReset=false;
            if(env.config.hints){
                System.out.println("new hints:");
            table.hints();
            }
            Timer=env.config.turnTimeoutMillis;

        } 

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
   public void placeCardsOnTable() {
        // TODO implement
            synchronized (table) {
                Collections.shuffle(deck);
                while (!(FreeSlots.isEmpty())) {
                    Integer slot = FreeSlots.remove(0);
                    if (!deck.isEmpty()) {
                        Integer card=deck.remove(0);
                        table.placeCard(card, slot);
                    }

                }
              
                canPress = true; 
                            
            }
            
            
              
            

    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        // TODO implement
        //there is no need for it
    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay() {
        // TODO implement
        
       
        if (TimeToReset) {
            Timer = env.config.turnTimeoutMillis;
             TimeToReset = false;
         }
         synchronized (this) {
 
             notifyAll();
         }
    }

    /**
     * Returns all the cards from the table to the deck.
     */
    private void removeAllCardsFromTable() {
        
        // TODO implement
        TimeToReset = true;
        canPress = false;
        synchronized (table) {
            for (int deleted = 0; deleted < env.config.tableSize; deleted++) {
                FreeSlots.add(deleted);
                for (int playerId = 0; playerId < players.length; playerId++) {
                    if (Press[playerId][deleted] == true) {
                        Press[playerId][deleted] = false;
                        table.removeToken(playerId, deleted);
                    }
                    while(!( players[playerId].Token.isEmpty()))
                        players[playerId].Token.remove(0);
                }
            }
            Collections.shuffle(deck);
            for (int k = 0; k < env.config.tableSize && !FreeSlots.isEmpty(); k++) {
                int i = FreeSlots.remove(FreeSlots.size() - 1);
                if (table.slotToCard[i] != null) {
                    deck.add(table.slotToCard[i]);
                }
                table.removeCard(i);
            }
        
            canPress=true;
            FreeSlots = IntStream.range(0, env.config.tableSize).boxed().collect(Collectors.toList());
            PlayerSet.clear();
            for(Player p:players){
            p.Token.clear();  
            }     
        }
            
        
     
    }
        
       

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        // TODO implement
        int max=0;
        int winners=0;
        for (Player p : players)
            if(p.score()> max)
                max=p.score();

        for (Player p : players)  
            if(p.score()==max)
                winners++;
                
        int[]winnersArr=new int[winners];
        for (int i = 0, j = 0; j < winners; i++) {
            if (players[i].score() == max) {
                winnersArr[j] = players[i].id;
                j++;
            }
        }  
        env.ui.announceWinner(winnersArr);      

        }
        private void initializing (){
            for(int i=0 ; i<players.length;i++){
                playersThreads[i]=new Thread(players[i],env.config.playerNames[i]);
                playersThreads[i].start();
            }
    
        }
        private int[] ChangeToArr(List<Integer> a) {
            int[] ans = new int[env.config.featureSize];
            for (int i = 0; i < a.size(); i++) {
                ans[i] = a.get(i);
            }
            return ans;
        }
        public boolean SameCardWasPreseed(int[] cardid) {
            for (int i = 0; i < env.config.featureSize; i++) {
                if (DeletedCards.contains(cardid[i])) {
                    return true;
                }
            }
            return false;
        }
    }
