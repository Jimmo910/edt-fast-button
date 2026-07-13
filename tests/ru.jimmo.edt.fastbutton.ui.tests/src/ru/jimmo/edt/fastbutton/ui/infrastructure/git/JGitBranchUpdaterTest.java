/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.infrastructure.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FileUtils;
import org.junit.Test;

import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateException;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateException.Reason;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateOutcome;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateResult;
import ru.jimmo.edt.fastbutton.ui.application.OperationProgress;
import ru.jimmo.edt.fastbutton.ui.application.SwitchAndUpdateBranchUseCase;

/** Integration tests for the safe JGit adapter using local temporary repositories. */
public class JGitBranchUpdaterTest
{
    private static final String MAIN = "main"; //$NON-NLS-1$
    private static final String TOPIC = "topic"; //$NON-NLS-1$
    private static final String ORIGIN = "origin"; //$NON-NLS-1$
    private static final String BASE_FILE = "base.txt"; //$NON-NLS-1$

    private static final OperationProgress NO_PROGRESS = new OperationProgress()
    {
        @Override
        public void beginTask(String title, int totalWork)
        {
            // Tests do not display progress.
        }

        @Override
        public void worked(int completed)
        {
            // Tests do not display progress.
        }

        @Override
        public boolean isCancelled()
        {
            return false;
        }
    };

    @Test
    public void switchesAndFastForwardsExistingBranch() throws Exception
    {
        try (RepositoryFixture fixture = createFixture())
        {
            fixture.advanceRemote("remote update"); //$NON-NLS-1$

            BranchUpdateResult result = update(fixture, MAIN);

            assertEquals(MAIN, fixture.client.getRepository().getBranch());
            String fileContent = Files.readString(new File(fixture.clientDirectory, BASE_FILE).toPath());
            assertEquals("remote update", fileContent); //$NON-NLS-1$
            assertTrue(result.switched());
            assertEquals(BranchUpdateOutcome.UPDATED, result.outcome());
            assertEquals(ORIGIN, result.remote());
        }
    }

    @Test
    public void createsMissingLocalBranchFromRemote() throws Exception
    {
        try (RepositoryFixture fixture = createFixture())
        {
            fixture.client.branchDelete().setBranchNames(MAIN).setForce(true).call();
            fixture.advanceRemote("created branch update"); //$NON-NLS-1$

            BranchUpdateResult result = update(fixture, MAIN);

            assertEquals(BranchUpdateOutcome.CREATED, result.outcome());
            assertEquals(MAIN, fixture.client.getRepository().getBranch());
            assertEquals(ORIGIN, fixture.client.getRepository().getConfig().getString(
                ConfigConstants.CONFIG_BRANCH_SECTION, MAIN, ConfigConstants.CONFIG_KEY_REMOTE));
        }
    }

    @Test
    public void reportsEqualBranchesAsUpToDate() throws Exception
    {
        try (RepositoryFixture fixture = createFixture())
        {
            BranchUpdateResult result = update(fixture, MAIN);

            assertEquals(BranchUpdateOutcome.UP_TO_DATE, result.outcome());
            assertTrue(result.switched());
            assertTrue(fixture.client.status().call().isClean());
        }
    }

    @Test
    public void reportsLocalCommitsAsAhead() throws Exception
    {
        try (RepositoryFixture fixture = createFixture())
        {
            fixture.createLocalMainCommit();

            BranchUpdateResult result = update(fixture, MAIN);

            assertEquals(BranchUpdateOutcome.AHEAD, result.outcome());
            assertEquals(MAIN, fixture.client.getRepository().getBranch());
        }
    }

    @Test
    public void usesConfiguredUpstreamRemote() throws Exception
    {
        try (RepositoryFixture fixture = createFixture())
        {
            String upstream = "upstream"; //$NON-NLS-1$
            fixture.client.remoteAdd().setName(upstream)
                .setUri(new URIish(fixture.remoteDirectory.toURI().toString())).call();
            fixture.client.getRepository().getConfig().setString(ConfigConstants.CONFIG_BRANCH_SECTION, MAIN,
                ConfigConstants.CONFIG_KEY_REMOTE, upstream);
            fixture.client.getRepository().getConfig().save();

            BranchUpdateResult result = update(fixture, MAIN);

            assertEquals(upstream, result.remote());
        }
    }

