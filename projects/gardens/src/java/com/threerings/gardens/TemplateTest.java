//
// $Id$

package com.threerings.gardens;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Test;

import org.apache.velocity.VelocityContext;

import com.samskivert.velocity.VelocityTestCase;

/**
 * Tests all of our Velocity templates.
 */
public class TemplateTest extends VelocityTestCase
{
    public TemplateTest ()
    {
        super(TemplateTest.class.getName());
    }

    public static Test suite ()
    {
        return new TemplateTest();
    }

    protected void setUp ()
    {
        super.setUp();

        // enumerate our template files
        String templatesLocation = System.getProperty("templatesLocation", "");
        scanTemplates(new File(templatesLocation), new File(templatesLocation + "web"));
    }

    protected Collection<String> getTemplates ()
    {
        return _templates;
    }

    protected void scanTemplates (File root, File tempdir)
    {
        for (File file : tempdir.listFiles()) {
            if (file.isDirectory() && !file.getName().equals(".svn")) {
                scanTemplates(root, file);
            } else if (file.getName().endsWith(".wm")) {
                _templates.add(file.getAbsolutePath().substring(
                                   root.getAbsolutePath().length()));
            }
        }
    }

    // temporarily disable context checking
    @Override // from VelocityTestCase
    protected void testMerge (String template)
    {
        // do nothing
    }

    protected ArrayList<String> _templates = new ArrayList<String>();
}
