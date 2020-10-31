package com.ericdebouwer.zombieapocalypse;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class ZombieApocalypse extends JavaPlugin {
	

	private ZombieEvents zombieEvents;
	private ApocalypseManager apoManager;
	private ConfigurationManager configManager;

	@Override
	public void onEnable(){
		configManager = new ConfigurationManager(this);
		if (!configManager.isValid()){
			getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" +ChatColor.RED + configManager.logPrefix +"Invalid config.yml, plugin will disable to prevent crashing!");
 			getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + configManager.logPrefix + "See the header of the config.yml about fixing the problem.");
			return;
		}
		getServer().getConsoleSender().sendMessage(configManager.logPrefix +"Configuration has been successfully loaded!");
		getServer().getConsoleSender().sendMessage(configManager.logPrefix +"If you really love this project, you could consider donating to help me keep this project alive! https://paypal.me/3ricL");
		ApocalypseCommand apoCommand = new ApocalypseCommand(this);
		this.getCommand("apocalypse").setExecutor(apoCommand);
		this.getCommand("apocalypse").setTabCompleter(apoCommand);
		
		apoManager = new ApocalypseManager(this);
		getServer().getPluginManager().registerEvents(apoManager, this);
		
		ZombieEgg zombieCommand = new ZombieEgg(this);
		this.getCommand("zombie").setExecutor(zombieCommand);
		this.getCommand("zombie").setTabCompleter(zombieCommand);
		getServer().getPluginManager().registerEvents(zombieCommand, this);
		
		zombieEvents = new ZombieEvents(this);
		getServer().getPluginManager().registerEvents(zombieEvents, this);
		
	}
	
	@Override
	public void onDisable(){
		apoManager.onDisable();
	}
	
	public ZombieEvents getZombieManager(){
		return this.zombieEvents;
	}
	
	public ApocalypseManager getApocalypseManager() {
		return this.apoManager;
	}
	
	public ConfigurationManager getConfigManager(){
		return this.configManager;
	}
	
	public ArrayList<String> filter(List<String> original, String query){
		ArrayList<String> result = new ArrayList<String>();
		for (String s: original){
			if (s != null && s.startsWith(query)){
				result.add(s);
			}
		}
		return result;
	}
}
