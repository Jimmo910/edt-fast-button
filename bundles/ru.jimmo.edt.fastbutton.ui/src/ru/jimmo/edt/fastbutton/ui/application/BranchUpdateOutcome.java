/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.application;

/** Relationship between the local branch and its remote after a successful operation. */
public enum BranchUpdateOutcome
{
    /** A missing local branch was created from the remote branch. */
    CREATED,
    /** The local branch was moved forward to the remote commit. */
    UPDATED,
    /** The local and remote branches already referenced the same commit. */
    UP_TO_DATE,
    /** The local branch contains commits that are not present on the remote branch. */
    AHEAD
}
