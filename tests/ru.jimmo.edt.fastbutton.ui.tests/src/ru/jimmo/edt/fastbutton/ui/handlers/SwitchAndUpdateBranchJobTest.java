/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Test;

import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateException;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateException.Reason;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateOutcome;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateResult;
import ru.jimmo.edt.fastbutton.ui.application.SwitchAndUpdateBranchUseCase;
import ru.jimmo.edt.fastbutton.ui.infrastructure.repository.GitRepositoryContext;
import ru.jimmo.edt.fastbutton.ui.infrastructure.repository.GitRepositoryContextResolver;
import ru.jimmo.edt.fastbutton.ui.infrastructure.repository.GitRepositoryContexts;
import ru.jimmo.edt.fastbutton.ui.ui.UpdateMessageResolver;
import ru.jimmo.edt.fastbutton.ui.ui.UserNotifier;
import ru.jimmo.edt.fastbutton.ui.ui.WorkbenchUnsavedChangesGuard.DirtyEditor;

/** Headless orchestration tests for the background switch-and-update job. */
public class SwitchAndUpdateBranchJobTest
{
    private static final String BRANCH = "main"; //$NON-NLS-1$
    private static final String PROJECT_NAME = "fastbutton-job-test"; //$NON-NLS-1$
    private static final String OTHER_PROJECT_NAME = "fastbutton-job-other"; //$NON-NLS-1$

    private final UpdateMessageResolver messages = new UpdateMessageResolver();
    private final RecordingNotifier notifier = new RecordingNotifier();
    private final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
    private final IProject otherProject = ResourcesPlugin.getWorkspace().getRoot().getProject(OTHER_PROJECT_NAME);

    private Path repositoryDirectory;
    private Git git;

    @After
    public void tearDown() throws Exception
    {
        if (git != null)
        {
            git.close();
        }
        if (repositoryDirectory != null)
        {
            FileUtils.delete(repositoryDirectory.toFile(),
                FileUtils.RECURSIVE | FileUtils.RETRY | FileUtils.IGNORE_ERRORS | FileUtils.SKIP_MISSING);
        }
    }

    @Test
    public void resolverFailureReportsUnexpectedError() throws Exception
    {
        IOException failure = new IOException("disk error"); //$NON-NLS-1$
        AtomicBoolean invoked = new AtomicBoolean();
        GitRepositoryContextResolver resolver = selectedProject ->
        {
            throw failure;
        };

        IStatus result = run(resolver, List.of(), factory(useCaseReturning(sampleResult()), invoked));

        assertEquals(IStatus.ERROR, result.getSeverity());
        assertEquals(List.of(messages.unexpectedFailure(failure.getMessage())), notifier.warnings);
        assertTrue(notifier.successes.isEmpty());
        assertFalse(invoked.get());
    }

    @Test
    public void missingRepositoryReportsWarning() throws Exception
    {
        AtomicBoolean invoked = new AtomicBoolean();
        GitRepositoryContextResolver resolver = selectedProject -> Optional.empty();

        IStatus result = run(resolver, List.of(), factory(useCaseReturning(sampleResult()), invoked));

        assertEquals(IStatus.WARNING, result.getSeverity());
        assertEquals(List.of(messages.noRepository(PROJECT_NAME)), notifier.warnings);
        assertTrue(notifier.successes.isEmpty());
        assertFalse(invoked.get());
    }

    @Test
    public void unsavedEditorInRepositoryProjectBlocksOperation() throws Exception
    {
        AtomicBoolean invoked = new AtomicBoolean();
        GitRepositoryContext context = newContext(List.of(project));
        GitRepositoryContextResolver resolver = selectedProject -> Optional.of(context);
        List<DirtyEditor> dirtyEditors = List.of(new DirtyEditor("Module", project)); //$NON-NLS-1$

        IStatus result = run(resolver, dirtyEditors, factory(useCaseReturning(sampleResult()), invoked));

        assertEquals(IStatus.WARNING, result.getSeverity());
        assertEquals(List.of(messages.unsavedEditors(Set.of("Module"))), notifier.warnings); //$NON-NLS-1$
        assertTrue(notifier.successes.isEmpty());
        assertFalse(invoked.get());
    }

    @Test
    public void unsavedEditorInUnrelatedProjectAllowsOperation() throws Exception
    {
        AtomicBoolean invoked = new AtomicBoolean();
        BranchUpdateResult expected = sampleResult();
        GitRepositoryContext context = newContext(List.of(project));
        GitRepositoryContextResolver resolver = selectedProject -> Optional.of(context);
        List<DirtyEditor> dirtyEditors = List.of(new DirtyEditor("Other", otherProject)); //$NON-NLS-1$

        IStatus result = run(resolver, dirtyEditors, factory(useCaseReturning(expected), invoked));

        assertTrue(result.isOK());
        assertTrue(invoked.get());
        assertEquals(List.of(expected), notifier.successes);
        assertTrue(notifier.warnings.isEmpty());
    }

    @Test
    public void successfulUpdateNotifiesResult() throws Exception
    {
        AtomicBoolean invoked = new AtomicBoolean();
        BranchUpdateResult expected = sampleResult();
        GitRepositoryContext context = newContext(List.of(project));
        GitRepositoryContextResolver resolver = selectedProject -> Optional.of(context);

        IStatus result = run(resolver, List.of(), factory(useCaseReturning(expected), invoked));

        assertTrue(result.isOK());
        assertEquals(List.of(expected), notifier.successes);
        assertTrue(notifier.warnings.isEmpty());
    }

