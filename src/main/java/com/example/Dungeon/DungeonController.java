package com.example.Dungeon;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
public class DungeonController {

    private DungeonPlayer player;
    private DungeonEnemy[] enemies;
    private ArrayList<DungeonItems> items;
    private int[][] dungeon;
    private int level = 1;

    // ---------- START / NEW LEVEL ----------
    @GetMapping("/start")
    public DungeonState startGame(@RequestParam(value="level", required=false) Integer requestedLevel) {

        // If level query param exists, increment
        if(requestedLevel != null) level = requestedLevel;

        // If first start, create player
        if(player == null) player = new DungeonPlayer(100, 10, 5);

        // Create enemies if null
        if(enemies == null) {
            enemies = new DungeonEnemy[] {
                    new DungeonEnemy("Goblin", 0,0, 20, 5),
                    new DungeonEnemy("Orc", 0,0, 30, 8)
            };
        }

        // Create items if null
        if(items == null || items.isEmpty()) {
            items = new ArrayList<>();
            items.add(new DungeonItems("Health Potion"));
            items.add(new DungeonItems("ATK Potion"));
            items.add(new DungeonItems("Luck Potion"));
        }

        // Create dungeon with requested level
        dungeon = DungeonUtilServer.createDungeon(player, enemies, items, level);

        DungeonState state = new DungeonState(player, dungeon, level);
        state.lastMoveResult = 0; // nothing happened yet
        return state;
    }

    // ---------- MOVE ----------
    @PostMapping("/move")
    public DungeonState move(@RequestParam String direction) {

        int lastMove = DungeonUtilServer.movePlayer(dungeon, player, enemies, items, direction);

        DungeonState state = new DungeonState(player, dungeon, level);
        state.lastMoveResult = lastMove; // IMPORTANT for frontend exit/combat

        return state;
    }

    // ---------- COMBAT ----------
    @PostMapping("/combat")
    public DungeonState combat(@RequestParam String action) {
        DungeonUtilServer.combatAction(player, action, enemies, items, dungeon);
        return new DungeonState(player, dungeon, level);
    }

    // ---------- USE ITEM ----------
    @PostMapping("/useItem")
    public DungeonState useItem(@RequestParam int index) {
        DungeonUtilServer.useItem(player, index);
        return new DungeonState(player, dungeon, level);
    }

    // ---------- GET CURRENT STATE (optional) ----------
    @GetMapping("/state")
    public DungeonState getState() {
        return new DungeonState(player, dungeon, level);
    }
}
