//
// $Id: SampleBoard.java,v 1.6 2002/07/26 21:53:22 mdb Exp $

package com.whomever.sample.data;

import com.threerings.io.SimpleStreamableObject;

/**
 * Provides access to the abstract board representation and manages
 * encoding and decoding of same.
 */
public class SampleBoard extends SimpleStreamableObject
{
    /** The board width (in cells). */
    public int width;

    /** The board height (in cells). */
    public int height;

    /**
     * Used when unserializing.
     */
    public SampleBoard ()
    {
    }

    /**
     * Creates a sample board of the specified dimensions.
     */
    public SampleBoard (int width, int height)
    {
        this.width = width;
        this.height = height;
    }
}
