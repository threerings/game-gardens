//
// $Id: UserLogic.java,v 1.1 2004/01/20 14:35:13 mdb Exp $

package com.threerings.gametable.logic;

import com.samskivert.velocity.Application;
import com.samskivert.velocity.InvocationContext;
import com.samskivert.velocity.Logic;

import com.samskivert.servlet.user.User;

import com.threerings.gametable.Log;
import com.threerings.gametable.GameTableApp;

/**
 * A base logic class for pages that require an authenticated user.
 */
public abstract class UserLogic implements Logic
{
    /**
     * Logic classes should implement this method to perform their normal
     * duties.
     *
     * @param ctx the context in which the request is being invoked.
     * @param app the web application.
     * @param user the user record for the authenticated user.
     */
    public abstract void invoke (
        InvocationContext ctx, GameTableApp app, User user)
        throws Exception;

    // documentation inherited from interface
    public void invoke (Application app, InvocationContext ctx)
        throws Exception
    {
        GameTableApp gtapp = (GameTableApp)app;
        User user = gtapp.getUserManager().requireUser(ctx.getRequest());
        invoke(ctx, gtapp, user);
    }
}
