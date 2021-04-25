package com.ericdebouwer.zombieapocalypse;

import java.util.List;
import java.util.stream.Collectors;

import com.ericdebouwer.zombieapocalypse.apocalypse.ApocalypseCommand;
import com.ericdebouwer.zombieapocalypse.apocalypse.ApocalypseListener;
import com.ericdebouwer.zombieapocalypse.apocalypse.ApocalypseManager;
import com.ericdebouwer.zombieapocalypse.config.ConfigurationManager;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieEgg;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieListener;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieFactory;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class ZombieApocalypse extends JavaPlugin {

	private ApocalypseManager apoManager;
	private ConfigurationManager configManager;
	private ZombieFactory zombieFactory;
	
	public boolean isPaperMC = false;
	
	public String logPrefix;

	@Override
	public void onEnable(){
		logPrefix = "[" + this.getName() + "] ";
		
		configManager = new ConfigurationManager(this);
		if (!configManager.isValid()){
			getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + logPrefix + "Invalid config.yml, plugin will disable to prevent crashing!");
 			getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + logPrefix + "See the header of the config.yml about fixing the problem.");
			return;
		}
		getLogger().info("Configuration has been successfully loaded!");
		getLogger().info("If you really love this project, you could consider donating to help me keep this project alive! https://paypal.me/3ricL");

		ApocalypseCommand apoCommand = new ApocalypseCommand(this);
		this.getCommand("apocalypse").setExecutor(apoCommand);
		this.getCommand("apocalypse").setTabCompleter(apoCommand);
		
		apoManager = new ApocalypseManager(this);
		zombieFactory = new ZombieFactory(this);
		
		ZombieEgg zombieCommand = new ZombieEgg(this);
		this.getCommand("zombie").setExecutor(zombieCommand);
		this.getCommand("zombie").setTabCompleter(zombieCommand);
		getServer().getPluginManager().registerEvents(zombieCommand, this);
		
		try {
			Class.forName("co.aikar.timings.Timings");
		    isPaperMC = true;
		    getServer().getConsoleSender().sendMessage(logPrefix + "PaperMC detected! Changing spawning algorithm accordingly");
		} catch (ClassNotFoundException ignore) {
		}

		getServer().getPluginManager().registerEvents(new ZombieListener(this), this);
		getServer().getPluginManager().registerEvents(new ApocalypseListener(this), this);

		if (configManager.checkUpdates) {
			new UpdateChecker(this)
					.onStart(() -> getLogger().info( "Checking for updates..."))
					.onError(() -> getLogger().warning( "Failed to check for updates!"))
					.onOldVersion((oldVersion, newVersion) -> {
						getLogger().info( "Update detected! You are using version " + oldVersion + ", but version " + newVersion + " is available!");
						getLogger().info("You can download the new version here -> https://www.spigotmc.org/resources/" +  UpdateChecker.RESOURCE_ID + "/updates");
					})
					.onNoUpdate(() -> getLogger().info("You are running the latest version."))
					.run();
		}
		
	}
	
	@Override
	public void onDisable(){
		if (apoManager != null)
			apoManager.onDisable();
	}
	
	public ApocalypseManager getApocalypseManager() {
		return this.apoManager;
	}
	
	public ConfigurationManager getConfigManager(){
		return this.configManager;
	}

	public ZombieFactory getZombieFactory(){return this.zombieFactory;}
}
