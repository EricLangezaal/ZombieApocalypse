package com.ericdebouwer.zombieapocalypse;

import com.ericdebouwer.zombieapocalypse.apocalypse.ApocalypseCommand;
import com.ericdebouwer.zombieapocalypse.apocalypse.ApocalypseListener;
import com.ericdebouwer.zombieapocalypse.apocalypse.ApocalypseManager;
import com.ericdebouwer.zombieapocalypse.config.ConfigurationManager;
import com.ericdebouwer.zombieapocalypse.integration.silkspawners.SilkZombieItems;
import com.ericdebouwer.zombieapocalypse.integration.silkspawners.SpawnerBreakListener;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieCommand;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieFactory;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieItems;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieListener;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.function.Consumer;

@Getter
public class ZombieApocalypse extends JavaPlugin {

	private ApocalypseManager apocalypseManager;
	private ConfigurationManager configManager;
	private ZombieFactory zombieFactory;
	private ZombieItems zombieItems;
	
	private boolean paperMC = false;

	@Override
	public void onEnable(){
		zombieFactory = new ZombieFactory(this);
		configManager = new ConfigurationManager(this);

		if (!configManager.isValid()){
			getLogger().warning("Invalid config.yml, plugin will disable to prevent crashing!");
			getLogger().warning("See the header of the config.yml about fixing the problem.");
			return;
		}
		getLogger().info("Configuration has been successfully loaded!");
		getLogger().info("If you really love this project, you could consider donating to help me keep this project alive! https://paypal.me/3ricL");

		ApocalypseCommand apoCommand = new ApocalypseCommand(this);
		this.getCommand("apocalypse").setExecutor(apoCommand);
		this.getCommand("apocalypse").setTabCompleter(apoCommand);
		
		apocalypseManager = new ApocalypseManager(this);
		zombieItems = new ZombieItems(this);
		
		ZombieCommand zombieCommand = new ZombieCommand(this);
		this.getCommand("zombie").setExecutor(zombieCommand);
		this.getCommand("zombie").setTabCompleter(zombieCommand);

		try {
			World.class.getMethod("spawn", Location.class, Class.class, CreatureSpawnEvent.SpawnReason.class, Consumer.class);
			paperMC = true;
			getLogger().info("PaperMC detected! Changing spawning algorithm accordingly");
		} catch (NoSuchMethodException ignore) {
		}

		getServer().getPluginManager().registerEvents(new ZombieListener(this), this);
		getServer().getPluginManager().registerEvents(new ApocalypseListener(this), this);


		Plugin silkSpawnerPlugin = getServer().getPluginManager().getPlugin("SilkSpawners");
		if (silkSpawnerPlugin != null && silkSpawnerPlugin.isEnabled()){
			getLogger().info("SilkSpawners detected! Making our spawners comply with it");
			zombieItems = new SilkZombieItems(this);
			getServer().getPluginManager().registerEvents(new SpawnerBreakListener(this), this);
		}

		if (configManager.isCollectMetrics()){
			new Metrics(this, 14674);
		}

		if (configManager.isCheckUpdates()) {
			int resourceId = 82106;
			new UpdateChecker(this, resourceId)
					.onStart(() -> getLogger().info( "Checking for updates..."))
					.onError(() -> getLogger().warning( "Failed to check for updates!"))
					.onOldVersion((oldVersion, newVersion) -> {
						getLogger().info( "Update detected! You are using version " + oldVersion + ", but version " + newVersion + " is available!");
						getLogger().info("You can download the new version here -> https://www.spigotmc.org/resources/" + resourceId + "/updates");
					})
					.onNoUpdate(() -> getLogger().info("You are running the latest version."))
					.run();
		}
	}
	
	@Override
	public void onDisable(){
		if (apocalypseManager != null)
			apocalypseManager.onDisable();
	}
}
