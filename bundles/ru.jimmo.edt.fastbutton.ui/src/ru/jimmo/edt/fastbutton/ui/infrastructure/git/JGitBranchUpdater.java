/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.infrastructure.git;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;

import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateException;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateException.Reason;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateOutcome;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateResult;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdater;
import ru.jimmo.edt.fastbutton.ui.application.OperationProgress;

/** JGit adapter that performs a non-destructive branch switch and fast-forward update. */
public final class JGitBranchUpdater implements BranchUpdater
{
    private static final String DEFAULT_REMOTE = "origin"; //$NON-NLS-1$
    private static final String LOCAL_REMOTE = "."; //$NON-NLS-1$

    private final Repository repository;

    /**
     * @param repository EGit-managed non-bare repository
     */
    public JGitBranchUpdater(Repository repository)
    {
        this.repository = Objects.requireNonNull(repository, "repository"); //$NON-NLS-1$
    }

    @Override
    public BranchUpdateResult switchAndUpdate(String targetBranch, OperationProgress progress)
        throws BranchUpdateException
    {
        ProgressMonitor monitor = new JGitProgressMonitor(progress);
        // The repository is owned by the surrounding workspace operation. Git.close() would close it as well.
        Git git = Git.wrap(repository);
        try
        {
            ensureSafeRepository();
            ensureClean(git, monitor);
            checkCancelled(monitor);

            String remote = findRemote(targetBranch);
            Ref remoteBranch = fetchRemoteBranch(git, remote, targetBranch, monitor);
            checkCancelled(monitor);

            // Fetch can take long enough for an external process to modify the worktree.
            ensureSafeRepository();
            ensureClean(git, monitor);
            checkCancelled(monitor);

            String localRefName = Constants.R_HEADS + targetBranch;
            Ref localBranch = repository.exactRef(localRefName);
            BranchRelation relation = determineRelation(localBranch, remoteBranch);
            boolean switched = !localRefName.equals(repository.getFullBranch());

            if (relation == BranchRelation.CREATED || switched)
            {
                checkout(git, targetBranch, remoteBranch.getName(), relation == BranchRelation.CREATED, monitor);
                checkCancelled(monitor);
            }

            BranchUpdateOutcome outcome = switch (relation)
            {
            case CREATED -> BranchUpdateOutcome.CREATED;
            case SAME -> BranchUpdateOutcome.UP_TO_DATE;
            case AHEAD -> BranchUpdateOutcome.AHEAD;
            case BEHIND -> fastForward(git, remoteBranch, monitor);
            };
            return new BranchUpdateResult(targetBranch, remote, outcome, switched);
        }
        catch (BranchUpdateException e)
        {
            throw e;
        }
        catch (IOException | RuntimeException e)
        {
            if (monitor.isCancelled())
            {
                throw new BranchUpdateException(Reason.CANCELLED, null, e);
            }
            throw new BranchUpdateException(Reason.UNEXPECTED, e.getMessage(), e);
        }
    }

    private void ensureSafeRepository() throws BranchUpdateException
    {
        RepositoryState state = repository.getRepositoryState();
        if (repository.isBare() || state != RepositoryState.SAFE)
        {
            throw new BranchUpdateException(Reason.UNSAFE_REPOSITORY_STATE, state.getDescription());
        }
    }

    private static void ensureClean(Git git, ProgressMonitor monitor) throws BranchUpdateException
    {
        Status status;
        try
        {
            status = git.status().setProgressMonitor(monitor).call();
        }
        catch (GitAPIException e)
        {
            throw operationFailure(monitor, Reason.UNEXPECTED, e);
        }
        if (!status.isClean())
        {
            Set<String> changedPaths = new TreeSet<>(status.getUncommittedChanges());
            changedPaths.addAll(status.getUntracked());
            throw new BranchUpdateException(new ArrayList<>(changedPaths));
        }
    }

    private String findRemote(String branch) throws BranchUpdateException
    {
        Set<String> remotes = repository.getRemoteNames();
        String configured = repository.getConfig().getString(ConfigConstants.CONFIG_BRANCH_SECTION, branch,
            ConfigConstants.CONFIG_KEY_REMOTE);
        if (configured != null && !LOCAL_REMOTE.equals(configured))
        {
            if (remotes.contains(configured))
            {
                return configured;
            }
            throw new BranchUpdateException(Reason.NO_REMOTE, configured);
        }
        if (remotes.contains(DEFAULT_REMOTE))
        {
            return DEFAULT_REMOTE;
        }
        if (remotes.size() == 1)
        {
            return remotes.iterator().next();
        }
        throw new BranchUpdateException(Reason.NO_REMOTE, branch);
    }

