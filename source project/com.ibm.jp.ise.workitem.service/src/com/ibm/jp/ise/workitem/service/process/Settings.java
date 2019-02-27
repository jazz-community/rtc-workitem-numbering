package com.ibm.jp.ise.workitem.service.process;

import org.eclipse.osgi.util.NLS;

/**
 * Settings
 */
public class Settings extends NLS {
	/** Bundle name */
	private static final String BUNDLE_NAME = "com.ibm.jp.ise.workitem.service.process.settings"; //$NON-NLS-1$
	/** Key of workitem number attribute */
	public static String WORKITEM_NUMBER_ATTRIBUTE_KEY;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Settings.class);
	}

	/** constructor */
	private Settings() {
	}

}
