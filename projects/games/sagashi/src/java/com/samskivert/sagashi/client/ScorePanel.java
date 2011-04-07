//
// Sagashi - A word finding game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sagashi/LICENSE

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

        @Override
        public String getColumnName (int col) {
            return _ctx.xlate(SagashiCodes.SAGASHI_MSG_BUNDLE,
                              "m.col_" + COLUMNS[col]);
        }

        @Override
        public Class<?> getColumnClass (int col) {
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

        @Override
        public boolean isCellEditable (int row, int col) {
            return false;
        }

        @Override
        public void setValueAt (Object value, int row, int col) {
            // nada
        }
    }

    protected ToyBoxContext _ctx;
    protected ScoreTableModel _model;
    protected SagashiObject _sagaobj;

    protected static final String[] COLUMNS = { "player", "score" };
}
