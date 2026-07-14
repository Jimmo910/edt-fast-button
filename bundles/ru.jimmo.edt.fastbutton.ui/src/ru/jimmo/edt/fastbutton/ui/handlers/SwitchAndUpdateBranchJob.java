/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.handlers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import ru.jimmo.edt.fastbutton.ui.FastButtonPlugin;
import ru.jimmo.edt.fastbutton.ui.Messages;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateException;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateResult;
import ru.jimmo.edt.fastbutton.ui.application.SwitchAndUpdateBranchUseCase;
import ru.jimmo.edt.fastbutton.ui.infrastructure.repository.GitRepositoryContext;
import ru.jimmo.edt.fastbutton.ui.infrastructure.repository.GitRepositoryContextResolver;
import ru.jimmo.edt.fastbutton.ui.ui.UpdateMessageResolver;
import ru.jimmo.edt.fastbutton.ui.ui.UserNotifier;
import ru.jimmo.edt.fastbutton.ui.ui.WorkbenchUnsavedChangesGuard;
import ru.jimmo.edt.fastbutton.ui.ui.WorkbenchUnsavedChangesGuard.DirtyEditor;

/**
 * Background job that resolves the repository context off the UI thread and runs the application
 * use case under repository-wide workspace locking.
 */
final class SwitchAndUpdateBranchJob extends Job
{
    private static final int RULE_WAIT_TICKS = 5;
    private static final int GIT_OPERATION_TICKS = 75;
    private static final int REFRESH_TICKS = 20;

    private final IProject project;
    private final List<DirtyEditor> dirtyEditors;
    private final String branch;
    private final GitRepositoryContextResolver repositoryResolver;
    private final UpdateMessageResolver messages;
    private final UserNotifier notifier;
    private final UseCaseFactory useCaseFactory;

    SwitchAndUpdateBranchJob(IProject project, List<DirtyEditor> dirtyEditors, String branch,
        GitRepositoryContextResolver repositoryResolver, UpdateMessageResolver messages, UserNotifier notifier,
        UseCaseFactory useCaseFactory)
    {
        super(NLS.bind(Messages.Job_Name, branch));
        this.project = project;
        this.dirtyEditors = List.copyOf(dirtyEditors);
        this.branch = branch;
        this.repositoryResolver = repositoryResolver;
        this.messages = messages;
        this.notifier = notifier;
        this.useCaseFactory = useCaseFactory;
        setUser(true);
    }

    /** Creates the application use case bound to a resolved repository context. */
    @FunctionalInterface
    interface UseCaseFactory
    {
        /**
         * @param context resolved repository context
         * @return a use case bound to the context's repository
         */
        SwitchAndUpdateBranchUseCase create(GitRepositoryContext context);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
        SubMonitor progress = SubMonitor.convert(monitor, getName(),
            RULE_WAIT_TICKS + GIT_OPERATION_TICKS + REFRESH_TICKS);
        Optional<GitRepositoryContext> resolved;
        try
        {
            resolved = repositoryResolver.resolve(project);
        }
        catch (IOException e)
        {
            String message = messages.unexpectedFailure(e.getMessage());
            FastButtonPlugin.logError(message, e);
            notifier.warning(message);
            return new Status(IStatus.ERROR, FastButtonPlugin.PLUGIN_ID, message, e);
        }
        if (resolved.isEmpty())
        {
            String message = messages.noRepository(project.getName());
            notifier.warning(message);
            return new Status(IStatus.WARNING, FastButtonPlugin.PLUGIN_ID, message);
        }

        try (GitRepositoryContext context = resolved.get())
        {
            return runWithContext(context, progress);
        }
    }

    /** Acquires the repository-wide scheduling rule, then delegates to {@link #runLocked}. */
    private IStatus runWithContext(GitRepositoryContext context, SubMonitor progress)
    {
        Set<String> unsavedEditors = WorkbenchUnsavedChangesGuard.blockingEditorTitles(dirtyEditors,
            context.getProjects());
        if (!unsavedEditors.isEmpty())
        {
            String message = messages.unsavedEditors(unsavedEditors);
            notifier.warning(message);
            return new Status(IStatus.WARNING, FastButtonPlugin.PLUGIN_ID, message);
        }

        // The rule may be acquired even when beginRule is cancelled, so endRule stays in finally.
        // The monitor child is split outside the try so a cancelled split cannot skip beginRule.
        ISchedulingRule rule = context.getSchedulingRule();
        IProgressMonitor ruleWait = progress.split(RULE_WAIT_TICKS);
        try
        {
            Job.getJobManager().beginRule(rule, ruleWait);
            return runLocked(context, progress);
        }
        finally
        {
            Job.getJobManager().endRule(rule);
        }
    }

    /** Runs the branch update while the repository-wide scheduling rule is held. */
    private IStatus runLocked(GitRepositoryContext context, SubMonitor progress)
    {
        try
        {
            SwitchAndUpdateBranchUseCase useCase = useCaseFactory.create(context);
            BranchUpdateResult result = useCase.execute(branch,
                new EclipseOperationProgress(progress.split(GIT_OPERATION_TICKS)));
            // Once the git operation above has changed the worktree, the refresh must complete
            // even if cancellation arrives in this window, so it stays consistent with a success
            // notification; a cancellable split here could skip both and leave the resource tree stale.
            context.refreshProjects(progress.split(REFRESH_TICKS, SubMonitor.SUPPRESS_ISCANCELED));
            notifier.success(result);
            return Status.OK_STATUS;
        }
        catch (BranchUpdateException e)
        {
            refreshAfterFailure(context);
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
            String message = messages.refreshFailure(e.getMessage());
            FastButtonPlugin.logError(message, e);
            notifier.warning(message);
            return new Status(IStatus.ERROR, FastButtonPlugin.PLUGIN_ID, message, e);
        }
    }

    private void refreshAfterFailure(GitRepositoryContext context)
    {
        try
        {
            // Restore workspace consistency even after cancellation, so no cancellable monitor here.
            context.refreshProjects(new NullProgressMonitor());
        }
        catch (CoreException e)
        {
            FastButtonPlugin.logError(messages.refreshFailure(e.getMessage()), e);
        }
    }
}
