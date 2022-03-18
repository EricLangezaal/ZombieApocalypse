package com.ericdebouwer.zombieapocalypse.apocalypse;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.api.Apocalypse;
import com.ericdebouwer.zombieapocalypse.config.Message;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.List;

public class ApocalypseWorld implements Apocalypse {

	public String worldName;
	@Getter
	public long endEpochSecond;
	@Getter
	public BossBar bossBar;
	@Getter @Setter
	public int mobCap;

	private BukkitTask barCountDown;

	private final ZombieApocalypse plugin;
	
	public ApocalypseWorld(ZombieApocalypse plugin, String worldName, long endTime, int mobCap){
		this.plugin = plugin;
		this.worldName = worldName;
		this.endEpochSecond = endTime;
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
		NamespacedKey nameKey = new NamespacedKey(plugin, "apocalypsebar-"  +
				worldName.replaceAll("[^a-zA-Z0-9/._-]", ""));

		String barTitle = plugin.getConfigManager().getString(Message.BOSS_BAR_TITLE);
		this.bossBar = plugin.getServer().createBossBar(nameKey, barTitle, BarColor.PURPLE, BarStyle.SOLID);
		if (plugin.getConfigManager().isBossBarFog()) {
			this.bossBar.addFlag(BarFlag.CREATE_FOG);
		}
		this.bossBar.setProgress(oldProgress);
		this.bossBar.setVisible(plugin.getConfigManager().doBossBar());
	}

	@Override
	public void addPlayer(@Nonnull Player player){
		bossBar.addPlayer(player);
	}

	@Override
	public void removePlayer(@Nonnull Player player){
		bossBar.removePlayer(player);
	}

	public void startCountDown(){
		final double STEPS = 120D;
		long now = java.time.Instant.now().getEpochSecond();
		int period = (int) Math.ceil((this.endEpochSecond - now) / STEPS * 20D);

		barCountDown = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			double progress = bossBar.getProgress() - 1D / STEPS;
			if (progress >= 0) bossBar.setProgress(progress);

		}, period, period);
	}

	public void endCountDown(){
		if (barCountDown != null) barCountDown.cancel();
		barCountDown = null;
	}



}
