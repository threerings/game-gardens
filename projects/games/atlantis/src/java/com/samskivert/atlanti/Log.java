//
// Atlantis - A tile laying game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/atlantis/LICENSE

package com.samskivert.atlanti;

import com.samskivert.util.Logger;

/**
 * A placeholder class that contains a reference to the log object used by
 * this package. This is a useful pattern to use when using the samskivert
 * logging facilities. One creates a top-level class like this one that
 * instantiates a log object with an name that identifies log messages
 * from that package and then provides static methods that generate log
 * messages using that instance. Then, classes in that package need only
 * import the log wrapper class and can easily use it to generate log
 * messages. For example:
 *
 * <pre>
 * import static com.samskivert.atlanti.Log.log;
 * // ...
 * log.warning("All hell is breaking loose!");
 * // ...
 * </pre>
 *
 * @see com.samskivert.util.Logger
 */
public class Log
{
    /** The static log instance configured for use by this package. */
    public static Logger log = Logger.getLogger("atlanti");
}
