package com.ericdebouwer.zombieapocalypse.zombie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.api.ZombieSpawnedEvent;
import com.ericdebouwer.zombieapocalypse.config.Message;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.google.common.collect.ImmutableMap;

public class ZombieEgg implements Listener, CommandExecutor, TabCompleter{
	
	ZombieApocalypse plugin;
	NamespacedKey zombieTypeKey;
	
	public ZombieEgg(ZombieApocalypse plugin) {
		this.plugin = plugin;
		zombieTypeKey = new NamespacedKey(plugin, "ZombieType");
	}

	public boolean onCommand(CommandSender sender, Command command,	String label, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "This command can only be run by a player!");
			return true;
		}
		Player player = (Player) sender;
		if (!sender.hasPermission("apocalypse.manage")){
			plugin.getConfigManager().sendMessage(player, Message.NO_PERMISSION, null);
			return true;
		}
		
		if (args.length < 1) return false;
		
		try {
			ZombieType type = ZombieType.valueOf(args[0].toUpperCase());
			ItemStack zombieEgg = new ItemStack(Material.ZOMBIE_SPAWN_EGG);
			ItemMeta meta = zombieEgg.getItemMeta();
			meta.getPersistentDataContainer().set(zombieTypeKey, PersistentDataType.STRING, type.toString());
			String itemName = WordUtils.capitalizeFully(type.toString()) + " Zombie Egg";
			meta.setDisplayName(ChatColor.RESET + itemName);
			zombieEgg.setItemMeta(meta);
			player.getInventory().addItem(zombieEgg);
			plugin.getConfigManager().sendMessage(player, Message.EGG_GIVEN, ImmutableMap.of("item_name", itemName));
			
		}catch (IllegalArgumentException e){
			plugin.getConfigManager().sendMessage(player, Message.INVALID_ZOMBIE, ImmutableMap.of("input", args[0]));
		}
		return true;
	
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1 && sender.hasPermission("apocalypse.zombie")){
			return Stream.of(ZombieType.values()).map(t -> t.toString().toLowerCase())
					.filter(t -> t.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
	
	@EventHandler
	public void onSpawn(PlayerInteractEvent e){
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null) return;
		if (!(e.getItem().hasItemMeta())) return;
		PersistentDataContainer container = e.getItem().getItemMeta().getPersistentDataContainer();
		if (!container.has(zombieTypeKey, PersistentDataType.STRING)) return;

		e.setCancelled(true);

		try {
			ZombieType type = ZombieType.valueOf(container.get(zombieTypeKey, PersistentDataType.STRING));
			Location spawnLoc = e.getClickedBlock().getLocation().add(0, 1, 0);
			plugin.getZombieFactory().spawnZombie(spawnLoc, type, ZombieSpawnedEvent.SpawnReason.SPAWN_EGG);
		}
		catch (IllegalArgumentException ex){
			e.getPlayer().sendMessage(ChatColor.GRAY + "Invalid zombie egg :(, try getting a new one!");
		}

	}
	

}
