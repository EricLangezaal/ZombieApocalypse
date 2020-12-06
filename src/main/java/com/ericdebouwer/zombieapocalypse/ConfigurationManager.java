package com.ericdebouwer.zombieapocalypse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class ConfigurationManager {

	ZombieApocalypse plugin;
	private final String MESSAGES_PREFIX = "messages.";
	private final String ZOMBIES_PREFIX = "zombies.";
	
	public String pluginPrefix;
	public boolean doBabies = true;
	private boolean isValid = true;
	
	private Map<ZombieType, String> zombieTypes = new HashMap<>();
	private Map<ZombieType, ItemStack> heads = new HashMap<>(); //cache them to prevent needless reflection
	
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
					Bukkit.getLogger().log(Level.INFO, plugin.logPrefix + "================================================================");
		        	Bukkit.getLogger().log(Level.INFO, plugin.logPrefix + "Automatically updated old/invalid configuration file!");
		        	Bukkit.getLogger().log(Level.INFO, plugin.logPrefix + "================================================================");
		        	return true;
				}
			}
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED +  plugin.logPrefix + "Automatic configuration update failed! You can delete the old 'config.yml' to get a new one.");
		}
		return valid;
	}
	
	public void loadConfig(){
		pluginPrefix = plugin.getConfig().getString("plugin-prefix");
		doBabies = plugin.getConfig().getBoolean("allow-babies");
		
		ConfigurationSection section = plugin.getConfig().getConfigurationSection(ZOMBIES_PREFIX);
		for (String zombie: section.getKeys(false)){
			String headUrl = section.getString(zombie + ".head", "");
			try {
				ZombieType type = ZombieType.valueOf(zombie.toUpperCase());
				zombieTypes.put(type, headUrl);
			} catch (IllegalArgumentException e){
				Bukkit.getConsoleSender().sendMessage(ChatColor.BOLD + "" +ChatColor.RED + plugin.logPrefix +"Zombie type '" + zombie + "' doesn't exist and isn't loaded in.");
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
	
	private boolean validateSection(String template_path, String real_path, boolean deep, boolean log){
		InputStream templateFile = getClass().getClassLoader().getResourceAsStream("config.yml");
                FileConfiguration templateConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(templateFile));
        
                ConfigurationSection real_section = plugin.getConfig().getConfigurationSection(real_path);
                ConfigurationSection template_section = templateConfig.getConfigurationSection(template_path);
        
                if (real_section == null || template_section == null) return false;
        
 		for(String key: template_section.getKeys(deep)){
 			if (!real_section.getKeys(deep).contains(key) || template_section.get(key).getClass() != real_section.get(key).getClass()){
 				if (log) Bukkit.getLogger().log(Level.WARNING, plugin.logPrefix + "Missing or invalid datatype key '" + key + "' and possibly others in config.yml");
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
		return new ArrayList<>(zombieTypes.keySet());
	}
	
	public ItemStack getHead(ZombieType type){
		ItemStack savedHead = heads.get(type);
		if (savedHead != null) return savedHead;
		
		String textureUrl = zombieTypes.get(type);
		if (textureUrl == null || textureUrl.isEmpty()) return null;
		
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta headMeta = (SkullMeta) head.getItemMeta();
		
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", textureUrl).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
        	Bukkit.getLogger().log(Level.INFO,  plugin.logPrefix + "Zombie head could not be set, is the minecraft version correct?");
        }
        
        head.setItemMeta(headMeta);
        heads.put(type, head);
        return head;
	}


}
