package com.ericdebouwer.zombieapocalypse.zombie;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.api.ZombieSpawnedEvent;
import org.bukkit.*;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class ZombieListener implements Listener {

	private final ZombieApocalypse plugin;

	private final Set<CreatureSpawnEvent.SpawnReason> ignoreReasons = EnumSet.of(CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM,
			CreatureSpawnEvent.SpawnReason.BUILD_WITHER, CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN, CreatureSpawnEvent.SpawnReason.CUSTOM,
			CreatureSpawnEvent.SpawnReason.SPAWNER_EGG, CreatureSpawnEvent.SpawnReason.CURED);
	
	public ZombieListener(ZombieApocalypse plugin){
		this.plugin = plugin;
	}
	
	@EventHandler
	private void onMobSpawn(CreatureSpawnEvent e){
		if (!(plugin.getApocalypseManager().isApocalypse(e.getLocation().getWorld().getName()))) return;
		if (!(e.getEntity() instanceof Monster)) return;
		if (ignoreReasons.contains(e.getSpawnReason())) return;

		if (e.getEntity() instanceof Zombie && ZombieType.getType((Zombie) e.getEntity()) != null) return;
		if (e.getEntity().hasMetadata("ignoreZombie")) return;

		e.setCancelled(true);
		plugin.getZombieFactory().spawnApocalypseZombie(e.getLocation());
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

	@EventHandler(priority = EventPriority.HIGH)
	private void onSpawnerSpawn(SpawnerSpawnEvent event){
		ZombieType type = plugin.getZombieItems().getZombieType(event.getSpawner());
		if (type == null) return;

		event.setCancelled(true);
		plugin.getZombieFactory().spawnZombie(event.getLocation(), type, ZombieSpawnedEvent.SpawnReason.CUSTOM_SPAWNER);
	}

	@EventHandler
	public void onEggSpawn(PlayerInteractEvent e){
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null) return;
		if (e.getItem().getType() != Material.ZOMBIE_SPAWN_EGG) return;

		ZombieType type = plugin.getZombieItems().getZombieType(e.getItem().getItemMeta());
		if (type == null) return;

		e.setCancelled(true);

		Location spawnLoc = e.getClickedBlock().getLocation().add(0, 1, 0);
		plugin.getZombieFactory().spawnZombie(spawnLoc, type, ZombieSpawnedEvent.SpawnReason.SPAWN_EGG);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event){
		ZombieType type = plugin.getZombieItems().getZombieType(event.getItemInHand().getItemMeta());
		if (type == null) return;

		if (!(event.getBlock().getState() instanceof CreatureSpawner)) return;

		CreatureSpawner spawner = (CreatureSpawner) event.getBlock().getState();

		spawner.getPersistentDataContainer().set(plugin.getZombieItems().getKey(), PersistentDataType.STRING, type.toString());
		spawner.setSpawnedType(EntityType.ZOMBIE);
		spawner.update();
	}
	
}
