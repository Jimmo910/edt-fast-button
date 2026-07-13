/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.application;

/** Validates configured short branch names. */
@FunctionalInterface
public interface BranchNamePolicy
{
    /**
     * @param branch branch name to validate
     * @return {@code true} when the branch can be used by the update operation
     */
    boolean isValid(String branch);
}
