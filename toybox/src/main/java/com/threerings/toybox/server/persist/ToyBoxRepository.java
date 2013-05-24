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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.depot.DepotRepository;
import com.samskivert.depot.Exps;
import com.samskivert.depot.PersistenceContext;
import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.expression.FluentExp;
import com.samskivert.depot.clause.Limit;
import com.samskivert.depot.clause.OrderBy;
import com.samskivert.depot.clause.Where;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.Tuple;

import com.threerings.toybox.server.ToyBoxManager;

import static com.threerings.toybox.Log.log;

/**
 * Provides an interface to our persistent repository of game metadata.
 */
@Singleton public class ToyBoxRepository extends DepotRepository
    implements ToyBoxManager.GameRepository
{
    /**
     * Constructs a new repository with the specified persistence context.
     */
    @Inject public ToyBoxRepository (PersistenceContext ctx)
    {
        super(ctx);
    }

    /**
     * Loads up all of the games in the repository.
     */
    public List<GameRecord> loadGames ()
    {
        return findAll(GameRecord.class, new Where(ready()));
    }

    /**
     * Loads up all of the games in the repository with the specified category.
     */
    public List<GameRecord> loadGames (String category)
    {
        return findAll(GameRecord.class, new Where(GameRecord.CATEGORY.eq(category).and(ready())));
    }

    /**
     * Loads the specified number of the most popular games as determined by the number of minutes
     * played in those games in the current and previous week.
     */
    public List<GameRecord> loadPopularGames (final int count)
        throws PersistenceException
    {
        class GameData implements Comparable<GameData> {
            public int gameId;
            public int playtime;
            public int compareTo (GameData other) {
                return other.playtime - playtime;
            }
        }

        Map<Integer,GameData> data = Maps.newHashMap();
        for (Date when : new Date[] { getWeek(0), getWeek(-1) }) {
            for (PlaytimeRecord prec : findAll(
                     PlaytimeRecord.class, new Where(PlaytimeRecord.PERIOD.eq(when)))) {
                GameData gd = data.get(prec.gameId);
                if (gd == null) {
                    data.put(prec.gameId, gd = new GameData());
                    gd.gameId = prec.gameId;
                }
                gd.playtime += prec.playtime;
            }
        }

        final Map<Integer,GameRecord> games = Maps.newHashMap();
        for (GameRecord grec : findAll(
                 GameRecord.class, new Where(GameRecord.GAME_ID.in(data.keySet())))) {
            games.put(grec.gameId, grec);
        }

        List<GameData> popular = Lists.newArrayList(data.values());
        Collections.sort(popular);
        return Lists.transform(popular.subList(0, Math.min(popular.size(), count)),
                               new Function<GameData,GameRecord>() {
                                   public GameRecord apply (GameData data) {
                                       return games.get(data.gameId);
                                   }
                               });
    }

    /**
     * Loads the specified number of the most recently created games in the system.
     */
    public List<GameRecord> loadRecentlyAdded (final int count)
    {
        return findAll(GameRecord.class, new Where(ready()),
                       OrderBy.descending(GameRecord.CREATED), new Limit(0, count));
    }

    /**
     * Loads the specified number of the most recently updated games in the system.
     */
    public List<GameRecord> loadRecentlyUpdated (final int count)
    {
        return findAll(GameRecord.class, new Where(ready()),
                       OrderBy.descending(GameRecord.LAST_UPDATED), new Limit(0, count));
    }

    /**
     * Loads information on a single game from the repository. Returns null if no game exists with
     * the specifed id.
     */
    public GameRecord loadGame (int gameId)
        throws PersistenceException
    {
        return load(GameRecord.getKey(gameId));
    }

    /**
     * Inserts the supplied game into the repository. {@link GameRecord#gameId} will be filled in
     * by this method with the game's newly assigned unique identifier.
     */
    public void insertGame (final GameRecord game)
        throws PersistenceException
    {
        insert(game);
    }

    /**
     * Updates the supplied game in the repository. Returns true if a matching row was found and
     * updated, false if no rows matched.
     */
    public boolean updateGame (final GameRecord game)
        throws PersistenceException
    {
        int mod = update(game);
        switch (mod) {
        case 0: return false;
        case 1: return true;
        default:
            log.warning("updateGame() modified more than one row?!", "game", game, "modified", mod);
            return true; // something was updated!
        }
    }

    /**
     * Increments the number of minutes played for the specified game in the current week.
     */
    public void incrementPlaytime (int gameId, int minutes)
        throws PersistenceException
    {
        Date when = getWeek(0);
        // first try updating
        int mods = updatePartial(PlaytimeRecord.class,
                                 new Where(PlaytimeRecord.GAME_ID.eq(gameId).and(
                                               PlaytimeRecord.PERIOD.eq(when))), null,
                                 PlaytimeRecord.PLAYTIME, PlaytimeRecord.PLAYTIME.plus(minutes));
        if (mods == 0) {
            // if that failed to modify anything, insert
            PlaytimeRecord pr = new PlaytimeRecord();
            pr.gameId = gameId;
            pr.period = when;
            pr.playtime = minutes;
            insert(pr);
        }
    }

    /**
     * Clears out the players online counts. Called when the server starts up.
     */
    public void clearOnlineCounts ()
        throws PersistenceException
    {
        deleteAll(OnlineRecord.class, new Where(Exps.value(true)));
    }

    /**
     * Updates the number of players online for the specified game.
     */
    public void updateOnlineCount (int gameId, int players)
        throws PersistenceException
    {
        OnlineRecord record = new OnlineRecord();
        record.gameId = gameId;
        record.players = players;
        store(record);
    }

    /**
     * Looks up the number of players online for the specified game.
     */
    public int getOnlineCount (int gameId)
        throws PersistenceException
    {
        OnlineRecord record = load(OnlineRecord.getKey(gameId));
        return (record == null) ? 0 : record.players;
    }

    /**
     * Returns information on how many players are in games.
     */
    public List<Tuple<String,OnlineRecord>> getOnlineCounts ()
        throws PersistenceException
    {
        // first obtain the game ids of the top N most frequently played games
        List<OnlineRecord> orecs = findAll(
            OnlineRecord.class, new Where(OnlineRecord.PLAYERS.greaterThan(0)));

        Set<Integer> gameIds = Sets.newHashSet();
        for (OnlineRecord orec : orecs) {
            gameIds.add(orec.gameId);
        }
        final Map<Integer, String> gameNames = Maps.newHashMap();
        for (GameRecord grec : findAll(
                 GameRecord.class, new Where(GameRecord.GAME_ID.in(gameIds)))) {
            gameNames.put(grec.gameId, grec.name);
        }

        return Lists.transform(orecs, new Function<OnlineRecord, Tuple<String,OnlineRecord>>() {
            public Tuple<String,OnlineRecord> apply (OnlineRecord orec) {
                return Tuple.newTuple(gameNames.get(orec.gameId), orec);
            }
        });
    }

    /** Returns a {@link Date} instance configured to the beginning of the current (or current +
     * offset) week. */
    protected static Date getWeek (int offset)
    {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int week = now.get(Calendar.WEEK_OF_YEAR);
        now.clear();
        now.set(Calendar.YEAR, year);
        now.set(Calendar.WEEK_OF_YEAR, week);
        now.add(Calendar.WEEK_OF_YEAR, offset);
        return new Date(now.getTimeInMillis());
    }

    @Override
    protected void getManagedRecords (Set<Class<? extends PersistentRecord>> classes)
    {
        classes.add(GameRecord.class);
        classes.add(OnlineRecord.class);
        classes.add(PlaytimeRecord.class);
    }

    protected static FluentExp<Boolean> ready ()
    {
        return GameRecord.STATUS.eq(GameRecord.Status.READY.toString());
    }
}
