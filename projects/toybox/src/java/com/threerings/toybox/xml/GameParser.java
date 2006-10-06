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

package com.threerings.toybox.xml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreateRule;
import org.apache.commons.digester.Rule;

import com.samskivert.util.StringUtil;
import com.samskivert.xml.SetFieldRule;
import com.samskivert.xml.SetNextFieldRule;
import com.samskivert.xml.SetPropertyFieldsRule;

import com.threerings.toybox.data.ChoiceParameter;
import com.threerings.toybox.data.FileParameter;
import com.threerings.toybox.data.GameDefinition;
import com.threerings.toybox.data.Library;
import com.threerings.toybox.data.MatchConfig;
import com.threerings.toybox.data.AIParameter;
import com.threerings.toybox.data.RangeParameter;
import com.threerings.toybox.data.TableMatchConfig;
import com.threerings.toybox.data.ToggleParameter;

/**
 * Parses the XML definition of a game.
 */
public class GameParser
{
    public GameParser ()
    {
        // create and configure our digester
        _digester = new Digester() {
            public void fatalError (SAXParseException exception)
                throws SAXException {
                // the standard digester needlessly logs a fatal warning here
                if (errorHandler != null) {
                    errorHandler.fatalError(exception);
                }
            }
        };

        // add the rules to parse the GameDefinition and its fields
        _digester.addObjectCreate("game", getGameDefinitionClass());
        _digester.addRule("game/ident", new SetFieldRule("ident"));
        _digester.addRule("game/controller", new SetFieldRule("controller"));
        _digester.addRule("game/manager", new SetFieldRule("manager"));

        _digester.addRule("game/match", new Rule() {
            public void begin (String namespace, String name, Attributes attrs)
                throws Exception {
                String type = attrs.getValue("type");
                if (StringUtil.isBlank("type")) {
                    String errmsg = "<match> block missing type attribute.";
                    throw new Exception(errmsg);
                }
                addMatchParsingRules(digester, type);
            }
            public void end (String namespace, String name)
                throws Exception {
                MatchConfig match = (MatchConfig)digester.pop();
                ((GameDefinition)digester.peek()).match = match;
            }
        });

        // these rules handle customization parameters
        _digester.addRule("game/params", new ObjectCreateRule(ArrayList.class));
        _digester.addSetNext("game/params", "setParams",
                             ArrayList.class.getName());
        addParameter("game/params/ai", AIParameter.class);
        addParameter("game/params/range", RangeParameter.class);
        addParameter("game/params/choice", ChoiceParameter.class);
        addParameter("game/params/toggle", ToggleParameter.class);
        addParameter("game/params/file", FileParameter.class);

        // these rules parse the library dependencies
        _digester.addRule("game/libs", new ObjectCreateRule(ArrayList.class));
        _digester.addRule("game/libs/library",
                          new ObjectCreateRule(Library.class));
        _digester.addRule("game/libs/library", new SetPropertyFieldsRule());
        _digester.addSetNext("game/libs/library",
                             "add", Object.class.getName());
        _digester.addSetNext("game/libs", "setLibs", ArrayList.class.getName());

        // add a rule to put the parsed definition onto our list
        _digester.addSetNext("game", "add", Object.class.getName());
    }

    /**
     * Parses a game definition from the supplied XML file.
     *
     * @exception IOException thrown if an error occurs reading the file.
     * @exception SAXException thrown if an error occurs parsing the XML.
     */
    public GameDefinition parseGame (File source)
        throws IOException, SAXException
    {
        return parseGame(new FileReader(source));
    }

    /**
     * Parses a game definition from the supplied XML source.
     *
     * @exception IOException thrown if an error occurs reading the file.
     * @exception SAXException thrown if an error occurs parsing the XML.
     */
    public GameDefinition parseGame (Reader source)
        throws IOException, SAXException
    {
        // make sure nothing is lingering on the stack from a previous failure
        _digester.clear();
        // push an array list on the digester which will receive the
        // parsed game definition
        ArrayList list = new ArrayList();
        _digester.push(list);
        _digester.parse(source);
        return (list.size() > 0) ? (GameDefinition)list.get(0) : null;
    }

    /**
     * Returns the {@link GameDefinition} class (or derived class) to use when
     * parsing our game definition.
     */
    protected String getGameDefinitionClass ()
    {
        return GameDefinition.class.getName();
    }

    protected void addParameter (String path, Class pclass)
    {
        _digester.addRule(path, new ObjectCreateRule(pclass));
        _digester.addRule(path, new SetPropertyFieldsRule());
        _digester.addSetNext(path, "add", Object.class.getName());
    }

    /**
     * Adds the rules needed to parse a custom match config, as well as
     * the {@link MatchConfig} derived instance itself, based on the
     * supplied type.
     */
    protected void addMatchParsingRules (Digester digester, String type)
        throws Exception
    {
        if (type.equals("table")) {
            digester.push(new TableMatchConfig());
            digester.addRule("game/match/min_seats",
                             new SetFieldRule("minSeats"));
            digester.addRule("game/match/max_seats",
                             new SetFieldRule("maxSeats"));
            digester.addRule("game/match/start_seats",
                             new SetFieldRule("startSeats"));

        } else if (type.equals("party")) {
            // party games are handled by a specially configured table
            TableMatchConfig config = new TableMatchConfig();
            config.minSeats = config.maxSeats = config.startSeats = 1;
            config.isPartyGame = true;
            digester.push(config);

        } else {
            String errmsg = "Unknown match-making config type '" + type + "'.";
            throw new Exception(errmsg);
        }
    }

    /**
     * A simple hook for parsing a game definitions from the command line.
     */
    public static void main (String[] args)
    {
        if (args.length < 1) {
            System.err.println("Usage: GameParser game.xml [game.xml ...]");
            System.exit(-1);
        }

        GameParser gp = new GameParser();
        for (int ii = 0; ii < args.length; ii++) {
            try {
                System.out.println("Parsed " + args[ii] + ": " +
                                   gp.parseGame(new File(args[ii])));
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    /** Used to process XML descriptions. */
    protected Digester _digester;
}
