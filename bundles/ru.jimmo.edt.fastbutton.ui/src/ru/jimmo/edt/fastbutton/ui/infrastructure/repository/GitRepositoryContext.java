/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.infrastructure.repository;

import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jgit.lib.Repository;

/** Git repository together with all open workspace projects mapped to it. */
public final class GitRepositoryContext implements AutoCloseable
{
    private final Repository repository;
    private final List<IProject> projects;
    private final ISchedulingRule schedulingRule;

    GitRepositoryContext(Repository repository, List<IProject> projects)
    {
        this.repository = Objects.requireNonNull(repository, "repository"); //$NON-NLS-1$
        this.projects = List.copyOf(projects);
        this.schedulingRule = MultiRule.combine(this.projects.toArray(ISchedulingRule[]::new));
    }

    /** @return the repository owned by this context */
    public Repository getRepository()
    {
        return repository;
    }

    /** @return all open workspace projects backed by this repository */
    public List<IProject> getProjects()
    {
        return projects;
    }

    /** @return a rule that serializes workspace changes for every mapped project */
    public ISchedulingRule getSchedulingRule()
    {
        return schedulingRule;
    }

    /** Refreshes every open project that can be changed by checkout. */
    public void refreshProjects(IProgressMonitor monitor) throws CoreException
    {
        for (IProject project : projects)
        {
            if (project.isAccessible())
            {
                project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
            }
        }
    }

    @Override
    public void close()
    {
        repository.close();
    }
}