    @Test
    public void rejectsMissingConfiguredRemoteWithoutFallback() throws Exception
    {
        try (RepositoryFixture fixture = createFixture())
        {
            fixture.client.getRepository().getConfig().setString(ConfigConstants.CONFIG_BRANCH_SECTION, MAIN,
                ConfigConstants.CONFIG_KEY_REMOTE, "removed-remote"); //$NON-NLS-1$
            fixture.client.getRepository().getConfig().save();

            BranchUpdateException exception = expectFailure(() -> update(fixture, MAIN));

            assertEquals(Reason.NO_REMOTE, exception.getReason());
            assertEquals(TOPIC, fixture.client.getRepository().getBranch());
        }
    }

    @Test
    public void rejectsTrackedChangesBeforeNetworkAccess() throws Exception
    {
        try (RepositoryFixture fixture = createFixture())
        {
            Files.writeString(new File(fixture.clientDirectory, BASE_FILE).toPath(), "dirty"); //$NON-NLS-1$
            fixture.breakRemoteUrl();

            BranchUpdateException exception = expectFailure(() -> update(fixture, MAIN));

            assertEquals(Reason.DIRTY_WORKTREE, exception.getReason());
            assertTrue(exception.getChangedPaths().contains(BASE_FILE));
            assertEquals(TOPIC, fixture.client.getRepository().getBranch());
        }
    }

    @Test
    public void rejectsUntrackedFilesBeforeNetworkAccess() throws Exception
    {
        try (RepositoryFixture fixture = createFixture())
        {
            File untrackedFile = new File(fixture.clientDirectory, "untracked.txt"); //$NON-NLS-1$
            Files.writeString(untrackedFile.toPath(), "dirty"); //$NON-NLS-1$
            fixture.breakRemoteUrl();

            BranchUpdateException exception = expectFailure(() -> update(fixture, MAIN));

            assertEquals(Reason.DIRTY_WORKTREE, exception.getReason());
            assertTrue(exception.getChangedPaths().contains("untracked.txt")); //$NON-NLS-1$
            assertEquals(TOPIC, fixture.client.getRepository().getBranch());
        }
    }

    @Test
    public void rejectsStagedFilesBeforeNetworkAccess() throws Exception
    {
        try (RepositoryFixture fixture = createFixture())
        {
            String stagedPath = "staged.txt"; //$NON-NLS-1$
            Files.writeString(new File(fixture.clientDirectory, stagedPath).toPath(), "staged"); //$NON-NLS-1$
            fixture.client.add().addFilepattern(stagedPath).call();
            fixture.breakRemoteUrl();

            BranchUpdateException exception = expectFailure(() -> update(fixture, MAIN));

            assertEquals(Reason.DIRTY_WORKTREE, exception.getReason());
            assertTrue(exception.getChangedPaths().contains(stagedPath));
            assertEquals(TOPIC, fixture.client.getRepository().getBranch());
        }
    }

    @Test
    public void honorsCancellationBeforeNetworkAccess() throws Exception
    {
        try (RepositoryFixture fixture = createFixture())
        {
            fixture.breakRemoteUrl();
            OperationProgress cancelled = new OperationProgress()
            {
                @Override
                public void beginTask(String title, int totalWork)
                {
                    // Tests do not display progress.
                }

                @Override
                public void worked(int completed)
                {
                    // Tests do not display progress.
                }

                @Override
                public boolean isCancelled()
                {
                    return true;
                }
            };

            BranchUpdateException exception = expectFailure(
                () -> new JGitBranchUpdater(fixture.client.getRepository()).switchAndUpdate(MAIN, cancelled));

            assertEquals(Reason.CANCELLED, exception.getReason());
            assertEquals(TOPIC, fixture.client.getRepository().getBranch());
        }
    }

