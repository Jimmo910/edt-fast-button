/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import ru.jimmo.edt.fastbutton.ui.FastButtonPlugin;

/** Initializes workspace defaults for the plug-in. */
public final class PreferenceInitializer extends AbstractPreferenceInitializer
{
    @Override
    public void initializeDefaultPreferences()
    {
        var store = FastButtonPlugin.getDefault().getPreferenceStore();
        store.setDefault(PreferenceConstants.TARGET_BRANCH, PreferenceConstants.DEFAULT_TARGET_BRANCH);
        store.setDefault(PreferenceConstants.SHOW_SWITCH_AND_UPDATE_BUTTON, true);
        store.setDefault(PreferenceConstants.SHOW_SWITCH_BRANCH_BUTTON, true);
        store.setDefault(PreferenceConstants.SHOW_MERGE_BUTTON, true);
    }
}
