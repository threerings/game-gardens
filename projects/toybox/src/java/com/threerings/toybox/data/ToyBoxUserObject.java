//
// $Id$
//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.toybox.data;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.TokenRing;
import com.whirled.game.data.WhirledGameOccupantInfo;
import com.whirled.game.data.WhirledPlayerObject;

/**
 * Extends the {@link BodyObject} with some custom bits needed for ToyBox.
 * NOTE: This intentionally does not subclass from {@link WhirledPlayerObject} because toybox does 
 * not require {@link WhirledGameOccupantInfo} or other extra functionality as of yet.
 */
public class ToyBoxUserObject extends BodyObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>tokens</code> field. */
    public static final String TOKENS = "tokens";
    // AUTO-GENERATED: FIELDS END

    /** Indicates which access control tokens are held by this user. */
    public TokenRing tokens;

    @Override // from BodyObject
    public TokenRing getTokens ()
    {
        return tokens;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>tokens</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setTokens (TokenRing value)
    {
        TokenRing ovalue = this.tokens;
        requestAttributeChange(
            TOKENS, value, ovalue);
        this.tokens = value;
    }
    // AUTO-GENERATED: METHODS END
}
