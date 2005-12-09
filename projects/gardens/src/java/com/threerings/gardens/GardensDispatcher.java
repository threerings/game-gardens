//
// $Id$

package com.threerings.gardens;

import java.util.regex.Pattern;

import org.apache.velocity.Template;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeSingleton;

import com.samskivert.velocity.DispatcherServlet;
import com.samskivert.velocity.InvocationContext;
import com.samskivert.velocity.Logic;

/**
 * Handles some custom business with regard to Velocity dispatching.
 */
public class GardensDispatcher extends DispatcherServlet
{
    @Override // documentation inherited
    protected Template selectTemplate (int siteId, InvocationContext ctx)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        // do some massaging of the path to handle JNLP files in a way that
        // doesn't confuse Java Web Start (it doesn't allow JNLP files to
        // differ only by query parameters)
        String path = ctx.getRequest().getServletPath();
        if (_jnlppat.matcher(path).matches()) {
            path = "/game_jnlp.wm";
        }

        if (_usingSiteLoading) {
            // if we're using site resource loading, we need to prefix the path
            // with the site identifier
            path = siteId + ":" + path;
        }
        return RuntimeSingleton.getRuntimeServices().getTemplate(path);
    }

    @Override // documentation inherited
    protected Logic resolveLogic (String path)
    {
        if (_jnlppat.matcher(path).matches()) {
            path = "/game_jnlp.wm";
        }
        return super.resolveLogic(path);
    }

    protected Pattern _jnlppat = Pattern.compile("/game_[0-9]+.jnlp");
}
