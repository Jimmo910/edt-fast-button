/*
 * SPDX-License-Identifier: EPL-2.0
 */

package ru.jimmo.edt.fastbutton.ui;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The plugin activator: bundle lifecycle plus a tiny logging facade.
 */
public class FastButtonPlugin extends AbstractUIPlugin
{
    /** The plugin id (equals the bundle symbolic name). */
    public static final String PLUGIN_ID = "ru.jimmo.edt.fastbutton.ui"; //$NON-NLS-1$

    private static FastButtonPlugin plugin;

    @Override
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        setPlugin(this);
    }

    @Override
    public void stop(BundleContext context) throws Exception
    {
        setPlugin(null);
        super.stop(context);
    }

    private static void setPlugin(FastButtonPlugin instance)
    {
        plugin = instance;
    }

    /**
     * @return the shared plugin instance, or {@code null} when the bundle is not active
     */
    public static FastButtonPlugin getDefault()
    {
        return plugin;
    }

    /**
     * Logs an informational message to the platform log (workspace .metadata/.log).
     *
     * @param message the message to log
     */
    public static void logInfo(String message)
    {
        log(Status.info(message));
    }

    /**
     * Logs an error with an optional cause to the platform log.
     *
     * @param message the message to log
     * @param e the cause, may be {@code null}
     */
    public static void logError(String message, Throwable e)
    {
        log(e != null ? Status.error(message, e) : Status.error(message));
    }

    private static void log(IStatus status)
    {
        FastButtonPlugin instance = plugin;
        if (instance != null)
        {
            ILog log = instance.getLog();
            if (log != null)
            {
                log.log(status);
            }
        }
    }
}
