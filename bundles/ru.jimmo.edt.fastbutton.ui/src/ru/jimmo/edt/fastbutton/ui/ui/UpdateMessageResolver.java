/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.ui;

import java.util.Collection;

import org.eclipse.osgi.util.NLS;

import ru.jimmo.edt.fastbutton.ui.Messages;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateException;

/** Converts application outcomes and failures into localized user messages. */
public final class UpdateMessageResolver
{
    private static final int MAX_LISTED_ITEMS = 10;

    /** Returns a localized failure message. */
    public String failure(BranchUpdateException exception, String branch)
    {
        return switch (exception.getReason())
        {
        case INVALID_BRANCH -> Messages.InvalidBranch_Message;
        case DIRTY_WORKTREE -> NLS.bind(Messages.DirtyRepository_Message,
            summarize(exception.getChangedPaths()));
        case UNSAFE_REPOSITORY_STATE -> NLS.bind(Messages.UnsafeRepository_Message, detail(exception.getDetail()));
        case NO_REMOTE -> NLS.bind(Messages.NoRemote_Message, branch);
        case REMOTE_BRANCH_MISSING -> NLS.bind(Messages.RemoteBranchMissing_Message,
            detail(exception.getDetail()));
        case DIVERGED -> NLS.bind(Messages.Diverged_Message, branch);
        case FETCH_FAILED -> NLS.bind(Messages.FetchFailed_Message, detail(exception.getDetail()));
        case CHECKOUT_FAILED -> NLS.bind(Messages.CheckoutFailed_Message, detail(exception.getDetail()));
        case UPDATE_FAILED -> NLS.bind(Messages.UpdateFailed_Message, detail(exception.getDetail()));
        case CANCELLED -> ""; //$NON-NLS-1$
        case UNEXPECTED -> NLS.bind(Messages.UnexpectedFailure_Message, detail(exception.getDetail()));
        };
    }

    /** Returns whether the failure should be recorded as an error rather than a user precondition warning. */
    public boolean isError(BranchUpdateException exception)
    {
        return switch (exception.getReason())
        {
        case FETCH_FAILED, CHECKOUT_FAILED, UPDATE_FAILED, UNEXPECTED -> true;
        default -> false;
        };
    }

    /** Returns a localized warning for a project that is not inside a Git repository. */
    public String noRepository(String projectName)
    {
        return NLS.bind(Messages.NoRepository_Message, projectName);
    }

    /** Returns a localized warning listing unsaved editors that block the operation. */
    public String unsavedEditors(Collection<String> editorTitles)
    {
        return NLS.bind(Messages.UnsavedEditors_Message, summarize(editorTitles));
    }

    /** Returns a localized message for a workspace refresh failure after the Git operation. */
    public String refreshFailure(String failureDetail)
    {
        return NLS.bind(Messages.RefreshFailed_Message, detail(failureDetail));
    }

    /** Returns a localized message for an unexpected repository access failure. */
    public String unexpectedFailure(String failureDetail)
    {
        return NLS.bind(Messages.UnexpectedFailure_Message, detail(failureDetail));
    }

    /** Formats a bounded, localized-independent list for embedding in a message. */
    public String summarize(Collection<String> values)
    {
        if (values.isEmpty())
        {
            return ""; //$NON-NLS-1$
        }
        String summary = String.join(", ", values.stream().limit(MAX_LISTED_ITEMS).toList()); //$NON-NLS-1$
        int remaining = values.size() - MAX_LISTED_ITEMS;
        return remaining > 0 ? NLS.bind(Messages.MoreItems_Suffix, summary, remaining) : summary;
    }

    private static String detail(String value)
    {
        return value != null ? value : ""; //$NON-NLS-1$
    }
}
