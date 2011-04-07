//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2011 Three Rings Design, Inc., All Rights Reserved
// http://github.com/threerings/game-gardens
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

package com.threerings.gardens.logic;

import com.samskivert.velocity.Application;
import com.samskivert.velocity.InvocationContext;
import com.samskivert.velocity.Logic;

import com.samskivert.servlet.user.User;

import com.threerings.gardens.Log;
import com.threerings.gardens.GardensApp;

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
        InvocationContext ctx, GardensApp app, User user)
        throws Exception;

    // documentation inherited from interface
    public void invoke (Application app, InvocationContext ctx)
        throws Exception
    {
        GardensApp gtapp = (GardensApp)app;
        User user = gtapp.getUserManager().requireUser(ctx.getRequest());
        ctx.put("user", user);
        invoke(ctx, gtapp, user);
    }
}
