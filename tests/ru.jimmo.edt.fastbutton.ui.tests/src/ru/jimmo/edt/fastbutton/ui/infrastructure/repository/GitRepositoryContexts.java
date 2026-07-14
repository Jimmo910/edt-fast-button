/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.infrastructure.repository;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jgit.lib.Repository;

/** Test factory for repository contexts, reaching the package-private constructor from the fragment. */
public final class GitRepositoryContexts
{
    private GitRepositoryContexts()
    {
    }

    /**
     * @param repository open repository owned by the created context
     * @param projects workspace projects mapped to the repository
     * @return a repository context built through the package-private constructor
     */
    public static GitRepositoryContext create(Repository repository, List<IProject> projects)
    {
        return new GitRepositoryContext(repository, projects);
    }
}
