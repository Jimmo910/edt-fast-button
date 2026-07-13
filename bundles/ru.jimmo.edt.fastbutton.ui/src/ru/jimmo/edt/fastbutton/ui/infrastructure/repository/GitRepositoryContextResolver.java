/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.infrastructure.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.team.core.RepositoryProvider;

/** Resolves EGit-shared projects without depending on EGit internal Java APIs. */
public final class GitRepositoryContextResolver
{
    private static final String EGIT_PROVIDER_ID = "org.eclipse.egit.core.GitProvider"; //$NON-NLS-1$

    /**
     * @param selectedProject selected EDT project
     * @return repository context, or empty when the project is not shared with EGit
     * @throws IOException when repository metadata cannot be read
     */
    public Optional<GitRepositoryContext> resolve(IProject selectedProject) throws IOException
    {
        if (!isSharedWithGit(selectedProject))
        {
            return Optional.empty();
        }

        FileRepositoryBuilder selectedBuilder = findRepository(selectedProject);
        if (selectedBuilder == null)
        {
            return Optional.empty();
        }

        Repository repository = selectedBuilder.setMustExist(true).build();
        boolean contextCreated = false;
        try
        {
            Path repositoryKey = repositoryKey(repository.getDirectory());
            List<IProject> mappedProjects = new ArrayList<>();
            for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects())
            {
                if (!project.isAccessible() || !isSharedWithGit(project))
                {
                    continue;
                }
                FileRepositoryBuilder projectBuilder = findRepository(project);
                if (projectBuilder != null && repositoryKey.equals(repositoryKey(projectBuilder.getGitDir())))
                {
                    mappedProjects.add(project);
                }
            }
            if (!mappedProjects.contains(selectedProject))
            {
                mappedProjects.add(selectedProject);
            }
            GitRepositoryContext context = new GitRepositoryContext(repository, mappedProjects);
            contextCreated = true;
            return Optional.of(context);
        }
        finally
        {
            if (!contextCreated)
            {
                repository.close();
            }
        }
    }

    private static boolean isSharedWithGit(IProject project)
    {
        RepositoryProvider provider = RepositoryProvider.getProvider(project);
        return provider != null && EGIT_PROVIDER_ID.equals(provider.getID());
    }

    private static FileRepositoryBuilder findRepository(IProject project)
    {
        if (project.getLocation() == null)
        {
            return null;
        }
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.findGitDir(project.getLocation().toFile());
        return builder.getGitDir() != null ? builder : null;
    }

    private static Path repositoryKey(File gitDirectory) throws IOException
    {
        return gitDirectory.getCanonicalFile().toPath();
    }
}
