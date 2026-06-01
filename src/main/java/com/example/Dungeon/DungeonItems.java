package com.example.Dungeon;

public class DungeonItems {
    public String name;
    public int quantity=1;
    public int duration=0;

    public DungeonItems(String n){ name=n; }
    public DungeonItems(DungeonItems i){
        name=i.name; quantity=i.quantity;
    }
}
