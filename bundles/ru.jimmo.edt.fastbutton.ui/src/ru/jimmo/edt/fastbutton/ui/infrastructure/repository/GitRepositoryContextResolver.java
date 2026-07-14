/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.infrastructure.repository;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.core.resources.IProject;

/** Resolves the Git repository context backing a selected EDT project. */
@FunctionalInterface
public interface GitRepositoryContextResolver
{
    /**
     * @param selectedProject selected EDT project
     * @return repository context, or empty when the project is not shared with EGit
     * @throws IOException when repository metadata cannot be read
     */
    Optional<GitRepositoryContext> resolve(IProject selectedProject) throws IOException;
}
