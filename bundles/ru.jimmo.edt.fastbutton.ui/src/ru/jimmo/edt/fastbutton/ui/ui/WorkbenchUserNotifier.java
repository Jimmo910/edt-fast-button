/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ru.jimmo.edt.fastbutton.ui.FastButtonPlugin;
import ru.jimmo.edt.fastbutton.ui.Messages;
import ru.jimmo.edt.fastbutton.ui.application.BranchUpdateResult;

/** Workbench implementation of operation notifications. */
public final class WorkbenchUserNotifier implements UserNotifier
{
    @Override
    public void warning(String message)
    {
        runInUi(() -> MessageDialog.openWarning(activeShell(), Messages.Dialog_Title, message));
    }

    @Override
    public void success(BranchUpdateResult result)
    {
        String pattern = switch (result.outcome())
        {
        case CREATED -> Messages.Success_Created;
        case UPDATED -> Messages.Success_Updated;
        case UP_TO_DATE -> Messages.Success_UpToDate;
        case AHEAD -> Messages.Success_Ahead;
        };
        String message = NLS.bind(pattern, new Object[] { result.branch(), result.remote() });
        runInUi(() ->
        {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPart part = window != null && window.getActivePage() != null
                ? window.getActivePage().getActivePart() : null;
            if (part instanceof IViewPart viewPart)
            {
                viewPart.getViewSite().getActionBars().getStatusLineManager().setMessage(message);
            }
            FastButtonPlugin.logInfo(message);
        });
    }

    private static Shell activeShell()
    {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        return window != null ? window.getShell() : Display.getDefault().getActiveShell();
    }

    private static void runInUi(Runnable runnable)
    {
        Display display = Display.getDefault();
        if (!display.isDisposed())
        {
            display.asyncExec(runnable);
        }
    }
}
