package com.example.Dungeon;

import java.util.*;

public class DungeonUtilServer {

    private static Random rand = new Random();
    static int size = 0;
    // ---------------- CREATE DUNGEON ----------------
    public static int[][] createDungeon(DungeonPlayer player,
                                        int level) {
        System.out.println(size);
        size = (int) (5 * (level * 0.1 + 1));
        System.out.println(size);
        int[][] dungeon = new int[size][size];

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int roll = rand.nextInt(100);

                if (roll < 60) dungeon[y][x] = 0;       // Floor
                else if (roll < 75) dungeon[y][x] = 1;  // Enemy
                else if (roll < 85) dungeon[y][x] = 2;  // Trap
                else if (roll < 95) dungeon[y][x] = 3;  // Loot
                else dungeon[y][x] = 0;
            }
        }

        // Place player
        int px = rand.nextInt(size);
        int py = rand.nextInt(size);
        player.x = px;
        player.y = py;
        dungeon[py][px] = 5;

        // Place exit
        int ex, ey;
        do {
            ex = rand.nextInt(size);
            ey = rand.nextInt(size);
        } while (dungeon[ey][ex] == 5);

        dungeon[ey][ex] = 4;

        return dungeon;

    }

    // ---------------- MOVE PLAYER ----------------
    public static int movePlayer(int[][] dungeon, DungeonPlayer player,
                                 DungeonEnemy[] enemies, ArrayList<DungeonItems> items,
                                 String direction) {

        if (player.pendingCombat) return -1;

        int newX = player.x;
        int newY = player.y;

        switch (direction.toLowerCase()) {
            case "w": newY--; break;
            case "s": newY++; break;
            case "a": newX--; break;
            case "d": newX++; break;
            default: return -1;
        }

        if (newX < 0 || newX >= dungeon[0].length || newY < 0 || newY >= dungeon.length)
            return -1;

        int cell = dungeon[newY][newX];

        // -------- ENEMY --------
        if (cell == 1) {
            if (!player.pendingCombat) {
                DungeonEnemy base = enemies[rand.nextInt(enemies.length)];
                player.currentEnemy = new DungeonEnemy(base);
                player.pendingCombat = true;
                player.inCombat = true;

                player.tempTileX = newX;
                player.tempTileY = newY;

                return -2; // ✅ COMBAT TRIGGER
            }
            return -1;
        }

        // -------- TRAP --------
        if (cell == 2) {
            trapCollide(player);
            dungeon[newY][newX] = 0;
            player.lastPickedUpItem = "You triggered a trap!";
        }

        // -------- LOOT --------
        if (cell == 3) {
            lootChest(player, items);
            dungeon[newY][newX] = 0;
        }

        // -------- EXIT --------
        if (cell == 4) {
            dungeon[player.y][player.x] = 0;
            player.x = newX;
            player.y = newY;
            dungeon[player.y][player.x] = 5;

            player.lastPickedUpItem = "You found the exit!";
            return -3; // EXIT TRIGGER
        }

        // -------- NORMAL MOVE --------
        dungeon[player.y][player.x] = 0;
        player.x = newX;
        player.y = newY;
        dungeon[player.y][player.x] = 5;

        return 0;
    }

    // ---------------- COMBAT ----------------
    public static void combatAction(DungeonPlayer player, String action,
                                    DungeonEnemy[] enemies, ArrayList<DungeonItems> items,
                                    int[][] dungeon) {

        if (!player.inCombat || player.currentEnemy == null) return;

        DungeonEnemy enemy = player.currentEnemy;

        // Player turn
        if (action.equalsIgnoreCase("basic")) {
            enemy.hp -= player.atk;
            player.lastPickedUpItem = "You used Basic Attack!";
        }
        if (action.equalsIgnoreCase("skill")) {
            enemy.hp -= player.atk * 2;
            player.lastPickedUpItem = "You used Skill Attack!";
        }

        // Enemy turn
        if (enemy.hp > 0) {
            int roll = rand.nextInt(100);
            if (roll < 70) {
                player.hp -= enemy.atk;
                player.lastPickedUpItem += " | Enemy used Basic Attack!";
            } else {
                player.hp -= enemy.atk * 2;
                player.lastPickedUpItem += " | Enemy used Skill Attack!";
            }
        }

        // Enemy defeated
        if (enemy.hp <= 0) {
            enemy.hp = 0;
            player.inCombat = false;
            player.pendingCombat = false;

            dungeon[player.tempTileY][player.tempTileX] = 0;

            getLoot(items, player, player.backpack);

            player.currentEnemy = null;
            player.lastPickedUpItem = "Enemy defeated!";
        }

        // Player death
        if (player.hp <= 0) {
            player.hp = 0;
            player.inCombat = false;
            player.pendingCombat = false;
            player.currentEnemy = null;
            player.lastPickedUpItem = "You died!";
        }
    }

    // ---------------- TRAP ----------------
    static void trapCollide(DungeonPlayer player) {
        int dmg = rand.nextInt(5) + 1;
        player.hp -= dmg;
        if (player.hp < 0) player.hp = 0;
    }

    // ---------------- LOOT CHEST ----------------
    static void lootChest(DungeonPlayer player, ArrayList<DungeonItems> items) {
        if (items.isEmpty()) return;

        DungeonItems base = items.get(rand.nextInt(items.size()));
        DungeonItems item = new DungeonItems(base);

        player.backpack.add(item);
        player.lastPickedUpItem = "Picked up: " + item.name;
    }

    // ---------------- COMBAT DROP LOOT ----------------
    public static void getLoot(ArrayList<DungeonItems> items,
                               DungeonPlayer player,
                               ArrayList<DungeonItems> backpack) {

        int dropRoll = rand.nextInt(100) + 1;

        if (dropRoll <= (20 + player.luck) && !items.isEmpty()) {
            DungeonItems base = items.get(rand.nextInt(items.size()));
            DungeonItems drop = new DungeonItems(base);
            backpack.add(drop);
            player.lastPickedUpItem = "Loot dropped: " + drop.name;
        }
    }

    // ---------------- USE ITEM ----------------
    public static void useItem(DungeonPlayer player, int index) {

        if (index < 0 || index >= player.backpack.size()) return;

        DungeonItems item = player.backpack.get(index);

        switch (item.name.toLowerCase()) {
            case "health potion":
                player.hp += 10;
                player.lastPickedUpItem = "Used Health Potion!";
                break;

            case "atk potion":
                player.atk += 10;
                item.duration = 6;
                player.activePotions.add(item);
                player.lastPickedUpItem = "Used ATK Potion!";
                break;

            case "luck potion":
                player.luck += 10;
                item.duration = 6;
                player.activePotions.add(item);
                player.lastPickedUpItem = "Used Luck Potion!";
                break;
        }

        player.backpack.remove(index);
    }

    // ---------------- DECREASE POTION DURATIONS ----------------
    public static void decreasePotionDurations(DungeonPlayer player) {

        Iterator<DungeonItems> it = player.activePotions.iterator();

        while (it.hasNext()) {
            DungeonItems potion = it.next();
            potion.duration--;

            if (potion.duration <= 0) {
                switch (potion.name.toLowerCase()) {
                    case "atk potion":
                        player.atk -= 10;
                        break;

                    case "luck potion":
                        player.luck -= 10;
                        break;
                }

                it.remove();
            }
        }
    }
}
