package me.xa5.xrankup;

import java.util.List;

public class Rankup {
    private long cost;
    private String nextRank;
    private String oldRank;
    private List<String> actions;

    public Rankup(long cost, String oldRank, String nextRank, List<String> actions) {
        this.cost = cost;
        this.nextRank = nextRank;
        this.oldRank = oldRank;
        this.actions = actions;
    }

    public long getCost() {
        return cost;
    }

    public String getNextRank() {
        return nextRank;
    }

    public String getOldRank() {
        return oldRank;
    }

    public List<String> getActions() {
        return actions;
    }
}
