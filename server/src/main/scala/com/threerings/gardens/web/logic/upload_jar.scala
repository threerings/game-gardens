//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.web.logic

import java.io.File
import java.security.MessageDigest
import java.sql.Date
import java.util.Iterator

import javax.servlet.http.HttpServletRequest

import com.google.inject.Inject

import com.samskivert.servlet.user.User
import com.samskivert.servlet.util.FriendlyException
import com.samskivert.velocity.InvocationContext

import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload

import com.threerings.getdown.data.Resource

import com.threerings.toybox.data.GameDefinition
import com.threerings.toybox.server.ToyBoxConfig
import com.threerings.toybox.server.persist.GameRecord

import com.threerings.gardens.web.GardensApp

/** Handles the updating of a game's jar file. */
class upload_jar @Inject() (config :ToyBoxConfig) extends UserLogic {

  override def invoke (ctx :InvocationContext, app :GardensApp, user :User) {
    val req = ctx.getRequest

    // TODO: check disk usage, set max size to current quota
    val fact = new DiskFileItemFactory(4096, new File("/tmp"))
    val fu = new ServletFileUpload(fact)
    fu.setSizeMax(MAX_GAME_JAR_SIZE)
    val iter = fu.parseRequest(req).iterator

    // the first item should be the gameid
    val item = iter.next.asInstanceOf[FileItem]
    if (item == null || !item.getFieldName.equals("gameid")) {
      _log.warning(s"upload_jar: Invalid first item: ${toString(item)}.")
      throw new FriendlyException("error.internal_error")
    }
    val gameId = try {
      Integer.parseInt(item.getString)
    } catch {
      case e :Exception => throw new FriendlyException("error.invalid_gameid")
    }

    // now load up the associated game record
    val game = if (gameId == 0) null else app.toyBoxRepo.loadGame(gameId)
    if (game == null) {
      throw new FriendlyException("error.no_such_game")
    }

    // we'll use this later
    ctx.put("gameid", gameId)

    // get a handle on the game definition
    val gamedef = game.parseGameDefinition
    val md = MessageDigest.getInstance("MD5")

    // TODO: put game jars in gameId subdirectories

    // determine where we will be uploading the jar file
    val gdir = config.getResourceDir
    _log.info(s"Uploading jar for '${gamedef.ident}'.")

    // the next item should be the jar file itself
    val jitem = iter.next.asInstanceOf[FileItem]
    if (jitem == null || !jitem.getFieldName.equals("jar")) {
      _log.warning(s"upload_jar: Invalid second item: ${toString(item)}.")
      throw new FriendlyException("error.internal_error")
    }
    if (item.getSize == 0) {
      throw new FriendlyException("upload_jar.error.missing_jar")
    }

    val jar = new File(gdir, gamedef.getMediaPath(gameId))
    jitem.write(jar)
    _log.info(s"Wrote $jar.")

    // compute the digest
    val digest = Resource.computeDigest(jar, md, null)
    if (!digest.equals(game.digest)) {
      game.digest = digest
      game.lastUpdated = new Date(System.currentTimeMillis)
      // if the game was pending upgrade it to ready now that it has
      // a jar file
      if (game.getStatus == GameRecord.Status.PENDING) {
        game.setStatus(GameRecord.Status.READY)
      }
      // finally update the game record
      app.toyBoxRepo.updateGame(game)
    }

    ctx.put("status", "upload_jar.updated")
  }

  protected def toString (item :FileItem) = item match {
    case null => "null"
    case _ => s"[field=${item.getFieldName}, size=${item.getSize}, type=${item.getContentType}]"
  }

  /** TODO: move this into the config. */
  protected final val MAX_GAME_JAR_SIZE = 1024 * 1024 * 1024

  private val _log = java.util.logging.Logger.getLogger("gardens")
}
