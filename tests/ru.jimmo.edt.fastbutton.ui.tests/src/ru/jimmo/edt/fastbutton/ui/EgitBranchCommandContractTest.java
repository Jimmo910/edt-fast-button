/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.junit.Test;

/** Guards the public command contribution reused from the pinned EGit target platform. */
public class EgitBranchCommandContractTest
{
    private static final String BRANCH_COMMAND_ID = "org.eclipse.egit.ui.team.Branch"; //$NON-NLS-1$
    private static final String COMMANDS_EXTENSION_POINT = "org.eclipse.ui.commands"; //$NON-NLS-1$
    private static final String COMMAND_IMAGES_EXTENSION_POINT = "org.eclipse.ui.commandImages"; //$NON-NLS-1$
    private static final String COMMAND_ELEMENT = "command"; //$NON-NLS-1$
    private static final String IMAGE_ELEMENT = "image"; //$NON-NLS-1$
    private static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
    private static final String COMMAND_ID_ATTRIBUTE = "commandId"; //$NON-NLS-1$

    @Test
    public void targetPlatformProvidesBranchCommandWithIcon()
    {
        assertNotNull(findElement(COMMANDS_EXTENSION_POINT, COMMAND_ELEMENT, ID_ATTRIBUTE, BRANCH_COMMAND_ID));

        IConfigurationElement image = findElement(COMMAND_IMAGES_EXTENSION_POINT, IMAGE_ELEMENT,
            COMMAND_ID_ATTRIBUTE, BRANCH_COMMAND_ID);
        assertNotNull(image);
        assertNotNull(image.getAttribute("icon")); //$NON-NLS-1$
    }

    private static IConfigurationElement findElement(String extensionPointId, String elementName,
        String attributeName, String attributeValue)
    {
        return Arrays.stream(Platform.getExtensionRegistry().getConfigurationElementsFor(extensionPointId))
            .filter(element -> elementName.equals(element.getName()))
            .filter(element -> attributeValue.equals(element.getAttribute(attributeName)))
            .findFirst()
            .orElse(null);
    }
}
