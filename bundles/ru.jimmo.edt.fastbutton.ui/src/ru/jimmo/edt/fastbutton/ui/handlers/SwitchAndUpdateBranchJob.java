/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.handlers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import ru.jimmo.edt.fastbutton.ui.FastButtonPlugin;
import ru.jimmo.edt.fastbutton.ui.Messages;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateException;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateResult;
import ru.jimmo.edt.fastbutton.ui.application.SwitchAndUpdateBranchUseCase;
import ru.jimmo.edt.fastbutton.ui.infrastructure.repository.GitRepositoryContext;
import ru.jimmo.edt.fastbutton.ui.ui.UpdateMessageResolver;
import ru.jimmo.edt.fastbutton.ui.ui.UserNotifier;

/** Background job that runs the application use case under repository-wide workspace locking. */
final class SwitchAndUpdateBranchJob extends Job
{
    private final GitRepositoryContext repositoryContext;
    private final String branch;
    private final SwitchAndUpdateBranchUseCase useCase;
    private final UpdateMessageResolver messages;
    private final UserNotifier notifier;

    SwitchAndUpdateBranchJob(GitRepositoryContext repositoryContext, String branch,
        SwitchAndUpdateBranchUseCase useCase, UpdateMessageResolver messages, UserNotifier notifier)
    {
        super(NLS.bind(Messages.Job_Name, branch));
        this.repositoryContext = repositoryContext;
        this.branch = branch;
        this.useCase = useCase;
        this.messages = messages;
        this.notifier = notifier;
        setRule(repositoryContext.getSchedulingRule());
        setUser(true);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
        monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
        try
        {
            BranchUpdateResult result = useCase.execute(branch, new EclipseOperationProgress(monitor));
            repositoryContext.refreshProjects(new NullProgressMonitor());
            notifier.success(result);
            return Status.OK_STATUS;
        }
        catch (BranchUpdateException e)
        {
            refreshAfterFailure();
            if (e.getReason() == BranchUpdateException.Reason.CANCELLED)
            {
                return Status.CANCEL_STATUS;
            }

            String message = messages.failure(e, branch);
            notifier.warning(message);
            if (messages.isError(e))
            {
                FastButtonPlugin.logError(message, e);
                return new Status(IStatus.ERROR, FastButtonPlugin.PLUGIN_ID, message, e);
            }
            return new Status(IStatus.WARNING, FastButtonPlugin.PLUGIN_ID, message);
        }
        catch (CoreException e)
        {
            String message = NLS.bind(Messages.RefreshFailed_Message, detail(e.getMessage()));
            FastButtonPlugin.logError(message, e);
            notifier.warning(message);
            return new Status(IStatus.ERROR, FastButtonPlugin.PLUGIN_ID, message, e);
        }
        finally
        {
            monitor.done();
            repositoryContext.close();
        }
    }

    private void refreshAfterFailure()
    {
        try
        {
            repositoryContext.refreshProjects(new NullProgressMonitor());
        }
        catch (CoreException e)
        {
            FastButtonPlugin.logError(NLS.bind(Messages.RefreshFailed_Message, detail(e.getMessage())), e);
        }
    }

    private static String detail(String value)
    {
        return value != null ? value : ""; //$NON-NLS-1$
    }
}