    private Ref fetchRemoteBranch(Git git, String remote, String branch, ProgressMonitor monitor)
        throws BranchUpdateException, IOException
    {
        String remoteHeadName = Constants.R_HEADS + branch;
        String trackingRefName = Constants.R_REMOTES + remote + '/' + branch;
        FetchResult result;
        try
        {
            result = configureCredentials(git.fetch().setRemote(remote).setProgressMonitor(monitor)).call();
        }
        catch (GitAPIException e)
        {
            throw operationFailure(monitor, Reason.FETCH_FAILED, e);
        }

        Ref advertisedBranch = result.getAdvertisedRef(remoteHeadName);
        if (advertisedBranch == null)
        {
            throw new BranchUpdateException(Reason.REMOTE_BRANCH_MISSING, remote + '/' + branch);
        }

        Ref trackingBranch = repository.exactRef(trackingRefName);
        if (trackingBranch == null || !advertisedBranch.getObjectId().equals(trackingBranch.getObjectId()))
        {
            RefSpec branchSpec = new RefSpec().setSource(remoteHeadName).setDestination(trackingRefName)
                .setForceUpdate(true);
            try
            {
                configureCredentials(git.fetch().setRemote(remote).setRefSpecs(branchSpec).setProgressMonitor(monitor))
                    .call();
            }
            catch (GitAPIException e)
            {
                throw operationFailure(monitor, Reason.FETCH_FAILED, e);
            }
            trackingBranch = repository.exactRef(trackingRefName);
        }

        if (trackingBranch == null)
        {
            throw new BranchUpdateException(Reason.REMOTE_BRANCH_MISSING, remote + '/' + branch);
        }
        return trackingBranch;
    }

    private static FetchCommand configureCredentials(FetchCommand command)
    {
        CredentialsProvider provider = CredentialsProvider.getDefault();
        return provider != null ? command.setCredentialsProvider(provider) : command;
    }

    private BranchRelation determineRelation(Ref localBranch, Ref remoteBranch)
        throws BranchUpdateException, IOException
    {
        if (localBranch == null)
        {
            return BranchRelation.CREATED;
        }
        if (localBranch.getObjectId().equals(remoteBranch.getObjectId()))
        {
            return BranchRelation.SAME;
        }

        try (RevWalk walk = new RevWalk(repository))
        {
            RevCommit localCommit = walk.parseCommit(localBranch.getObjectId());
            RevCommit remoteCommit = walk.parseCommit(remoteBranch.getObjectId());
            if (walk.isMergedInto(localCommit, remoteCommit))
            {
                return BranchRelation.BEHIND;
            }
            walk.reset();
            if (walk.isMergedInto(remoteCommit, localCommit))
            {
                return BranchRelation.AHEAD;
            }
        }
        throw new BranchUpdateException(Reason.DIVERGED, Repository.shortenRefName(localBranch.getName()));
    }

    private static void checkout(Git git, String branch, String startPoint, boolean create, ProgressMonitor monitor)
        throws BranchUpdateException
    {
        try
        {
            var command = git.checkout().setName(branch).setProgressMonitor(monitor);
            if (create)
            {
                command.setCreateBranch(true).setStartPoint(startPoint)
                    .setUpstreamMode(org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.TRACK);
            }
            command.call();
        }
        catch (GitAPIException e)
        {
            throw operationFailure(monitor, Reason.CHECKOUT_FAILED, e);
        }
    }

    private static BranchUpdateOutcome fastForward(Git git, Ref remoteBranch, ProgressMonitor monitor)
        throws BranchUpdateException
    {
        MergeResult result;
        try
        {
            result = git.merge().include(remoteBranch).setFastForward(FastForwardMode.FF_ONLY)
                .setProgressMonitor(monitor).call();
        }
        catch (GitAPIException e)
        {
            throw operationFailure(monitor, Reason.UPDATE_FAILED, e);
        }

        MergeStatus status = result.getMergeStatus();
        if (status == MergeStatus.FAST_FORWARD)
        {
            return BranchUpdateOutcome.UPDATED;
        }
        if (status == MergeStatus.ALREADY_UP_TO_DATE)
        {
            return BranchUpdateOutcome.UP_TO_DATE;
        }
        throw new BranchUpdateException(Reason.UPDATE_FAILED, status.toString());
    }

    private static BranchUpdateException operationFailure(ProgressMonitor monitor, Reason reason, Exception cause)
    {
        return monitor.isCancelled() ? new BranchUpdateException(Reason.CANCELLED, null, cause)
            : new BranchUpdateException(reason, cause.getMessage(), cause);
    }

    private static void checkCancelled(ProgressMonitor monitor) throws BranchUpdateException
    {
        if (monitor.isCancelled())
        {
            throw new BranchUpdateException(Reason.CANCELLED, null);
        }
    }

    private enum BranchRelation
    {
        CREATED,
        SAME,
        BEHIND,
        AHEAD
    }

    private static final class JGitProgressMonitor implements ProgressMonitor
    {
        private final OperationProgress delegate;

        JGitProgressMonitor(OperationProgress delegate)
        {
            this.delegate = Objects.requireNonNull(delegate, "delegate"); //$NON-NLS-1$
        }

        @Override
        public void start(int totalTasks)
        {
            // The enclosing application operation owns the top-level task.
        }

        @Override
        public void beginTask(String title, int totalWork)
        {
            delegate.beginTask(title, totalWork);
        }

        @Override
        public void update(int completed)
        {
            delegate.worked(completed);
        }

        @Override
        public void endTask()
        {
            // JGit may report multiple nested tasks.
        }

        @Override
        public boolean isCancelled()
        {
            return delegate.isCancelled();
        }

        @Override
        public void showDuration(boolean enabled)
        {
            // Duration presentation belongs to Eclipse.
        }
    }
}
