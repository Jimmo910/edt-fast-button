/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.application;

import java.util.Objects;

/** Result of a successful safe branch update. */
public record BranchUpdateResult(String branch, String remote, BranchUpdateOutcome outcome, boolean switched)
{
    /** Creates and validates an immutable operation result. */
    public BranchUpdateResult
    {
        Objects.requireNonNull(branch, "branch"); //$NON-NLS-1$
        Objects.requireNonNull(remote, "remote"); //$NON-NLS-1$
        Objects.requireNonNull(outcome, "outcome"); //$NON-NLS-1$
    }
}
