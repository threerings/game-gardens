//
// $Id$

package com.threerings.toybox.data;

import com.threerings.parlor.game.data.GameConfig;

/**
 * Extends {@link MatchConfig} with information about match-making in the table style.
 */
public class TableMatchConfig extends MatchConfig
{
    /** The minimum number of seats at this table. */
    public int minSeats;

    /** The starting setting for the number of seats at this table. */
    public int startSeats;

    /** The maximum number of seats at this table. */
    public int maxSeats;

    /** This is set to true if this is a party game. */
    public boolean isPartyGame;

    @Override // from MatchConfig
    public int getMatchType ()
    {
        return isPartyGame ? GameConfig.PARTY : GameConfig.SEATED_GAME;
    }

    @Override // from MatchConfig
    public int getMinimumPlayers ()
    {
        return minSeats;
    }

    @Override // from MatchConfig
    public int getMaximumPlayers ()
    {
        return maxSeats;
    }
}
