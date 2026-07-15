/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.expressions;

import java.util.Objects;

import org.eclipse.core.expressions.PropertyTester;

import ru.jimmo.edt.fastbutton.ui.FastButtonPlugin;

/** Reports whether a Fast Button navigator command is enabled by its workspace preference. */
public class CommandVisibilityTester extends PropertyTester
{
    /** Looks up whether a button is enabled, keyed by its boolean preference key. */
    @FunctionalInterface
    public interface EnablementLookup
    {
        /**
         * @param preferenceKey the button's boolean preference key
         * @return whether the button is enabled
         */
        boolean isEnabled(String preferenceKey);
    }

    private final EnablementLookup lookup;

    /** Creates a tester backed by the plug-in's workspace preference store. */
    public CommandVisibilityTester()
    {
        this(key -> FastButtonPlugin.getDefault().getPreferenceStore().getBoolean(key));
    }

    CommandVisibilityTester(EnablementLookup lookup)
    {
        this.lookup = Objects.requireNonNull(lookup, "lookup"); //$NON-NLS-1$
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
    {
        return expectedValue instanceof String key && lookup.isEnabled(key);
    }
}
