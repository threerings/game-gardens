//
// $Id: ToyBoxContext.java,v 1.1 2004/11/15 01:48:51 mdb Exp $

package com.threerings.toybox.util;

import com.threerings.parlor.util.ParlorContext;
import com.threerings.util.MessageManager;

/**
 * Aggregates the various bits that are needed on the ToyBox client.
 */
public interface ToyBoxContext extends ParlorContext
{
    /**
     * Returns a reference to the message manager used by the client to
     * generate localized messages.
     */
    public MessageManager getMessageManager ();
}
