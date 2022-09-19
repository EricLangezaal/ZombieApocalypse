package com.ericdebouwer.zombieapocalypse.api;

/**
 * Called when an apocalypse is about to start in a certain world.
 * If ApocalypseStartEvent is cancelled, the apocalypse will not start.
 *
 * Some information about the apocalypse is not available until one tick later <br>
 * <b>Author</b> - 3ricL (Ericdebouwer).
 */

public class ApocalypseStartEvent extends AbstractApocalypseEvent {

    public ApocalypseStartEvent(Apocalypse apocalypse) {
        super(apocalypse);
    }
}
