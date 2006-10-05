//
// $Id$
//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2006 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.toybox.data {

import com.threerings.util.*; // for Float, Integer, etc.

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService_ResultListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;
import com.threerings.toybox.client.ToyBoxService;

/**
 * Provides the implementation of the {@link ToyBoxService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class ToyBoxMarshaller extends InvocationMarshaller
    implements ToyBoxService
{
    /** The method id used to dispatch {@link #getLobbyOid} requests. */
    public static const GET_LOBBY_OID :int = 1;

    // from interface ToyBoxService
    public function getLobbyOid (arg1 :Client, arg2 :int, arg3 :InvocationService_ResultListener) :void
    {
        var listener3 :InvocationMarshaller_ResultMarshaller = new InvocationMarshaller_ResultMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, GET_LOBBY_OID, [
            Integer.valueOf(arg2), listener3
        ]);
    }
}
}
