/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.osgi.util.NLS;
import org.junit.Test;

import ru.jimmo.edt.fastbutton.ui.Messages;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateException;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateException.Reason;

/** Unit tests for localized message resolution. */
public class UpdateMessageResolverTest
{
    private static final String BRANCH = "main"; //$NON-NLS-1$
    private static final String DETAIL = "technical detail"; //$NON-NLS-1$

    private final UpdateMessageResolver resolver = new UpdateMessageResolver();

    @Test
    public void mapsEveryReasonToItsLocalizedMessage()
    {
        assertEquals(Messages.InvalidBranch_Message,
            resolver.failure(new BranchUpdateException(Reason.INVALID_BRANCH, BRANCH), BRANCH));
        assertEquals(NLS.bind(Messages.DirtyRepository_Message, "a.txt, b.txt"), //$NON-NLS-1$
            resolver.failure(new BranchUpdateException(List.of("a.txt", "b.txt")), BRANCH)); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(NLS.bind(Messages.UnsafeRepository_Message, DETAIL),
            resolver.failure(new BranchUpdateException(Reason.UNSAFE_REPOSITORY_STATE, DETAIL), BRANCH));
        assertEquals(NLS.bind(Messages.NoRemote_Message, BRANCH),
            resolver.failure(new BranchUpdateException(Reason.NO_REMOTE, BRANCH), BRANCH));
        assertEquals(NLS.bind(Messages.RemoteBranchMissing_Message, DETAIL),
            resolver.failure(new BranchUpdateException(Reason.REMOTE_BRANCH_MISSING, DETAIL), BRANCH));
        assertEquals(NLS.bind(Messages.Diverged_Message, BRANCH),
            resolver.failure(new BranchUpdateException(Reason.DIVERGED, BRANCH), BRANCH));
        assertEquals(NLS.bind(Messages.FetchFailed_Message, DETAIL),
            resolver.failure(new BranchUpdateException(Reason.FETCH_FAILED, DETAIL), BRANCH));
        assertEquals(NLS.bind(Messages.CheckoutFailed_Message, DETAIL),
            resolver.failure(new BranchUpdateException(Reason.CHECKOUT_FAILED, DETAIL), BRANCH));
        assertEquals(NLS.bind(Messages.UpdateFailed_Message, DETAIL),
            resolver.failure(new BranchUpdateException(Reason.UPDATE_FAILED, DETAIL), BRANCH));
        assertEquals("", resolver.failure(new BranchUpdateException(Reason.CANCELLED, null), BRANCH)); //$NON-NLS-1$
        assertEquals(NLS.bind(Messages.UnexpectedFailure_Message, DETAIL),
            resolver.failure(new BranchUpdateException(Reason.UNEXPECTED, DETAIL), BRANCH));
    }

    @Test
    public void bindsEmptyStringForMissingDetail()
    {
        assertEquals(NLS.bind(Messages.FetchFailed_Message, ""), //$NON-NLS-1$
            resolver.failure(new BranchUpdateException(Reason.FETCH_FAILED, null), BRANCH));
    }

    @Test
    public void classifiesOperationFailuresAsErrors()
    {
        assertTrue(resolver.isError(new BranchUpdateException(Reason.FETCH_FAILED, DETAIL)));
        assertTrue(resolver.isError(new BranchUpdateException(Reason.CHECKOUT_FAILED, DETAIL)));
        assertTrue(resolver.isError(new BranchUpdateException(Reason.UPDATE_FAILED, DETAIL)));
        assertTrue(resolver.isError(new BranchUpdateException(Reason.UNEXPECTED, DETAIL)));
    }

    @Test
    public void classifiesPreconditionFailuresAsWarnings()
    {
        assertFalse(resolver.isError(new BranchUpdateException(Reason.INVALID_BRANCH, BRANCH)));
        assertFalse(resolver.isError(new BranchUpdateException(List.of())));
        assertFalse(resolver.isError(new BranchUpdateException(Reason.UNSAFE_REPOSITORY_STATE, DETAIL)));
        assertFalse(resolver.isError(new BranchUpdateException(Reason.NO_REMOTE, BRANCH)));
        assertFalse(resolver.isError(new BranchUpdateException(Reason.REMOTE_BRANCH_MISSING, DETAIL)));
        assertFalse(resolver.isError(new BranchUpdateException(Reason.DIVERGED, BRANCH)));
        assertFalse(resolver.isError(new BranchUpdateException(Reason.CANCELLED, null)));
    }

    @Test
    public void summarizesNothingAsEmptyString()
    {
        assertEquals("", resolver.summarize(List.of())); //$NON-NLS-1$
    }

    @Test
    public void summarizesShortListsCompletely()
    {
        assertEquals("one, two", resolver.summarize(List.of("one", "two"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Test
    public void summarizesLongListsWithRemainderSuffix()
    {
        List<String> values = IntStream.rangeClosed(1, 12)
            .mapToObj(index -> "item" + index).toList(); //$NON-NLS-1$
        String listed = String.join(", ", values.subList(0, 10)); //$NON-NLS-1$

        assertEquals(NLS.bind(Messages.MoreItems_Suffix, listed, 2), resolver.summarize(values));
    }

    @Test
    public void formatsMissingRepositoryWarning()
    {
        assertEquals(NLS.bind(Messages.NoRepository_Message, "Demo"), //$NON-NLS-1$
            resolver.noRepository("Demo")); //$NON-NLS-1$
    }

    @Test
    public void formatsUnsavedEditorsWarning()
    {
        assertEquals(NLS.bind(Messages.UnsavedEditors_Message, "Editor A, Editor B"), //$NON-NLS-1$
            resolver.unsavedEditors(List.of("Editor A", "Editor B"))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void formatsRefreshFailure()
    {
        assertEquals(NLS.bind(Messages.RefreshFailed_Message, DETAIL), resolver.refreshFailure(DETAIL));
        assertEquals(NLS.bind(Messages.RefreshFailed_Message, ""), resolver.refreshFailure(null)); //$NON-NLS-1$
    }

    @Test
    public void formatsUnexpectedFailure()
    {
        assertEquals(NLS.bind(Messages.UnexpectedFailure_Message, DETAIL), resolver.unexpectedFailure(DETAIL));
        assertEquals(NLS.bind(Messages.UnexpectedFailure_Message, ""), resolver.unexpectedFailure(null)); //$NON-NLS-1$
    }
}
