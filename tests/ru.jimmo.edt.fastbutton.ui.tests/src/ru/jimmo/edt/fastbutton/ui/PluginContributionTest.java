/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.w3c.dom.Document;

/** Guards the exact EDT navigator and workspace preference contributions. */
public class PluginContributionTest
{
    @Test
    public void contributesGitOnlyActionBeforeNewGroup() throws Exception
    {
        Document pluginXml = loadPluginXml();
        var xpath = XPathFactory.newInstance().newXPath();
        String commandPath = "/plugin/extension[@point='org.eclipse.ui.menus']"
            + "/menuContribution[@locationURI="
            + "'popup:com._1c.g5.v8.dt.navigator.ui.navigator.popup?before=group.new']"
            + "/command[@commandId='ru.jimmo.edt.fastbutton.ui.commands.switchAndUpdateBranch']"; //$NON-NLS-1$

        Double commandCount = (Double)xpath.evaluate(
            "count(" + commandPath + ")", pluginXml, XPathConstants.NUMBER); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(1.0, commandCount, 0.0);
        assertEquals(1.0, (Double)xpath.evaluate("count(" + commandPath
            + "/visibleWhen/and/iterate/and/instanceof[@value='org.eclipse.core.resources.IProject'])", //$NON-NLS-1$
            pluginXml, XPathConstants.NUMBER), 0.0);
        assertEquals(1.0, (Double)xpath.evaluate("count(" + commandPath
            + "/visibleWhen/and/iterate/and/test[@property='GitResource.isShared' and "
            + "@forcePluginActivation='true'])", pluginXml, XPathConstants.NUMBER), 0.0); //$NON-NLS-1$
    }

    @Test
    public void contributesWorkspacePreferencePage() throws Exception
    {
        Document pluginXml = loadPluginXml();
        var xpath = XPathFactory.newInstance().newXPath();
        String expression = "count(/plugin/extension[@point='org.eclipse.ui.preferencePages']"
            + "/page[@class='ru.jimmo.edt.fastbutton.ui.preferences.FastButtonPreferencePage'])"; //$NON-NLS-1$

        assertEquals(1.0, (Double)xpath.evaluate(expression, pluginXml, XPathConstants.NUMBER), 0.0);
    }

    private static Document loadPluginXml() throws Exception
    {
        ClassLoader classLoader = PluginContributionTest.class.getClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream("plugin.xml")) //$NON-NLS-1$
        {
            assertNotNull(stream);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); //$NON-NLS-1$
            return factory.newDocumentBuilder().parse(stream);
        }
    }
}
