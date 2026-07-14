/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.handlers;

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import ru.jimmo.edt.fastbutton.ui.Messages;
import ru.jimmo.edt.fastbutton.ui.infrastructure.repository.GitRepositoryContextResolver;
import ru.jimmo.edt.fastbutton.ui.preferences.PreferenceConstants;
import ru.jimmo.edt.fastbutton.ui.ui.UpdateMessageResolver;
import ru.jimmo.edt.fastbutton.ui.ui.UserNotifier;
import ru.jimmo.edt.fastbutton.ui.ui.WorkbenchUnsavedChangesGuard;
import ru.jimmo.edt.fastbutton.ui.ui.WorkbenchUnsavedChangesGuard.DirtyEditor;
import ru.jimmo.edt.fastbutton.ui.ui.WorkbenchUserNotifier;

/** Eclipse command adapter for the safe switch-and-update use case. */
public final class SwitchAndUpdateBranchHandler extends AbstractHandler implements IElementUpdater
{
    public static final String COMMAND_ID = "ru.jimmo.edt.fastbutton.ui.commands.switchAndUpdateBranch"; //$NON-NLS-1$

    private final GitRepositoryContextResolver repositoryResolver = new GitRepositoryContextResolver();
    private final WorkbenchUnsavedChangesGuard unsavedChangesGuard = new WorkbenchUnsavedChangesGuard();
    private final UpdateMessageResolver messages = new UpdateMessageResolver();
    private final UserNotifier notifier = new WorkbenchUserNotifier();

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
        if (!(selection.getFirstElement() instanceof IProject project))
        {
            return null;
        }

        // Only the workbench snapshot needs the UI thread; repository I/O happens in the job.
        List<DirtyEditor> dirtyEditors = unsavedChangesGuard.snapshotDirtyEditors();
        new SwitchAndUpdateBranchJob(project, dirtyEditors, PreferenceConstants.getTargetBranch(),
            repositoryResolver, messages, notifier).schedule();
        return Boolean.TRUE;
    }

    @Override
    public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters)
    {
        element.setText(NLS.bind(Messages.SwitchAndUpdate_Label, PreferenceConstants.getTargetBranch()));
    }
}
