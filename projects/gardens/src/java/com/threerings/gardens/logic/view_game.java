//
// $Id: view_game.java 36 2004-12-14 22:05:31Z mdb $

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
