//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sreversi/LICENSE

package com.samskivert.reversi

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * Test our custom Scala-friendly accessors.
 */
class ScalaDObjectSpec extends FlatSpec with ShouldMatchers
{
  "ScalaDObject" should "create the proper mix of accessors" in {
    val ro = new ReversiObject

    // make sure we have Scala attributes
    ro.getAttribute("pieces") should not equal(null)

    // make sure we have Java attributes
    ro.getAttribute("state") should not equal(null)

    // make sure non-existent attributes throw IAE
    intercept[IllegalArgumentException] {
      ro.getAttribute("nonexistent")
    }
  }
}
