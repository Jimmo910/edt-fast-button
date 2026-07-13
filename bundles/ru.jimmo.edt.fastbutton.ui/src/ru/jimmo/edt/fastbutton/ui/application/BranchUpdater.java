/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.application;

/** Port used by the switch-and-update use case to access a Git repository. */
@FunctionalInterface
public interface BranchUpdater
{
    /**
     * Switches to the target branch and updates it without destructive operations.
     *
     * @param targetBranch validated short branch name
     * @param progress operation progress and cancellation
     * @return successful operation result
     * @throws BranchUpdateException when the operation cannot be completed safely
     */
    BranchUpdateResult switchAndUpdate(String targetBranch, OperationProgress progress) throws BranchUpdateException;
}
