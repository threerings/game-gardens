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
import com.samskivert.velocity.SiteResourceKey;

/**
 * Handles some custom business with regard to Velocity dispatching.
 */
public class GardensDispatcher extends DispatcherServlet
{
    @Override // documentation inherited
    protected Template selectTemplate (int siteId, InvocationContext ctx)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        // create a site resource key based on the template path and the
        // id of the site through which this request was made
        String path = ctx.getRequest().getServletPath();

        // do some massaging of the path to handle JNLP files in a way
        // that doesn't confuse Java Web Start (it doesn't allow JNLP
        // files to differ only by query parameters)
        if (_jnlppat.matcher(path).matches()) {
            path = "/game_jnlp.wm";
        }

        return RuntimeSingleton.getRuntimeServices().getTemplate(
            new SiteResourceKey(siteId, path));
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
