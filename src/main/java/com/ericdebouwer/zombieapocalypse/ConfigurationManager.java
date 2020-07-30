package com.ericdebouwer.zombieapocalypse;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

enum Message {
	START_SUCCESS("started-success"),START_BROADCAST ("started-broadcast"),START_FAIL ("start-failed"),
	END_SUCCESS ("ended-success"),END_FAIL ("end-failed"),END_BROADCAST ("ended-broadcast"),
	INVALID_WORLD ("invalid-world"), NO_PERMISSION ("no-command-permission"),
	EGG_GIVEN ("given-zombie-egg"), INVALID_ZOMBIE ("invalid-egg-type"), BOSS_BAR_TITLE ("apocalypse-boss-bar-title");
	
	String key;
	Message(String key){
		this.key = key;
	}
	public String getKey(){ return this.key;}
}

public class ConfigurationManager {

	ZombieApocalypse plugin;
	private final String MESSAGES_PREFIX = "messages.";
	private final String APOCALYPSE_WORLDS = "apocalypse-worlds";
	public String logPrefix;
	public String pluginPrefix;
	private boolean isValid = true;
	
	public ConfigurationManager(ZombieApocalypse plugin){
		this.plugin = plugin;
		plugin.saveDefaultConfig();
		this.logPrefix = "[" + this.plugin.getName() + "] ";
		
		this.isValid = this.validateSection("", "", true);
		
		if (isValid){
			pluginPrefix = plugin.getConfig().getString("plugin-prefix");
		}
	}
	
	public boolean isValid(){
		return this.isValid;
	}
	
	public String getString(Message m){
		String msg = plugin.getConfig().getString(MESSAGES_PREFIX + m.getKey());
		return msg == null ? "" : msg;
	}
	
	public void sendMessage(Player p, Message message, ImmutableMap<String, String> replacements){
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
	
	private boolean validateSection(String template_path, String real_path, boolean deep){
		InputStream templateFile = getClass().getClassLoader().getResourceAsStream("config.yml");
                FileConfiguration templateConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(templateFile));
        
                ConfigurationSection real_section = plugin.getConfig().getConfigurationSection(real_path);
                ConfigurationSection template_section = templateConfig.getConfigurationSection(template_path);
        
                if (real_section == null || template_section == null) return false;
        
 		for(String key: template_section.getKeys(deep)){
 			if (!real_section.getKeys(deep).contains(key) || template_section.get(key).getClass() != real_section.get(key).getClass()){
 				Bukkit.getLogger().log(Level.WARNING, this.logPrefix + "Missing or invalid datatype key '" + key + "' and possibly others in config.yml");
 				return false;
 			}
 		}
 		return true;
	}
	
	public void saveApocalypseWorlds(ArrayList<String> worldNames){
		plugin.reloadConfig();
		plugin.getConfig().set(APOCALYPSE_WORLDS, worldNames);
		//plugin.getConfig().options().copyHeader(false);
		plugin.saveConfig();
	}
	
	public ArrayList<String> getApocalypseWorlds(){
		ArrayList<String> worlds = new ArrayList<String>(plugin.getConfig().getStringList(APOCALYPSE_WORLDS));
		return worlds;
	}
}
