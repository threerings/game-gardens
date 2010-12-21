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

package com.samskivert.sagashi.client;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.threerings.media.SafeScrollPane;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.toybox.util.ToyBoxContext;

import com.samskivert.sagashi.data.SagashiCodes;
import com.samskivert.sagashi.data.SagashiObject;

/**
 * Does something extraordinary.
 */
public class ScorePanel extends JPanel
    implements PlaceView
{
    public ScorePanel (ToyBoxContext ctx)
    {
        setLayout(new BorderLayout(5, 5));
        _ctx = ctx;
        _model = new ScoreTableModel();

        add(new JLabel(_ctx.xlate(
                           SagashiCodes.SAGASHI_MSG_BUNDLE, "m.scores")));
        add(new SafeScrollPane(new JTable(_model)));
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        _sagaobj = (SagashiObject)plobj;
        _sagaobj.addListener(_model);
        _model.init();
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject plobj)
    {
        if (_sagaobj != null) {
            _sagaobj.removeListener(_model);
            _sagaobj = null;
        }
    }

    protected class ScoreTableModel extends AbstractTableModel
        implements AttributeChangeListener
    {
        public void init () {
            fireTableDataChanged();
        }

        public void attributeChanged (AttributeChangedEvent event) {
            if (event.getName().equals(SagashiObject.SCORES)) {
                fireTableDataChanged();
            }
        }

        public String getColumnName (int col) {
            return _ctx.xlate(SagashiCodes.SAGASHI_MSG_BUNDLE,
                              "m.col_" + COLUMNS[col]);
        }

        public Class getColumnClass (int col) {
            return (col == 0) ? String.class : Integer.class;
        }

        public int getRowCount () {
            return (_sagaobj == null) ? 0 : _sagaobj.scores.length;
        }

        public int getColumnCount() {
            return COLUMNS.length;
        }

        public Object getValueAt (int row, int col) {
            if (col == 0) {
                OccupantInfo oinfo = null;
                if (_sagaobj != null) {
                    oinfo = _sagaobj.occupantInfo.get(_sagaobj.scores[row].userOid);
                }
                return (oinfo == null) ? "<departed>" : oinfo.username;
            } else {
                return (_sagaobj == null) ? 0 : _sagaobj.scores[row].score;
            }
        }

        public boolean isCellEditable (int row, int col) {
            return false;
        }

        public void setValueAt (Object value, int row, int col) {
            // nada
        }
    }

    protected ToyBoxContext _ctx;
    protected ScoreTableModel _model;
    protected SagashiObject _sagaobj;

    protected static final String[] COLUMNS = { "player", "score" };
}
