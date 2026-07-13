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
        FastButtonPlugin.getDefault().getPreferenceStore().setDefault(PreferenceConstants.TARGET_BRANCH,
            PreferenceConstants.DEFAULT_TARGET_BRANCH);
    }
}
