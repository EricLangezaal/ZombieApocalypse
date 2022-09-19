package com.ericdebouwer.zombieapocalypse.api;

/**
 * Called when an apocalypse is about to end in a certain world.
 * If ApocalypseEndEvent is cancelled the apocalypse will not end, regardless of duration. <br>
 * <b>Author</b> - 3ricL (Ericdebouwer).
 */

public class ApocalypseEndEvent extends AbstractApocalypseEvent {

    public ApocalypseEndEvent(Apocalypse apocalypse) {
        super(apocalypse);
    }
}
