package com.ibm.jp.ise.workitem.ide.ui.aspecteditor;

import org.eclipse.osgi.util.NLS;

/**
 * Settings class
 */
public class Settings extends NLS {
	/** Bundle name */
	private static final String BUNDLE_NAME = "com.ibm.jp.ise.workitem.ide.ui.aspecteditor.settings"; //$NON-NLS-1$
	/** Number of digit */
	public static String WorkItemNumberingAspectEditor_NUMBER_OF_DIGIT_OPTIONS;
	/** Key of workitem number attribute */
	public static String WorkItemNumberingAspectEditor_WORKITEM_NUMBER_ATTRIBUTE_KEY;
	/** Supportted attribute types as workitem numbering key */
	public static String WorkItemNumberingAspectEditor_SUPPORTED_ATTRIBUTE_TYPE;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Settings.class);
	}

	/** Constructor */
	private Settings() {
	}
}
