//
// $Id$

package com.threerings.toybox.xml;

/**
 * Models a paramter that can contain an integer value in a specified
 * range.
 */
public class RangeParameter extends Parameter
{
    /** The minimum value of this parameter. */
    public int minimum;

    /** The maximum value of this parameter. */
    public int maximum;

    /** The starting value for this parameter. */
    public int start;
}
