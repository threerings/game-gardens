//
// $Id$

package com.threerings.toybox.xml;

/**
 * Models a parameter that allows the selection of one of a list of
 * choices (specified as strings).
 */
public class ChoiceParameter
{
    /** The set of choices available for this parameter. */
    public String[] choices;

    /** The starting selection. */
    public String start;
}
