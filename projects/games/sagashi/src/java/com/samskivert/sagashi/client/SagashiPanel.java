//
// Sagashi - A word finding game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sagashi/LICENSE

package com.samskivert.sagashi.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.MultiLineLabel;
import com.samskivert.swing.VGroupLayout;
import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.Interval;
import com.samskivert.util.RunQueue;
import com.threerings.util.MessageBundle;

import com.threerings.media.SafeScrollPane;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;

import com.threerings.crowd.client.PlacePanel;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.toybox.client.ChatPanel;
import com.threerings.toybox.client.ToyBoxUI;
import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.util.ToyBoxContext;

import com.samskivert.sagashi.data.SagashiCodes;
import com.samskivert.sagashi.data.SagashiObject;

/**
 * Contains the primary client interface for a Sagashi game.
 */
public class SagashiPanel extends PlacePanel
{
    /**
     * Creates a Sagashi panel and its associated interface components.
     */
    public SagashiPanel (ToyBoxContext ctx, ToyBoxGameConfig config, SagashiController ctrl)
    {
        super(ctrl);
        _ctx = ctx;
        _msgs = _ctx.getMessageManager().getBundle(
            SagashiCodes.SAGASHI_MSG_BUNDLE);
        _minLength = (Integer)config.params.get("min_length");

        // give ourselves a wee bit of a border
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        HGroupLayout gl = new HGroupLayout(HGroupLayout.STRETCH);
        gl.setOffAxisPolicy(HGroupLayout.STRETCH);
        setLayout(gl);

        // create the panel to display the list of found words
        JPanel wpanel = VGroupLayout.makeVStretchBox(5);
        wpanel.setOpaque(false);
        wpanel.add(_wtitle = new JLabel(_msgs.get("m.your_words")), VGroupLayout.FIXED);
        wpanel.add(new SafeScrollPane(_words = new JTextArea()));
        wpanel.setPreferredSize(new Dimension(150, 10));
        add(wpanel, HGroupLayout.FIXED);

        // create a panel to hold the board and some associated bits
        JPanel panel = VGroupLayout.makeVBox();
        panel.setOpaque(false);

        // add a big fat label because we love it!
        MultiLineLabel vlabel = new MultiLineLabel(_msgs.get("m.title"));
        vlabel.setFont(ToyBoxUI.fancyFont);
        panel.add(vlabel, VGroupLayout.FIXED);

        panel.add(_bview = new SagashiBoardView(ctx));
        JPanel hpanel = HGroupLayout.makeHBox(HGroupLayout.NONE, HGroupLayout.CENTER);
        hpanel.setOpaque(false);
        hpanel.add(new JLabel(_msgs.xlate("m.enter_word")));
        hpanel.add(_input = new JTextField() {
            @Override
            public Dimension getPreferredSize () {
                Dimension d = super.getPreferredSize();
                d.width = Math.max(150, d.width);
                return d;
            }
        });
        _input.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent evt) {
                considerSubmission(_input.getText().trim());
            }
        });
        hpanel.add(_remain = new JLabel("0:00"));
        panel.add(hpanel);
        panel.add(_status = new JLabel(_msgs.get("m.waiting_for_start")));
        add(panel);

        // create our side panel
        VGroupLayout sgl = new VGroupLayout(VGroupLayout.STRETCH);
        sgl.setOffAxisPolicy(VGroupLayout.STRETCH);
        sgl.setJustification(VGroupLayout.TOP);
        JPanel sidePanel = new JPanel(sgl);
        sidePanel.setPreferredSize(new Dimension(200, 10));

        // add our score indicator
        sidePanel.add(new ScorePanel(_ctx));

        // de-opaquify everything before we add the chat box
        SwingUtil.setOpaque(sidePanel, false);
        setOpaque(true);
        setBackground(new Color(0xDAEB9C));

        // add a chat box
        ChatPanel chat = new ChatPanel(ctx);
        sidePanel.add(chat);

        // add a "back" button
        JButton back = new JButton(_msgs.get("m.back_to_lobby"));
        back.setActionCommand(SagashiController.BACK_TO_LOBBY);
        back.addActionListener(SagashiController.DISPATCHER);
        sidePanel.add(back, VGroupLayout.FIXED);

        // add our side panel to the main display
        add(sidePanel, HGroupLayout.FIXED);
    }

    /**
     * Prepares the interface for the start of the game.
     */
    public void gameDidStart ()
    {
        _wtitle.setText(_msgs.get("m.your_words"));
        _words.setText("");
        _status.setText(_msgs.get("m.go"));
    }

    /**
     * Translates and displays the specified status message.
     */
    public void displayStatus (String status)
    {
        _status.setText(_msgs.xlate(status));
    }

    /**
     * Records an accepted word.
     */
    public void recordScore (String word, int score)
    {
        _words.append(word + " " + score + "\n");
    }

    // documentation inherited from interface
    @Override
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);
        _sagaobj = (SagashiObject)plobj;

        // this guy handles the clock
        _ticker = new Ticker(_ctx.getClient().getRunQueue());
        _ticker.schedule(1000L, true);
        _sagaobj.addListener(_ticker);
    }

    // documentation inherited from interface
    @Override
    public void didLeavePlace (PlaceObject plobj)
    {
        super.didLeavePlace(plobj);

        if (_sagaobj != null) {
            _ticker.cancel();
            _sagaobj.removeListener(_ticker);
            _sagaobj = null;
        }
    }

    protected void considerSubmission (String word)
    {
        if (word.length() < _minLength) {
            _status.setText(_msgs.get("m.not_long_enough", "" + _minLength));
            return;
        }

        if (!_sagaobj.board.containsWord(word)) {
            _status.setText(_msgs.get("m.not_on_board", word));
            _input.setText("");
            return;
        }

        SagashiController.postAction(
            SagashiPanel.this, SagashiController.SUBMIT_WORD, word);
        _input.setText("");
    }

    protected void displayAllWords ()
    {
        _wtitle.setText(_msgs.get("m.everyones_words"));
        _words.setText("");
        for (String word : _sagaobj.allWords) {
            _words.append(word + "\n");
        }
    }

    protected class Ticker extends Interval
        implements AttributeChangeListener
    {
        public Ticker (RunQueue queue) {
            super(queue);
            updateEstimate();
            expired();
        }

        @Override
        public void expired () {
            int remain = (int)(_estimate - System.currentTimeMillis())/1000;
            remain = Math.max(0, remain);
            int mins = remain / 60, secs = remain % 60;
            String sep = (secs < 10) ? ":0" : ":";
            _remain.setText(mins + sep + secs);
            _remain.setForeground(remain > 10 ? Color.black : Color.red);

            // disable the input field
            if (!_sagaobj.isInPlay() && _input.isEnabled()) {
                _input.setText("");
                _input.setEnabled(false);
            } else if (_sagaobj.isInPlay() && !_input.isEnabled()) {
                _input.setEnabled(true);
                _input.requestFocus();
            }
        }

        public void attributeChanged (AttributeChangedEvent event) {
            String name = event.getName();
            if (name.equals(SagashiObject.SECONDS_UNTIL)) {
                updateEstimate();
            } else if (name.equals(SagashiObject.ALL_WORDS)) {
                displayAllWords();
            }
        }

        protected void updateEstimate () {
            _estimate = System.currentTimeMillis() + 1000L * _sagaobj.secondsUntil;
        }

        protected long _estimate;
    };

    protected ToyBoxContext _ctx;
    protected MessageBundle _msgs;
    protected SagashiObject _sagaobj;
    protected int _minLength;
    protected Ticker _ticker;

    protected SagashiBoardView _bview;
    protected JTextField _input;
    protected JLabel _wtitle;
    protected JTextArea _words;
    protected JLabel _remain;
    protected JLabel _status;
}
