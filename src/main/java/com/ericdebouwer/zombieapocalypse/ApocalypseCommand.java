package com.ericdebouwer.zombieapocalypse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

public class ApocalypseCommand implements CommandExecutor, TabCompleter {
	
	private ZombieApocalypse plugin;
	private ConfigurationManager configManager;
	
	private String START_ARG = "start";
	private String END_ARG = "end";
	private String MOBCAP_ARG = "setlimit";
	private String RELOAD_ARG = "reload";
	
	public List<String> arguments;
	
	public ApocalypseCommand(ZombieApocalypse plugin){
		this.plugin = plugin;
		this.configManager = plugin.getConfigManager();
		this.arguments = Arrays.asList(START_ARG, END_ARG, RELOAD_ARG, MOBCAP_ARG);
	}

	public boolean onCommand(CommandSender sender, Command command,	String label, String[] args) {
		if (!sender.hasPermission("apocalypse.manage")){
			configManager.sendMessage(sender, Message.NO_PERMISSION, null);
			return true;
		}
		if (args.length < 1 || !arguments.contains(args[0].toLowerCase())){
			return false;
		}
		
		if (args[0].equalsIgnoreCase(RELOAD_ARG)){
			configManager.reloadConfig();
			if (configManager.isValid()) configManager.sendMessage(sender, Message.RELOAD_SUCCESS, null);
			else configManager.sendMessage(sender, Message.RELOAD_FAIL, null);
			return true;
		}
		
		if (args.length < 2 && !(sender instanceof Player)){
			configManager.sendMessage(sender, Message.NO_WORLD, null);
			return true;
		}
		String worldName = (args.length < 2) ? ((Player) sender).getWorld().getName() : args[1];
		
		ApocalypseManager manager = plugin.getApocalypseManager();
		
		if (args[0].equalsIgnoreCase(MOBCAP_ARG)){
			if (args.length < 3){
				configManager.sendMessage(sender, Message.CAP_NO_ARGS, null);
				return true;
			}
			if (!manager.isApocalypse(worldName)){
				configManager.sendMessage(sender, Message.CAP_NO_APOCALYPSE, ImmutableMap.of("world_name", worldName));
				return true;
			}
			String lastArg = args[args.length - 1];
			try {
				int cap = Integer.parseInt(lastArg);
				manager.setMobCap(worldName, cap);
				configManager.sendMessage(sender, Message.CAP_SUCCESS, ImmutableMap.of("mobcap", lastArg, "world_name", worldName));
			}catch (NumberFormatException e){
				configManager.sendMessage(sender, Message.CAP_INVALID_AMOUNT, ImmutableMap.of("input", lastArg));
			}
			return true;
		}
		
		if (manager.isApocalypse(worldName) == args[0].equalsIgnoreCase(START_ARG)){
			Message fail = args[0].equalsIgnoreCase(START_ARG)? Message.START_FAIL : Message.END_FAIL;
			configManager.sendMessage(sender, fail, ImmutableMap.of("world_name", worldName));
			return true;
		}
		
		boolean result;
		
		if (args[0].equalsIgnoreCase(START_ARG)){
			long endTime = -1;
			if (args.length >= 3){
				try{
					int duration = Integer.parseInt(args[2]); //minutes
					endTime = java.time.Instant.now().getEpochSecond() + duration * 60;
				}catch (NumberFormatException e){
					configManager.sendMessage(sender, Message.START_INVALID_INT, ImmutableMap.of("input", args[2]));
					return true;
				}
			}
			result = manager.startApocalypse(worldName, endTime);
		}else {
			result = manager.endApocalypse(worldName);
		}
		
		Message success = args[0].equalsIgnoreCase(START_ARG)? Message.START_SUCCESS : Message.END_SUCCESS;
		
		if (result) configManager.sendMessage(sender, success, ImmutableMap.of("world_name", worldName));
		else configManager.sendMessage(sender, Message.INVALID_WORLD, ImmutableMap.of("world_name", worldName));

		return true;
	}


	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!sender.hasPermission("apocalypse.manage")){
			return Collections.emptyList();
		}
		if (args.length == 1){
			return plugin.filter(arguments, args[0]);	
		}
		else if (args.length == 2){
			List<String> worldNames = plugin.getServer().getWorlds().stream().map(w -> w.getName()).collect(Collectors.toList());
			return plugin.filter(worldNames, args[1]);
		}
		else if (args.length == 3 && args[0].equalsIgnoreCase(START_ARG)){
			return plugin.filter(Arrays.asList("5", "10", "60", "240"), args[2]);
		}
		else if (args.length == 3 && args[0].equalsIgnoreCase(MOBCAP_ARG)){
			return plugin.filter(Arrays.asList("70"), args[2]);
		}
		return Collections.emptyList();
	}

}
