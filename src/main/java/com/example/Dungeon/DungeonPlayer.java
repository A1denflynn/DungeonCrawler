package com.example.Dungeon;

import java.util.ArrayList;

public class DungeonPlayer {
    public int hp,atk,luck,critr,critd;
    public int x,y;
    public boolean inCombat,pendingCombat;
    public DungeonEnemy currentEnemy;
    public String lastPickedUpItem="";

    public ArrayList<DungeonItems> backpack=new ArrayList<>();
    public ArrayList<DungeonItems> activePotions=new ArrayList<>();

    public int tempTileX=-1,tempTileY=-1;

    public DungeonPlayer(int hp,int atk,int luck){
        this.hp=hp;
        this.atk=atk;
        this.luck=luck;
        critr=10;
        critd=50;
    }
}
