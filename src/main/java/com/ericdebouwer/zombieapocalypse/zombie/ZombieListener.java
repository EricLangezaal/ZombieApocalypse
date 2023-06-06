package com.ericdebouwer.zombieapocalypse.zombie;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.api.ZombieSpawnedEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
public class ZombieListener implements Listener {

	private final ZombieApocalypse plugin;
	private final Set<CreatureSpawnEvent.SpawnReason> ignoreReasons = EnumSet.of(CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM,
			CreatureSpawnEvent.SpawnReason.BUILD_WITHER, CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN, CreatureSpawnEvent.SpawnReason.CUSTOM,
			CreatureSpawnEvent.SpawnReason.SPAWNER_EGG, CreatureSpawnEvent.SpawnReason.CURED, CreatureSpawnEvent.SpawnReason.RAID);
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	private void onMobSpawn(CreatureSpawnEvent event){
		if (!(plugin.getApocalypseManager().isApocalypse(event.getLocation().getWorld().getName()))) return;
		if (!(event.getEntity() instanceof Monster)) return;
		if (ignoreReasons.contains(event.getSpawnReason())) return;

		if (event.getEntity() instanceof Zombie && ZombieType.getType((Zombie) event.getEntity()) != null) return;
		if (event.getEntity().hasMetadata("ignoreZombie")) return;

		event.setCancelled(true);
		plugin.getZombieFactory().spawnApocalypseZombie(event.getLocation());
	}
	
	@EventHandler
	private void onDeath(EntityDeathEvent event){
		if (!(event.getEntity() instanceof Zombie)) return;
		Zombie zombie = (Zombie) event.getEntity();
		
		event.getDrops().removeIf(i -> i.getType() == Material.PLAYER_HEAD);
		
		ZombieType type = ZombieType.getType(zombie);
		if (type == ZombieType.BOOMER){
			event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(), 3f, false,
					plugin.getConfigManager().isBlockDamage(), zombie);
		}
		else if (type == ZombieType.MULTIPLIER){
			int zombieAmount = ThreadLocalRandom.current().nextInt(5);
			
			for (int i = 0; i <= zombieAmount; i++){
				double xOffset = ThreadLocalRandom.current().nextDouble() * 2 - 1;
				double zOffset = ThreadLocalRandom.current().nextDouble() * 2 - 1;
				plugin.getZombieFactory().spawnZombie(event.getEntity().getLocation().add(xOffset,0, zOffset),
						ZombieType.DEFAULT, ZombieSpawnedEvent.SpawnReason.ZOMBIE_EFFECT);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	private void throwerHit(final EntityDamageByEntityEvent event){
		if (!(event.getDamager() instanceof Zombie)) return;
		if (!(event.getEntity() instanceof LivingEntity)) return;
		Zombie zombie = (Zombie) event.getDamager();
		ZombieType type = ZombieType.getType(zombie);
		
		if (type == ZombieType.THROWER){
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				Vector newSpeed = event.getDamager().getLocation().getDirection().multiply(1.5).setY(1.5);
				event.getEntity().setVelocity(newSpeed);
			});
		}
	}

	@EventHandler
	private void onBurn(EntityCombustEvent event){
		if (plugin.getConfigManager().isBurnInDay()) return;
		if (!(event.getEntity() instanceof Zombie)) return;

		ZombieType type = ZombieType.getType((Zombie) event.getEntity());
		if (type != null) event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	private void onSpawnerSpawn(SpawnerSpawnEvent event){
		ZombieType type = plugin.getZombieItems().getZombieType(event.getSpawner());
		if (type == null) return;

		event.getEntity().remove(); // cancelling will make it respawn too quickly
		plugin.getZombieFactory().spawnZombie(event.getLocation(), type, ZombieSpawnedEvent.SpawnReason.CUSTOM_SPAWNER);
	}

	@EventHandler
	public void onEggSpawn(PlayerInteractEvent event){
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getItem() == null) return;
		if (event.getItem().getType() != Material.ZOMBIE_SPAWN_EGG) return;

		ZombieType type = plugin.getZombieItems().getZombieType(event.getItem().getItemMeta());
		if (type == null) return;

		event.setCancelled(true);

		Location spawnLoc = event.getClickedBlock().getLocation().add(0, 1, 0);
		plugin.getZombieFactory().spawnZombie(spawnLoc, type, ZombieSpawnedEvent.SpawnReason.SPAWN_EGG);

		if (event.getPlayer().getGameMode() == GameMode.SURVIVAL){
			if (event.getItem().getAmount() > 1) {
				event.getItem().setAmount(event.getItem().getAmount() - 1);
				event.getPlayer().getInventory().setItem(event.getHand(), event.getItem());
			} else event.getPlayer().getInventory().setItem(event.getHand(), null);
		}
	}

	@EventHandler
	public void onZombieClick(PlayerInteractEntityEvent event){
		if (event.getPlayer().getEquipment() == null) return;

		ItemStack hand = event.getPlayer().getEquipment().getItem(event.getHand());
		if (hand.getType() != Material.ZOMBIE_SPAWN_EGG) return;

		ZombieType type = plugin.getZombieItems().getZombieType(hand.getItemMeta());
		if (type == null) return;
		event.setCancelled(true);
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
