package com.example.Dungeon;

public class DungeonState {
    public DungeonPlayer player;
    public int[][] dungeon;
    public int level;
    public int lastMoveResult;

    public DungeonState(DungeonPlayer p,int[][] d,int lvl){
        player=p; dungeon=d; level=lvl;
    }

    public void setLastMoveResult(int r){
        lastMoveResult=r;
    }
}
