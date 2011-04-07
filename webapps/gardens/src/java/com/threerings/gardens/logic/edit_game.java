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

import java.net.URL;
import java.sql.Date;
import javax.servlet.http.HttpServletRequest;

import com.samskivert.servlet.RedirectException;
import com.samskivert.servlet.user.User;
import com.samskivert.servlet.util.FriendlyException;
import com.samskivert.servlet.util.HTMLUtil;
import com.samskivert.servlet.util.ParameterUtil;

import com.samskivert.text.MessageUtil;
import com.samskivert.velocity.InvocationContext;

import com.threerings.presents.server.InvocationException;

import com.threerings.toybox.data.GameDefinition;
import com.threerings.toybox.server.ToyBoxConfig;
import com.threerings.toybox.server.persist.GameRecord.Status;
import com.threerings.toybox.server.persist.GameRecord;

import com.threerings.gardens.Log;
import com.threerings.gardens.GardensApp;

/**
 * Handles the logic behind creating and managing a game's metadata.
 */
public class edit_game extends UserLogic
{
    // documentation inherited
    public void invoke (InvocationContext ctx, GardensApp app, User user)
        throws Exception
    {
        HttpServletRequest req = ctx.getRequest();

        // load up the game if an id was provided
        int gameId = ParameterUtil.getIntParameter(
            req, "gameid", 0, "error.invalid_gameid");
        GameRecord game = null;
        if (gameId != 0) {
            game = app.getToyBoxRepository().loadGame(gameId);
            if (game != null) {
                ctx.put("game", game);
            }
        }

        // make sure this user is the maintainer or an admin
        if (game != null &&
            !(user.userId == game.maintainerId || user.isAdmin())) {
            throw new RedirectException(app.getProperty("access_denied_url"));
        }

        // determine where uploads should be sent
        URL rurl = new URL(ToyBoxConfig.getResourceURL());
        URL upurl = new URL(rurl.getProtocol(), rurl.getHost(),
                            req.getContextPath() + "/upload_jar.wm");
        ctx.put("upload_url", upurl);

        // assume we're updating unless later overridden
        ctx.put("action", "update");

        // figure out what we're doing
        String action = ParameterUtil.getParameter(req, "action", false);
        if (action.equals("update")) {
            if (game == null) {
                throw new FriendlyException("error.no_such_game");
            }

            // read the parameters over top of the existing game
            populateGame(req, game);

            // update it in the database
            app.getToyBoxRepository().updateGame(game);
            ctx.put("status", "edit_game.status.updated");

        } else if (action.equals("create")) {
            // we're still creating (just in case things fail)
            ctx.put("action", "create");

            // create a blank game and configure it
            game = new GameRecord();
            game.setStatus(Status.PENDING);
            game.maintainerId = user.userId;
            game.host = ToyBoxConfig.getServerHost();
            game.digest = "";
            game.created = new Date(System.currentTimeMillis());
            game.lastUpdated = game.created;
            ctx.put("game", game);

            // fill in the user supplied information
            populateGame(req, game);

            // insert it into the repository
            app.getToyBoxRepository().insertGame(game);
            ctx.put("status", "edit_game.status.created");

            // now we can switch back to update mode
            ctx.put("action", "update");

        }  else if (gameId != 0) {
            // nothing to do, the game is already in the context

        } else {
            ctx.put("action", "create");
            game = new GameRecord();
            game.category = "other";
            ctx.put("game", game);
        }
    }

    protected void populateGame (HttpServletRequest req, GameRecord game)
        throws Exception
    {
        // read in and validate our various bits
        game.name = requireString(req, "name", 50, true);
        game.category = requireString(req, "category", 255, false);
        game.definition = requireString(req, "definition", 2500, false);
        game.description = requireString(req, "description", 1000, true);
        game.instructions = requireString(req, "instructions", 5000, true);
        game.credits = requireString(req, "credits", 1000, true);

        // TODO: validate definition
        try {
            GameDefinition gamedef = game.parseGameDefinition();
        } catch (InvocationException ie) {
            String errmsg = "edit_game.error.malformed_definition";
            Throwable cause;
            if ((cause = ie.getCause()) != null) {
                errmsg = MessageUtil.tcompose(
                    "edit_game.error.malformed_definition_why",
                    cause.getMessage());
            }
            throw new FriendlyException(errmsg);
        }

        // TODO: set the status to PUBLISHED if all is groovy?
    }

    protected String requireString (
        HttpServletRequest req, String name, int maxLength, boolean entify)
        throws Exception
    {
        String err = MessageUtil.compose("error.missing_field", "f." + name);
        String value = ParameterUtil.requireParameter(req, name, err);
        if (value.length() > maxLength) {
            err = MessageUtil.compose("error.field_too_long", "f." + name,
                                      MessageUtil.taint("" + maxLength));
            throw new FriendlyException(err);
        }
        if (entify) {
            value = HTMLUtil.restrictHTML(value, true, true, true);
        }
        return value;
    }
}
