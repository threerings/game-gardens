//
// $Id$

package com.threerings.gardens.logic;

import java.io.File;
import java.security.MessageDigest;
import java.sql.Date;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import com.samskivert.servlet.user.User;
import com.samskivert.servlet.util.FriendlyException;
import com.samskivert.servlet.util.ParameterUtil;
import com.samskivert.velocity.InvocationContext;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.threerings.getdown.data.Resource;

import com.threerings.toybox.data.GameDefinition;
import com.threerings.toybox.server.ToyBoxConfig;
import com.threerings.toybox.server.persist.GameRecord;

import com.threerings.gardens.GardensApp;

import static com.threerings.gardens.Log.log;

/**
 * Handles the updating of a game's jar file.
 */
public class upload_jar extends UserLogic
{
    // documentation inherited
    public void invoke (InvocationContext ctx, GardensApp app, User user)
        throws Exception
    {
        HttpServletRequest req = ctx.getRequest();

        // we'll need this to get back to the main website
        ctx.put("website_url", ToyBoxConfig.getWebsiteURL());

        // TODO: check disk usage, set max size to current quota
        DiskFileItemFactory fact = new DiskFileItemFactory(4096, new File("/tmp"));
        ServletFileUpload fu = new ServletFileUpload(fact);
        fu.setSizeMax(MAX_GAME_JAR_SIZE);
        Iterator iter = fu.parseRequest(req).iterator();

        // the first item should be the gameid
        FileItem item = (FileItem)iter.next();
        if (item == null || !item.getFieldName().equals("gameid")) {
            log.warning("upload_jar: Invalid first item: " +
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
        GameRecord game = null;
        if (gameId != 0) {
            game = app.getToyBoxRepository().loadGame(gameId);
        }
        if (game == null) {
            throw new FriendlyException("error.no_such_game");
        }

        // we'll use this later
        ctx.put("gameid", gameId);

        // get a handle on the game definition
        GameDefinition gamedef = game.parseGameDefinition();
        MessageDigest md = MessageDigest.getInstance("MD5");

        // TODO: put game jars in gameId subdirectories

        // determine where we will be uploading the jar file
        File gdir = ToyBoxConfig.getResourceDir();
        log.info("Uploading jar for '" + gamedef.ident + "'.");

        // the next item should be the jar file itself
        item = (FileItem)iter.next();
        if (item == null || !item.getFieldName().equals("jar")) {
            log.warning("upload_jar: Invalid second item: " +
                        toString(item) + ".");
            throw new FriendlyException("error.internal_error");
        }
        if (item.getSize() == 0) {
            throw new FriendlyException("upload_jar.error.missing_jar");
        }

        File jar = new File(gdir, gamedef.getMediaPath(gameId));
        item.write(jar);
        log.info("Wrote " + jar + ".");

        // compute the digest
        String digest = Resource.computeDigest(jar, md, null);
        if (!digest.equals(game.digest)) {
            game.digest = digest;
            game.lastUpdated = new Date(System.currentTimeMillis());
            // if the game was pending upgrade it to ready now that it has
            // a jar file
            if (game.getStatus() == GameRecord.Status.PENDING) {
                game.setStatus(GameRecord.Status.READY);
            }
            // finally update the game record
            app.getToyBoxRepository().updateGame(game);
        }

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