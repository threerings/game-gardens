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

package com.threerings.toybox.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.google.common.collect.Maps;

/**
 * An ant task that generates a new game project from a template file and some user input.
 */
public class NewGameProjectTask extends Task
{
    /**
     * Tells the task where to find its template files.
     */
    public void setTemplates (File templates)
    {
        _templates = templates;
    }

    @Override // documentation inherited
    public void execute () throws BuildException
    {
        // first we need to ask the user for some information
        BufferedReader input =
            new BufferedReader(new InputStreamReader(System.in));

        String ident, pkgpre, classpre, fullpkg;

        do {
            ident = readInput(input, "Please enter a string that " +
                "will be used to identify your game (e.g. reversi):");
            ident = ident.toLowerCase();
            pkgpre = readInput(input, "Please enter the package to use " +
                "for your game classes (e.g. com.something):");
            pkgpre = pkgpre.toLowerCase();
            classpre = readInput(
                input, "Please enter the prefix to use for naming your game " +
                "classes (e.g. Reversi):");
            fullpkg = pkgpre + "." + ident;

            System.out.println("We will generate classes named:");
            System.out.println("  " + fullpkg + "." + classpre + "Object");
            System.out.println("  " + fullpkg + "." + classpre + "Panel");
            System.out.println("  " + fullpkg + "." + classpre + "Controller");
            System.out.println("  " + fullpkg + "." + classpre + "Manager");

        } while (!readConfirm(input));

        // create a directory for the project
        File pdir = new File(ident);
        makeDir(pdir);

        // create the source directory
        File sdir = new File(pdir, "src" + File.separator + "java" +
            File.separator + fullpkg.replace('.', File.separatorChar));
        makeDir(sdir);

        // create the resource directory
        File rdir = new File(pdir, "rsrc" + File.separator + "i18n");
        makeDir(rdir);

        // customize the template files and copy them into the right place
        HashMap<String, String> subs = Maps.newHashMap();
        subs.put("project", ident);
        subs.put("package", fullpkg);
        subs.put("classpre", classpre);

        copyFile(input, new File(_templates, "build.xml"),
            new File(pdir, "build.xml"), subs);
        copyFile(input, new File(_templates, "template.xml"),
            new File(pdir, ident + ".xml"), subs);
        copyFile(input, new File(_templates, "template.properties"),
            new File(rdir, ident + ".properties"), subs);

        for (String cname : CLASSES) {
            copyFile(input, new File(_templates, "Template" + cname + ".java"),
                new File(sdir, classpre + cname + ".java"), subs);
        }

        System.out.println("Done! Your new game has been created in '" + pdir + "'.");
        System.out.println("");
        System.out.println("Go into that directory and try the following commands:");
        System.out.println("  Build the game: ant dist");
        System.out.println("  Run the server: ant server");
        System.out.println("  Run a client: ant -Dusername=NAME client");
        System.out.println("");
        System.out.println("Have fun making your new game.");
    }

    protected String readInput (BufferedReader input, String prompt)
        throws BuildException
    {
        String line;
        try {
            do {
                System.out.print(prompt);
                line = input.readLine();
                if (line == null) { // handle EOF
                    throw new BuildException("Aborting");
                }

            } while (line.length() == 0);
            return line;

        } catch (IOException ioe) {
            throw new BuildException("Error reading input: " + ioe);
        }
    }

    protected boolean readConfirm (BufferedReader input)
        throws BuildException
    {
        return readInput(input, "Is this OK? [y/n]").equalsIgnoreCase("y");
    }

    protected void makeDir (File dir)
        throws BuildException
    {
        if (!(dir.exists() && dir.isDirectory())) {
            if (!dir.mkdirs()) {
                throw new BuildException("Failed to create directory '" + dir + "'.");
            }
        }
    }

    protected void copyFile (
        BufferedReader input, File source, File dest, HashMap<String, String> subs)
    {
        // ask whether to overwrite if the file already exists
        if (dest.exists()) {
            if (!_overwriteAll) {
                String response = readInput(
                    input, "File '" + dest + "' already exists. Overwrite? [y/n/A]");
                if (response.equalsIgnoreCase("y")) {
                    // fall through and overwrite
                } else if (response.equals("A")) {
                    _overwriteAll = true;
                    // fall through and overwrite
                } else {
                    System.out.println("Leaving '" + dest + "' as is.");
                    return;
                }
            }
            System.out.println("  Overwriting '" + dest + "'.");
        } else {
            System.out.println("  Creating '" + dest + "'.");
        }

        try {
            BufferedReader in = new BufferedReader(new FileReader(source));
            FileWriter out = new FileWriter(dest);
            String line;
            StringBuffer sline = new StringBuffer();
            while ((line = in.readLine()) != null) {
                Matcher m = _subre.matcher(line);
                while (m.find()) {
                    m.appendReplacement(sline, subs.get(m.group(1)));
                }
                m.appendTail(sline);
                out.write(sline.toString());
                sline.setLength(0);
                out.write(LINE_SEP);
            }
            out.close();
            in.close();

        } catch (IOException ioe) {
            throw new BuildException("Failed to create '" + dest + "': " + ioe);
        }
    }

    protected File _templates;
    protected boolean _overwriteAll;
    protected Pattern _subre = Pattern.compile("@([A-Za-z0-9]+)@");

    protected static final String LINE_SEP = System.getProperty("line.separator");

    protected static final String[] CLASSES = {
        "BoardView", "BoardViewTest", "Controller", "Panel", "Manager", "Object"
    };
}
