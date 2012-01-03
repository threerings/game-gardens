//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2012 Three Rings Design, Inc., All Rights Reserved
// http://github.com/threerings/game-gardens
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

package com.threerings.toybox.lobby.data;

import javax.annotation.Generated;
import com.samskivert.util.ArrayIntSet;

import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.RootDObjectManager;

import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.data.Table;
import com.threerings.parlor.data.TableLobbyObject;
import com.threerings.parlor.data.TableMarshaller;

/**
 * Presently the lobby object contains nothing specific, but the class acts as a placeholder in
 * case lobby-wide fields are needed in the future.
 */
public class LobbyObject extends PlaceObject
     implements TableLobbyObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>name</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String NAME = "name";

    /** The field name of the <code>tableSet</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String TABLE_SET = "tableSet";

    /** The field name of the <code>tableService</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String TABLE_SERVICE = "tableService";
    // AUTO-GENERATED: FIELDS END

    /** The name of the game we're match-making in this lobby. */
    public String name;

    /** A set containing all of the tables being managed by this lobby.  This may be empty if we're
     * not using tables to match-make. */
    public DSet<Table> tableSet = new DSet<Table>();

    /** Used to communicate to the table manager. */
    public TableMarshaller tableService;

    // from interface TableLobbyObject
    public DSet<Table> getTables ()
    {
        return tableSet;
    }

    // from interface TableLobbyObject
    public TableMarshaller getTableService ()
    {
        return tableService;
    }

    // from interface TableLobbyObject
    public void addToTables (Table table)
    {
        addToTableSet(table);
    }

    // from interface TableLobbyObject
    public void updateTables (Table table)
    {
        updateTableSet(table);
    }

    // from interface TableLobbyObject
    public void removeFromTables (Comparable<?> key)
    {
        removeFromTableSet(key);
    }

    /**
     * Counts up the occupants of this lobby and of all games hosted from this lobby.
     */
    public int countOccupants (RootDObjectManager omgr)
    {
        // add the occupants of the room
        ArrayIntSet occids = new ArrayIntSet();
        for (int ii = 0; ii < occupants.size(); ii++) {
            occids.add(occupants.get(ii));
        }
        for (Table table : tableSet) {
            if (table.gameOid > 0) {
                // for now we can directly reference the game object
                Object obj = omgr.getObject(table.gameOid);
                if (obj instanceof PlaceObject) {
                    PlaceObject plobj = (PlaceObject)obj;
                    for (int ii = 0; ii < plobj.occupants.size(); ii++) {
                        occids.add(plobj.occupants.get(ii));
                    }
                }
            }
        }
        // remove any zeros that got in from a zero bodyOid
        occids.remove(0);
        return occids.size();
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>name</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setName (String value)
    {
        String ovalue = this.name;
        requestAttributeChange(
            NAME, value, ovalue);
        this.name = value;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>tableSet</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void addToTableSet (Table elem)
    {
        requestEntryAdd(TABLE_SET, tableSet, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>tableSet</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void removeFromTableSet (Comparable<?> key)
    {
        requestEntryRemove(TABLE_SET, tableSet, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>tableSet</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void updateTableSet (Table elem)
    {
        requestEntryUpdate(TABLE_SET, tableSet, elem);
    }

    /**
     * Requests that the <code>tableSet</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setTableSet (DSet<Table> value)
    {
        requestAttributeChange(TABLE_SET, value, this.tableSet);
        DSet<Table> clone = (value == null) ? null : value.clone();
        this.tableSet = clone;
    }

    /**
     * Requests that the <code>tableService</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setTableService (TableMarshaller value)
    {
        TableMarshaller ovalue = this.tableService;
        requestAttributeChange(
            TABLE_SERVICE, value, ovalue);
        this.tableService = value;
    }
    // AUTO-GENERATED: METHODS END
}
