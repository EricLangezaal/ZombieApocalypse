package com.ericdebouwer.zombieapocalypse.api;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieType;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Small API to properly interact with the ZombieApocalypse plugin. <br>
 * <b>Author</b> - 3ricL (Ericdebouwer).
 */
public class ApocalypseAPI {
    private static final ApocalypseAPI instance = new ApocalypseAPI();
    private final ZombieApocalypse plugin;

    private ApocalypseAPI(){
        plugin = JavaPlugin.getPlugin(ZombieApocalypse.class);
    }

    /**
     * Get an instance of this API. Make sure your plugin is a dependency of <b>ZombieApocalypse</b>,
     * so it will load after ZombieApocalypse has initialised.
     * @return an instance of this API.
     */
    public static @Nonnull ApocalypseAPI getInstance(){
        return instance;
    }

    /**
     * Start the apocalypse <b>indefinitely</b> for a world.
     *
     * @param worldName the name of the world, this world does not have to be loaded.
     * @return true if the apocalypse did start, false if it failed (already started, world folder does not exist)
     */
    public boolean startApocalypse(@Nonnull String worldName){
        return this.startApocalypse(worldName, true);
    }

    /**
     * Start the apocalypse <b>indefinitely</b> for a world.
     *
     * @param worldName the name of the world, this world does not have to be loaded.
     * @param broadCastStart whether or not to message players about the start of the apocalypse
     * @return true if the apocalypse did start, false if it failed (already started, world folder does not exist)
     */
    public boolean startApocalypse(@Nonnull String worldName, boolean broadCastStart){
        return plugin.getApocalypseManager().startApocalypse(worldName, -1, broadCastStart);
    }

    /**
     * Start the apocalypse for a world <b>for a certain duration</b>.
     *
     * @param worldName the name of the world, this world does not have to be loaded.
     * @param durationMinutes the amount of <b>minutes</b> the apocalypse should last.
     * @return true if the apocalypse did start, false if it failed (already started, world folder does not exist)
     */
    public boolean startApocalypse(@Nonnull String worldName, long durationMinutes){
        Validate.isTrue(durationMinutes > 0, "duration should be a positive amount of seconds!");
        return plugin.getApocalypseManager().startApocalypse(worldName,
                java.time.Instant.now().getEpochSecond() + durationMinutes * 60, true);
    }

    /**
     * Start the apocalypse for a world <b>for a certain duration</b>.
     *
     * @param worldName the name of the world, this world does not have to be loaded.
     * @param durationMinutes the amount of <b>minutes</b> the apocalypse should last.
     * @param mobCap the maximum amount of zombies per chunk, 70 by default, negative for global
     * @param broadCastStart whether or not to message players about the start of the apocalypse
     * @return true if the apocalypse did start, false if it failed (already started, world folder does not exist)
     */
    public boolean startApocalypse(@Nonnull String worldName, long durationMinutes, int mobCap, boolean broadCastStart){
        Validate.isTrue(durationMinutes > 0, "duration should be a positive amount of seconds!");

        return plugin.getApocalypseManager().startApocalypse(worldName,
                java.time.Instant.now().getEpochSecond() + durationMinutes * 60, mobCap, broadCastStart);
    }

    /**
     * End the apocalypse for a world.
     *
     * @param worldName the name of the world, this world does not have to be loaded.
     * @return true if the apocalypse did end, false if it failed to (no apocalypse ongoing, world folder does not exist)
     */
    public boolean endApocalypse(@Nonnull String worldName){
        return this.endApocalypse(worldName, true);
    }

    /**
     * End the apocalypse for a world.
     *
     * @param worldName the name of the world, this world does not have to be loaded.
     * @param broadCastEnd whether or not to message players about the end of the apocalypse
     * @return true if the apocalypse did end, false if it failed to (no apocalypse ongoing, world folder does not exist)
     */
    public boolean endApocalypse(@Nonnull String worldName, boolean broadCastEnd){
        return plugin.getApocalypseManager().endApocalypse(worldName, broadCastEnd);
    }

    /**
     * Returns if there is currently an apocalypse in that world.
     *
     * @param worldName the name of the world, this world does not have to be loaded.
     * @return true if there is an apocalypse, false otherwise
     */
    public boolean isApocalypse(@Nonnull String worldName){
        return plugin.getApocalypseManager().isApocalypse(worldName);
    }

    /**
     * Set the the maximum amount of zombies per chunk.
     *
     * @param worldName the name of the world, this world does not have to be loaded.
     * @param mobCap the maximum amount of zombies per chunk, 70 by default
     */
    public void setMobCap(@Nonnull String worldName, int mobCap){
        plugin.getApocalypseManager().setMobCap(worldName, mobCap);
    }

    /**
     * Get the apocalypse currently ongoing in a world, if there is one.
     *
     * @param worldName the name of the world, this world does not have to be loaded.
     * @return an instance of {@link Apocalypse}, or null if there is no apocalypse ongoing.
     */
    public @Nullable Apocalypse getApocalypse(@Nonnull String worldName){
        return plugin.getApocalypseManager().getApoWorld(worldName).orElse(null);
    }

    /**
     * Spawn a custom zombie.
     *
     * @param location where to spawn the zombie.
     * @param type the type of zombie to spawn.
     * @return an instance of {@link org.bukkit.entity.Zombie}, which can be modified.
     */
    public @Nonnull Zombie spawnZombie(@Nonnull Location location, @Nonnull ZombieType type){
        return plugin.getZombieFactory().spawnZombie(location, type, ZombieSpawnedEvent.SpawnReason.API);
    }

    /**
     * Spawn a <b>random</b> custom zombie that <b>is enabled</b>.
     *
     * @param location where to spawn the zombie.
     * @return an instance of {@link org.bukkit.entity.Zombie}, which can be modified.
     */
    public @Nonnull Zombie spawnZombie(@Nonnull Location location){
        return plugin.getZombieFactory().spawnZombie(location, ZombieSpawnedEvent.SpawnReason.API);
    }

}
