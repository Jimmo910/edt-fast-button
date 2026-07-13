/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui.ui;

import java.util.Collection;
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
    /**
     * @param repositoryProjects projects that checkout can modify
     * @return sorted editor titles that must be saved or reverted first
     */
    public Set<String> findUnsavedEditors(Collection<IProject> repositoryProjects)
    {
        Set<String> dirtyEditors = new TreeSet<>();
        if (!PlatformUI.isWorkbenchRunning())
        {
            return dirtyEditors;
        }

        Set<IProject> projects = Set.copyOf(repositoryProjects);
        for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows())
        {
            for (IWorkbenchPage page : window.getPages())
            {
                for (IEditorPart editor : page.getDirtyEditors())
                {
                    IEditorInput input = editor.getEditorInput();
                    IResource resource = input != null ? input.getAdapter(IResource.class) : null;
                    if (resource == null || projects.contains(resource.getProject()))
                    {
                        dirtyEditors.add(editor.getTitle());
                    }
                }
            }
        }
        return dirtyEditors;
    }
}
