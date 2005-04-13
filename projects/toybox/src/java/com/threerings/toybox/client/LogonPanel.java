//
// $Id$
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

package com.threerings.toybox.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import java.io.InputStream;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.samskivert.servlet.user.Password;
import com.samskivert.util.StringUtil;
import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.MultiLineLabel;
import com.samskivert.swing.VGroupLayout;

import com.threerings.media.image.BufferedMirage;
import com.threerings.media.image.ImageUtil;
import com.threerings.media.image.Mirage;

import com.threerings.util.MessageBundle;
import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientObserver;
import com.threerings.presents.client.LogonException;
import com.threerings.presents.net.Credentials;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.toybox.util.ToyBoxContext;

import static com.threerings.toybox.Log.log;

public class LogonPanel extends JPanel
    implements ActionListener, ClientObserver
{
    public LogonPanel (ToyBoxContext ctx)
    {
        // keep these around for later
        _ctx = ctx;
        _msgs = _ctx.getMessageManager().getBundle("client.logon");

	setLayout(new VGroupLayout());
        setBackground(ToyBoxUI.LIGHT_BLUE);

        // stick the logon components into a panel that will stretch them
        // to a sensible width
        JPanel box = new JPanel(
            new VGroupLayout(VGroupLayout.NONE, VGroupLayout.NONE,
                             5, VGroupLayout.CENTER)) {
            public Dimension getPreferredSize () {
                Dimension psize = super.getPreferredSize();
                psize.width = Math.max(psize.width, 300);
                return psize;
            }
        };
        box.setOpaque(false);
        add(box);

        // load our background imagery
        try {
            _bgimg = ImageIO.read(
                getClass().getClassLoader().getResourceAsStream(
                    "rsrc/media/logon_background.png"));
            _flowers = new BufferedMirage(
                ImageIO.read(
                    getClass().getClassLoader().getResourceAsStream(
                        "rsrc/media/lobby_background.png")));
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to load background image.", e);
        }

        // try obtaining our title text from a system property
        String tstr = null;
        try {
            tstr = System.getProperty("game_name");
        } catch (Throwable t) {
        }
        if (tstr == null) {
            tstr = _msgs.get("m.default_title");
        }

        // create a big fat label
        MultiLineLabel title = new MultiLineLabel(tstr, MultiLineLabel.CENTER);
        title.setFont(ToyBoxUI.fancyFont);
        title.setAntiAliased(true);
        box.add(title);

        // float the logon bits side-by-side inside the wider panel
        HGroupLayout hlay = new HGroupLayout();
        hlay.setOffAxisJustification(HGroupLayout.BOTTOM);
        JPanel hbox = new JPanel(hlay);
        hbox.setOpaque(false);
        box.add(hbox);

        // this contains the username and password stuff
        VGroupLayout vlay = new VGroupLayout();
        vlay.setOffAxisJustification(VGroupLayout.RIGHT);
        JPanel subbox = new JPanel(vlay);
        subbox.setOpaque(false);
        hbox.add(subbox);
        JPanel bar = new JPanel(new HGroupLayout(GroupLayout.NONE));
        bar.add(new JLabel(_msgs.get("m.username")));
        bar.setOpaque(false);
        _username = new JTextField();
        _username.setText(ToyBoxPrefs.getUsername());
        _username.setPreferredSize(new Dimension(100, 20));
        _username.setActionCommand("skipToPassword");
        _username.addActionListener(this);
        bar.add(_username);
        subbox.add(bar);
        bar = new JPanel(new HGroupLayout(GroupLayout.NONE));
        bar.setOpaque(false);
        bar.add(new JLabel(_msgs.get("m.password")));
        _password = new JPasswordField();
        _password.setPreferredSize(new Dimension(100, 20));
        _password.setActionCommand("logon");
        _password.addActionListener(this);
        _password.setText(StringUtil.fill('*', ToyBoxPrefs.getPasswordLength()));
        bar.add(_password);
        subbox.add(bar);

        // create the logon button bar
        _logon = new JButton(_msgs.get("m.logon"));
        _logon.setActionCommand("logon");
        _logon.addActionListener(this);
        hbox.add(_logon);

        _remember = new JCheckBox(_msgs.get("m.remember_password"));
        _remember.setOpaque(false);
        _remember.setSelected(ToyBoxPrefs.getRememberPassword());
        box.add(_remember);

        _status = new MultiLineLabel(_msgs.get("m.please_logon"));
        box.add(_status);

        // we'll want to listen for logon failure
        _ctx.getClient().addClientObserver(this);

        // start with focus in the username field
        _username.requestFocus();
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("skipToPassword")) {
            _password.requestFocus();

        } else if (cmd.equals("logon")) {
            logon();

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    // documentation inherited from interface
    public void clientDidLogon (Client client)
    {
        _status.setText(_msgs.get("m.logon_success") + "\n");
    }

    // documentation inherited from interface
    public void clientDidLogoff (Client client)
    {
        _status.setText(_msgs.get("m.logged_off") + "\n");
        setLogonEnabled(true);
    }

    // documentation inherited from interface
    public void clientFailedToLogon (Client client, Exception cause)
    {
        String msg;
        if (cause instanceof LogonException) {
            msg = MessageBundle.compose("m.logon_failed", cause.getMessage());
        } else {
            msg = MessageBundle.tcompose("m.logon_failed", cause.getMessage());
        }
        _status.setText(_msgs.xlate(msg) + "\n");
        setLogonEnabled(true);
    }

    // documentation inherited from interface
    public void clientObjectDidChange (Client client)
    {
        // nothing we can do here...
    }

    // documentation inherited from interface
    public void clientConnectionFailed (Client client, Exception cause)
    {
        String msg = MessageBundle.tcompose("m.connection_failed",
                                            cause.getMessage());
        _status.setText(_msgs.xlate(msg) + "\n");
        setLogonEnabled(true);
    }

    // documentation inherited from interface
    public boolean clientWillLogoff (Client client)
    {
        // no vetoing here
        return true;
    }

    // documentation inherited
    protected void paintComponent (Graphics g)
    {
        super.paintComponent(g);
        int width = getWidth(), height = getHeight();

        // first tile the flowers
        if (_flowers != null) {
            Graphics2D gfx = (Graphics2D)g;
            ImageUtil.tileImage(gfx, _flowers, 0, 0, width, height);
        }

        // then draw the background image centered on top of that
        if (_bgimg != null) {
            g.drawImage(_bgimg, (width - _bgimg.getWidth())/2,
                        (height - _bgimg.getHeight())/2, null);
        }
    }

    protected void logon ()
    {
        // disable further logon attempts until we hear back
        setLogonEnabled(false);

        String username = _username.getText().trim();
        String password = new String(_password.getPassword()).trim();

        String server = _ctx.getClient().getHostname();
        int port = _ctx.getClient().getPort();
        String msg = MessageBundle.tcompose("m.logging_on",
                                            server, String.valueOf(port));
        _status.setText(_msgs.xlate(msg) + "\n");

        // take care of the password stuff
        int pwLen = ToyBoxPrefs.getPasswordLength();
        Password encPw;
        if (StringUtil.fill('*', pwLen).equals(password)) {
            // use their stored value
            encPw = Password.makeFromCrypto(ToyBoxPrefs.getPassword());
        } else {
            // use what they typed
            pwLen = password.length();
            encPw = Password.makeFromClear(password);
        }

        // update the logon-related config values
        ToyBoxPrefs.setUsername(username);
        boolean remember = _remember.isSelected();
        ToyBoxPrefs.setPassword(remember ? encPw.getEncrypted() : "");
        ToyBoxPrefs.setPasswordLength(remember ? pwLen : 0); 
        ToyBoxPrefs.setRememberPassword(remember);

        // configure the client with some credentials and logon
        Client client = _ctx.getClient();
        client.setCredentials(createCredentials(username, encPw));
        client.logon();
    }

    protected void setLogonEnabled (boolean enabled)
    {
        _username.setEnabled(enabled);
        _password.setEnabled(enabled);
        _logon.setEnabled(enabled);
    }

    /** Creates the appropriate type of credentials from the supplied
     * username and plaintext password. */
    public static Credentials createCredentials (String username, String pw)
    {
        return createCredentials(username, Password.makeFromClear(pw));
    }

    /** Creates the appropriate type of credentials from the supplied
     * username and encrypted password. */
    public static Credentials createCredentials (String username, Password pw)
    {
        return new UsernamePasswordCreds(new Name(username), pw.getEncrypted());
    }

    protected ToyBoxContext _ctx;
    protected MessageBundle _msgs;

    protected JTextField _username;
    protected JPasswordField _password;
    protected JCheckBox _remember;
    protected JButton _logon;
    protected MultiLineLabel _status;

    protected BufferedImage _bgimg;
    protected Mirage _flowers;
}
