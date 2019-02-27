package com.ibm.jp.ise.workitem.service.process;

import org.eclipse.osgi.util.NLS;

/**
 * Messages
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
	/** Bundle name */
	private static final String BUNDLE_NAME = "com.ibm.jp.ise.workitem.service.process.messages"; //$NON-NLS-1$
	public static String WORKITEM_NUMBER_ATTRIBUTE_NOT_FOUND;
	public static String WORKITEM_NUMBER_ATTRIBUTE_NOT_CONFIGURATION;
	public static String FORMAT_NUMBER_OF_DIGIT;
	public static String LAST_WORKITEM_NUMBER_NOT_NUMERICAL;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
