package com.ericdebouwer.zombieapocalypse;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
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
		//if (!Arrays.asList(SpawnReason.SPAWNER, SpawnReason.NATURAL, SpawnReason.INFECTION, SpawnReason.SILVERFISH_BLOCK, SpawnReason.DROWNED).contains(e.getSpawnReason())) return;
		
		if (e.getEntity() instanceof Zombie && ZombieType.getType((Zombie) e.getEntity()) != null) return;
		
		e.setCancelled(true);
		this.spawnZombie(e.getLocation());
	}
	
	public Zombie spawnZombie(Location loc){
		List<ZombieType> types = plugin.getConfigManager().getZombieTypes();
		if (types.isEmpty()) types = Arrays.asList(ZombieType.values());
		ZombieType randomType = types.get(ThreadLocalRandom.current().nextInt(types.size()));
		return this.spawnZombie(loc, randomType);
	}
	
	public Zombie spawnZombie(Location loc, ZombieType type){
		Zombie zombie = this.spawnForEnvironment(loc, type);
		zombie.setRemoveWhenFarAway(true);
		if (zombie.getVehicle() != null){
			zombie.getVehicle().remove();
		}
		
		ItemStack head = plugin.getConfigManager().getHead(type);
		if (head != null)
			zombie.getEquipment().setHelmet(head);
		
		if (!plugin.getConfigManager().doBabies)
			zombie.setBaby(false);
		
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
		else if (type == ZombieType.JUMPER){
			// type, duration, amplifier, extra particles , particles, icon in player inv
			zombie.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 5, false, false, false));
		}
		else if (type == ZombieType.PILLAR){
			zombie.setBaby(false);
			int passengers = ThreadLocalRandom.current().nextInt(4) + 1; // [1-4], so 2-5 high
			Zombie lowerZombie = zombie;
			for (int i = 1; i <= passengers; i++){
				Zombie newZombie = this.spawnZombie(loc.add(0, 1.5, 0), ZombieType.DEFAULT);
				newZombie.setBaby(false);
				lowerZombie.addPassenger(newZombie);
				lowerZombie = newZombie;
			}
		}
		return zombie;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Zombie spawnForEnvironment(Location loc, ZombieType type){
		Class environmentType = (loc.getWorld().getEnvironment() == Environment.NETHER) ? PigZombie.class : Zombie.class;
		Consumer<Zombie> action =  new Consumer<Zombie>() {
			@Override
			public void accept(Zombie zomb){
				ZombieType.set(zomb, type);
			}
		};
		Zombie zombie;
		if (plugin.isPaperMC){
			// as Paper ignores all other spawns in mob cap
			zombie = loc.getWorld().spawn(loc, environmentType , SpawnReason.NATURAL, action);
		}
		else {
			zombie = loc.getWorld().spawn(loc, environmentType, action);
		}
		return zombie;
	}
	
	@EventHandler
	private void onDeath(EntityDeathEvent e){
		if (!(e.getEntity() instanceof Zombie)) return;
		Zombie zombie = (Zombie) e.getEntity();
		
		e.getDrops().removeIf(i -> i.getType() == Material.PLAYER_HEAD);
		
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
