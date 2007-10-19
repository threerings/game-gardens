//
// $Id$

package com.threerings.gardens.logic;

import java.util.ArrayList;
import java.util.HashMap;

import com.samskivert.servlet.util.ParameterUtil;
import com.samskivert.velocity.Application;
import com.samskivert.velocity.InvocationContext;
import com.samskivert.velocity.Logic;

import com.threerings.toybox.server.persist.GameRecord;

import com.threerings.gardens.GardensApp;

/**
 * Provides information on recently added, updated and most-popular games.
 */
public class snapshots
    implements Logic
{
    // documentation inherited from interface
    public void invoke (Application app, InvocationContext ctx)
        throws Exception
    {
        final GardensApp gtapp = (GardensApp)app;
        String category = ParameterUtil.getParameter(ctx.getRequest(), "type", ADDED);

        ArrayList<GameRecord> games;
        if (POPULAR.equals(category)) {
            games = getGames(POPULAR, new RefreshFunc() {
                public ArrayList<GameRecord> refresh () throws Exception {
                    return gtapp.getToyBoxRepository().loadPopularGames(SNAPSHOT_COUNT);
                }
            });

        } else if (UPDATED.equals(category)) {
            games = getGames(POPULAR, new RefreshFunc() {
                public ArrayList<GameRecord> refresh () throws Exception {
                    return gtapp.getToyBoxRepository().loadRecentlyUpdated(SNAPSHOT_COUNT);
                }
            });

        } else { // ADDED or whatever
            games = getGames(POPULAR, new RefreshFunc() {
                public ArrayList<GameRecord> refresh () throws Exception {
                    return gtapp.getToyBoxRepository().loadRecentlyAdded(SNAPSHOT_COUNT);
                }
            });
        }

        ctx.put("games", games);
    }

    protected ArrayList<GameRecord> getGames (String category, RefreshFunc func)
        throws Exception
    {
        CachedGames games = _cache.get(category);
        long now = System.currentTimeMillis();
        if (games == null || (now - games.loadStamp) > REFRESH_INTERVAL) {
            if (games == null) {
                _cache.put(category, games = new CachedGames());
            }
            games.games = func.refresh();
            games.loadStamp = now;
        }
        return games.games;
    }

    protected static interface RefreshFunc
    {
        public ArrayList<GameRecord> refresh () throws Exception;
    }

    protected static class CachedGames
    {
        public long loadStamp;
        public ArrayList<GameRecord> games;
    }

    protected HashMap<String,CachedGames> _cache = new HashMap<String,CachedGames>();

    protected static final String ADDED = "added";
    protected static final String UPDATED = "updated";
    protected static final String POPULAR = "popular";

    protected static final int SNAPSHOT_COUNT = 9;
    protected static final long REFRESH_INTERVAL = 15 * 60 * 1000L;
}
