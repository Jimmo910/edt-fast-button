/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/** Finds unsaved editor buffers before a Git checkout can change their backing files. */
public final class WorkbenchUnsavedChangesGuard
{
    /** A dirty editor title with the project owning its input, or {@code null} when unknown. */
    public record DirtyEditor(String title, IProject project)
    {
    }

    /**
     * Captures every dirty editor of the running workbench. Must be called from the UI thread.
     *
     * @return dirty editors with their owning projects when known
     */
    public List<DirtyEditor> snapshotDirtyEditors()
    {
        List<DirtyEditor> dirtyEditors = new ArrayList<>();
        if (!PlatformUI.isWorkbenchRunning())
        {
            return dirtyEditors;
        }

        for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows())
        {
            for (IWorkbenchPage page : window.getPages())
            {
                collectDirtyEditors(page, dirtyEditors);
            }
        }
        return dirtyEditors;
    }

    /**
     * Filters a dirty-editor snapshot down to editors that a checkout can affect. Editors without
     * a known owning project are treated as affected.
     *
     * @param dirtyEditors captured dirty editors
     * @param repositoryProjects projects that checkout can modify
     * @return sorted titles of editors that must be saved or reverted first
     */
    public static Set<String> blockingEditorTitles(Collection<DirtyEditor> dirtyEditors,
        Collection<IProject> repositoryProjects)
    {
        Set<IProject> projects = Set.copyOf(repositoryProjects);
        Set<String> titles = new TreeSet<>();
        for (DirtyEditor editor : dirtyEditors)
        {
            if (editor.project() == null || projects.contains(editor.project()))
            {
                titles.add(editor.title());
            }
        }
        return titles;
    }

    private static void collectDirtyEditors(IWorkbenchPage page, List<DirtyEditor> dirtyEditors)
    {
        for (IEditorPart editor : page.getDirtyEditors())
        {
            IEditorInput input = editor.getEditorInput();
            IResource resource = input != null ? input.getAdapter(IResource.class) : null;
            dirtyEditors.add(new DirtyEditor(editor.getTitle(), resource != null ? resource.getProject() : null));
        }
    }
}
