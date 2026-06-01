package com.example.Dungeon;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class DungeonController {

    private static final Random rand = new Random();

    private DungeonPlayer player;
    private DungeonEnemy[] enemies;
    private ArrayList<DungeonItems> items;
    private int[][] dungeon;
    private int level = 1;

    // -------------------------------------------------
    // START / RESUME GAME
    // -------------------------------------------------
    @GetMapping("/start")
    public DungeonState startGame() {

        if (player == null) {

            enemies = new DungeonEnemy[] {
                    new DungeonEnemy("Goblin", 0, 0, 20, 5),
                    new DungeonEnemy("Orc", 0, 0, 30, 8)
            };

            items = new ArrayList<>();
            items.add(new DungeonItems("Health Potion"));
            items.add(new DungeonItems("ATK Potion"));
            items.add(new DungeonItems("Luck Potion"));

            dungeon = DungeonUtilServer.createDungeon(level);

            // FIX: correct constructor
            player = new DungeonPlayer(0, 0);

            spawnPlayerRandomly();
        }

        return new DungeonState(player, dungeon, level);
    }

    // -------------------------------------------------
    // NEXT LEVEL
    // -------------------------------------------------
    @GetMapping("/next-level")
    public DungeonState nextLevel() {

        level++;

        dungeon = DungeonUtilServer.createDungeon(level);
        spawnPlayerRandomly();

        DungeonState state = new DungeonState(player, dungeon, level);
        state.lastMoveResult = 0;
        return state;
    }

    // -------------------------------------------------
    // MOVE
    // -------------------------------------------------
    @PostMapping("/move")
    public DungeonState move(@RequestParam String direction) {

        int result = DungeonUtilServer.movePlayer(
                dungeon, player, enemies, items, direction
        );

        if (result == -3) {
            level++;

            dungeon = DungeonUtilServer.createDungeon(level);
            spawnPlayerRandomly();

            DungeonState state = new DungeonState(player, dungeon, level);
            state.lastMoveResult = 0;
            return state;
        }

        DungeonState state = new DungeonState(player, dungeon, level);
        state.lastMoveResult = result;
        return state;
    }

    // -------------------------------------------------
    // COMBAT
    // -------------------------------------------------
    @PostMapping("/combat")
    public DungeonState combat(@RequestParam String action) {

        DungeonUtilServer.combatAction(
                player, action, enemies, items, dungeon
        );

        return new DungeonState(player, dungeon, level);
    }

    // -------------------------------------------------
    // USE ITEM
    // -------------------------------------------------
    @PostMapping("/useItem")
    public DungeonState useItem(@RequestParam int index) {

        DungeonUtilServer.useItem(player, index);
        return new DungeonState(player, dungeon, level);
    }

    // -------------------------------------------------
    // STATE
    // -------------------------------------------------
    @GetMapping("/state")
    public DungeonState getState() {
        return new DungeonState(player, dungeon, level);
    }

    // -------------------------------------------------
    // SPAWN PLAYER
    // -------------------------------------------------
    private void spawnPlayerRandomly() {

        int px, py;

        do {
            px = rand.nextInt(dungeon[0].length);
            py = rand.nextInt(dungeon.length);
        } while (dungeon[py][px] != 0);

        player.x = px;
        player.y = py;

        dungeon[py][px] = 5;
    }
    @PostMapping("/unlockSkill")
    public DungeonState unlockSkill(@RequestParam String id) {

        if (player.skillPoints <= 0) {
            return new DungeonState(player, dungeon, level);
        }

        if (player.skills.contains(id)) {
            return new DungeonState(player, dungeon, level);
        }

        switch (id) {

            case "atk1":
                player.atk += 5;
                break;

            case "atk2":
                player.atk += 10;
                break;

            case "hp1":
                player.hp += 20;
                break;

            case "crit":
                // simple crit system: luck boost
                player.luck += 5;
                break;

            case "tank":
                player.hp += 30;
                break;

            case "god":
                player.atk += 20;
                player.hp += 50;
                player.luck += 10;
                break;
        }

        player.skills.add(id);
        player.skillPoints--;

        return new DungeonState(player, dungeon, level);
    }
}