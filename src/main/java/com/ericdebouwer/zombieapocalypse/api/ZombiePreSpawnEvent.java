package com.ericdebouwer.zombieapocalypse.api;

import com.ericdebouwer.zombieapocalypse.zombie.ZombieType;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

/**
 * Called when a zombie is about to spawn due to the zombie apocalypse only
 * If ZombiePreSpawnEvent is cancelled, the zombie will not spawn.  <br>
 *
 * If you want to modify the actual zombie, use {@link ZombieSpawnedEvent}.
 *
 * <b>Author</b> - 3ricL (Ericdebouwer).
 */
public class ZombiePreSpawnEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private Location location;
    private ZombieType type;

    public ZombiePreSpawnEvent(Location location, ZombieType type){
        this.location = location;
        this.type = type;
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
     * Get the type of the zombie that just spawned
     *
     * @return the type of the zombie spawn.
     */
    public @Nonnull ZombieType getType(){
        return type;
    }

    /**
     * Change the type of zombie that will spawn.
     *
     * @param type the updated type of the zombie.
     */
    public void setType(@Nonnull ZombieType type){
        this.type = type;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
