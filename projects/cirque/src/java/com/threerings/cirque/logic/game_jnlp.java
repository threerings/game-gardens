//
// $Id: game_jnlp.java,v 1.2 2004/11/15 01:49:34 mdb Exp $

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

        String path = gtapp.getProperty("game_data_path");
        URL codebase;
        try {
            codebase = new URL("http", game.host, 80, path);
        } catch (Exception e) {
            Log.warning("Error creating codebase URL [ghost=" + game.host +
                        ", path=" + path + ", error=" + e + "].");
            throw new FriendlyException("error.internal_error");
        }

        ctx.put("base_path", req.getContextPath());
        ctx.put("codebase", codebase.toString());
        ctx.put("libs_dir", ToyBoxCodes.LIBRARY_SUBDIR);
        ctx.put("jre_version", "1.4.2+"); // TODO: allow per game custom.
        ctx.put("server", game.host);
    }
}
