/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/** Guards completeness of the English and Russian resource bundles. */
public class LocalizationResourcesTest
{
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\d+\\}"); //$NON-NLS-1$

    @Test
    public void codeMessageBundlesHaveMatchingKeys() throws Exception
    {
        assertMatchingKeys("ru/jimmo/edt/fastbutton/ui/messages.properties", //$NON-NLS-1$
            "ru/jimmo/edt/fastbutton/ui/messages_ru.properties"); //$NON-NLS-1$
    }

    @Test
    public void extensionMessageBundlesHaveMatchingKeys() throws Exception
    {
        assertMatchingKeys("plugin.properties", "plugin_ru.properties"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static void assertMatchingKeys(String englishResource, String russianResource) throws Exception
    {
        Properties english = load(englishResource);
        Properties russian = load(russianResource);
        assertFalse(english.isEmpty());
        assertEquals(english.stringPropertyNames(), russian.stringPropertyNames());
        for (String key : english.stringPropertyNames())
        {
            assertEquals(key, placeholders(english.getProperty(key)), placeholders(russian.getProperty(key)));
        }
    }

    private static Set<String> placeholders(String value)
    {
        Set<String> result = new TreeSet<>();
        Matcher matcher = PLACEHOLDER.matcher(value);
        while (matcher.find())
        {
            result.add(matcher.group());
        }
        return result;
    }

    private static Properties load(String resource) throws IOException
    {
        try (InputStream stream = LocalizationResourcesTest.class.getClassLoader().getResourceAsStream(resource))
        {
            assertNotNull(resource, stream);
            Properties properties = new Properties();
            properties.load(stream);
            return properties;
        }
    }
}
