package com.ericdebouwer.zombieapocalypse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

public class ApocalypseCommand implements CommandExecutor, TabCompleter {
	
	private ZombieApocalypse plugin;
	private ConfigurationManager messageSender;
	
	public ApocalypseCommand(ZombieApocalypse plugin){
		this.plugin = plugin;
		this.messageSender = plugin.getConfigManager();
	}

	public boolean onCommand(CommandSender sender, Command command,	String label, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "This command can only be run by a player!");
			return true;
		}
		Player player = (Player) sender;
		if (!sender.hasPermission("apocalypse.manage")){
			messageSender.sendMessage(player, Message.NO_PERMISSION, null);
			return true;
		}
		if (args.length < 1){
			return false;
		}
		if (!Arrays.asList("start", "end").contains(args[0].toLowerCase())){
			return false;
		}
		
		String worldName = player.getWorld().getName();
		if (args.length >= 2){
			worldName = args[1];
		}
		ApocalypseManager manager = plugin.getApocalypseManager();
		if (args[0].equalsIgnoreCase("start")){
			if (manager.isApocalypse(worldName)){
				messageSender.sendMessage(player, Message.START_FAIL, ImmutableMap.of("world_name", worldName));
				return true;
			}
			long endTime = -1;
			if (args.length == 3){
				try{
					int duration = Integer.parseInt(args[2]); //minutes
					endTime = java.time.Instant.now().getEpochSecond() + duration * 60;
				}catch (NumberFormatException e){
					messageSender.sendMessage(player, Message.START_INVALID_INT, ImmutableMap.of("input", args[2]));
					return true;
				}
			}
			
			boolean result = manager.startApocalypse(worldName, endTime);
			if (result) messageSender.sendMessage(player, Message.START_SUCCESS, ImmutableMap.of("world_name", worldName));
			else messageSender.sendMessage(player, Message.INVALID_WORLD, ImmutableMap.of("world_name", worldName));
		
		}
		else if (args[0].equalsIgnoreCase("end")){
			if (!manager.isApocalypse(worldName)){
				messageSender.sendMessage(player, Message.END_FAIL, ImmutableMap.of("world_name", worldName));
				return true;
			}
			
			boolean result = manager.endApocalypse(worldName);
			if (result) messageSender.sendMessage(player, Message.END_SUCCESS, ImmutableMap.of("world_name", worldName));
			else messageSender.sendMessage(player, Message.INVALID_WORLD, ImmutableMap.of("world_name", worldName));
		}
		
		return true;	
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!sender.hasPermission("apocalypse.zombie")){
			return Collections.emptyList();
		}
		if (args.length == 1){
			return plugin.filter(Arrays.asList("start", "end"), args[0]);	
		}
		else if (args.length == 2){
			ArrayList<String> worldNames = new ArrayList<String>();
			for (World w: plugin.getServer().getWorlds()){
				worldNames.add(w.getName());
			}
			return plugin.filter(worldNames, args[1]);
		}
		else if (args.length == 3 && args[0].equalsIgnoreCase("start")){
			return plugin.filter(Arrays.asList("5", "10", "60", "240"), args[2]);
		}
		return Collections.emptyList();
	}

}
