package com.ericdebouwer.zombieapocalypse;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class ApocalypseManager implements Listener {

	private ArrayList<String> apocalypseWorlds = new ArrayList<String>();
	private BossBar bossBar;
	private ZombieApocalypse plugin;
	
	public ApocalypseManager(ZombieApocalypse plugin){
		this.plugin = plugin;
		NamespacedKey key = new NamespacedKey(plugin, "apocalypsebar");
		String barTitle = plugin.getConfigManager().getString(Message.BOSS_BAR_TITLE);
		bossBar = plugin.getServer().createBossBar(key, barTitle, BarColor.PURPLE, BarStyle.SOLID, BarFlag.CREATE_FOG);
		
		apocalypseWorlds = plugin.getConfigManager().getApocalypseWorlds();
	}
	
	public void save(){
		this.plugin.getConfigManager().saveApocalypseWorlds(apocalypseWorlds);
	}
	
	public boolean isApocalypse(String worldName){
		return this.apocalypseWorlds.contains(worldName);
	}
	
	public boolean startApocalypse(String worldName){
		File potentialWorld = new File(Bukkit.getServer().getWorldContainer(), worldName);
		if (!potentialWorld.exists()) return false;
		apocalypseWorlds.add(worldName);
		for (Player player: Bukkit.getOnlinePlayers()){
			if (player.getWorld().getName().equals(worldName)){
				plugin.getConfigManager().sendMessage(player, Message.START_BROADCAST, null);
				bossBar.addPlayer(player);
			}
		}
		return true;
	}
	
	public boolean endApocalypse(String worldName){
		if (!(apocalypseWorlds.contains(worldName))) return false;
		apocalypseWorlds.remove(worldName);
		
		World world = Bukkit.getWorld(worldName);
		if (world == null) return true;
		
		for (Player player: world.getPlayers()){
			bossBar.removePlayer(player);
			plugin.getConfigManager().sendMessage(player, Message.END_BROADCAST, null);
		}
		
		for (Zombie zomb: world.getEntitiesByClass(Zombie.class)){
			if (ZombieType.getType(zomb) != ZombieType.DEFAULT) zomb.remove();
		}
		return true;
	}
	
	
	@EventHandler
	public void worldSwitch(PlayerChangedWorldEvent e){
		bossBar.removePlayer(e.getPlayer());
		World to = e.getPlayer().getWorld();
		if (apocalypseWorlds.contains(to.getName())){
			bossBar.addPlayer(e.getPlayer());
		}
	}
	
	@EventHandler
	public void newPlayerJoin(PlayerJoinEvent e){
		if (apocalypseWorlds.contains(e.getPlayer().getWorld().getName())){
			bossBar.addPlayer(e.getPlayer());
		}
	}
}
