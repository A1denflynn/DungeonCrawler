package com.example.Dungeon;

import java.util.ArrayList;

public class GameState {
    public DungeonPlayer player;
    public int[][] dungeon;
    public ArrayList<DungeonItems> backpack;
    public int level;

    public boolean inCombat = false;
    public DungeonEnemy currentEnemy;

    public GameState(DungeonPlayer player, int[][] dungeon, ArrayList<DungeonItems> items, int level) {
        this.player = player;
        this.dungeon = dungeon;
        this.backpack = player.backpack;
        this.level = level;
    }

    public GameState(DungeonPlayer player, int[][] dungeon, ArrayList<DungeonItems> items, int level, boolean inCombat, DungeonEnemy enemy) {
        this(player, dungeon, items, level);
        this.inCombat = inCombat;
        this.currentEnemy = enemy;
    }
}
