//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sreversi/LICENSE

package com.samskivert.reversi

import java.awt.{BorderLayout, Color, Font, Polygon}
import java.awt.geom.Ellipse2D

import javax.swing.{BorderFactory, Icon, JButton, JLabel, JPanel}

import com.samskivert.swing.{Controller, GroupLayout, ShapeIcon, MultiLineLabel}

import com.threerings.crowd.client.PlacePanel
import com.threerings.parlor.turn.client.TurnDisplay
import com.threerings.util.MessageBundle

import com.threerings.toybox.client.{ToyBoxUI, ChatPanel}
import com.threerings.toybox.util.ToyBoxContext

/**
 * Contains the primary client interface for the game.
 */
class ReversiPanel (ctx :ToyBoxContext, ctrl :ReversiController) extends PlacePanel(ctrl)
{
  /** The board view. */
  var bview = new ReversiBoardView(ctx, ctrl)

  /** Called by the controller when the game has started. */
  def gameDidStart (revobj :ReversiObject) {
    val lips = new Ellipse2D.Float(0, 0, 12, 12)
    _turnDisplay.setPlayerIcons(Array(
      new ShapeIcon(lips, colorForColor(revobj.getColor(0)), null),
      new ShapeIcon(lips, colorForColor(revobj.getColor(1)), null)))
  }

  protected def colorForColor (color :Reversi.Color) = color match {
    case Reversi.Black => Color.black
    case Reversi.White => Color.white
    case _ => Color.red
  }

  protected val _turnDisplay = new TurnDisplay

  /* ctor */ {
    // this is used to look up localized strings
    val msgs = ctx.getMessageManager().getBundle("reversi")

    // give ourselves a wee bit of a border
	  setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))
    setLayout(new BorderLayout)

    // give ourself a soothing blue background
    setBackground(new Color(0x6699CC))

    // create a container that will hold our board view in its center
    val box = GroupLayout.makeHBox
    // create and add our board view
    box.add(bview)
    box.setOpaque(false)
    add(box, BorderLayout.CENTER)

    // create a side panel to hold our chat and other extra interfaces
    val sidePanel = GroupLayout.makeVStretchBox(5)
    sidePanel.setOpaque(false)

    // add a big fat label
    val vlabel = new MultiLineLabel(msgs.get("m.title"))
    vlabel.setFont(ToyBoxUI.fancyFont)
    sidePanel.add(vlabel, GroupLayout.FIXED)

    // add a standard turn display
    _turnDisplay.setOpaque(false)
    val triangle = new Polygon(Array(0, 12, 0), Array(0, 6, 12), 3)
    _turnDisplay.setTurnIcon(new ShapeIcon(triangle, Color.yellow, null))
    _turnDisplay.setWinnerText(ctx.xlate("reversi", "m.winner"))
    _turnDisplay.setDrawText(ctx.xlate("reversi", "m.draw"))
    sidePanel.add(_turnDisplay, GroupLayout.FIXED)

    // add a chat box
    sidePanel.add(new ChatPanel(ctx))

    // add a "back to lobby" button
    val back = Controller.createActionButton(msgs.get("m.back_to_lobby"), "backToLobby")
    sidePanel.add(back, GroupLayout.FIXED)

    // add our side panel to the main display
    add(sidePanel, BorderLayout.EAST)
  }
}
