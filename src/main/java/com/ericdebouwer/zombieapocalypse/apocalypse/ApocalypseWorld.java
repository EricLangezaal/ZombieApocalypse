package com.ericdebouwer.zombieapocalypse.apocalypse;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.api.Apocalypse;
import com.ericdebouwer.zombieapocalypse.config.Message;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class ApocalypseWorld implements Apocalypse {

	public String worldName;
	public long endTime;
	public BossBar bossBar;
	public int mobCap;

	private BukkitTask barCountDown;

	private ZombieApocalypse plugin;
	
	public ApocalypseWorld(ZombieApocalypse plugin, String worldName, long endTime, int mobCap){
		this.plugin = plugin;
		this.worldName = worldName;
		this.endTime = endTime;
		this.mobCap = mobCap;
		loadBossBar(1.0);
	}

	public void reloadBossBar(){
		List<Player> players = this.bossBar.getPlayers();
		double progress = bossBar.getProgress();
		bossBar.removeAll();
		this.loadBossBar(progress);
		for (Player player: players){
			bossBar.addPlayer(player);
		}
	}

	private void loadBossBar(double oldProgress){
		NamespacedKey nameKey = new NamespacedKey(plugin, "apocalypsebar-"  + worldName);
		String barTitle = plugin.getConfigManager().getString(Message.BOSS_BAR_TITLE);

		if (plugin.getConfigManager().bossBarFog){
			this.bossBar = plugin.getServer().createBossBar(nameKey, barTitle, BarColor.PURPLE, BarStyle.SOLID, BarFlag.CREATE_FOG);
		}else {
			this.bossBar = plugin.getServer().createBossBar(nameKey, barTitle, BarColor.PURPLE, BarStyle.SOLID);
		}

		this.bossBar.setProgress(oldProgress);
		this.bossBar.setVisible(plugin.getConfigManager().doBossBar);
	}
	
	public void setMobCap(int mobCap){
		this.mobCap = mobCap;
	}

	@Override
	public BossBar getBossBar() {
		return bossBar;
	}

	@Override
	public void addPlayer(Player player){
		bossBar.addPlayer(player);
	}

	@Override
	public void removePlayer(Player player){
		bossBar.removePlayer(player);
	}

	@Override
	public int getMobCap() {
		return mobCap;
	}

	@Override
	public long getEndEpochSecond() {
		return  endTime;
	}

	public void startCountDown(){
		final double STEPS = 120D;
		long now = java.time.Instant.now().getEpochSecond();
		int period = (int) Math.ceil((this.endTime - now) / STEPS * 20D);
		barCountDown = new BukkitRunnable(){
			@Override
			public void run() {
				double progress = bossBar.getProgress() - 1D / STEPS;
				if (progress >= 0) bossBar.setProgress(progress);
			}
		}.runTaskTimer(plugin, period, period);
	}

	public void endCountDown(){
		if (barCountDown != null) barCountDown.cancel();
		barCountDown = null;
	}



}
