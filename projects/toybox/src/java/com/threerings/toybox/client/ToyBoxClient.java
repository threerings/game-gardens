//
// $Id$
//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2004 Three Rings Design, Inc., All Rights Reserved
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

import java.io.File;
import java.io.IOException;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.samskivert.util.Config;
import com.samskivert.util.StringUtil;
import com.threerings.util.MessageManager;

import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.DObjectManager;

import com.threerings.crowd.chat.client.ChatDirector;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.client.LocationDirector;
import com.threerings.crowd.client.OccupantDirector;
import com.threerings.crowd.client.PlaceView;

import com.threerings.parlor.client.ParlorDirector;

import com.threerings.toybox.util.ToyBoxContext;

/**
 * The ToyBox client takes care of instantiating all of the proper
 * managers and loading up all of the necessary configuration and getting
 * the client bootstrapped.
 */
public class ToyBoxClient
    implements Client.Invoker
{
    /**
     * Initializes a new client and provides it with a frame in which to
     * display everything.
     */
    public void init (ToyBoxFrame frame)
        throws IOException
    {
        // create our context
        _ctx = createContextImpl();

        // create the directors/managers/etc. provided by the context
        createContextServices();

        // for test purposes, hardcode the server info
        _client.setServer("localhost", Client.DEFAULT_SERVER_PORT);

        // keep this for later
        _frame = frame;

        // log off when they close the window
        _frame.addWindowListener(new WindowAdapter() {
            public void windowClosing (WindowEvent evt) {
                // if we're logged on, log off
                if (_client.isLoggedOn()) {
                    _client.logoff(true);
                } else {
                    // otherwise get the heck out
                    System.exit(0);
                }
            }
        });

        // create our client controller and stick it in the frame
        _frame.setController(new ClientController(_ctx, _frame));
    }

    /**
     * Returns a reference to the context in effect for this client. This
     * reference is valid for the lifetime of the application.
     */
    public ToyBoxContext getContext ()
    {
        return _ctx;
    }

    /**
     * Creates the {@link ToyBoxContext} implementation that will be
     * passed around to all of the client code. Derived classes may wish
     * to override this and create some extended context implementation.
     */
    protected ToyBoxContext createContextImpl ()
    {
        return new ToyBoxContextImpl();
    }

    /**
     * Creates and initializes the various services that are provided by
     * the context. Derived classes that provide an extended context
     * should override this method and create their own extended
     * services. They should be sure to call
     * <code>super.createContextServices</code>.
     */
    protected void createContextServices ()
        throws IOException
    {
        // create the handles on our various services
        _client = new Client(null, this);

        // we use this to handle i18n
        _msgmgr = new MessageManager(MESSAGE_MANAGER_PREFIX);

        // create our managers and directors
        _locdir = new LocationDirector(_ctx) {
            protected ClassLoader getClassLoader (PlaceConfig config) {
                return _toydtr.getClassLoader(config);
            }
        };
        _occdir = new OccupantDirector(_ctx);
        _chatdir = new ChatDirector(_ctx, _msgmgr, "global");
        _pardtr = new ParlorDirector(_ctx);
        _toydtr = new ToyBoxDirector(_ctx);
    }

    // documentation inherited
    public void invokeLater (Runnable run)
    {
        // queue it on up on the swing thread
        SwingUtilities.invokeLater(run);
    }

    /**
     * Given a subdirectory name (that should correspond to the calling
     * service), returns a file path that can be used to store local data.
     */
    public static String localDataDir (String subdir)
    {
        String appdir = System.getProperty("appdir");
        if (StringUtil.blank(appdir)) {
            appdir = ".toybox";
            String home = System.getProperty("user.home");
            if (!StringUtil.blank(home)) {
                appdir = home + File.separator + appdir;
            }
        }
        return appdir + File.separator + subdir;
    }

    /**
     * The context implementation. This provides access to all of the
     * objects and services that are needed by the operating client.
     */
    protected class ToyBoxContextImpl implements ToyBoxContext
    {
        /**
         * Apparently the default constructor has default access, rather
         * than protected access, even though this class is declared to be
         * protected. Why, I don't know, but we need to be able to extend
         * this class elsewhere, so we need this.
         */
        protected ToyBoxContextImpl ()
        {
        }

        public Client getClient ()
        {
            return _client;
        }

        public DObjectManager getDObjectManager ()
        {
            return _client.getDObjectManager();
        }

        public Config getConfig ()
        {
            return _config;
        }

        public LocationDirector getLocationDirector ()
        {
            return _locdir;
        }

        public OccupantDirector getOccupantDirector ()
        {
            return _occdir;
        }

        public ChatDirector getChatDirector ()
        {
            return _chatdir;
        }

        public ParlorDirector getParlorDirector ()
        {
            return _pardtr;
        }

        public void setPlaceView (PlaceView view)
        {
            // stick the place view into our frame
            _frame.setPanel((JPanel)view);
        }

        public void clearPlaceView (PlaceView view)
        {
            // we'll just let the next place view replace our old one
        }

        public ToyBoxFrame getFrame ()
        {
            return _frame;
        }

        public MessageManager getMessageManager ()
        {
            return _msgmgr;
        }

        public ToyBoxDirector getToyBoxDirector ()
        {
            return _toydtr;
        }
    }

    protected ToyBoxContext _ctx;
    protected ToyBoxFrame _frame;
    protected Config _config = new Config("toybox");

    protected Client _client;
    protected MessageManager _msgmgr;

    protected LocationDirector _locdir;
    protected OccupantDirector _occdir;
    protected ChatDirector _chatdir;
    protected ParlorDirector _pardtr;
    protected ToyBoxDirector _toydtr;

    /** The prefix prepended to localization bundle names before looking
     * them up in the classpath. */
    protected static final String MESSAGE_MANAGER_PREFIX = "rsrc.i18n";
}
