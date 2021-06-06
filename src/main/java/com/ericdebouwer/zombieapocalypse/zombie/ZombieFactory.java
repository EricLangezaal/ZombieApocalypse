package com.ericdebouwer.zombieapocalypse.zombie;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.api.ZombiePreSpawnEvent;
import com.ericdebouwer.zombieapocalypse.api.ZombieSpawnedEvent;
import com.ericdebouwer.zombieapocalypse.config.ZombieWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ZombieFactory {

    private final ZombieApocalypse plugin;
    private final Map<ZombieType, ZombieWrapper> zombieWrappers = new HashMap<>();

    public ZombieFactory(ZombieApocalypse plugin){
        this.plugin = plugin;
    }

    public void reload() {
        zombieWrappers.clear();
    }

    public void addZombieWrapper(ZombieWrapper wrapper){
        zombieWrappers.put(wrapper.getType(), wrapper);
    }

    private ZombieType getRandomZombieType(){
        List<ZombieType> types = new ArrayList<>(zombieWrappers.keySet());
        if (types.isEmpty()) types = Arrays.asList(ZombieType.values());
        return types.get(ThreadLocalRandom.current().nextInt(types.size()));
    }

    // only invoked for regular apocalypse zombies
    public void spawnApocalypseZombie(Location loc){
        ZombiePreSpawnEvent preSpawnEvent = new ZombiePreSpawnEvent(loc, getRandomZombieType());
        Bukkit.getServer().getPluginManager().callEvent(preSpawnEvent);

        if (!preSpawnEvent.isCancelled()){
            this.spawnZombie(loc, preSpawnEvent.getType(), ZombieSpawnedEvent.SpawnReason.APOCALYPSE);
        }
    }

    public Zombie spawnZombie(Location loc, ZombieSpawnedEvent.SpawnReason reason){
        return this.spawnZombie(loc, getRandomZombieType(), reason);
    }

    public Zombie spawnZombie(Location loc, ZombieType type, ZombieSpawnedEvent.SpawnReason reason){
        Zombie zombie = this.spawnForEnvironment(loc, type);
        zombie.setRemoveWhenFarAway(true);
        if (zombie.getVehicle() != null){
            zombie.getVehicle().remove();
        }

        ZombieWrapper wrapper = zombieWrappers.getOrDefault(type, new ZombieWrapper(type));
        zombie = wrapper.apply(zombie);

        if (!plugin.getConfigManager().doBabies)
            zombie.setBaby(false);

        if (type == ZombieType.JUMPER){
            // type, duration, amplifier, extra particles , particles, icon in player inv
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 5, false, false, false));
        }
        else if (type == ZombieType.PILLAR){
            zombie.setBaby(false);
            int passengers = ThreadLocalRandom.current().nextInt(4) + 1; // [1-4], so 2-5 high
            Zombie lowerZombie = zombie;
            for (int i = 1; i <= passengers; i++){
                Zombie newZombie = this.spawnZombie(loc.add(0, 1.5, 0), ZombieType.DEFAULT, ZombieSpawnedEvent.SpawnReason.ZOMBIE_EFFECT);
                newZombie.setBaby(false);
                lowerZombie.addPassenger(newZombie);
                lowerZombie = newZombie;
            }
        }

        ZombieSpawnedEvent spawnedEvent = new ZombieSpawnedEvent(loc, type, reason, zombie);
        Bukkit.getServer().getPluginManager().callEvent(spawnedEvent);
        return spawnedEvent.getZombie();
    }

    @SuppressWarnings({ "unchecked", "rawtypes"})
    private Zombie spawnForEnvironment(Location loc, ZombieType type){
        boolean isNether = loc.getWorld().getEnvironment() == World.Environment.NETHER;
        Class environmentType = (isNether && plugin.getConfigManager().doNetherPigmen) ? PigZombie.class : Zombie.class;

        if (loc.getBlock().getType() == Material.WATER){
            environmentType = Drowned.class;
        }

        Zombie zombie;
        if (plugin.isPaperMC){
            // as Paper ignores all other spawns in mob cap
            zombie = loc.getWorld().spawn(loc, environmentType, type::set, CreatureSpawnEvent.SpawnReason.NATURAL);
        }
        else {
            zombie = loc.getWorld().spawn(loc, environmentType, type::set);
        }

        if (zombie.getType() == EntityType.ZOMBIE && isNether){
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1, false, false));
        }

        return zombie;
    }
}
