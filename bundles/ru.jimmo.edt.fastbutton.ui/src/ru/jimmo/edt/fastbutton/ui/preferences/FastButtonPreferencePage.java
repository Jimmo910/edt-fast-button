/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import ru.jimmo.edt.fastbutton.ui.FastButtonPlugin;
import ru.jimmo.edt.fastbutton.ui.Messages;
import ru.jimmo.edt.fastbutton.ui.handlers.SwitchAndUpdateBranchHandler;

/** Workspace preferences shown under Window > Preferences. */
public final class FastButtonPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
    public FastButtonPreferencePage()
    {
        super(GRID);
        setPreferenceStore(FastButtonPlugin.getDefault().getPreferenceStore());
        setDescription(Messages.PreferencePage_Description);
    }

    @Override
    public void init(IWorkbench workbench)
    {
        // No workbench services are required during page initialization.
    }

    @Override
    protected void createFieldEditors()
    {
        addField(new BranchFieldEditor(PreferenceConstants.TARGET_BRANCH, Messages.PreferencePage_TargetBranch,
            getFieldEditorParent()));
    }

    @Override
    public boolean performOk()
    {
        boolean saved = super.performOk();
        if (saved)
        {
            ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
            if (commandService != null)
            {
                commandService.refreshElements(SwitchAndUpdateBranchHandler.COMMAND_ID, null);
            }
        }
        return saved;
    }
}
