/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.ui;

import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateResult;

/** Presentation port for non-blocking operation notifications. */
public interface UserNotifier
{
    /** Shows a localized warning. */
    void warning(String message);

    /** Reports a successful operation. */
    void success(BranchUpdateResult result);
}
