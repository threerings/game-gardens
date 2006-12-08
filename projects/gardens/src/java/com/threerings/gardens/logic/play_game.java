//
// $Id$

package com.threerings.gardens.logic;

import com.samskivert.velocity.InvocationContext;
import com.samskivert.servlet.user.User;

import com.threerings.toybox.server.ToyBoxConfig;

import com.threerings.gardens.GardensApp;

/**
 * Fires up a game in an applet.
 */
public class play_game extends view_game
{
    // documentation inherited
    public void invoke (InvocationContext ctx, GardensApp app, User user)
        throws Exception
    {
        super.invoke(ctx, app, user);
        ctx.put("port", ToyBoxConfig.getServerPort());
        ctx.put("resource_url", ToyBoxConfig.getResourceURL());
    }
}
