package com.ericdebouwer.zombieapocalypse.apocalypse;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.config.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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
    public void worldLoad(WorldLoadEvent e){
        Optional<ApocalypseWorld> apoWorld = manager.getApoWorld(e.getWorld().getName());
        if (!apoWorld.isPresent()) return;
        e.getWorld().setMonsterSpawnLimit(apoWorld.get().mobCap);
    }

    @EventHandler
    public void worldSwitch(PlayerChangedWorldEvent e){
        Optional<ApocalypseWorld> apoFrom = manager.getApoWorld(e.getFrom().getName());
        apoFrom.ifPresent(aw -> aw.removePlayer(e.getPlayer()));

        Optional<ApocalypseWorld> apoTo = manager.getApoWorld(e.getPlayer().getWorld().getName());
        apoTo.ifPresent(aw -> aw.addPlayer(e.getPlayer()));
    }

    @EventHandler
    public void newPlayerJoin(PlayerJoinEvent e){
        Optional<ApocalypseWorld> apoWorld = manager.getApoWorld(e.getPlayer().getWorld().getName());
        apoWorld.ifPresent(aw -> aw.addPlayer(e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onSleep(PlayerBedEnterEvent event){
        if (plugin.getConfigManager().allowSleep) return;
        if (!manager.isApocalypse(event.getPlayer().getWorld().getName())) return;

        plugin.getConfigManager().sendMessage(event.getPlayer(), Message.NO_SLEEP, null);
        event.setCancelled(true);
    }
}
