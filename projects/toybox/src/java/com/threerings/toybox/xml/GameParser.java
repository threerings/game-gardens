//
// $Id$

package com.threerings.toybox.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.ArrayList;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreateRule;
import org.apache.commons.digester.Rule;

import com.samskivert.xml.SetFieldRule;
import com.samskivert.xml.SetNextFieldRule;
import com.samskivert.xml.SetPropertyFieldsRule;

/**
 * Parses the XML definition of a game.
 */
public class GameParser
{
    public GameParser ()
    {
        // create and configure our digester
        _digester = new Digester();

        // add the rules to parse the GameDefinition and its fields
        _digester.addObjectCreate("game", GameDefinition.class.getName());

        _digester.addRule("game/ident", new SetFieldRule("ident"));
        _digester.addRule("game/config", new SetFieldRule("config"));

        _digester.addRule("game/match", new Rule() {
            public void begin (String namespace, String name, Attributes attrs)
                throws Exception {
            }
            public void end (String namespace, String name)
                throws Exception {
            }
        });

        // these rules handle customization parameters
        _digester.addRule("game/params", new ObjectCreateRule(ArrayList.class));
        _digester.addRule("game/params", new SetNextFieldRule("params"));

        _digester.addRule("game/params/range",
                          new ObjectCreateRule(RangeParameter.class));
        _digester.addRule("game/params/range", new SetPropertyFieldsRule());
        _digester.addSetNext("game/params/range",
                             "add", Object.class.getName());

        _digester.addRule("game/params/choice",
                          new ObjectCreateRule(ChoiceParameter.class));
        _digester.addRule("game/params/choice", new SetPropertyFieldsRule());
        _digester.addSetNext("game/params/choice",
                             "add", Object.class.getName());

        _digester.addRule("game/params/toggle",
                          new ObjectCreateRule(ToggleParameter.class));
        _digester.addRule("game/params/toggle", new SetPropertyFieldsRule());
        _digester.addSetNext("game/params/toggle",
                             "add", Object.class.getName());

        // these rules parse the library dependencies
        _digester.addRule("game/libs", new ObjectCreateRule(ArrayList.class));
        _digester.addRule("game/libs/library",
                          new ObjectCreateRule(Library.class));
        _digester.addRule("game/libs/library", new SetPropertyFieldsRule());
        _digester.addSetNext("game/libs/library",
                             "add", Object.class.getName());
        _digester.addRule("game/libs", new SetNextFieldRule("libs"));

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
        // push an array list on the digester which will receive the
        // parsed game definition
        ArrayList list = new ArrayList();
        _digester.push(list);
        _digester.parse(new FileInputStream(source));
        return (list.size() > 0) ? (GameDefinition)list.get(0) : null;
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
