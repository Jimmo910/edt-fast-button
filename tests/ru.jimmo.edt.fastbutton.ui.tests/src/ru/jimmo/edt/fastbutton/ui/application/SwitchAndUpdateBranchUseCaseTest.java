/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateException.Reason;

/** Unit tests for application-layer validation and cancellation. */
public class SwitchAndUpdateBranchUseCaseTest
{
    @Test
    public void invalidBranchDoesNotReachRepositoryPort() throws Exception
    {
        AtomicBoolean called = new AtomicBoolean();
        BranchUpdater updater = (branch, progress) ->
        {
            called.set(true);
            return new BranchUpdateResult(branch, "origin", BranchUpdateOutcome.UP_TO_DATE, false); //$NON-NLS-1$
        };
        var useCase = new SwitchAndUpdateBranchUseCase(branch -> false, updater);

        BranchUpdateException exception = expectFailure(
            () -> useCase.execute("invalid", progress(false))); //$NON-NLS-1$

        assertEquals(Reason.INVALID_BRANCH, exception.getReason());
        assertFalse(called.get());
    }

    @Test
    public void cancellationDoesNotReachRepositoryPort() throws Exception
    {
        AtomicBoolean called = new AtomicBoolean();
        BranchUpdater updater = (branch, progress) ->
        {
            called.set(true);
            return new BranchUpdateResult(branch, "origin", BranchUpdateOutcome.UP_TO_DATE, false); //$NON-NLS-1$
        };
        var useCase = new SwitchAndUpdateBranchUseCase(branch -> true, updater);

        BranchUpdateException exception = expectFailure(() -> useCase.execute("main", progress(true))); //$NON-NLS-1$

        assertEquals(Reason.CANCELLED, exception.getReason());
        assertFalse(called.get());
    }

    private static OperationProgress progress(boolean cancelled)
    {
        return new OperationProgress()
        {
            @Override
            public void beginTask(String title, int totalWork)
            {
                // Progress reporting is irrelevant to these validation tests.
            }

            @Override
            public void worked(int completed)
            {
                // Progress reporting is irrelevant to these validation tests.
            }

            @Override
            public boolean isCancelled()
            {
                return cancelled;
            }
        };
    }

    private static BranchUpdateException expectFailure(CheckedOperation operation) throws Exception
    {
        try
        {
            operation.run();
            fail("Expected BranchUpdateException"); //$NON-NLS-1$
            return null;
        }
        catch (BranchUpdateException e)
        {
            return e;
        }
    }

    @FunctionalInterface
    private interface CheckedOperation
    {
        void run() throws Exception;
    }
}
