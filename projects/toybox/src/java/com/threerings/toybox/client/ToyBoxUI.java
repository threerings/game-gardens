//
// $Id$
//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005 Three Rings Design, Inc., All Rights Reserved
// http://www.gamegardens.com/code/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.toybox.client;

import java.awt.Color;
import java.awt.Font;

import java.io.InputStream;
import java.util.logging.Level;

import com.threerings.toybox.util.ToyBoxContext;

import static com.threerings.toybox.Log.log;

/**
 * Contains various bits needed for our look and feel.
 */
public class ToyBoxUI
{
    /** The fancy cursive font we use to display game names. */
    public static Font fancyFont;

    /** The nice blue background we use for scrolly bits. */
    public static final Color LIGHT_BLUE = new Color(0xC8E1E9);

    public static void init (ToyBoxContext ctx)
    {
        _ctx = ctx;

        // try to load our fancy font
        try {
            InputStream in =
                ToyBoxUI.class.getClassLoader().getResourceAsStream(
                    "rsrc/media/porc.ttf");
            fancyFont = Font.createFont(Font.TRUETYPE_FONT, in);
            fancyFont = fancyFont.deriveFont(Font.PLAIN, 52);
            in.close();
        } catch (Exception e) {
            log.warning("Failed to load custom font, falling " +
                    "back to default.", e);
            fancyFont = BORING_DEFAULT;
        }
    }

    protected static ToyBoxContext _ctx;

    /** The boring default font used if the custom font can't be loaded. */
    protected static final Font BORING_DEFAULT =
        new Font("Dialog", Font.PLAIN, 12);
}
