/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.preferences;

import ru.jimmo.edt.fastbutton.ui.FastButtonPlugin;

/** Workspace preference keys and defaults. */
public final class PreferenceConstants
{
    public static final String TARGET_BRANCH = "targetBranch"; //$NON-NLS-1$
    public static final String DEFAULT_TARGET_BRANCH = "main"; //$NON-NLS-1$

    /**
     * @return the target branch stored for the current workspace
     */
    public static String getTargetBranch()
    {
        return FastButtonPlugin.getDefault().getPreferenceStore().getString(TARGET_BRANCH);
    }

    private PreferenceConstants()
    {
    }
}
