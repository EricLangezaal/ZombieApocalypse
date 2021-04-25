package com.ericdebouwer.zombieapocalypse.zombie;

import java.util.concurrent.ThreadLocalRandom;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.api.ZombieSpawnedEvent;
import com.ericdebouwer.zombieapocalypse.config.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.util.Vector;

public class ZombieListener implements Listener{

	ZombieApocalypse plugin;
	
	public ZombieListener(ZombieApocalypse plugin){
		this.plugin = plugin;
	}
	
	@EventHandler
	private void onMobSpawn(CreatureSpawnEvent e){
		if (!(plugin.getApocalypseManager().isApocalypse(e.getLocation().getWorld().getName()))) return;
		if (!(e.getEntity() instanceof Monster)) return;
		
		if (e.getEntity() instanceof Zombie && ZombieType.getType((Zombie) e.getEntity()) != null) return;
		
		e.setCancelled(true);
		plugin.getZombieFactory().spawnZombie(e.getLocation());
	}
	
	@EventHandler
	private void onDeath(EntityDeathEvent e){
		if (!(e.getEntity() instanceof Zombie)) return;
		Zombie zombie = (Zombie) e.getEntity();
		
		e.getDrops().removeIf(i -> i.getType() == Material.PLAYER_HEAD);
		
		ZombieType type = ZombieType.getType(zombie);
		if (type == ZombieType.BOOMER){
			e.getEntity().getWorld().createExplosion(e.getEntity().getLocation(), 3f, false,
					plugin.getConfigManager().blockDamage, zombie);
		}
		else if (type == ZombieType.MULTIPLIER){
			int zombieAmount = ThreadLocalRandom.current().nextInt(5);
			
			for (int i = 0; i <= zombieAmount; i++){
				double xOffset = ThreadLocalRandom.current().nextDouble() * 2 - 1;
				double zOffset = ThreadLocalRandom.current().nextDouble() * 2 - 1;
				plugin.getZombieFactory().spawnZombie(e.getEntity().getLocation().add(xOffset,0, zOffset),
						ZombieType.DEFAULT, ZombieSpawnedEvent.SpawnReason.ZOMBIE_EFFECT);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	private void throwerHit(final EntityDamageByEntityEvent e){
		if (!(e.getDamager() instanceof Zombie)) return;
		if (!(e.getEntity() instanceof LivingEntity)) return;
		Zombie zombie = (Zombie) e.getDamager();
		ZombieType type = ZombieType.getType(zombie);
		
		if (type == ZombieType.THROWER){
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				Vector newSpeed = e.getDamager().getLocation().getDirection().multiply(1.5).setY(1.5);
				e.getEntity().setVelocity(newSpeed);
			});
		}
	}

	@EventHandler
	private void onBurn(EntityCombustEvent event){
		if (plugin.getConfigManager().burnInDay) return;
		if (!(event.getEntity() instanceof Zombie)) return;

		ZombieType type = ZombieType.getType((Zombie) event.getEntity());
		if (type != null) event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	private void onSleep(PlayerBedEnterEvent event){
		if (plugin.getConfigManager().allowSleep) return;
		if (!plugin.getApocalypseManager().isApocalypse(event.getPlayer().getWorld().getName())) return;

		plugin.getConfigManager().sendMessage(event.getPlayer(), Message.NO_SLEEP, null);
		event.setCancelled(true);
	}
	
}