    @Test
    public void rejectsDivergedBranchWithoutSwitching() throws Exception
    {
        try (RepositoryFixture fixture = createFixture())
        {
            fixture.advanceRemote("remote side"); //$NON-NLS-1$
            fixture.createLocalMainCommit();

            BranchUpdateException exception = expectFailure(() -> update(fixture, MAIN));

            assertEquals(Reason.DIVERGED, exception.getReason());
            assertEquals(TOPIC, fixture.client.getRepository().getBranch());
        }
    }

    @Test
    public void reportsMissingRemoteBranchWithoutSwitching() throws Exception
    {
        try (RepositoryFixture fixture = createFixture())
        {
            BranchUpdateException exception = expectFailure(() -> update(fixture, "release/missing")); //$NON-NLS-1$

            assertEquals(Reason.REMOTE_BRANCH_MISSING, exception.getReason());
            assertEquals(TOPIC, fixture.client.getRepository().getBranch());
        }
    }

    @Test
    public void reportsFetchFailureWithoutSwitching() throws Exception
    {
        try (RepositoryFixture fixture = createFixture())
        {
            fixture.breakRemoteUrl();

            BranchUpdateException exception = expectFailure(() -> update(fixture, MAIN));

            assertEquals(Reason.FETCH_FAILED, exception.getReason());
            assertEquals(TOPIC, fixture.client.getRepository().getBranch());
        }
    }

    @Test
    public void rejectsBareRepositoryAsUnsafe() throws Exception
    {
        Path root = Files.createTempDirectory("fastbutton-bare-test-"); //$NON-NLS-1$
        try (Git bare = Git.init().setBare(true).setDirectory(root.toFile()).call())
        {
            var useCase = new SwitchAndUpdateBranchUseCase(new JGitBranchNamePolicy(),
                new JGitBranchUpdater(bare.getRepository()));

            BranchUpdateException exception = expectFailure(() -> useCase.execute(MAIN, NO_PROGRESS));

            assertEquals(Reason.UNSAFE_REPOSITORY_STATE, exception.getReason());
        }
        finally
        {
            FileUtils.delete(root.toFile(),
                FileUtils.RECURSIVE | FileUtils.RETRY | FileUtils.IGNORE_ERRORS | FileUtils.SKIP_MISSING);
        }
    }

    @Test
    public void validatesShortBranchNames()
    {
        JGitBranchNamePolicy policy = new JGitBranchNamePolicy();
        assertTrue(policy.isValid(MAIN));
        assertTrue(policy.isValid("release/1.0")); //$NON-NLS-1$
        assertFalse(policy.isValid("")); //$NON-NLS-1$
        assertFalse(policy.isValid(" main")); //$NON-NLS-1$
        assertFalse(policy.isValid("bad..name")); //$NON-NLS-1$
        assertFalse(policy.isValid("refs/heads/main")); //$NON-NLS-1$
        assertFalse(policy.isValid(Constants.HEAD));
    }

    private static BranchUpdateResult update(RepositoryFixture fixture, String branch) throws BranchUpdateException
    {
        var useCase = new SwitchAndUpdateBranchUseCase(new JGitBranchNamePolicy(),
            new JGitBranchUpdater(fixture.client.getRepository()));
        return useCase.execute(branch, NO_PROGRESS);
    }

    private static BranchUpdateException expectFailure(CheckedOperation operation) throws Exception
    {
        try
        {
            operation.run();
            fail("Expected BranchUpdateException"); //$NON-NLS-1$
            return null;
        }
        catch (BranchUpdateException e)
        {
            return e;
        }
    }

