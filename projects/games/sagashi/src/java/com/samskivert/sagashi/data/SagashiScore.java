//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2011 Three Rings Design, Inc., All Rights Reserved
// http://github.com/samskivert/game-gardens
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

package com.samskivert.sagashi.data;

import com.threerings.io.SimpleStreamableObject;
import com.threerings.presents.dobj.DSet;

/**
 * A score record for a particular player.
 */
public class SagashiScore extends SimpleStreamableObject
    implements DSet.Entry, Comparable<SagashiScore>
{
    /** The object id of the user associated with this record. */
    public int userOid;

    /** This user's score. */
    public int score;

    public SagashiScore ()
    {
    }

    public SagashiScore (int userOid)
    {
        this.userOid = userOid;
    }

    // documentation inherited from interface DSet.Entry
    public Comparable getKey ()
    {
        if (_key == null) {
            _key = new Integer(userOid);
        }
        return _key;
    }

    // documentation inherited from interface Comparable
    public int compareTo (SagashiScore other)
    {
        return other.score - score;
    }

    protected transient Integer _key;
}
