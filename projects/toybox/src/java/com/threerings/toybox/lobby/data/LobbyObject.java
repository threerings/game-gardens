//
// $Id$

package com.threerings.toybox.lobby.data;

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
    public static final String NAME = "name";

    /** The field name of the <code>tableSet</code> field. */
    public static final String TABLE_SET = "tableSet";

    /** The field name of the <code>tableService</code> field. */
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
    public void addToTableSet (Table elem)
    {
        requestEntryAdd(TABLE_SET, tableSet, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>tableSet</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromTableSet (Comparable<?> key)
    {
        requestEntryRemove(TABLE_SET, tableSet, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>tableSet</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
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
    public void setTableSet (DSet<Table> value)
    {
        requestAttributeChange(TABLE_SET, value, this.tableSet);
        DSet<Table> clone = (value == null) ? null : value.typedClone();
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
    public void setTableService (TableMarshaller value)
    {
        TableMarshaller ovalue = this.tableService;
        requestAttributeChange(
            TABLE_SERVICE, value, ovalue);
        this.tableService = value;
    }
    // AUTO-GENERATED: METHODS END
}
