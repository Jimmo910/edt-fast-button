/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui;

import org.eclipse.osgi.util.NLS;

/** Localized UI messages. */
public final class Messages extends NLS
{
    private static final String BUNDLE_NAME = "ru.jimmo.edt.fastbutton.ui.messages"; //$NON-NLS-1$

    public static String PreferencePage_Description;
    public static String PreferencePage_TargetBranch;
    public static String PreferencePage_InvalidBranch;
    public static String SwitchAndUpdate_Label;
    public static String Job_Name;
    public static String Dialog_Title;
    public static String DirtyRepository_Message;
    public static String UnsavedEditors_Message;
    public static String MoreItems_Suffix;
    public static String NoRepository_Message;
    public static String InvalidBranch_Message;
    public static String UnsafeRepository_Message;
    public static String NoRemote_Message;
    public static String RemoteBranchMissing_Message;
    public static String Diverged_Message;
    public static String FetchFailed_Message;
    public static String CheckoutFailed_Message;
    public static String UpdateFailed_Message;
    public static String UnexpectedFailure_Message;
    public static String RefreshFailed_Message;
    public static String Success_Created;
    public static String Success_Updated;
    public static String Success_UpToDate;
    public static String Success_Ahead;

    static
    {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
