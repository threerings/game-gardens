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

import javax.servlet.http.HttpServletRequest;

import com.samskivert.servlet.user.User;
import com.samskivert.servlet.util.FriendlyException;
import com.samskivert.servlet.util.HTMLUtil;
import com.samskivert.servlet.util.ParameterUtil;
import com.samskivert.velocity.InvocationContext;

import com.threerings.presents.server.InvocationException;
import com.threerings.toybox.server.persist.GameRecord;

import com.threerings.gardens.Log;
import com.threerings.gardens.GardensApp;

/**
 * Handles the logic behind creating and managing a game's metadata.
 */
public class view_game extends OptionalUserLogic
{
    // documentation inherited
    public void invoke (InvocationContext ctx, GardensApp app, User user)
        throws Exception
    {
        HttpServletRequest req = ctx.getRequest();
        int gameId = ParameterUtil.requireIntParameter(req, "gameid", "error.invalid_gameid");
        GameRecord game = app.getToyBoxRepository().loadGame(gameId);
        if (game == null) {
            throw new FriendlyException("error.no_such_game");
        }
        ctx.put("game", game);
        User creator = app.getUserManager().getRepository().loadUser(game.maintainerId);
        if (creator != null) {
            ctx.put("creator", creator.username);
            ctx.put("creator_profile", PROFILE_URL + creator.username);
        }
        ctx.put("players", app.getToyBoxRepository().getOnlineCount(gameId));
        try {
            ctx.put("single_player", game.parseGameDefinition().isSinglePlayerPlayable());
        } catch (InvocationException ie) {
            String errmsg = (ie.getCause() == null) ? ie.getMessage() : ie.getCause().getMessage();
            Log.log.warning("Failed to parse gamedef [game=" + game.which() +
                            ", error=" + errmsg + "].");
        }
    }

    // somewhat hacky link to creator profiles
    protected static final String PROFILE_URL =
        "http://forums.gamegardens.com/discussion/mvnforum/viewmember?member=";
}
