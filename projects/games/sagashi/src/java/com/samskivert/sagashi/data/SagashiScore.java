//
// Sagashi - A word finding game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sagashi/LICENSE

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
    public Comparable<?> getKey ()
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
