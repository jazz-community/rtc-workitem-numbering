package com.ibm.jp.ise.workitem.ide.ui.util;

import java.util.List;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.Settings;
import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.editor.Messages;
import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.editor.WorkItemNumberingAspectEditor.TreeElement;
import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.type.TypeCategory;
import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.type.TypeCategory.CustomAttribute;
import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.type.TypeCategory.Type;
import com.ibm.team.process.internal.common.NLS;
import com.ibm.team.repository.rcp.ui.internal.utils.JFaceUtils;

/**
 * 
 *
 */
@SuppressWarnings("restriction")
public class DefaultCheckAttributeBehavior extends SelectionAdapter implements TreeListener {

	private static final String BLANK = " "; //$NON-NLS-1$
	private static final String HYPHEN = "-"; //$NON-NLS-1$
	/**
	 * List of TypeCategory
	 */
	private static List<TypeCategory> fTypeCategories;

	/**
	 * Install this behavior to tree
	 * 
	 * @param tree Tree
	 * @param typeCategories List of workitem category
	 */
	public static void install(Tree tree, List<TypeCategory> typeCategories) {
		DefaultCheckAttributeBehavior behavior = new DefaultCheckAttributeBehavior();
		tree.addSelectionListener(behavior);
		tree.addTreeListener(behavior);
		fTypeCategories = typeCategories;
	}

	/**
	 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.detail == SWT.CHECK) {
			handleEvent(event);
		}
	}

	/**
	 * @see org.eclipse.swt.events.TreeListener#treeExpanded(org.eclipse.swt.events.TreeEvent)
	 */
	@Override
	public void treeExpanded(TreeEvent event) {
		// blank block
	}

	/**
	 * @see org.eclipse.swt.events.TreeListener#treeCollapsed(org.eclipse.swt.events.TreeEvent)
	 */
	@Override
	public void treeCollapsed(TreeEvent e) {
		// blank block
	}

	/**
	 * Handle selection event
	 * 
	 * @param event SelectionEvent
	 */
	private static void handleEvent(SelectionEvent event) {
		TreeItem item = (TreeItem) event.item;
		boolean checked = item.getChecked();
		if (checked) {
			if (!checkElement(item)) {
				item.setChecked(false);
				return;
			}
		} else {
			uncheckElement(item);
		}
	}

	/**
	 * Check TreeElemebt
	 * 
	 * @param item TreeItem
	 * @return result
	 */
	private static boolean checkElement(TreeItem item) {
		TreeElement element = (TreeElement) item.getData();
		Object realElement = element.getElement();
		if (realElement instanceof Type) {
			if (isValidTargetWorkItemConfig((Type) realElement)) {
				item.setChecked(true);
				return true;
			}
			return false;
		} else if (realElement instanceof CustomAttribute) {
			return checkElement(item.getParentItem());
		}

		return true;
	}

	/**
	 * Uncheck Element
	 * @param item TreeItem
	 */
	private static void uncheckElement(TreeItem item) {
		TreeElement element = (TreeElement) item.getData();
		Object realElement = element.getElement();
		if (realElement instanceof Type) {
			for (TreeItem childItem : item.getItems()) {
				childItem.setChecked(false);
			}
		}
		
	}

	/**
	 * Judge if process configuration is valid
	 * 
	 * @param type Workitem type
	 * @return result
	 */
	private static boolean isValidTargetWorkItemConfig(Type type) {
		String workItemTypeCategoryID = type.getCategory().getIdentifier();
		String typeName = type.getName() + BLANK + HYPHEN + BLANK + type.getId();
		for (TypeCategory typeCategory : fTypeCategories) {
			if (typeCategory.getIdentifier().equals(workItemTypeCategoryID)) {
				for (CustomAttribute customAttribute : typeCategory.getCustomAttributes()) {
					if (customAttribute.getId().equals(
							Settings.WorkItemNumberingAspectEditor_WORKITEM_NUMBER_ATTRIBUTE_KEY)) {
						return true;
					}
				}
				break;
			}
		}
		JFaceUtils.showMessageBlocking(
				Messages.DefaultCheckAttributeBehavior_WORKITEM_NUMBER_ATTRIBUTE_NOT_CONFIGURATION_TITLE,
				NLS.bind(Messages.DefaultCheckAttributeBehavior_WORKITEM_NUMBER_ATTRIBUTE_NOT_CONFIGURATION, typeName),
				new String[] { IDialogConstants.OK_LABEL }, MessageDialog.ERROR);

		return false;
	}

}
