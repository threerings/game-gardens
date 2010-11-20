//
// $Id$

package com.threerings.gardens.logic;

import java.util.List;
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

        List<GameRecord> games;
        if (POPULAR.equals(category)) {
            games = getGames(POPULAR, new RefreshFunc() {
                public List<GameRecord> refresh () throws Exception {
                    return gtapp.getToyBoxRepository().loadPopularGames(SNAPSHOT_COUNT);
                }
            });

        } else if (UPDATED.equals(category)) {
            games = getGames(UPDATED, new RefreshFunc() {
                public List<GameRecord> refresh () throws Exception {
                    return gtapp.getToyBoxRepository().loadRecentlyUpdated(SNAPSHOT_COUNT);
                }
            });

        } else { // ADDED or whatever
            games = getGames(ADDED, new RefreshFunc() {
                public List<GameRecord> refresh () throws Exception {
                    return gtapp.getToyBoxRepository().loadRecentlyAdded(SNAPSHOT_COUNT);
                }
            });
        }

        ctx.put("games", games);
    }

    protected List<GameRecord> getGames (String category, RefreshFunc func)
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
        public List<GameRecord> refresh () throws Exception;
    }

    protected static class CachedGames
    {
        public long loadStamp;
        public List<GameRecord> games;
    }

    protected HashMap<String,CachedGames> _cache = new HashMap<String,CachedGames>();

    protected static final String ADDED = "added";
    protected static final String UPDATED = "updated";
    protected static final String POPULAR = "popular";

    protected static final int SNAPSHOT_COUNT = 9;
    protected static final long REFRESH_INTERVAL = 15 * 60 * 1000L;
}
