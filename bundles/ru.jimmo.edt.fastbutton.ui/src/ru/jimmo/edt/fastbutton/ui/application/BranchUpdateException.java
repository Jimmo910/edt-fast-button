/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.application;

import java.util.List;
import java.util.Objects;

/** Describes an expected failure of the safe branch update operation. */
public final class BranchUpdateException extends Exception
{
    private static final long serialVersionUID = 1L;

    /** Failure categories that the UI can localize without parsing exception text. */
    public enum Reason
    {
        INVALID_BRANCH,
        DIRTY_WORKTREE,
        UNSAFE_REPOSITORY_STATE,
        NO_REMOTE,
        REMOTE_BRANCH_MISSING,
        DIVERGED,
        FETCH_FAILED,
        CHECKOUT_FAILED,
        UPDATE_FAILED,
        CANCELLED,
        UNEXPECTED
    }

    private final Reason reason;
    private final String detail;
    private final List<String> changedPaths;

    /**
     * Creates a failure with optional technical context.
     *
     * @param reason machine-readable failure category
     * @param detail optional technical context
     */
    public BranchUpdateException(Reason reason, String detail)
    {
        this(reason, detail, null, List.of());
    }

    /**
     * Creates a failure with technical context and its cause.
     *
     * @param reason machine-readable failure category
     * @param detail optional technical context
     * @param cause original failure
     */
    public BranchUpdateException(Reason reason, String detail, Throwable cause)
    {
        this(reason, detail, cause, List.of());
    }

    /**
     * Creates a dirty-worktree failure.
     *
     * @param changedPaths sorted changed repository paths
     */
    public BranchUpdateException(List<String> changedPaths)
    {
        this(Reason.DIRTY_WORKTREE, null, null, changedPaths);
    }

    private BranchUpdateException(Reason reason, String detail, Throwable cause, List<String> changedPaths)
    {
        super(detail, cause);
        this.reason = Objects.requireNonNull(reason, "reason"); //$NON-NLS-1$
        this.detail = detail;
        this.changedPaths = List.copyOf(changedPaths);
    }

    /** @return the machine-readable failure category */
    public Reason getReason()
    {
        return reason;
    }

    /** @return optional technical context, or {@code null} */
    public String getDetail()
    {
        return detail;
    }

    /** @return changed paths when {@link Reason#DIRTY_WORKTREE} was detected */
    public List<String> getChangedPaths()
    {
        return changedPaths;
    }
}
