/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.expressions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

/** Unit tests for the per-button visibility property tester. */
public class CommandVisibilityTesterTest
{
    private static final String PROPERTY = "commandEnabled"; //$NON-NLS-1$

    @Test
    public void reportsEnabledStateForTheGivenPreferenceKey()
    {
        Set<String> enabled = Set.of("showMergeButton"); //$NON-NLS-1$
        CommandVisibilityTester tester = new CommandVisibilityTester(enabled::contains);

        assertTrue(tester.test(null, PROPERTY, new Object[0], "showMergeButton")); //$NON-NLS-1$
        assertFalse(tester.test(null, PROPERTY, new Object[0], "showSwitchBranchButton")); //$NON-NLS-1$
    }

    @Test
    public void rejectsNonStringExpectedValue()
    {
        CommandVisibilityTester tester = new CommandVisibilityTester(key -> true);

        assertFalse(tester.test(null, PROPERTY, new Object[0], Boolean.TRUE));
        assertFalse(tester.test(null, PROPERTY, new Object[0], null));
    }
}
