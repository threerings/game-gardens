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

package com.threerings.toybox.server;

import com.threerings.presents.net.BootstrapData;

import com.threerings.crowd.server.CrowdClient;

import com.threerings.toybox.data.ToyBoxBootstrapData;
import com.threerings.toybox.server.ToyBoxConfig;

/**
 * Extends {@link CrowdClient} and customizes it for the ToyBox system.
 */
public class ToyBoxClient extends CrowdClient
{
    // documentation inherited
    protected BootstrapData createBootstrapData ()
    {
        return new ToyBoxBootstrapData();
    }

    // documentation inherited
    protected void populateBootstrapData (BootstrapData data)
    {
        super.populateBootstrapData(data);
        ((ToyBoxBootstrapData)data).resourceURL = ToyBoxConfig.getResourceURL();
    }
}
