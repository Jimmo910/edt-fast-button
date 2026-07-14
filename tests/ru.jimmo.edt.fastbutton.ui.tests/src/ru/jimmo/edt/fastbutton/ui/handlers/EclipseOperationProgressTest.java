/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

/** Unit tests for the Eclipse job monitor to application progress adapter. */
public class EclipseOperationProgressTest
{
    @Test
    public void beginTaskReportsTitleAsSubTask()
    {
        RecordingMonitor monitor = new RecordingMonitor();

        new EclipseOperationProgress(monitor).beginTask("Fetching", 10); //$NON-NLS-1$

        assertEquals("Fetching", monitor.subTaskName); //$NON-NLS-1$
    }

    @Test
    public void workedForwardsCompletedUnits()
    {
        RecordingMonitor monitor = new RecordingMonitor();

        new EclipseOperationProgress(monitor).worked(7);

        assertEquals(7, monitor.workedUnits);
    }

    @Test
    public void cancellationReflectsMonitorState()
    {
        RecordingMonitor monitor = new RecordingMonitor();
        EclipseOperationProgress progress = new EclipseOperationProgress(monitor);

        assertFalse(progress.isCancelled());
        monitor.setCanceled(true);
        assertTrue(progress.isCancelled());
    }

    private static final class RecordingMonitor extends NullProgressMonitor
    {
        private String subTaskName;
        private int workedUnits;

        @Override
        public void subTask(String name)
        {
            this.subTaskName = name;
        }

        @Override
        public void worked(int work)
        {
            this.workedUnits += work;
        }
    }
}
