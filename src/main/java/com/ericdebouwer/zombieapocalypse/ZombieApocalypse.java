package com.ericdebouwer.zombieapocalypse;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class ZombieApocalypse extends JavaPlugin {
	

	private ZombieEvents zombieEvents;
	private ApocalypseManager apoManager;
	private ConfigurationManager configManager;
	
	boolean isPaperMC = false;
	
	public String logPrefix;

	@Override
	public void onEnable(){
		
		this.logPrefix = "[" + this.getName() + "] ";
		
		configManager = new ConfigurationManager(this);
		if (!configManager.isValid()){
			getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" +ChatColor.RED + logPrefix +"Invalid config.yml, plugin will disable to prevent crashing!");
 			getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + logPrefix + "See the header of the config.yml about fixing the problem.");
			return;
		}
		getServer().getConsoleSender().sendMessage(logPrefix +"Configuration has been successfully loaded!");
		getServer().getConsoleSender().sendMessage(logPrefix +"If you really love this project, you could consider donating to help me keep this project alive! https://paypal.me/3ricL");
		ApocalypseCommand apoCommand = new ApocalypseCommand(this);
		this.getCommand("apocalypse").setExecutor(apoCommand);
		this.getCommand("apocalypse").setTabCompleter(apoCommand);
		
		apoManager = new ApocalypseManager(this);
		getServer().getPluginManager().registerEvents(apoManager, this);
		
		ZombieEgg zombieCommand = new ZombieEgg(this);
		this.getCommand("zombie").setExecutor(zombieCommand);
		this.getCommand("zombie").setTabCompleter(zombieCommand);
		getServer().getPluginManager().registerEvents(zombieCommand, this);
		
		try {
		    isPaperMC = Class.forName("co.aikar.timings.Timings") != null;
		    getServer().getConsoleSender().sendMessage(logPrefix + "PaperMC detected! Changing spawning algorithm accordingly");
		} catch (ClassNotFoundException ignore) {
		}
		
		zombieEvents = new ZombieEvents(this);
		getServer().getPluginManager().registerEvents(zombieEvents, this);
		
	}
	
	@Override
	public void onDisable(){
		if (apoManager != null)
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
	
	public List<String> filter(List<String> original, String query){
		return original.stream().filter(s -> s.startsWith(query)).collect(Collectors.toList());
	}
}
