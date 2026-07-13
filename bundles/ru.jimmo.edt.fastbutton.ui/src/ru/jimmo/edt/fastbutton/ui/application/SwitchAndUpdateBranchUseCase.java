/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.application;

import java.util.Objects;

import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateException.Reason;

/** Application use case for safely switching and updating one configured branch. */
public final class SwitchAndUpdateBranchUseCase
{
    private final BranchNamePolicy branchNamePolicy;
    private final BranchUpdater branchUpdater;

    /**
     * @param branchNamePolicy branch-name validation policy
     * @param branchUpdater repository port
     */
    public SwitchAndUpdateBranchUseCase(BranchNamePolicy branchNamePolicy, BranchUpdater branchUpdater)
    {
        this.branchNamePolicy = Objects.requireNonNull(branchNamePolicy, "branchNamePolicy"); //$NON-NLS-1$
        this.branchUpdater = Objects.requireNonNull(branchUpdater, "branchUpdater"); //$NON-NLS-1$
    }

    /** Executes the safe switch-and-update use case. */
    public BranchUpdateResult execute(String targetBranch, OperationProgress progress) throws BranchUpdateException
    {
        Objects.requireNonNull(progress, "progress"); //$NON-NLS-1$
        if (!branchNamePolicy.isValid(targetBranch))
        {
            throw new BranchUpdateException(Reason.INVALID_BRANCH, targetBranch);
        }
        if (progress.isCancelled())
        {
            throw new BranchUpdateException(Reason.CANCELLED, null);
        }
        return branchUpdater.switchAndUpdate(targetBranch, progress);
    }
}
