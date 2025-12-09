package com.example.Dungeon;

public class DungeonEnemy {
    public String name;
    public int x,y,hp,atk;

    public DungeonEnemy(String n,int x,int y,int h,int a){
        name=n; this.x=x; this.y=y; hp=h; atk=a;
    }

    public DungeonEnemy(DungeonEnemy e){
        name=e.name;
        x=e.x; y=e.y;
        hp=e.hp; atk=e.atk;
    }
}
