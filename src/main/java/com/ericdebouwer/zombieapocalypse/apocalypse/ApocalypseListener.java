package com.ericdebouwer.zombieapocalypse.apocalypse;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.config.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.Optional;

public class ApocalypseListener implements Listener {

    private final ZombieApocalypse plugin;
    private final ApocalypseManager manager;

    public ApocalypseListener(ZombieApocalypse plugin){
        this.plugin = plugin;
        this.manager = plugin.getApocalypseManager();
    }

    @EventHandler
    public void worldLoad(WorldLoadEvent event){
        Optional<ApocalypseWorld> apoWorld = manager.getApoWorld(event.getWorld().getName());
        if (!apoWorld.isPresent()) return;
        event.getWorld().setMonsterSpawnLimit(apoWorld.get().mobCap);
    }

    @EventHandler
    public void worldSwitch(PlayerChangedWorldEvent event){
        Optional<ApocalypseWorld> apoFrom = manager.getApoWorld(event.getFrom().getName());
        apoFrom.ifPresent(aw -> aw.removePlayer(event.getPlayer()));

        Optional<ApocalypseWorld> apoTo = manager.getApoWorld(event.getPlayer().getWorld().getName());
        apoTo.ifPresent(aw -> aw.addPlayer(event.getPlayer()));
    }

    @EventHandler
    public void newPlayerJoin(PlayerJoinEvent event){
        Optional<ApocalypseWorld> apoWorld = manager.getApoWorld(event.getPlayer().getWorld().getName());
        apoWorld.ifPresent(aw -> aw.addPlayer(event.getPlayer()));
    }

    @EventHandler
    public void playerLeave(PlayerQuitEvent event){
        Optional<ApocalypseWorld> apoWorld = manager.getApoWorld(event.getPlayer().getWorld().getName());
        apoWorld.ifPresent(aw -> aw.removePlayer(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onSleep(PlayerBedEnterEvent event){
        if (plugin.getConfigManager().isAllowSleep()) return;
        if (!manager.isApocalypse(event.getPlayer().getWorld().getName())) return;

        plugin.getConfigManager().sendMessage(event.getPlayer(), Message.NO_SLEEP, null);
        event.setCancelled(true);
    }
}
