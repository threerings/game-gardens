//
// $Id$

package com.threerings.gardens.logic;

import java.net.URL;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;

import com.samskivert.servlet.user.User;
import com.samskivert.servlet.util.FriendlyException;
import com.samskivert.servlet.util.ParameterUtil;

import com.samskivert.velocity.Application;
import com.samskivert.velocity.InvocationContext;
import com.samskivert.velocity.Logic;

import com.threerings.toybox.data.ToyBoxCodes;
import com.threerings.toybox.server.ToyBoxConfig;
import com.threerings.toybox.server.persist.Game;

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

        // load up the game
        int gameId = ParameterUtil.requireIntParameter(
            req, "gameid", "error.invalid_gameid");
        Game game = gtapp.getToyBoxRepository().loadGame(gameId);
        if (game == null) {
            throw new FriendlyException("error.no_such_game");
        }
        ctx.put("game", game);

        String path = CLIENT_PATH;
        URL codebase;
        try {
            codebase = new URL("http", game.host, 80, path);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error creating codebase URL " +
                    "[ghost=" + game.host + ", path=" + path + "].", e);
            throw new FriendlyException("error.internal_error");
        }

        ctx.put("base_path", req.getContextPath());
        ctx.put("codebase", codebase.toString());
        ctx.put("libs_dir", ToyBoxCodes.LIBRARY_DIR);
        ctx.put("server", game.host);
        ctx.put("port", ToyBoxConfig.getServerPort());

        ctx.getResponse().setContentType("application/x-java-jnlp-file");
    }

    protected static final String CLIENT_PATH = "/client";
}
