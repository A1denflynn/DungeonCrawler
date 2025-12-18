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
    // START / RESUME GAME (NO RANDOMISATION HERE)
    // -------------------------------------------------
    @GetMapping("/start")
    public DungeonState startGame() {

        // Create player once
        if (player == null) {
            player = new DungeonPlayer(100, 10, 5);

            enemies = new DungeonEnemy[] {
                    new DungeonEnemy("Goblin", 0, 0, 20, 5),
                    new DungeonEnemy("Orc", 0, 0, 30, 8)
            };

            items = new ArrayList<>();
            items.add(new DungeonItems("Health Potion"));
            items.add(new DungeonItems("ATK Potion"));
            items.add(new DungeonItems("Luck Potion"));

            // First level initialisation
            dungeon = DungeonUtilServer.createDungeon(level);
            spawnPlayerRandomly();
        }

        return new DungeonState(player, dungeon, level);
    }

    // -------------------------------------------------
    // NEXT LEVEL  (ONLY PLACE THAT RANDOMISES PLAYER)
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

        // ✅ AUTO ADVANCE LEVEL
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
        System.out.println("Move result = " + result);
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
    // STATE (DEBUG / POLLING SAFE)
    // -------------------------------------------------
    @GetMapping("/state")
    public DungeonState getState() {
        return new DungeonState(player, dungeon, level);
    }

    // -------------------------------------------------
    // HELPER — RANDOM SPAWN (SINGLE RESPONSIBILITY)
    // -------------------------------------------------
    private void spawnPlayerRandomly() {

        int px, py;

        do {
            px = rand.nextInt(dungeon[0].length);
            py = rand.nextInt(dungeon.length);
        } while (dungeon[py][px] != 0); // floor only

        player.x = px;
        player.y = py;
        dungeon[py][px] = 5;
    }
}
