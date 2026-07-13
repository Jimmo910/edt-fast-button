/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.application;

/** Framework-independent progress and cancellation port. */
public interface OperationProgress
{
    /** Reports the start of a nested task. */
    void beginTask(String title, int totalWork);

    /** Reports completed work for the active nested task. */
    void worked(int completed);

    /** @return whether the user requested cancellation */
    boolean isCancelled();
}
