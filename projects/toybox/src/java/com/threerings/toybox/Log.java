//
// $Id$
//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

package com.threerings.toybox;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A placeholder class that contains a reference to the log object used by
 * this library.
 */
public class Log
{
    /** We dispatch our log messages through this logger. */
    public static Logger logger = Logger.getLogger("com.threerings.toybox");

    /** Convenience function. */
    public static void debug (String message)
    {
	logger.fine(message);
    }

    /** Convenience function. */
    public static void info (String message)
    {
	logger.info(message);
    }

    /** Convenience function. */
    public static void config (String message)
    {
	logger.config(message);
    }

    /** Convenience function. */
    public static void warning (String message)
    {
	logger.warning(message);
    }

    /** Convenience function. */
    public static void warning (String message, Throwable t)
    {
	logger.log(Level.WARNING, message, t);
    }
}
