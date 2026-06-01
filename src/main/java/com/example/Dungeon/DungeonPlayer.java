package com.example.Dungeon;

import java.util.ArrayList;

public class DungeonPlayer {

    // ---------------- POSITION ----------------
    public int x;
    public int y;

    // ---------------- CORE STATS ----------------
    public int hp = 100;
    public int atk = 10;
    public int luck = 5;

    // ---------------- PROGRESSION SYSTEM ----------------
    public int level = 1;
    public int xp = 0;
    public int xpToNext = 20;
    public int skillPoints = 0;

    // ---------------- COMBAT STATE ----------------
    public boolean inCombat = false;
    public boolean pendingCombat = false;

    public DungeonEnemy currentEnemy;

    // ---------------- TEMP POSITION (combat rollback) ----------------
    public int tempTileX;
    public int tempTileY;

    // ---------------- INVENTORY ----------------
    public ArrayList<DungeonItems> backpack = new ArrayList<>();
    public ArrayList<DungeonItems> activePotions = new ArrayList<>();
    public ArrayList<String> skills = new ArrayList<>();

    // ---------------- UI FEEDBACK ----------------
    public String lastPickedUpItem = "";


    // ---------------- CONSTRUCTOR ----------------
    public DungeonPlayer(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }
}