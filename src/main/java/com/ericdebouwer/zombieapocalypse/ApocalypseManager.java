package com.ericdebouwer.zombieapocalypse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.ImmutableMap;

public class ApocalypseManager implements Listener {

	private File apoFile;
	private FileConfiguration apoConfig;
	
	private final String UNTIL_KEY = ".until";
	private final String MOB_CAP_KEY = ".mobcap";
	
	private List<ApocalypseWorld> apocalypseWorlds = new ArrayList<>();
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
			
			int mobCap = apoConfig.getInt(key + MOB_CAP_KEY, Bukkit.getMonsterSpawnLimit());
			this.startApocalypse(key, endTime, mobCap, false);
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
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + plugin.logPrefix + "Failed to load apocalypse data! Removed 'apocalypse.yml' and restart the server!");
		}
	}
	
	
	private void saveConfig(){
		try{
			for (ApocalypseWorld world: apocalypseWorlds){
				apoConfig.set(world.worldName + UNTIL_KEY, world.endTime);
				apoConfig.set(world.worldName + MOB_CAP_KEY, world.mobcap);
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
	
	public Optional<ApocalypseWorld> getApoWorld(String worldName){
		return apocalypseWorlds.stream().filter(w -> w.worldName.equals(worldName)).findFirst();
	}
	
	public boolean isApocalypse(String worldName){
		return this.getApoWorld(worldName).isPresent();
	}
	
	public boolean startApocalypse(String worldName, long endTime){
		return this.startApocalypse(worldName, endTime, Bukkit.getMonsterSpawnLimit(), true);
	}
	
	public boolean startApocalypse(String worldName, long endTime, int mobCap, boolean firstTime){
		File potentialWorld = new File(Bukkit.getServer().getWorldContainer(), worldName);
		if (!potentialWorld.exists() || !potentialWorld.isDirectory()) return false;
		
		ApocalypseWorld apoWorld = new ApocalypseWorld(worldName, endTime, mobCap);
		apocalypseWorlds.add(apoWorld);
		
		if (endTime > 0){
			this.addEndDelay(worldName, endTime);
		}
		
		World world = Bukkit.getWorld(worldName);
		if (world == null) return true; //not loaded
		
		world.setMonsterSpawnLimit(mobCap);
		
		for (Player player: world.getPlayers()){
			if (firstTime) plugin.getConfigManager().sendMessage(player, Message.START_BROADCAST,  ImmutableMap.of("world_name", worldName));
			bossBar.addPlayer(player);
		}
		return true;
	}
	
	public void setMobCap(String worldName, int mobCap){
		Optional<ApocalypseWorld> apoWorld = getApoWorld(worldName);
		if (!apoWorld.isPresent()) return;
		apoWorld.get().setMobCap(mobCap);
		
		World world = Bukkit.getWorld(worldName);
		if (world == null) return;
		world.setMonsterSpawnLimit(mobCap);
		
	}
	
	public boolean endApocalypse(String worldName){
		if (!isApocalypse(worldName)) return false;
		apocalypseWorlds.remove(this.getApoWorld(worldName).get());
		apoEnders.remove(worldName);
		apoConfig.set(worldName, null);
		this.saveConfig();
		
		World world = Bukkit.getWorld(worldName);
		if (world == null) return true;
		
		for (Player player: world.getPlayers()){
			bossBar.removePlayer(player);
			plugin.getConfigManager().sendMessage(player, Message.END_BROADCAST, ImmutableMap.of("world_name", worldName));
		}
		
		for (Zombie zomb: world.getEntitiesByClass(Zombie.class)){
			zomb.remove();
		}
		return true;
	}
	
	@EventHandler
	public void worldLoad(WorldLoadEvent e){
		Optional<ApocalypseWorld> apoWorld = this.getApoWorld(e.getWorld().getName());
		if (!apoWorld.isPresent()) return;
		e.getWorld().setMonsterSpawnLimit(apoWorld.get().mobcap);
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
