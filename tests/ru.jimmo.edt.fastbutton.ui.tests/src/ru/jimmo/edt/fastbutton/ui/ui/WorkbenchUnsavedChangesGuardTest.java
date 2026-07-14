/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.Test;

import ru.jimmo.edt.fastbutton.ui.ui.WorkbenchUnsavedChangesGuard.DirtyEditor;

/** Unit tests for the pure dirty-editor filtering of the unsaved-changes guard. */
public class WorkbenchUnsavedChangesGuardTest
{
    private final IProject repositoryProject = project("guard-repository"); //$NON-NLS-1$
    private final IProject unrelatedProject = project("guard-unrelated"); //$NON-NLS-1$

    @Test
    public void blocksEditorsFromRepositoryProjects()
    {
        Set<String> titles = WorkbenchUnsavedChangesGuard.blockingEditorTitles(
            List.of(new DirtyEditor("Module", repositoryProject)), List.of(repositoryProject)); //$NON-NLS-1$

        assertEquals(Set.of("Module"), titles); //$NON-NLS-1$
    }

    @Test
    public void ignoresEditorsFromUnrelatedProjects()
    {
        Set<String> titles = WorkbenchUnsavedChangesGuard.blockingEditorTitles(
            List.of(new DirtyEditor("Other", unrelatedProject)), List.of(repositoryProject)); //$NON-NLS-1$

        assertTrue(titles.isEmpty());
    }

    @Test
    public void blocksEditorsWithoutKnownProject()
    {
        Set<String> titles = WorkbenchUnsavedChangesGuard.blockingEditorTitles(
            List.of(new DirtyEditor("Scratch", null)), List.of(repositoryProject)); //$NON-NLS-1$

        assertEquals(Set.of("Scratch"), titles); //$NON-NLS-1$
    }

    @Test
    public void sortsAndDeduplicatesBlockingTitles()
    {
        List<DirtyEditor> dirtyEditors = List.of(
            new DirtyEditor("Zulu", repositoryProject), //$NON-NLS-1$
            new DirtyEditor("Alpha", repositoryProject), //$NON-NLS-1$
            new DirtyEditor("Alpha", null), //$NON-NLS-1$
            new DirtyEditor("Other", unrelatedProject)); //$NON-NLS-1$

        Set<String> titles = WorkbenchUnsavedChangesGuard.blockingEditorTitles(dirtyEditors,
            List.of(repositoryProject));

        assertEquals(List.of("Alpha", "Zulu"), List.copyOf(titles)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static IProject project(String name)
    {
        // Project handles are pure workspace paths; nothing is created on disk.
        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    }
}
