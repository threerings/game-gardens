//
// $Id: TokenUtil.java,v 1.2 2002/07/26 21:53:22 mdb Exp $

package com.threerings.skirmish.client;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

import com.threerings.skirmish.data.SkirmishAction;

/**
 * Methods for rendering action tokens.
 */
public class TokenUtil
{
    /** The size of the token images in pixels. */
    public static final int TOKEN_SIZE = 32;

    /** The size of the cannon fire token images in pixels. */
    public static final int SMALL_TOKEN_SIZE = 24;

    /** The gap between tokens. */
    public static final int TOKEN_GAP = 2;

    /** The maximum allowed number of tokens across. */
    public static final int TOKENS_ACROSS = 6;

    /** Used when rendering action tokens. */
    public static final Color[] ACTION_COLORS = {
        Color.white,
        Color.blue,
        Color.yellow,
        Color.green,
        Color.red,
    };

    /** Used when rendering action tokens. */
    public static final String[] ACTION_LABELS = {
        "",
        "FWD",
        "LEFT",
        "RIGHT",
        "FIRE",
    };

    /**
     * Renders the specified action token at the specified coordinates.
     */
    public static void renderAction (
        Graphics g, int code, int x, int y, boolean small)
    {
        FontMetrics fm = g.getFontMetrics();

        // fire tokens are smaller
        int size = small ? SMALL_TOKEN_SIZE : TOKEN_SIZE;

        g.setColor(ACTION_COLORS[code]);
        g.fillRect(x, y, size, size);
        g.setColor(Color.black);
        g.drawRect(x, y, size-1, size-1);

        String label = ACTION_LABELS[code];
        int lwid = fm.stringWidth(label);
        g.drawString(label, x + (size - lwid)/2,
                     y + (size - fm.getHeight())/2 + fm.getAscent());
    }

    /**
     * Renders a "flipped over" action.
     */
    public static void renderBack (Graphics g, int x, int y, boolean small)
    {
        // fire tokens are smaller
        int size = small ? SMALL_TOKEN_SIZE : TOKEN_SIZE;
        g.setColor(Color.darkGray);
        g.fillRect(x, y, size, size);
        g.setColor(Color.black);
        g.drawRect(x, y, size-1, size-1);
    }
}
