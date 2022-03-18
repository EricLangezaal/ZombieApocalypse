package com.ericdebouwer.zombieapocalypse.config;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieType;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;

@Getter
public class ConfigurationManager {

	@Getter(AccessLevel.NONE)
	private final ZombieApocalypse plugin;
	@Getter(AccessLevel.NONE)
	private final String MESSAGES_PREFIX = "messages.";
	@Getter(AccessLevel.NONE)
	private final String ZOMBIES_PREFIX = "zombies.";
	@Getter(AccessLevel.NONE)
	private String pluginPrefix;

	private boolean checkUpdates;
	@Accessors(fluent=true)
	private boolean doBabies;
	@Accessors(fluent=true)
	private boolean doNetherPigmen;
	private boolean burnInDay;
	private boolean blockDamage;
	@Accessors(fluent=true)
	private boolean doBossBar;
	private boolean bossBarFog;
	private boolean removeZombiesOnEnd;
	private boolean allowSleep;
	private boolean isValid;
	
	public ConfigurationManager(ZombieApocalypse plugin){
		this.plugin = plugin;
		plugin.saveDefaultConfig();
		
		this.isValid = this.checkConfig();
		if (isValid) this.loadConfig();
	}

	private boolean checkConfig(){
		boolean valid = this.validateConfig(true);
		if (!valid){
			if (handleUpdate()){
				plugin.reloadConfig();
				if (validateConfig(false)) {
					plugin.getLogger().info("================================================================");
					plugin.getLogger().info("Automatically updated old/invalid configuration file!");
					plugin.getLogger().info("================================================================");
		        	return true;
				}
			}
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + plugin.getLogPrefix() + "Automatic configuration update failed! You can delete the old 'config.yml' to get a new one.");
		}
		return valid;
	}
	
	public void loadConfig(){
		pluginPrefix = plugin.getConfig().getString("plugin-prefix");
		checkUpdates = plugin.getConfig().getBoolean("check-for-updates", true);
		doBabies = plugin.getConfig().getBoolean("allow-babies", true);
		doNetherPigmen = plugin.getConfig().getBoolean("spawn-pigmen-in-nether", true);
		burnInDay = plugin.getConfig().getBoolean("burn-in-day", true);
		blockDamage = plugin.getConfig().getBoolean("do-zombie-block-damage", true);
		doBossBar = plugin.getConfig().getBoolean("do-bossbar", true);
		bossBarFog = plugin.getConfig().getBoolean("bossbar-fog", true);
		allowSleep = plugin.getConfig().getBoolean("allow-sleep", true);
		removeZombiesOnEnd = plugin.getConfig().getBoolean("remove-zombies-after-apocalypse", true);

		ConfigurationSection section = plugin.getConfig().getConfigurationSection(ZOMBIES_PREFIX);
		for (String zombie: section.getKeys(false)){
			if (!section.getBoolean(zombie + ".enabled")) continue;
			try {
				ZombieType type = ZombieType.valueOf(zombie.toUpperCase());
				ZombieWrapper wrapper = new ZombieWrapper(type, section.getConfigurationSection(zombie));
				plugin.getZombieFactory().addZombieWrapper(wrapper);
			} catch (IllegalArgumentException e){
				Bukkit.getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + plugin.getLogPrefix() + "Zombie type '" + zombie + "' doesn't exist and isn't loaded in.");
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
				colorMsg = colorMsg.replace("{"  + entry.getKey() + "}", entry.getValue());
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
 				if (log) plugin.getLogger().warning("Missing or invalid datatype key '" + key + "' and possibly others in config.yml");
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

}
