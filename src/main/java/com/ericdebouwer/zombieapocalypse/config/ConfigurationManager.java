package com.ericdebouwer.zombieapocalypse.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.ImmutableMap;

public class ConfigurationManager {

	ZombieApocalypse plugin;
	private final String MESSAGES_PREFIX = "messages.";
	private final String ZOMBIES_PREFIX = "zombies.";
	
	public String pluginPrefix;
	public boolean checkUpdates = true;
	public boolean doBabies = true;
	public boolean burnInDay = true;
	public boolean blockDamage = true;
	public boolean doBossBar = true;
	public boolean bossBarFog = true;

	private boolean isValid;
	
	private Map<ZombieType, ZombieWrapper> zombieWrappers = new HashMap<>();
	
	public ConfigurationManager(ZombieApocalypse plugin){
		this.plugin = plugin;
		plugin.saveDefaultConfig();
		
		this.isValid = this.checkConfig();
		if (isValid) this.loadConfig();
	}
	
	public boolean isValid(){
		return this.isValid;
	}
	
	private boolean checkConfig(){
		boolean valid = this.validateConfig(true);
		if (!valid){
			if (handleUpdate()){
				plugin.reloadConfig();
				if (validateConfig(false)) {
					Bukkit.getLogger().log(Level.INFO, ZombieApocalypse.logPrefix + "================================================================");
		        	Bukkit.getLogger().log(Level.INFO, ZombieApocalypse.logPrefix + "Automatically updated old/invalid configuration file!");
		        	Bukkit.getLogger().log(Level.INFO, ZombieApocalypse.logPrefix + "================================================================");
		        	return true;
				}
			}
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + ZombieApocalypse.logPrefix + "Automatic configuration update failed! You can delete the old 'config.yml' to get a new one.");
		}
		return valid;
	}
	
	public void loadConfig(){
		pluginPrefix = plugin.getConfig().getString("plugin-prefix");
		checkUpdates = plugin.getConfig().getBoolean("check-for-updates");
		doBabies = plugin.getConfig().getBoolean("allow-babies");
		burnInDay = plugin.getConfig().getBoolean("burn-in-day");
		blockDamage = plugin.getConfig().getBoolean("do-zombie-block-damage");
		doBossBar = plugin.getConfig().getBoolean("do-bossbar");
		bossBarFog = plugin.getConfig().getBoolean("bossbar-fog", true);

		zombieWrappers.clear();
		
		ConfigurationSection section = plugin.getConfig().getConfigurationSection(ZOMBIES_PREFIX);
		for (String zombie: section.getKeys(false)){
			if (!section.getBoolean(zombie + ".enabled")) continue;
			try {
				ZombieType type = ZombieType.valueOf(zombie.toUpperCase());
				ZombieWrapper wrapper = new ZombieWrapper(type, section.getConfigurationSection(zombie));
				zombieWrappers.put(type, wrapper);
			} catch (IllegalArgumentException e){
				Bukkit.getConsoleSender().sendMessage(ChatColor.BOLD + "" +ChatColor.RED + ZombieApocalypse.logPrefix +"Zombie type '" + zombie + "' doesn't exist and isn't loaded in.");
			}
		}
	}
	
	public String getString(Message m){
		String msg = plugin.getConfig().getString(MESSAGES_PREFIX + m.getKey());
		return msg == null ? "" : msg;
	}
	
	public void sendMessage(CommandSender p, Message message, ImmutableMap<String, String> replacements){
		String msg = getString(message);
		if (msg.isEmpty()) return;
		String colorMsg = ChatColor.translateAlternateColorCodes('§', this.pluginPrefix + msg);
		if (replacements != null){
			for (Map.Entry<String, String> entry: replacements.entrySet()){
				colorMsg = colorMsg.replaceAll("\\{" + entry.getKey() + "\\}", entry.getValue());
			}
		}
		p.sendMessage(colorMsg);		
	}
	
	private boolean validateConfig(boolean log){
		if (!this.validateSection("", "", false, log)) return false;
		if (!this.validateSection(MESSAGES_PREFIX, MESSAGES_PREFIX, false, log)) return false;
		return true;
	}
	
	private boolean validateSection(String templatePath, String realPath, boolean deep, boolean log){
		InputStream templateFile = getClass().getClassLoader().getResourceAsStream("config.yml");
		FileConfiguration templateConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(templateFile));

		ConfigurationSection realSection = plugin.getConfig().getConfigurationSection(realPath);
		ConfigurationSection templateSection = templateConfig.getConfigurationSection(templatePath);

		if (realSection == null || templateSection == null) return false;
        
 		for(String key: templateSection.getKeys(deep)){
 			if (!realSection.getKeys(deep).contains(key) || templateSection.get(key).getClass() != realSection.get(key).getClass()){
 				if (log) Bukkit.getLogger().log(Level.WARNING, ZombieApocalypse.logPrefix + "Missing or invalid datatype key '" + key + "' and possibly others in config.yml");
 				return false;
 			}
 		}
 		return true;
	}
	
	public boolean handleUpdate(){
		File oldConfig = new File(plugin.getDataFolder(), "config.yml");
		try {
			ConfigUpdater.update(plugin, "config.yml", oldConfig, Arrays.asList("apocalypse-worlds", "version"));
		} catch (IOException e){
			return false;
		}
		return true;
	}
	
	public void reloadConfig(){
		plugin.reloadConfig();
		this.isValid = this.checkConfig();
		if (isValid) this.loadConfig();
	}

	public List<ZombieType> getZombieTypes(){
		return new ArrayList<>(zombieWrappers.keySet());
	}

	public ZombieWrapper getZombieWrapper(ZombieType type){
		return zombieWrappers.getOrDefault(type, new ZombieWrapper(type));
	}

}
