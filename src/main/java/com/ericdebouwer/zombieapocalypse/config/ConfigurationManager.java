package com.ericdebouwer.zombieapocalypse.config;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieType;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

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
	private boolean collectMetrics;

	private boolean removeSkullDrops;
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
		
		this.isValid = this.checkAndUpdateConfig();
		if (isValid) this.loadConfig();
	}

	public boolean checkAndUpdateConfig() {
		final String CONFIG_NAME = "config.yml";
		File currentFile = new File(plugin.getDataFolder(), CONFIG_NAME);
		InputStream templateStream = getClass().getClassLoader().getResourceAsStream(CONFIG_NAME);
		if (templateStream == null) return false;
		FileConfiguration templateConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(templateStream));

		if (this.validateSection(templateConfig, plugin.getConfig(), true, true)) return true;
		try {
			ConfigUpdater.update(plugin, CONFIG_NAME, currentFile, Collections.emptyList());
		} catch (IOException e){
			return false;
		}
		plugin.reloadConfig();
		if (this.validateSection(templateConfig, plugin.getConfig(), true, false)){
			plugin.getLogger().info("Automatically updated old/invalid configuration file!");
			return true;
		}
		plugin.getLogger().warning("Automatic configuration update failed! See the header and the comments of the config.yml about fixing it");
		return false;
	}
	
	public void loadConfig(){
		pluginPrefix = plugin.getConfig().getString("plugin-prefix");
		checkUpdates = plugin.getConfig().getBoolean("check-for-updates", true);
		collectMetrics = plugin.getConfig().getBoolean("collect-bstats-metrics", true);
		removeSkullDrops = plugin.getConfig().getBoolean("remove-skulls-from-death-drops", true);
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
				plugin.getLogger().warning("Zombie type '" + zombie + "' doesn't exist and isn't loaded in.");
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

	private boolean validateSection(FileConfiguration template, FileConfiguration actual, boolean deep, boolean log) {
		if (template == null || actual == null) return false;
		boolean valid = true;

		for(String key: template.getKeys(deep)){
			if (!actual.getKeys(deep).contains(key) || template.get(key).getClass() != actual.get(key).getClass()){
				if (log) plugin.getLogger().log(Level.WARNING, "Missing or invalid datatype key '" + key + "' while parsing config.yml");
				valid = false;
			}
		}
		return valid;
	}
	
	public void reloadConfig(){
		plugin.reloadConfig();
		this.isValid = this.checkAndUpdateConfig();
		if (isValid) this.loadConfig();
	}

}
