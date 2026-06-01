package com.example.Dungeon;

import java.util.*;

public class DungeonUtilServer {

    static final Random rand = new Random();

    // ---------------- CREATE DUNGEON ----------------
    public static int[][] createDungeon(int level) {

        int size = (int) (5 * (level * 0.1 + 1));
        if (size < 5) size = 5;
        if (size > 15) size = 15;

        int[][] dungeon = new int[size][size];

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {

                int roll = rand.nextInt(100);

                if (roll < 60) dungeon[y][x] = 0;
                else if (roll < 75) dungeon[y][x] = 1;
                else if (roll < 85) dungeon[y][x] = 2;
                else if (roll < 95) dungeon[y][x] = 3;
                else dungeon[y][x] = 0;
            }
        }

        int ex, ey;
        do {
            ex = rand.nextInt(size);
            ey = rand.nextInt(size);
        } while (dungeon[ey][ex] != 0);

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

        if (cell == 1) {
            if (!player.pendingCombat) {
                DungeonEnemy base = enemies[rand.nextInt(enemies.length)];
                player.currentEnemy = new DungeonEnemy(base);
                player.pendingCombat = true;
                player.inCombat = true;

                player.tempTileX = newX;
                player.tempTileY = newY;

                return -2;
            }
            return -1;
        }

        if (cell == 2) {
            trapCollide(player);
            dungeon[newY][newX] = 0;
            player.lastPickedUpItem = "You triggered a trap!";
        }

        if (cell == 3) {
            lootChest(player, items);
            dungeon[newY][newX] = 0;
        }

        if (cell == 4) {
            dungeon[player.y][player.x] = 0;
            player.x = newX;
            player.y = newY;
            dungeon[player.y][player.x] = 5;

            player.lastPickedUpItem = "You found the exit!";
            return -3;
        }

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

        if (action.equalsIgnoreCase("basic")) {
            enemy.hp -= player.atk;
            player.lastPickedUpItem = "Basic Attack!";
        }

        if (action.equalsIgnoreCase("skill")) {
            enemy.hp -= player.atk * 2;
            player.lastPickedUpItem = "Skill Attack!";
        }

        if (enemy.hp > 0) {
            int roll = rand.nextInt(100);
            if (roll < 70) {
                player.hp -= enemy.atk;
            } else {
                player.hp -= enemy.atk * 2;
            }
        }

        // ---------------- ENEMY DEFEATED ----------------
        if (enemy.hp <= 0) {
            enemy.hp = 0;
            player.inCombat = false;
            player.pendingCombat = false;

            dungeon[player.tempTileY][player.tempTileX] = 0;

            getLoot(items, player, player.backpack);

            int xpGain = 10 + (enemy.atk / 2);
            addXP(player, xpGain);

            player.currentEnemy = null;
            player.lastPickedUpItem = "Enemy defeated! +" + xpGain + " XP";
        }

        if (player.hp <= 0) {
            player.hp = 0;
            player.inCombat = false;
            player.pendingCombat = false;
            player.currentEnemy = null;
            player.lastPickedUpItem = "You died!";
        }
    }

    // ---------------- XP SYSTEM ----------------
    static void addXP(DungeonPlayer player, int amount) {
        player.xp += amount;

        while (player.xp >= player.xpToNext) {
            player.xp -= player.xpToNext;
            player.level++;
            player.skillPoints++;

            player.xpToNext = (int)(player.xpToNext * 1.4);

            player.lastPickedUpItem =
                    "Level Up! Now level " + player.level +
                            " (+1 skill point)";
        }
    }

    // ---------------- TRAP ----------------
    static void trapCollide(DungeonPlayer player) {
        int dmg = rand.nextInt(5) + 1;
        player.hp -= dmg;
        if (player.hp < 0) player.hp = 0;
    }

    // ---------------- LOOT ----------------
    static void lootChest(DungeonPlayer player, ArrayList<DungeonItems> items) {
        if (items.isEmpty()) return;

        DungeonItems base = items.get(rand.nextInt(items.size()));
        DungeonItems item = new DungeonItems(base);

        player.backpack.add(item);
        player.lastPickedUpItem = "Picked up: " + item.name;
    }

    public static void getLoot(ArrayList<DungeonItems> items,
                               DungeonPlayer player,
                               ArrayList<DungeonItems> backpack) {

        int dropRoll = rand.nextInt(100) + 1;

        if (dropRoll <= (20 + player.luck) && !items.isEmpty()) {
            DungeonItems base = items.get(rand.nextInt(items.size()));
            DungeonItems drop = new DungeonItems(base);
            backpack.add(drop);
        }
    }

    public static void useItem(DungeonPlayer player, int index) {

        if (index < 0 || index >= player.backpack.size()) return;

        DungeonItems item = player.backpack.get(index);

        switch (item.name.toLowerCase()) {
            case "health potion":
                player.hp += 10;
                break;

            case "atk potion":
                player.atk += 10;
                item.duration = 6;
                player.activePotions.add(item);
                break;

            case "luck potion":
                player.luck += 10;
                item.duration = 6;
                player.activePotions.add(item);
                break;
        }

        player.backpack.remove(index);
    }

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