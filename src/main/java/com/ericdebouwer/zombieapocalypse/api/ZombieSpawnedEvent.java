package com.ericdebouwer.zombieapocalypse.api;

import com.ericdebouwer.zombieapocalypse.zombie.ZombieType;
import org.bukkit.Location;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

/**
 * Called when a zombie has fully spawned, allowing for altering of the zombie.
 * If you want to optionally cancel the spawn, use {@link ZombiePreSpawnEvent}. <br>
 *
 * <b>Author</b> - 3ricL (Ericdebouwer).
 */
public class ZombieSpawnedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Location location;
    private final ZombieType type;
    private final Zombie zombie;
    private final SpawnReason reason;

    public ZombieSpawnedEvent(Location location, ZombieType type, SpawnReason reason, Zombie zombie){
        this.location = location;
        this.type = type;
        this.zombie = zombie;
        this.reason = reason;
    }

    /**
     * Get the location of the zombie spawn
     *
     * @return the location of the zombie spawn.
     */
    public @Nonnull Location getLocation(){
        return location;
    }

    /**
     * Get the cause of this zombie spawn.
     *
     * @return an instance of {@link SpawnReason}
     */
    public @Nonnull SpawnReason getSpawnReason() { return reason; }


    /**
     * Get the type of the zombie that just spawned
     *
     * @return the type of the zombie spawn.
     */
    public @Nonnull ZombieType getType(){
        return type;
    }

    /**
     * Get the zombie that has just spawned. Modifying it will modify the actual zombie.
     *
     * @return an instance of {@link org.bukkit.entity.Zombie}
     */
    public @Nonnull Zombie getZombie() { return zombie;}

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * An enum to denote the cause of a zombie spawn.
     */
    public enum SpawnReason {
        /**
         * When a zombie spawns due to an ongoing apocalypse.
         * In this case a vanilla monster spawn has been converted to a custom zombie.
         */
        APOCALYPSE,
        /**
         * When a zombie spawns due to a special effect a zombie has.
         * Examples are zombies on top of a pillar zombie, or zombies spawned when a multiplier dies.
         */
        ZOMBIE_EFFECT,
        /**
         * When a zombie spawns due to a custom spawn egg being used.
         */
        SPAWN_EGG,
        /**
         * When a zombie spawns from a custom spawner.
         */
        CUSTOM_SPAWNER,
        /**
         * When a zombie spawns due to a plugin (yours potentially) using this API
         */
        API
    }
}
