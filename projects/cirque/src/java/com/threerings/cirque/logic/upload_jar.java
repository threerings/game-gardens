//
// $Id: upload_jar.java,v 1.1 2004/01/20 14:35:13 mdb Exp $

package com.threerings.gametable.logic;

import java.io.File;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;

import com.samskivert.servlet.user.User;
import com.samskivert.servlet.util.FriendlyException;
import com.samskivert.servlet.util.ParameterUtil;
import com.samskivert.velocity.InvocationContext;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

import com.threerings.toybox.data.Game;
import com.threerings.toybox.server.ToyBoxConfig;

import com.threerings.gametable.Log;
import com.threerings.gametable.GameTableApp;

/**
 * Handles the updating of a game's jar file.
 */
public class upload_jar extends UserLogic
{
    // documentation inherited
    public void invoke (InvocationContext ctx, GameTableApp app, User user)
        throws Exception
    {
        HttpServletRequest req = ctx.getRequest();

        DiskFileUpload fu = new DiskFileUpload();
        fu.setSizeMax(MAX_GAME_JAR_SIZE);
        fu.setSizeThreshold(4096); // memory buffer size
        fu.setRepositoryPath("/tmp");
        Iterator iter = fu.parseRequest(req).iterator();

        // the first item should be the gameid
        FileItem item = (FileItem)iter.next();
        if (item == null || !item.getFieldName().equals("gameid")) {
            Log.warning("upload_jar: Invalid first item: " +
                        toString(item) + ".");
            throw new FriendlyException("error.internal_error");
        }
        int gameId;
        try {
            gameId = Integer.parseInt(item.getString());
        } catch (Exception e) {
            throw new FriendlyException("error.invalid_gameid");
        }

        // now load up the associated game record
        Game game = null;
        if (gameId != 0) {
            game = app.getToyBoxRepository().loadGame(gameId);
        }
        if (game == null) {
            throw new FriendlyException("error.no_such_game");
        }

        // determine where we will be uploading the jar file
        File gdir = ToyBoxConfig.getGameDirectory(game);
        Log.info("Uploading jar for '" + game.name + "'.");

        // TODO: get a version number from the game record

        // the next item should be the jar file itself
        item = (FileItem)iter.next();
        if (item == null || !item.getFieldName().equals("jar")) {
            Log.warning("upload_jar: Invalid second item: " +
                        toString(item) + ".");
            throw new FriendlyException("error.internal_error");
        }

        File jar = new File(gdir, "game.jar");
        item.write(jar);
        Log.info("Wrote " + jar + ".");

        // TODO: update the version number and game record

        ctx.put("status", "upload_jar.updated");
    }

    protected String toString (FileItem item)
    {
        if (item == null) {
            return "null";
        }
        return "[field=" + item.getFieldName() + ", size=" + item.getSize() +
            ", type=" + item.getContentType() + "]";
    }

    /** TODO: move this into the config. */
    protected static final int MAX_GAME_JAR_SIZE = 1024 * 1024 * 1024;
}
