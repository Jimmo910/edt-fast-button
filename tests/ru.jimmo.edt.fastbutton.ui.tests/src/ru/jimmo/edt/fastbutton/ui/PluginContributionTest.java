/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.w3c.dom.Document;

/** Guards the exact EDT navigator and workspace preference contributions. */
public class PluginContributionTest
{
    @Test
    public void contributesGitOnlyActionsBeforeNewGroup() throws Exception
    {
        Document pluginXml = loadPluginXml();
        var xpath = XPathFactory.newInstance().newXPath();
        String contributionPath = "/plugin/extension[@point='org.eclipse.ui.menus']"
            + "/menuContribution[@locationURI="
            + "'popup:com._1c.g5.v8.dt.navigator.ui.navigator.popup?before=group.new']"; //$NON-NLS-1$
        String updateCommandPath = contributionPath
            + "/command[@commandId='ru.jimmo.edt.fastbutton.ui.commands.switchAndUpdateBranch']"; //$NON-NLS-1$
        String branchCommandPath = contributionPath
            + "/command[@commandId='org.eclipse.egit.ui.team.Branch']"; //$NON-NLS-1$
        String mergeCommandPath = contributionPath
            + "/command[@commandId='org.eclipse.egit.ui.team.Merge']"; //$NON-NLS-1$

        assertEquals(1, count(xpath, pluginXml, updateCommandPath));
        assertEquals(1, count(xpath, pluginXml, branchCommandPath + "[@label='%switchBranch.command.label']"));
        assertEquals(1, count(xpath, pluginXml, mergeCommandPath + "[@label='%merge.command.label']"));
        assertEquals(1, count(xpath, pluginXml, branchCommandPath
            + "/preceding-sibling::command[@commandId='ru.jimmo.edt.fastbutton.ui.commands.switchAndUpdateBranch']"));
        assertEquals(1, count(xpath, pluginXml, mergeCommandPath
            + "/preceding-sibling::command[@commandId='org.eclipse.egit.ui.team.Branch']"));
        assertVisibility(xpath, pluginXml, updateCommandPath, "showSwitchAndUpdateButton");
        assertVisibility(xpath, pluginXml, branchCommandPath, "showSwitchBranchButton");
        assertVisibility(xpath, pluginXml, mergeCommandPath, "showMergeButton");
    }

    @Test
    public void definesSingleGitProjectExpressionOnce() throws Exception
    {
        Document pluginXml = loadPluginXml();
        var xpath = XPathFactory.newInstance().newXPath();
        String expressionPath = "/plugin/extension[@point='org.eclipse.core.expressions.definitions']"
            + "/definition[@id='ru.jimmo.edt.fastbutton.ui.expressions.singleGitProject']"; //$NON-NLS-1$

        assertEquals(1, count(xpath, pluginXml, expressionPath));
        assertEquals(1, count(xpath, pluginXml, expressionPath
            + "/and[count[@value='1'] and iterate[@ifEmpty='false' and @operator='and']]"));
        assertEquals(1, count(xpath, pluginXml, expressionPath
            + "/and/iterate/and/instanceof[@value='org.eclipse.core.resources.IProject']"));
        assertEquals(1, count(xpath, pluginXml, expressionPath
            + "/and/iterate/and/test[@property='GitResource.isShared' and @forcePluginActivation='true']"));
    }

    @Test
    public void reusesEgitCommandsWithoutDeclaringInternalHandlers() throws Exception
    {
        Document pluginXml = loadPluginXml();
        var xpath = XPathFactory.newInstance().newXPath();
        String commandsPath = "/plugin/extension[@point='org.eclipse.ui.commands']/command"; //$NON-NLS-1$

        assertEquals(0, count(xpath, pluginXml, commandsPath + "[@id='org.eclipse.egit.ui.team.Branch']"));
        assertEquals(0, count(xpath, pluginXml, commandsPath + "[@id='org.eclipse.egit.ui.team.Merge']"));
    }

    @Test
    public void registersTheVisibilityPropertyTester() throws Exception
    {
        Document pluginXml = loadPluginXml();
        var xpath = XPathFactory.newInstance().newXPath();
        String testerPath = "/plugin/extension[@point='org.eclipse.core.expressions.propertyTesters']"
            + "/propertyTester[@namespace='ru.jimmo.edt.fastbutton.ui'"
            + " and @properties='commandEnabled'"
            + " and @class='ru.jimmo.edt.fastbutton.ui.expressions.CommandVisibilityTester']"; //$NON-NLS-1$

        assertEquals(1, count(xpath, pluginXml, testerPath));
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

    private static void assertVisibility(XPath xpath, Document pluginXml, String commandPath, String preferenceKey)
        throws Exception
    {
        String andPath = commandPath + "/visibleWhen[@checkEnabled='false']/and"; //$NON-NLS-1$
        assertEquals(1, count(xpath, pluginXml, andPath
            + "/reference[@definitionId='ru.jimmo.edt.fastbutton.ui.expressions.singleGitProject']"));
        assertEquals(1, count(xpath, pluginXml, andPath
            + "/test[@property='ru.jimmo.edt.fastbutton.ui.commandEnabled' and @value='" + preferenceKey + "']"));
    }

    private static int count(XPath xpath, Document document, String expression) throws Exception
    {
        Double result = (Double)xpath.evaluate("count(" + expression + ")", document, //$NON-NLS-1$ //$NON-NLS-2$
            XPathConstants.NUMBER);
        return result.intValue();
    }
}