    private RepositoryFixture createFixture() throws Exception
    {
        Path rootDirectory = Files.createTempDirectory("fastbutton-test-"); //$NON-NLS-1$
        File remoteDirectory = Files.createDirectory(rootDirectory.resolve("remote.git")).toFile(); //$NON-NLS-1$
        try (Git ignored = Git.init().setBare(true).setInitialBranch(MAIN).setDirectory(remoteDirectory).call())
        {
            // The initial branch is recorded in the bare repository's symbolic HEAD.
        }

        File seedDirectory = Files.createDirectory(rootDirectory.resolve("seed")).toFile(); //$NON-NLS-1$
        Git seed = Git.init().setInitialBranch(MAIN).setDirectory(seedDirectory).call();
        Files.writeString(new File(seedDirectory, BASE_FILE).toPath(), "base"); //$NON-NLS-1$
        commitAll(seed, "initial"); //$NON-NLS-1$
        seed.remoteAdd().setName(ORIGIN).setUri(new URIish(remoteDirectory.toURI().toString())).call();
        seed.push().setRemote(ORIGIN)
            .setRefSpecs(new RefSpec(Constants.R_HEADS + MAIN + ':' + Constants.R_HEADS + MAIN)).call();

        File clientDirectory = Files.createDirectory(rootDirectory.resolve("client")).toFile(); //$NON-NLS-1$
        Git client = Git.cloneRepository().setURI(remoteDirectory.toURI().toString()).setDirectory(clientDirectory)
            .call();
        client.checkout().setCreateBranch(true).setName(TOPIC).call();
        return new RepositoryFixture(rootDirectory, remoteDirectory, seedDirectory, clientDirectory, seed, client);
    }

    private static void commitAll(Git git, String message) throws Exception
    {
        git.add().addFilepattern(".").call(); //$NON-NLS-1$
        git.commit().setMessage(message).setAuthor("Test", "test@example.com") //$NON-NLS-1$ //$NON-NLS-2$
            .setCommitter("Test", "test@example.com").call(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private final class RepositoryFixture implements AutoCloseable
    {
        private final Path rootDirectory;
        private final File remoteDirectory;
        private final File seedDirectory;
        private final File clientDirectory;
        private final Git seed;
        private final Git client;

        RepositoryFixture(Path rootDirectory, File remoteDirectory, File seedDirectory, File clientDirectory, Git seed,
            Git client)
        {
            this.rootDirectory = rootDirectory;
            this.remoteDirectory = remoteDirectory;
            this.seedDirectory = seedDirectory;
            this.clientDirectory = clientDirectory;
            this.seed = seed;
            this.client = client;
        }

        void advanceRemote(String content) throws Exception
        {
            Files.writeString(new File(seedDirectory, BASE_FILE).toPath(), content);
            commitAll(seed, content);
            seed.push().setRemote(ORIGIN)
                .setRefSpecs(new RefSpec(Constants.R_HEADS + MAIN + ':' + Constants.R_HEADS + MAIN)).call();
        }

        void createLocalMainCommit() throws Exception
        {
            client.checkout().setName(MAIN).call();
            File localFile = new File(clientDirectory, "local.txt"); //$NON-NLS-1$
            Files.writeString(localFile.toPath(), "local side"); //$NON-NLS-1$
            commitAll(client, "local side"); //$NON-NLS-1$
            client.checkout().setName(TOPIC).call();
        }

        void breakRemoteUrl() throws Exception
        {
            File missingRemote = new File(remoteDirectory.getParentFile(), "missing.git"); //$NON-NLS-1$
            client.getRepository().getConfig().setString(ConfigConstants.CONFIG_REMOTE_SECTION, ORIGIN,
                ConfigConstants.CONFIG_KEY_URL, missingRemote.toURI().toString());
            client.getRepository().getConfig().save();
        }

        @Override
        public void close() throws Exception
        {
            client.close();
            seed.close();
            FileUtils.delete(rootDirectory.toFile(),
                FileUtils.RECURSIVE | FileUtils.RETRY | FileUtils.IGNORE_ERRORS | FileUtils.SKIP_MISSING);
        }
    }

    @FunctionalInterface
    private interface CheckedOperation
    {
        void run() throws Exception;
    }
}
