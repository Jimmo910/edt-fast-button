/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.preferences;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

import ru.jimmo.edt.fastbutton.ui.Messages;
import ru.jimmo.edt.fastbutton.ui.application.BranchNamePolicy;
import ru.jimmo.edt.fastbutton.ui.infrastructure.git.JGitBranchNamePolicy;

/** Validates a short Git branch name while the preference is edited. */
final class BranchFieldEditor extends StringFieldEditor
{
    private final BranchNamePolicy branchNamePolicy = new JGitBranchNamePolicy();

    BranchFieldEditor(String name, String labelText, Composite parent)
    {
        super(name, labelText, parent);
        setEmptyStringAllowed(false);
        setValidateStrategy(VALIDATE_ON_KEY_STROKE);
        setErrorMessage(Messages.PreferencePage_InvalidBranch);
    }

    @Override
    protected boolean doCheckState()
    {
        return branchNamePolicy.isValid(getStringValue());
    }
}
