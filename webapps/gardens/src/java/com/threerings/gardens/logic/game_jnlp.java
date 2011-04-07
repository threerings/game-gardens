//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2011 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.gardens.logic;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.net.URL;
import javax.servlet.http.HttpServletRequest;

import com.samskivert.servlet.user.User;
import com.samskivert.servlet.util.FriendlyException;
import com.samskivert.servlet.util.ParameterUtil;

import com.samskivert.velocity.Application;
import com.samskivert.velocity.InvocationContext;
import com.samskivert.velocity.Logic;

import com.threerings.toybox.data.ToyBoxCodes;
import com.threerings.toybox.server.ToyBoxConfig;
import com.threerings.toybox.server.persist.GameRecord;

import com.threerings.gardens.GardensApp;

import static com.threerings.gardens.Log.log;

/**
 * Provides a JNLP file for a particular game.
 */
public class game_jnlp implements Logic
{
    // documentation inherited
    public void invoke (Application app, InvocationContext ctx)
        throws Exception
    {
        GardensApp gtapp = (GardensApp)app;
        HttpServletRequest req = ctx.getRequest();

        // we get our game id from the path
        int gameId = -1;
        String rpath = req.getServletPath();
        Matcher m = _jnlppat.matcher(rpath);
        if (m.matches()) {
            try {
                gameId = Integer.parseInt(m.group(1));
            } catch (Exception e) {
            }
        }
        if (gameId < 0) {
            throw new FriendlyException("error.invalid_gameid");
        }

        // load up the game
        GameRecord game = gtapp.getToyBoxRepository().loadGame(gameId);
        if (game == null) {
            throw new FriendlyException("error.no_such_game");
        }
        ctx.put("game", game);

        // fake up a last modified header
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR), day = cal.get(Calendar.DAY_OF_YEAR);
        cal.setTime(game.lastUpdated);
        // if it was last updated today, use the current time as the last
        // modification as we don't have finer granularity
        if (year == cal.get(Calendar.YEAR) &&
            day == cal.get(Calendar.DAY_OF_YEAR)) {
            cal = Calendar.getInstance();
        } else {
            // otherwise claim last modification at 11:59:59 on the known date
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
        }
        long lastModified = cal.getTime().getTime();
        ctx.getResponse().setDateHeader("Last-Modified", lastModified);

        String path = CLIENT_PATH;
        URL codebase;
        try {
            codebase = new URL("http", game.host, path);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error creating codebase URL " +
                    "[ghost=" + game.host + ", path=" + path + "].", e);
            throw new FriendlyException("error.internal_error");
        }

        ctx.put("base_path", req.getContextPath());
        ctx.put("codebase", codebase.toString());
        ctx.put("server", game.host);
        ctx.put("port", ToyBoxConfig.getServerPort());
        ctx.put("resource_url", ToyBoxConfig.getResourceURL());

        ctx.getResponse().setContentType("application/x-java-jnlp-file");
    }

    protected Pattern _jnlppat = Pattern.compile("/game_([0-9]+).jnlp");

    protected static final String CLIENT_PATH = "/client";
}
