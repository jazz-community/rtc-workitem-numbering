package com.ibm.jp.ise.workitem.ide.ui.aspecteditor;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Activator
 */
public class WorkItemUIPlugin extends AbstractUIPlugin {
	/** Plugin ID */
	public static final String PLUGIN_ID = "com.ibm.jp.ise.workitem.ide.ui"; //$NON-NLS-1$

	/** Activator instance */
	private static WorkItemUIPlugin fgPlugin;

	/**
	 * Constructor
	 */
	public WorkItemUIPlugin() {
	}

	/**
	 * Start Plugin
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		fgPlugin = this;

	}

	/**
	 * Stop plugin
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		fgPlugin = null;
		super.stop(context);
	}

	/** @return Activator instance */
	public static WorkItemUIPlugin getDefault() {
		return fgPlugin;
	}

}
