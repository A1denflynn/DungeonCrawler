package com.example.Dungeon;

import java.util.ArrayList;

public class GameSess {
    public String id;
    public int[][] dungeon;
    public DungeonPlayer player;
    public DungeonEnemy[] enemies;
    public ArrayList<DungeonItems> backpack = new ArrayList<>();
    public ArrayList<DungeonItems> activePotions = new ArrayList<>();
    public int level;

    public void GameSession(String id) {
        this.id = id;
    }
}
