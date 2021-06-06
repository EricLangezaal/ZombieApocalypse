package com.ericdebouwer.zombieapocalypse.integration.silkspawners;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieType;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerBreakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;


public class SpawnerBreakListener implements Listener {

    private final ZombieApocalypse plugin;

    public SpawnerBreakListener(ZombieApocalypse plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSpawnerBreak(SilkSpawnersSpawnerBreakEvent event){
        ZombieType type = plugin.getZombieItems().getZombieType(event.getSpawner());
        if (type == null) return;

        event.setDrop(plugin.getZombieItems().getSpawner(type));
    }
}
