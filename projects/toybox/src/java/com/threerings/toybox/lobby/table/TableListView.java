//
// $Id: TableListView.java 3098 2004-08-27 02:12:55Z mdb $
//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.toybox.lobby.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.SimpleSlider;
import com.samskivert.swing.VGroupLayout;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.media.SafeScrollPane;
import com.threerings.util.MessageBundle;

import com.threerings.parlor.client.SeatednessObserver;
import com.threerings.parlor.client.TableDirector;
import com.threerings.parlor.client.TableObserver;
import com.threerings.parlor.data.Table;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.data.TableLobbyObject;

import com.threerings.parlor.game.client.GameConfigurator;
import com.threerings.parlor.game.client.SwingGameConfigurator;

import com.threerings.toybox.data.GameDefinition;
import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.util.ToyBoxContext;

import com.threerings.toybox.lobby.data.LobbyCodes;
import com.threerings.toybox.lobby.data.LobbyObject;

import static com.threerings.toybox.lobby.Log.log;

/**
 * A view that displays the tables in a table lobby. It displays two
 * separate lists, one of tables being matchmade and another of games in
 * progress. These tables are updated dynamically as they proceed through
 * the matchmaking process. UI mechanisms for creating and joining tables
 * are also provided.
 */
public class TableListView extends JPanel
    implements PlaceView, TableObserver, ActionListener, SeatednessObserver
{
    /**
     * Creates a new table list view, suitable for providing the user
     * interface for table-style matchmaking in a table lobby.
     */
    public TableListView (ToyBoxContext ctx, ToyBoxGameConfig config)
    {
        // keep track of these
        _config = config;
        _ctx = ctx;

        MessageBundle msgs =
            ctx.getMessageManager().getBundle(LobbyCodes.LOBBY_MSGS);

        // create our table director
        _tdtr = new TableDirector(ctx, LobbyObject.TABLE_SET, this);

        // add ourselves as a seatedness observer
        _tdtr.addSeatednessObserver(this);

        // set up a layout manager
	HGroupLayout gl = new HGroupLayout(HGroupLayout.STRETCH);
	gl.setOffAxisPolicy(HGroupLayout.STRETCH);
	setLayout(gl);

        // we have two lists of tables, one of tables being matchmade...
        VGroupLayout pgl = new VGroupLayout(VGroupLayout.STRETCH);
        pgl.setOffAxisPolicy(VGroupLayout.STRETCH);
        pgl.setJustification(VGroupLayout.TOP);
        JPanel panel = new JPanel(pgl);
        String cmsg = config.isPartyGame() ?
            "m.create_game" : "m.pending_tables";
        panel.add(new JLabel(msgs.get(cmsg)), VGroupLayout.FIXED);

        VGroupLayout mgl = new VGroupLayout(VGroupLayout.NONE);
        mgl.setOffAxisPolicy(VGroupLayout.STRETCH);
        mgl.setJustification(VGroupLayout.TOP);
        _matchList = new JPanel(mgl);
        if (!config.isPartyGame()) {
            _matchList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            panel.add(new SafeScrollPane(_matchList));
        }

        // create and initialize our configurator interface
        _figger = _config.createConfigurator();
        if (_figger != null) {
            _figger.init(_ctx);
            _figger.setGameConfig(config);
            panel.add(((SwingGameConfigurator) _figger).getPanel(),
                VGroupLayout.FIXED);
        }

        // add the interface for selecting the number of seats at the table
        panel.add(_pslide = new SimpleSlider(msgs.get("m.seats"), 0, 10, 0),
                  VGroupLayout.FIXED);

        // configure our slider
        _pslide.setMinimum(config.getMinimumPlayers());
        _pslide.setMaximum(config.getMaximumPlayers());
        _pslide.setValue(config.getDesiredPlayers());

        int range = config.getMaximumPlayers() - config.getMinimumPlayers();
        _pslide.getSlider().setPaintTicks(true);
        _pslide.getSlider().setMinorTickSpacing(1);
        _pslide.getSlider().setMajorTickSpacing(range / 2);
        _pslide.getSlider().setSnapToTicks(true);

        // if the min == the max, hide the slider because it's pointless
        _pslide.setVisible(config.getMinimumPlayers() !=
                           config.getMaximumPlayers());

        cmsg = config.isPartyGame() ? "m.create_game" : "m.create_table";
        _create = new JButton(msgs.get(cmsg));
        _create.addActionListener(this);
        JPanel bbox = HGroupLayout.makeButtonBox(HGroupLayout.RIGHT);
        bbox.add(_create);
        panel.add(bbox, VGroupLayout.FIXED);

        if (config.isPartyGame()) {
            panel.add(new JLabel(msgs.get("m.party_hint")), VGroupLayout.FIXED);
        }

        add(panel);

        // ...and one of games in progress
        panel = new JPanel(pgl);
        panel.add(new JLabel(msgs.get("m.in_progress")), VGroupLayout.FIXED);

        _playList = new JPanel(mgl);
    	_playList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(new SafeScrollPane(_playList));

        add(panel);
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject place)
    {
        // pass the good word on to our table director
        _tdtr.willEnterPlace(place);

        // iterate over the tables already active in this lobby and put
        // them in their respective lists
        TableLobbyObject tlobj = (TableLobbyObject)place;
        Iterator iter = tlobj.getTables().iterator();
        while (iter.hasNext()) {
            tableAdded((Table)iter.next());
        }
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject place)
    {
        // pass the good word on to our table director
        _tdtr.didLeavePlace(place);

        // clear out our table lists
        _matchList.removeAll();
        _playList.removeAll();
    }

    // documentation inherited
    public void tableAdded (Table table)
    {
        log.info("Table added [table=" + table + "].");

        // create a table item for this table and insert it into the
        // appropriate list
        JPanel panel = table.inPlay() ? _playList : _matchList;
        panel.add(new TableItem(_ctx, _tdtr, table));
        SwingUtil.refresh(panel);
    }

    // documentation inherited
    public void tableUpdated (Table table)
    {
        log.info("Table updated [table=" + table + "].");

        // locate the table item associated with this table
        TableItem item = getTableItem(table.getTableId());
        if (item == null) {
            log.warning("Received table updated notification for " +
                        "unknown table [table=" + table + "].");
            return;
        }

        // let the item perform any updates it finds necessary
        item.tableUpdated(table);

        // and we may need to move the item from the match to the in-play
        // list if it just transitioned
        if (table.gameOid != -1 && item.getParent() == _matchList) {
            _matchList.remove(item);
            SwingUtil.refresh(_matchList);
            _playList.add(item);
            SwingUtil.refresh(_playList);
        }
    }

    // documentation inherited
    public void tableRemoved (int tableId)
    {
        log.info("Table removed [tableId=" + tableId + "].");

        // locate the table item associated with this table
        TableItem item = getTableItem(tableId);
        if (item == null) {
            log.warning("Received table removed notification for " +
                        "unknown table [tableId=" + tableId + "].");
            return;
        }

        // remove this item from the user interface
        JPanel panel = (JPanel)item.getParent();
        panel.remove(item);
        SwingUtil.refresh(panel);

        // let the little fellow know that we gave him the boot
        item.tableRemoved();
    }

    // documentation inherited
    public void actionPerformed (ActionEvent event)
    {
        // the create table button was clicked. use the game config as
        // configured by the configurator to create a table
        ToyBoxGameConfig config = _config;
        if (_figger != null) {
            config = (ToyBoxGameConfig)_figger.getGameConfig();
        }

        // fill in our number of seats configuration
        config.setDesiredPlayers(_pslide.getValue());

        TableConfig tconfig = new TableConfig();
        tconfig.desiredPlayerCount = _pslide.getValue();

        _tdtr.createTable(tconfig, config);
    }

    // documentation inherited
    public void seatednessDidChange (boolean isSeated)
    {
        // update the create table button
        _create.setEnabled(!isSeated);
    }

    /**
     * Fetches the table item component associated with the specified
     * table id.
     */
    protected TableItem getTableItem (int tableId)
    {
        // first check the match list
        int ccount = _matchList.getComponentCount();
        for (int i = 0; i < ccount; i++) {
            TableItem child = (TableItem)_matchList.getComponent(i);
            if (child.table.getTableId() == tableId) {
                return child;
            }
        }

        // then the inplay list
        ccount = _playList.getComponentCount();
        for (int i = 0; i < ccount; i++) {
            TableItem child = (TableItem)_playList.getComponent(i);
            if (child.table.getTableId() == tableId) {
                return child;
            }
        }

        // sorry charlie
        return null;
    }

    /** A reference to the client context. */
    protected ToyBoxContext _ctx;

    /** The configuration for the game that we're match-making. */
    protected ToyBoxGameConfig _config;

    /** A reference to our table director. */
    protected TableDirector _tdtr;

    /** The list of tables currently being matchmade. */
    protected JPanel _matchList;

    /** Our number of players slider. */
    protected SimpleSlider _pslide;

    /** The list of tables that are in play. */
    protected JPanel _playList;

    /** The interface used to configure a table before creating it. */
    protected GameConfigurator _figger;

    /** Our create table button. */
    protected JButton _create;

    /** Our number of players indicator. */
    protected JLabel _pcount;
}
