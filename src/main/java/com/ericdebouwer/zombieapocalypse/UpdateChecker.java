package com.ericdebouwer.zombieapocalypse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class UpdateChecker {
	
	private final JavaPlugin plugin;
	@Getter
	private final int resourceId;
	private Runnable onStart;
	private Runnable onError;
	private Runnable onNoUpdate;
	private BiConsumer<String, String> onOldVersion;
	
	public UpdateChecker onError(Runnable errorTask){
		this.onError = errorTask;
		return this;
	}
	
	public UpdateChecker onOldVersion(BiConsumer<String, String> versionTask){
		this.onOldVersion = versionTask;
		return this;
	}
	
	public UpdateChecker onNoUpdate(Runnable task){
		this.onNoUpdate = task;
		return this;
	}
	
	public UpdateChecker onStart(Runnable task){
		this.onStart = task;
		return this;
	}
	
	public void run(){
		
		String currentVersion = plugin.getDescription().getVersion();
		String apiUrl = "https://api.spigotmc.org/legacy/update.php?resource=";
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, () -> {
			
			if (onStart != null) this.onStart.run();
			
            try (InputStream inputStream = new URL(apiUrl + resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
	               	String latestVersion = scanner.next();

            		if (!isUpToDate(currentVersion, latestVersion)) {
            			onOldVersion.accept(currentVersion, latestVersion);
            		}
            		else {
            			onNoUpdate.run();
            		}
            } catch (IOException | NoSuchElementException ex) {
                onError.run();
            }
        }, 3L);
	}
	
	private boolean isUpToDate(String currentString, String latestString){
		try {
			String[] cParts = currentString.split(Pattern.quote("."));
			String[] lParts = latestString.split(Pattern.quote("."));
			for (int i = 0; i < cParts.length; i++){
				int c = Integer.parseInt(cParts[i]);
				int l = Integer.parseInt(lParts[i]);
				if (c < l) return false;
				if (c > l) return true;
			}
			return lParts.length <= cParts.length;
		} catch (NumberFormatException | IndexOutOfBoundsException ex){
			return true;
		}
	}

}
