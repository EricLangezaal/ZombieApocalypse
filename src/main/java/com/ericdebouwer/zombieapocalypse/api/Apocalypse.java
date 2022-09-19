package com.ericdebouwer.zombieapocalypse.api;

import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

/**
 * Representation of an ongoing apocalypse.<br>
 *
 * <b>Author</b> - 3ricL (Ericdebouwer).
 */
public interface Apocalypse {

    /**
     * Get the name of the world this apocalypse is tied to, which does not have to be loaded.
     *
     * @return the name of the apocalyptic world
     */
    @Nonnull String getWorldName();

    /**
     * Get the {@link org.bukkit.boss.BossBar} that is shown for players in that apocalypse.
     * Modifying it will modify the BossBar shown to players.
     *
     * @return the current instance of {@link org.bukkit.boss.BossBar} for the apocalypse
     */
    @Nonnull BossBar getBossBar();

    /**
     * Add a player to the apocalypse, currently only adds them to the BossBar.
     *
     * @param player the player to add to the apocalypse
     */
    void addPlayer(@Nonnull Player player);

    /**
     * Remove a player from the apocalypse, currently only removes them from the BossBar.
     *
     * @param player the player to remove from the apocalypse
     */
    void removePlayer(@Nonnull Player player);

    /**
     * Get the current mob cap for the apocalypse.
     * To modify this use {@link ApocalypseAPI#setMobCap(String, int)}
     *
     * @return the current mob cap.
     */
    int getMobCap();

    /**
     * Get the end time of the apocalypse.
     * This will be in the format of the amount of <b>seconds</b> after 1970.
     *
     * @return The epoch second for the end time of the apocalypse,
     * or a negative number if the apocalypse will go on indefinitely
     */
    long getEndEpochSecond();

}
