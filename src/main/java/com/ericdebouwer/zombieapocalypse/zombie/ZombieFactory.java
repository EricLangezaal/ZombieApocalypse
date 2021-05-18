package com.ericdebouwer.zombieapocalypse.zombie;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.api.ZombiePreSpawnEvent;
import com.ericdebouwer.zombieapocalypse.api.ZombieSpawnedEvent;
import com.ericdebouwer.zombieapocalypse.config.ZombieWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Consumer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ZombieFactory {

    private final ZombieApocalypse plugin;

    public ZombieFactory(ZombieApocalypse plugin){
        this.plugin = plugin;
    }

    // only invoked for regular apocalypse zombies
    public void spawnZombie(Location loc){
        List<ZombieType> types = plugin.getConfigManager().getZombieTypes();
        if (types.isEmpty()) types = Arrays.asList(ZombieType.values());
        ZombieType randomType = types.get(ThreadLocalRandom.current().nextInt(types.size()));

        ZombiePreSpawnEvent preSpawnEvent = new ZombiePreSpawnEvent(loc, randomType);
        Bukkit.getServer().getPluginManager().callEvent(preSpawnEvent);

        if (!preSpawnEvent.isCancelled()){
            this.spawnZombie(loc, preSpawnEvent.getType(), ZombieSpawnedEvent.SpawnReason.APOCALYPSE);
        }
    }

    public Zombie spawnZombie(Location loc, ZombieType type, ZombieSpawnedEvent.SpawnReason reason){
        Zombie zombie = this.spawnForEnvironment(loc, type);
        zombie.setRemoveWhenFarAway(true);
        if (zombie.getVehicle() != null){
            zombie.getVehicle().remove();
        }

        ZombieWrapper wrapper = plugin.getConfigManager().getZombieWrapper(type);
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

        Consumer<Zombie> action = zomb -> ZombieType.set(zomb, type);
        Zombie zombie;
        if (plugin.isPaperMC){
            // as Paper ignores all other spawns in mob cap
            zombie = loc.getWorld().spawn(loc, environmentType , action, CreatureSpawnEvent.SpawnReason.NATURAL);
        }
        else {
            zombie = loc.getWorld().spawn(loc, environmentType, action);
        }

        if (zombie.getType() == EntityType.ZOMBIE && isNether){
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1, false, false));
        }

        return zombie;
    }
}