    @Test
    public void cancelledUpdateReportsCancelStatus() throws Exception
    {
        AtomicBoolean invoked = new AtomicBoolean();
        GitRepositoryContext context = newContext(List.of(project));
        GitRepositoryContextResolver resolver = selectedProject -> Optional.of(context);
        SwitchAndUpdateBranchUseCase useCase = useCaseThrowing(new BranchUpdateException(Reason.CANCELLED, null));

        IStatus result = run(resolver, List.of(), factory(useCase, invoked));

        assertEquals(IStatus.CANCEL, result.getSeverity());
        assertTrue(notifier.warnings.isEmpty());
        assertTrue(notifier.successes.isEmpty());
    }

    @Test
    public void dirtyWorktreeFailureReportsWarning() throws Exception
    {
        AtomicBoolean invoked = new AtomicBoolean();
        BranchUpdateException failure = new BranchUpdateException(List.of("file.txt")); //$NON-NLS-1$
        GitRepositoryContext context = newContext(List.of(project));
        GitRepositoryContextResolver resolver = selectedProject -> Optional.of(context);

        IStatus result = run(resolver, List.of(), factory(useCaseThrowing(failure), invoked));

        assertEquals(IStatus.WARNING, result.getSeverity());
        assertEquals(List.of(messages.failure(failure, BRANCH)), notifier.warnings);
        assertTrue(notifier.successes.isEmpty());
    }

    @Test
    public void fetchFailureReportsError() throws Exception
    {
        AtomicBoolean invoked = new AtomicBoolean();
        BranchUpdateException failure = new BranchUpdateException(Reason.FETCH_FAILED, "boom"); //$NON-NLS-1$
        GitRepositoryContext context = newContext(List.of(project));
        GitRepositoryContextResolver resolver = selectedProject -> Optional.of(context);

        IStatus result = run(resolver, List.of(), factory(useCaseThrowing(failure), invoked));

        assertEquals(IStatus.ERROR, result.getSeverity());
        assertEquals(List.of(messages.failure(failure, BRANCH)), notifier.warnings);
        assertTrue(notifier.successes.isEmpty());
    }

    @Test
    public void refreshesAndNotifiesWhenCancelledAfterGitOperation() throws Exception
    {
        AtomicBoolean invoked = new AtomicBoolean();
        BranchUpdateResult expected = sampleResult();
        GitRepositoryContext context = newContext(List.of(project));
        GitRepositoryContextResolver resolver = selectedProject -> Optional.of(context);
        // The job reference is only available once constructed, so the use-case lambda closes over
        // this holder and cancels the job from inside the git operation it is simulating.
        SwitchAndUpdateBranchJob[] jobHolder = new SwitchAndUpdateBranchJob[1];
        SwitchAndUpdateBranchUseCase useCase = new SwitchAndUpdateBranchUseCase(branch -> true,
            (targetBranch, progress) ->
            {
                jobHolder[0].cancel();
                return expected;
            });

        SwitchAndUpdateBranchJob job = new SwitchAndUpdateBranchJob(project, List.of(), BRANCH, resolver, messages,
            notifier, factory(useCase, invoked));
        jobHolder[0] = job;
        job.schedule();
        job.join();

        assertTrue(job.getResult().isOK());
        assertEquals(List.of(expected), notifier.successes);
        assertTrue(notifier.warnings.isEmpty());
    }

    private IStatus run(GitRepositoryContextResolver resolver, List<DirtyEditor> dirtyEditors,
        SwitchAndUpdateBranchJob.UseCaseFactory factory) throws InterruptedException
    {
        SwitchAndUpdateBranchJob job = new SwitchAndUpdateBranchJob(project, dirtyEditors, BRANCH, resolver, messages,
            notifier, factory);
        job.schedule();
        job.join();
        return job.getResult();
    }

    private GitRepositoryContext newContext(List<IProject> projects) throws Exception
    {
        repositoryDirectory = Files.createTempDirectory("fastbutton-job-test-"); //$NON-NLS-1$
        git = Git.init().setDirectory(repositoryDirectory.toFile()).call();
        // The job owns and closes the resolved context, so balance that close with a matching open.
        git.getRepository().incrementOpen();
        return GitRepositoryContexts.create(git.getRepository(), projects);
    }

    private static SwitchAndUpdateBranchJob.UseCaseFactory factory(SwitchAndUpdateBranchUseCase useCase,
        AtomicBoolean invoked)
    {
        return context ->
        {
            invoked.set(true);
            return useCase;
        };
    }

    private static SwitchAndUpdateBranchUseCase useCaseReturning(BranchUpdateResult result)
    {
        return new SwitchAndUpdateBranchUseCase(branch -> true, (branch, progress) -> result);
    }

    private static SwitchAndUpdateBranchUseCase useCaseThrowing(BranchUpdateException failure)
    {
        return new SwitchAndUpdateBranchUseCase(branch -> true, (branch, progress) ->
        {
            throw failure;
        });
    }

    private static BranchUpdateResult sampleResult()
    {
        return new BranchUpdateResult(BRANCH, "origin", BranchUpdateOutcome.UP_TO_DATE, true); //$NON-NLS-1$
    }

    private static final class RecordingNotifier implements UserNotifier
    {
        private final List<String> warnings = new ArrayList<>();
        private final List<BranchUpdateResult> successes = new ArrayList<>();

        @Override
        public void warning(String message)
        {
            warnings.add(message);
        }

        @Override
        public void success(BranchUpdateResult result)
        {
            successes.add(result);
        }
    }
}
