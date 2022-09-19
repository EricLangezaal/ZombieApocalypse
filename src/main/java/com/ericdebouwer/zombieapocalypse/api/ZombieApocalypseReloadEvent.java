package com.ericdebouwer.zombieapocalypse.api;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when ZombieApocalypse is reloaded by it's <b>own</b> reload command. <br>
 * <b>Author</b> - 3ricL (Ericdebouwer).
 */

@RequiredArgsConstructor
public class ZombieApocalypseReloadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final boolean success;

    /**
     * Determine if the configuration could be parsed correctly.
     *
     * @return if the reload of this plugin was deemed successful.
     */
    public boolean isSuccessful() {
        return success;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
