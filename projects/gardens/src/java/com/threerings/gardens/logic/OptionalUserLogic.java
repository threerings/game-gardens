//
// $Id: UserLogic.java 31 2004-12-10 05:32:38Z mdb $

package com.threerings.gardens.logic;

import com.samskivert.velocity.Application;
import com.samskivert.velocity.InvocationContext;
import com.samskivert.velocity.Logic;

import com.samskivert.servlet.user.User;

import com.threerings.gardens.Log;
import com.threerings.gardens.GardensApp;

/**
 * A base logic class for pages that optionally accept an authenticated user.
 */
public abstract class OptionalUserLogic implements Logic
{
    /**
     * Logic classes should implement this method to perform their normal
     * duties.
     *
     * @param ctx the context in which the request is being invoked.
     * @param app the web application.
     * @param user the user record for the authenticated user or null if
     * no user is authenticated.
     */
    public abstract void invoke (
        InvocationContext ctx, GardensApp app, User user)
        throws Exception;

    // documentation inherited from interface
    public void invoke (Application app, InvocationContext ctx)
        throws Exception
    {
        GardensApp gtapp = (GardensApp)app;
        User user = gtapp.getUserManager().loadUser(ctx.getRequest());
        if (user != null) {
            ctx.put("user", user);
        }
        invoke(ctx, gtapp, user);
    }
}
