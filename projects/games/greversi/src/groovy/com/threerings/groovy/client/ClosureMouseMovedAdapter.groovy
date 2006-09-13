//
// $Id$

package com.threerings.groovy.client;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * Maps a mouse moved event onto a closure.
 */
public class ClosureMouseMovedAdapter extends MouseMotionAdapter
{
    def closure
    public void mouseMoved (MouseEvent event) {
        closure(event);
    }
}
