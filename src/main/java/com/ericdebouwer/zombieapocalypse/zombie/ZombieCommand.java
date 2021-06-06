package com.ericdebouwer.zombieapocalypse.zombie;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.config.Message;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableMap;

public class ZombieCommand implements CommandExecutor, TabCompleter{
	
	ZombieApocalypse plugin;

	private final String EGG_ARG = "egg";
	private final String SPAWNER_ARG = "spawner";
	private final List<String> arguments = Arrays.asList(EGG_ARG, SPAWNER_ARG);
	
	public ZombieCommand(ZombieApocalypse plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command,	String label, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "This command can only be run by a player!");
			return true;
		}
		Player player = (Player) sender;
		if (!sender.hasPermission("apocalypse.zombie")){
			plugin.getConfigManager().sendMessage(player, Message.NO_PERMISSION, null);
			return true;
		}
		
		if (args.length < 2) return false;
		if (!arguments.contains(args[0].toLowerCase())) return false;

		ZombieType type;
		try {
			type = ZombieType.valueOf(args[1].toUpperCase());
		} catch (IllegalArgumentException e){
			plugin.getConfigManager().sendMessage(player, Message.INVALID_ZOMBIE, ImmutableMap.of("input", args[1]));
			return true;
		}

		ItemStack zombieItem = args[0].equalsIgnoreCase(SPAWNER_ARG) ?
				plugin.getZombieItems().getSpawner(type) : plugin.getZombieItems().getSpawnEgg(type);

		player.getInventory().addItem(zombieItem);
		plugin.getConfigManager().sendMessage(player, Message.ITEM_GIVEN, ImmutableMap.of("item_name", zombieItem.getItemMeta().getDisplayName()));
		return true;
	
	}

	private List<String> filter(List<String> original, String query){
		return original.stream().filter(s -> s.startsWith(query.toLowerCase())).collect(Collectors.toList());
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!sender.hasPermission("apocalypse.zombie")) return Collections.emptyList();
		if (args.length == 1)
			return this.filter(arguments, args[0]);

		else if (args.length == 2){
			return this.filter(Stream.of(ZombieType.values()).map(t -> t.toString().toLowerCase()).collect(Collectors.toList()), args[1]);
		}
		return Collections.emptyList();
	}


}
