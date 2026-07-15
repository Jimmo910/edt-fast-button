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

    /** Preference key: show the switch-and-update navigator button. */
    public static final String SHOW_SWITCH_AND_UPDATE_BUTTON = "showSwitchAndUpdateButton"; //$NON-NLS-1$

    /** Preference key: show the reused EGit "switch to another branch" navigator button. */
    public static final String SHOW_SWITCH_BRANCH_BUTTON = "showSwitchBranchButton"; //$NON-NLS-1$

    /** Preference key: show the reused EGit "merge" navigator button. */
    public static final String SHOW_MERGE_BUTTON = "showMergeButton"; //$NON-NLS-1$

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
