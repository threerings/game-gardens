//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2012 Three Rings Design, Inc., All Rights Reserved
// http://github.com/threerings/game-gardens
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.toybox.server.persist;

import java.sql.Date;

import com.samskivert.depot.Key;
import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.annotation.Column;
import com.samskivert.depot.annotation.Entity;
import com.samskivert.depot.annotation.Id;
import com.samskivert.depot.expression.ColumnExp;

/**
 * Contains information about recorded playtime.
 */
@Entity(name="PLAYTIME")
public class PlaytimeRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    public static final Class<PlaytimeRecord> _R = PlaytimeRecord.class;
    public static final ColumnExp<Integer> GAME_ID = colexp(_R, "gameId");
    public static final ColumnExp<Date> PERIOD = colexp(_R, "period");
    public static final ColumnExp<Integer> PLAYTIME = colexp(_R, "playtime");
    // AUTO-GENERATED: FIELDS END

    public static final int SCHEMA_VERSION = 1;

    @Id @Column(name="GAME_ID")
    public int gameId;

    @Id @Column(name="PERIOD")
    public Date period;

    @Column(name="PLAYTIME")
    public int playtime;

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link PlaytimeRecord}
     * with the supplied key values.
     */
    public static Key<PlaytimeRecord> getKey (int gameId, Date period)
    {
        return newKey(_R, gameId, period);
    }

    /** Register the key fields in an order matching the getKey() factory. */
    static { registerKeyFields(GAME_ID, PERIOD); }
    // AUTO-GENERATED: METHODS END
}
