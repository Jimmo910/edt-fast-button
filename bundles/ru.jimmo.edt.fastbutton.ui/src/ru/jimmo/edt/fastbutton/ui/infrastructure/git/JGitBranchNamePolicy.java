/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.infrastructure.git;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;

import ru.jimmo.edt.fastbutton.ui.application.BranchNamePolicy;

/** Validates short branch names according to JGit reference rules. */
public final class JGitBranchNamePolicy implements BranchNamePolicy
{
    @Override
    public boolean isValid(String branch)
    {
        return branch != null && !branch.isBlank() && branch.equals(branch.trim())
            && !branch.startsWith(Constants.R_REFS) && !Constants.HEAD.equals(branch)
            && Repository.isValidRefName(Constants.R_HEADS + branch);
    }
}
