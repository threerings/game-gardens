//
// $Id$

package com.threerings.gardens.logic;

import javax.servlet.http.HttpServletRequest;

import com.samskivert.servlet.user.User;
import com.samskivert.servlet.util.FriendlyException;
import com.samskivert.servlet.util.ParameterUtil;
import com.samskivert.velocity.InvocationContext;

import com.threerings.toybox.data.GameDefinition;
import com.threerings.toybox.server.persist.Game;
import com.threerings.toybox.server.persist.Game.Status;

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
        Game game = null;
        if (gameId != 0) {
            game = app.getToyBoxRepository().loadGame(gameId);
            if (game != null) {
                ctx.put("game", game);
            }
        }

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
            // create a blank game and configure it
            game = new Game();
            game.setStatus(Status.PENDING);
            game.maintainerId = user.userId;
            // TODO: get host from ToyBoxConfig?
            game.host = req.getServerName();
            game.testDefinition = "";
            ctx.put("game", game);

            // fill in the user supplied information
            populateGame(req, game);

            // insert it into the repository
            app.getToyBoxRepository().insertGame(game);
            ctx.put("status", "edit_game.status.created");

        }  else if (gameId != 0) {
            // nothing to do, the game is already in the context

        } else {
            ctx.put("action", "create");
            game = new Game();
            game.definition = "";
            game.testDefinition = "";
            ctx.put("game", game);
        }
    }

    protected void populateGame (HttpServletRequest req, Game game)
        throws Exception
    {
        // read in and validate the definition
        game.definition = ParameterUtil.requireParameter(
            req, "definition", "edit_game.error.missing_definition");

        // fill in the game identifier
        GameDefinition gamedef = game.parseGameDefinition();
        game.ident = gamedef.ident;
        // TODO: validate definition

        // TODO: set the status to PUBLISHED if all is groovy?
    }
}
