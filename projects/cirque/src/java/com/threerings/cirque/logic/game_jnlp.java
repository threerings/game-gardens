//
// $Id: game_jnlp.java,v 1.1 2004/01/20 14:35:13 mdb Exp $

package com.threerings.gametable.logic;

import java.net.URL;
import javax.servlet.http.HttpServletRequest;

import com.samskivert.servlet.user.User;
import com.samskivert.servlet.util.FriendlyException;
import com.samskivert.servlet.util.ParameterUtil;

import com.samskivert.velocity.Application;
import com.samskivert.velocity.InvocationContext;
import com.samskivert.velocity.Logic;

import com.threerings.toybox.data.Game;
import com.threerings.toybox.data.ToyBoxCodes;
import com.threerings.toybox.server.ToyBoxConfig;

import com.threerings.gametable.Log;
import com.threerings.gametable.GameTableApp;

/**
 * Provides a JNLP file for a particular game.
 */
public class game_jnlp implements Logic
{
    // documentation inherited
    public void invoke (Application app, InvocationContext ctx)
        throws Exception
    {
        GameTableApp gtapp = (GameTableApp)app;
        HttpServletRequest req = ctx.getRequest();

        // load up the game
        int gameId = ParameterUtil.requireIntParameter(
            req, "gameid", "error.invalid_gameid");
        Game game = gtapp.getToyBoxRepository().loadGame(gameId);
        if (game == null) {
            throw new FriendlyException("error.no_such_game");
        }
        ctx.put("game", game);

        String requrl = req.getRequestURL().toString();
        String path = gtapp.getProperty("game_data_path");
        URL codebase;
        try {
            codebase = new URL(new URL(requrl), path);
        } catch (Exception e) {
            Log.warning("Error creating codebase URL [req=" + requrl +
                        ", path=" + path + ", error=" + e + "].");
            throw new FriendlyException("error.internal_error");
        }

        ctx.put("codebase", codebase.toString());
        ctx.put("libs_dir", ToyBoxCodes.LIBRARY_SUBDIR);
        ctx.put("game_dir", ToyBoxConfig.getGameSubdir(game));
        ctx.put("jre_version", "1.4.2+"); // TODO: allow per game custom.
    }

    protected String appendToPath (String path, String more)
    {
        if (path.endsWith("/") || more.startsWith("/")) {
            return path + more;
        } else {
            return path + "/" + more;
        }
    }
}
