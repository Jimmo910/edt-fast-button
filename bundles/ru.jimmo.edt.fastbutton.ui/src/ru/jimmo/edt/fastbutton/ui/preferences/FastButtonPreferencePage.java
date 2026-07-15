/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IEvaluationService;

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
        addField(new BooleanFieldEditor(PreferenceConstants.SHOW_SWITCH_AND_UPDATE_BUTTON,
            Messages.PreferencePage_ShowSwitchAndUpdate, getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.SHOW_SWITCH_BRANCH_BUTTON,
            Messages.PreferencePage_ShowSwitchBranch, getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.SHOW_MERGE_BUTTON,
            Messages.PreferencePage_ShowMerge, getFieldEditorParent()));
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
            IEvaluationService evaluationService = PlatformUI.getWorkbench().getService(IEvaluationService.class);
            if (evaluationService != null)
            {
                evaluationService.requestEvaluation("ru.jimmo.edt.fastbutton.ui.commandEnabled"); //$NON-NLS-1$
            }
        }
        return saved;
    }
}
