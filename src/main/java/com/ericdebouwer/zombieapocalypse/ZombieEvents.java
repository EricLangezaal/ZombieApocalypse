package com.ericdebouwer.zombieapocalypse;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ZombieEvents implements Listener{

	ZombieApocalypse plugin;
	
	public ZombieEvents(ZombieApocalypse plugin){
		this.plugin = plugin;
	}
	
	@EventHandler
	private void onMobSpawn(CreatureSpawnEvent e){
		if (!(plugin.getApocalypseManager().isApocalypse(e.getLocation().getWorld().getName()))) return;
		if (!(e.getEntity() instanceof Monster)) return;
		if (!Arrays.asList(SpawnReason.SPAWNER, SpawnReason.NATURAL, SpawnReason.INFECTION, SpawnReason.SILVERFISH_BLOCK, SpawnReason.DROWNED).contains(e.getSpawnReason())) return;
		
		e.setCancelled(true);
		this.spawnZombie(e.getLocation());
	}
	
	public Zombie spawnZombie(Location loc){
		ZombieType randomType = ZombieType.values()[ThreadLocalRandom.current().nextInt(ZombieType.values().length)];
		return this.spawnZombie(loc, randomType);
	}
	
	public Zombie spawnZombie(Location loc, ZombieType type){
		Zombie zombie;
		if (loc.getWorld().getEnvironment() == Environment.NETHER){
			zombie = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.PIG_ZOMBIE);
		}
		else {
			zombie = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
		}
		zombie = ZombieType.set(zombie, type);
		
		if (type == ZombieType.SPRINTER){
			// very fast
			zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.45);
		}else if (type == ZombieType.TANK){	
			//slow, stronger, more damage
			zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0); 
			zombie.setHealth(40.0);
			zombie.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(3);
			zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
			zombie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(10.0);
		}
		else if (type == ZombieType.NINJA){
			// more damage, faster, less health
			zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.4);
			zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(10.0);
			zombie.setHealth(10.0);
			zombie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(6.0);
		}
		return zombie;
	}
	
	@EventHandler
	private void onDeath(EntityDeathEvent e){
		if (!(e.getEntity() instanceof Zombie)) return;
		Zombie zombie = (Zombie) e.getEntity();
		
		ZombieType type = ZombieType.getType(zombie);
		if (type == ZombieType.BOOMER){
			e.getEntity().getWorld().createExplosion(e.getEntity().getLocation(), 3f, false, true, zombie);
		}
		else if (type == ZombieType.MULTIPLIER){
			int zombieAmount = ThreadLocalRandom.current().nextInt(5);
			
			for (int i=0; i<=zombieAmount;i++){
				double xOffset = ThreadLocalRandom.current().nextDouble() * 2 - 1;
				double zOffset = ThreadLocalRandom.current().nextDouble() * 2 - 1;
				this.spawnZombie(e.getEntity().getLocation().add(xOffset,0, zOffset), ZombieType.DEFAULT);
			}
		}
	}
	
	@EventHandler
	private void throwerHit(final EntityDamageByEntityEvent e){
		if (!(e.getDamager() instanceof Zombie)) return;
		if (!(e.getEntity() instanceof LivingEntity)) return;
		Zombie zombie = (Zombie) e.getDamager();
		ZombieType type = ZombieType.getType(zombie);
		
		if (type == ZombieType.THROWER){
			new BukkitRunnable() {
				public void run() {
					Vector newSpeed = e.getDamager().getLocation().getDirection().multiply(1.5).setY(1.5);
					e.getEntity().setVelocity(newSpeed);
				}
			}.runTask(plugin);
		}
	}
	
	
}
