package com.ibm.jp.ise.workitem.ide.ui.aspecteditor.editor;

import org.eclipse.osgi.util.NLS;

/**
 * Messages
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
	/** Bundle name */
	private static final String BUNDLE_NAME = "com.ibm.jp.ise.workitem.ide.ui.aspecteditor.editor.messages"; //$NON-NLS-1$

	public static String WorkItemNumberingAspectEditor_NUMBERING_WORKITEM_TYPES;
	public static String WorkItemNumberingAspectEditor_NUMBER_OF_DIGIT;
	public static String DefaultCheckAttributeBehavior_WORKITEM_NUMBER_ATTRIBUTE_NOT_CONFIGURATION;
	public static String DefaultCheckAttributeBehavior_WORKITEM_NUMBER_ATTRIBUTE_NOT_CONFIGURATION_TITLE;
	public static String TypeCategory_LIST_OF_TYPES;
	public static String TypeCategory_CATEGORYID_LIST;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
