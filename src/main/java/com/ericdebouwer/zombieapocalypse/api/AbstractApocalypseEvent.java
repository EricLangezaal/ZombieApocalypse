package com.ericdebouwer.zombieapocalypse.api;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

/**
 * Do not listen to this event, listen to either {@link ApocalypseStartEvent} or {@link ApocalypseEndEvent}. <br>
 * <b>Author</b> - 3ricL (Ericdebouwer).
 */

@RequiredArgsConstructor
public abstract class AbstractApocalypseEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private final Apocalypse apocalypse;

    /**
     * Get information about the apocalypse.
     *
     * @return an instance of {@link Apocalypse}.
     */
    public @Nonnull
    Apocalypse getApocalypse(){
        return apocalypse;
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
