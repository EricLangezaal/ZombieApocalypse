package com.ericdebouwer.zombieapocalypse;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.ImmutableMap;

public class ApocalypseManager implements Listener {

	private File apoFile;
	private FileConfiguration apoConfig;
	
	private final String UNTIL_KEY = ".until";
	
	private Map<String, Long> apocalypseWorlds = new HashMap<>();
	private Map<String, BukkitTask> apoEnders = new HashMap<>();
	
	private BossBar bossBar;
	private ZombieApocalypse plugin;
	
	public ApocalypseManager(ZombieApocalypse plugin){
		this.plugin = plugin;
		
		NamespacedKey nameKey = new NamespacedKey(plugin, "apocalypsebar");
		String barTitle = plugin.getConfigManager().getString(Message.BOSS_BAR_TITLE);
		bossBar = plugin.getServer().createBossBar(nameKey, barTitle, BarColor.PURPLE, BarStyle.SOLID, BarFlag.CREATE_FOG);
		
		this.createFile();
		if (apoConfig == null) return;
		
		for (final String key: apoConfig.getKeys(false)){
			long endTime = apoConfig.getLong(key + UNTIL_KEY);
			long now = java.time.Instant.now().getEpochSecond();
			
			if (endTime > 0 && endTime < now) continue; //outdated apocalpyse
			apocalypseWorlds.put(key, endTime);
			if (endTime < 0) continue; //symbolises forever
				
			this.addEndDelay(key, endTime);
		}
		
		//If someone reloads, put it back for all players
		for (World w: Bukkit.getWorlds()){
			if (!isApocalypse(w.getName())) continue;
			for (Player player: w.getPlayers()){
				bossBar.addPlayer(player);
			}
		}
	}
	
	private void createFile(){
		try {
			apoFile = new File(plugin.getDataFolder(), "apocalypse.yml");
			if (!apoFile.exists()){
				apoFile.getParentFile().mkdirs();
				apoFile.createNewFile();
			}
			apoConfig = YamlConfiguration.loadConfiguration(apoFile);
			
		} catch (IOException e) {
			e.printStackTrace();
			Bukkit.getLogger().log(Level.SEVERE, "Failed to load apocalypse data! Removed 'apocalypse.yml' and restart the server!");
		}
	}
	
	
	private void saveConfig(){
		try{
			for (Map.Entry<String, Long> entry: apocalypseWorlds.entrySet()){
				apoConfig.set(entry.getKey() + UNTIL_KEY, entry.getValue());
			}
			this.apoConfig.save(apoFile);
		} catch (IOException | NullPointerException e){
			e.printStackTrace();
		}
	}
	
	public void onDisable(){
		for (Player player: Bukkit.getOnlinePlayers()){
			bossBar.removePlayer(player);
		}
		this.saveConfig();
	}
	
	private void addEndDelay(final String worldName, long endTime){
		long now = java.time.Instant.now().getEpochSecond();
		long delay = (endTime-now) * 20;
		BukkitTask task = new BukkitRunnable(){
			public void run() {
				endApocalypse(worldName);
			}
		}.runTaskLater(plugin, delay);
		apoEnders.put(worldName, task);
	}
	
	public boolean isApocalypse(String worldName){
		return this.apocalypseWorlds.keySet().contains(worldName);
	}
	
	public boolean startApocalypse(String worldName, long endTime){
		File potentialWorld = new File(Bukkit.getServer().getWorldContainer(), worldName);
		if (!potentialWorld.exists()) return false;
		
		apocalypseWorlds.put(worldName, endTime);
		
		if (endTime > 0){
			this.addEndDelay(worldName, endTime);
		}
		
		for (Player player: Bukkit.getOnlinePlayers()){
			if (player.getWorld().getName().equals(worldName)){
				plugin.getConfigManager().sendMessage(player, Message.START_BROADCAST,  ImmutableMap.of("world_name", worldName));
				bossBar.addPlayer(player);
			}
		}
		return true;
	}
	
	public boolean endApocalypse(String worldName){
		if (!isApocalypse(worldName)) return false;
		apocalypseWorlds.remove(worldName);
		apoEnders.remove(worldName);
		apoConfig.set(worldName, null);
		
		World world = Bukkit.getWorld(worldName);
		if (world == null) return true;
		
		for (Player player: world.getPlayers()){
			bossBar.removePlayer(player);
			plugin.getConfigManager().sendMessage(player, Message.END_BROADCAST, ImmutableMap.of("world_name", worldName));
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
		if (isApocalypse(to.getName())){
			bossBar.addPlayer(e.getPlayer());
		}
	}
	
	@EventHandler
	public void newPlayerJoin(PlayerJoinEvent e){
		if (isApocalypse(e.getPlayer().getWorld().getName())){
			bossBar.addPlayer(e.getPlayer());
		}
	}
}
