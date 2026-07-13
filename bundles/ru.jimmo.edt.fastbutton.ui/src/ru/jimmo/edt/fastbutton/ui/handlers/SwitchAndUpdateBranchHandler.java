/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.handlers;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import ru.jimmo.edt.fastbutton.ui.FastButtonPlugin;
import ru.jimmo.edt.fastbutton.ui.Messages;
import ru.jimmo.edt.fastbutton.ui.application.SwitchAndUpdateBranchUseCase;
import ru.jimmo.edt.fastbutton.ui.infrastructure.git.JGitBranchNamePolicy;
import ru.jimmo.edt.fastbutton.ui.infrastructure.git.JGitBranchUpdater;
import ru.jimmo.edt.fastbutton.ui.infrastructure.repository.GitRepositoryContext;
import ru.jimmo.edt.fastbutton.ui.infrastructure.repository.GitRepositoryContextResolver;
import ru.jimmo.edt.fastbutton.ui.preferences.PreferenceConstants;
import ru.jimmo.edt.fastbutton.ui.ui.UpdateMessageResolver;
import ru.jimmo.edt.fastbutton.ui.ui.UserNotifier;
import ru.jimmo.edt.fastbutton.ui.ui.WorkbenchUnsavedChangesGuard;
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
        Object selected = selection.getFirstElement();
        if (!(selected instanceof IProject project))
        {
            return null;
        }

        Optional<GitRepositoryContext> resolved;
        try
        {
            resolved = repositoryResolver.resolve(project);
        }
        catch (IOException e)
        {
            String message = NLS.bind(Messages.UnexpectedFailure_Message, detail(e.getMessage()));
            FastButtonPlugin.logError(message, e);
            notifier.warning(message);
            return null;
        }
        if (resolved.isEmpty())
        {
            notifier.warning(NLS.bind(Messages.NoRepository_Message, project.getName()));
            return null;
        }

        GitRepositoryContext context = resolved.get();
        Set<String> unsavedEditors = unsavedChangesGuard.findUnsavedEditors(context.getProjects());
        if (!unsavedEditors.isEmpty())
        {
            notifier.warning(NLS.bind(Messages.UnsavedEditors_Message, messages.summarize(unsavedEditors)));
            context.close();
            return null;
        }

        String branch = PreferenceConstants.getTargetBranch();
        boolean scheduled = false;
        try
        {
            var useCase = new SwitchAndUpdateBranchUseCase(new JGitBranchNamePolicy(),
                new JGitBranchUpdater(context.getRepository()));
            new SwitchAndUpdateBranchJob(context, branch, useCase, messages, notifier).schedule();
            scheduled = true;
        }
        finally
        {
            if (!scheduled)
            {
                context.close();
            }
        }
        return null;
    }

    private static String detail(String value)
    {
        return value != null ? value : ""; //$NON-NLS-1$
    }

    @Override
    public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters)
    {
        element.setText(NLS.bind(Messages.SwitchAndUpdate_Label, PreferenceConstants.getTargetBranch()));
    }
}
