/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.handlers;

import org.eclipse.core.runtime.IProgressMonitor;

import ru.jimmo.edt.fastbutton.ui.application.OperationProgress;

/** Adapts an Eclipse job monitor to the application progress port. */
final class EclipseOperationProgress implements OperationProgress
{
    private final IProgressMonitor delegate;

    EclipseOperationProgress(IProgressMonitor delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void beginTask(String title, int totalWork)
    {
        delegate.subTask(title);
    }

    @Override
    public void worked(int completed)
    {
        delegate.worked(completed);
    }

    @Override
    public boolean isCancelled()
    {
        return delegate.isCanceled();
    }
}
