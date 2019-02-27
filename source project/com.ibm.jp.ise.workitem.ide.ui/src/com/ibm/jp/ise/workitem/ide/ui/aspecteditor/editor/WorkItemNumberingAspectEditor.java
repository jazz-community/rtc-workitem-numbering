package com.ibm.jp.ise.workitem.ide.ui.aspecteditor.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.jp.ise.workitem.common.IWorkitemNumberingDefinisions;
import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.AspectEditorLabelProvider;
import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.ModeledElement;
import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.Settings;
import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.type.TypeCategory;
import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.type.TypeManager;
import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.type.TypeCategory.CustomAttribute;
import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.type.TypeCategory.Type;
import com.ibm.jp.ise.workitem.ide.ui.util.DefaultCheckAttributeBehavior;
import com.ibm.team.foundation.common.util.IMemento;

import com.ibm.team.process.common.IProcessItem;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.process.common.ModelElement;
import com.ibm.team.process.ide.ui.IProcessAspectEditorSite;
import com.ibm.team.process.ide.ui.OperationDetailsAspectEditor;
import com.ibm.team.process.ide.ui.ProcessAspect;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IQueryClient;
import com.ibm.team.workitem.common.internal.query.QueryCommon;
import com.ibm.team.workitem.common.internal.query.presentations.QueryEditorPresentation;
import com.ibm.team.workitem.common.query.IQueryType;
import com.ibm.team.workitem.common.query.QueryTypeRegistry;
import com.ibm.team.workitem.common.query.QueryTypes;

/**
 * Aspect editor
 */
@SuppressWarnings("restriction")
public class WorkItemNumberingAspectEditor extends OperationDetailsAspectEditor {
	private static final String COMMA_STRING = ","; //$NON-NLS-1$

	/**
	 * Tree Element
	 */
	public static class TreeElement {

		/**
		 * Parent Element
		 */
		private final TreeElement fParent;

		/**
		 * Element
		 */
		private final Object fElement;

		/**
		 * List of child elements
		 */
		private final List<TreeElement> fChildren;

		/**
		 * Constructor
		 * 
		 * @param parent Parent Element
		 * @param element Element
		 */
		public TreeElement(TreeElement parent, Object element) {
			this.fParent = parent;
			this.fElement = element;
			this.fChildren = new ArrayList<TreeElement>();
		}

		/**
		 * Judge if it has child elements
		 * 
		 * @return result
		 */
		public boolean hasChildren() {
			return !this.fChildren.isEmpty();
		}

		/**
		 * Add child element
		 * 
		 * @param child Child element
		 * @return Added element
		 */
		public TreeElement addChild(Object child) {
			TreeElement element = new TreeElement(this, child);
			this.fChildren.add(element);
			return element;
		}

		/**
		 * Get list of child elements
		 * 
		 * @return List of child elements
		 */
		public List<TreeElement> getChildren() {
			return this.fChildren;
		}

		/**
		 * Get element
		 * 
		 * @return Element
		 */
		public Object getElement() {
			return this.fElement;
		}

		/**
		 * Get parent element
		 * 
		 * @return Parent element
		 */
		public TreeElement getParent() {
			return this.fParent;
		}
	}

	/**
	 * Content Provider for TreeElement
	 */
	private static class TreeElementContentProvider implements ITreeContentProvider {

		/**
		 * Constructor
		 */
		public TreeElementContentProvider() {
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// blank block
		}

		/**
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
		 */
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof TreeElement)
				return ((TreeElement) inputElement).getChildren().toArray();
			return new Object[0];
		}

		/**
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof TreeElement)
				return ((TreeElement) parentElement).getChildren().toArray();
			return new Object[0];
		}

		/**
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		@Override
		public Object getParent(Object element) {
			if (element instanceof TreeElement)
				return ((TreeElement) element).getParent();
			return null;
		}

		/**
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof TreeElement)
				return ((TreeElement) element).hasChildren();
			return false;
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		@Override
		public void dispose() {
			// blank block
		}
	}

	/**
	 * Label Provider for TreeElement
	 */
	private static class TreeElementLabelProvider extends CellLabelProvider {

		/**
		 * LabelProvider
		 */
		private final ILabelProvider fLabelProvider;

		/**
		 * Constructor
		 * 
		 * @param labelProvider LabelProvider
		 */
		public TreeElementLabelProvider(ILabelProvider labelProvider) {
			this.fLabelProvider = labelProvider;
		}

		/**
		 * @see org.eclipse.jface.viewers.CellLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
		 */
		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			if (element instanceof TreeElement) {

				TreeElement treeElement = (TreeElement) element;
				Object realElement = treeElement.getElement();

				cell.setText(this.fLabelProvider.getText(realElement));
				cell.setImage(this.fLabelProvider.getImage(realElement));
			}
		}
	}

	/**
	 * Root of TreeElement
	 */
	private TreeElement fRoot;

	/**
	 * ResourceManager
	 */
	private ResourceManager fResourceManager = new LocalResourceManager(JFaceResources.getResources());

	/**
	 * Checkbox Tree Viewer
	 */
	private CheckboxTreeViewer fTreeViewer;

	/**
	 * List of TypeCategory
	 */
	private List<TypeCategory> fTypeCategories;

	/**
	 * Workitem Type for restoring
	 */
	private IMemento fWorkitemTypeToRestore;

	/**
	 * Tree
	 */
	Tree tree;

	/**
	 * Number of digit
	 */
	String fNumberOfDigit;

	/**
	 * Options for number of digit
	 */
	String[] fNumberOfDigitOptions;

	/**
	 * Combo box for number of digit
	 */
	Combo fNumberOfDigitCombo;

	/**
	 * Process configuration
	 */
	private IProcessItem fProcessItem;

	/**
	 * @see com.ibm.team.process.ide.ui.ProcessAspectEditor#createControl(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	public void createControl(Composite parent, FormToolkit toolkit) {
		// Add Combo box for number of digit
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parent);

		Label label = toolkit.createLabel(parent, Messages.WorkItemNumberingAspectEditor_NUMBER_OF_DIGIT);
		GridDataFactory.fillDefaults().applyTo(label);

		this.fNumberOfDigitCombo = new Combo(parent, SWT.DROP_DOWN);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(this.fNumberOfDigitCombo);
		this.fNumberOfDigitCombo.setItems(this.fNumberOfDigitOptions);
		this.fNumberOfDigitCombo.select(0);
		this.fNumberOfDigit = this.fNumberOfDigitOptions[0];
		this.fNumberOfDigitCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty();
				// Save number of digit
				WorkItemNumberingAspectEditor.this.fNumberOfDigit = WorkItemNumberingAspectEditor.this.fNumberOfDigitOptions[WorkItemNumberingAspectEditor.this.fNumberOfDigitCombo
						.getSelectionIndex()];
			}
		});

		label = toolkit.createLabel(parent, Messages.WorkItemNumberingAspectEditor_NUMBERING_WORKITEM_TYPES);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);

		this.tree = toolkit.createTree(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(this.tree);

		// Add checkbox tree
		CheckboxTreeViewer treeViewer = this.fTreeViewer = new CheckboxTreeViewer(this.tree);
		treeViewer.setContentProvider(new TreeElementContentProvider());
		treeViewer.setLabelProvider(new TreeElementLabelProvider(new AspectEditorLabelProvider(getAspect(),
				this.fResourceManager)));

		installTreeListeners(treeViewer);

		try {
			init();
		} catch (TeamRepositoryException e1) {
			throw new RuntimeException(e1);
		}
	}

	/**
	 * Initialize
	 * 
	 * @throws TeamRepositoryException
	 */
	private void init() throws TeamRepositoryException {
		this.fRoot = buildTree();
		this.fTreeViewer.setInput(this.fRoot);
		if (this.fWorkitemTypeToRestore != null)
			restore(this.fWorkitemTypeToRestore);
	}

	/**
	 * @see com.ibm.team.process.ide.ui.ProcessAspectEditor#init(com.ibm.team.process.ide.ui.IProcessAspectEditorSite,
	 *      com.ibm.team.process.ide.ui.ProcessAspect)
	 */
	@Override
	public void init(IProcessAspectEditorSite site, ProcessAspect inputAspect) {
		super.init(site, inputAspect);
		this.fProcessItem = getAspect().getProcessContainerWorkingCopy().getUnderlyingProcessItem();
		this.fNumberOfDigitOptions = Settings.WorkItemNumberingAspectEditor_NUMBER_OF_DIGIT_OPTIONS.split(COMMA_STRING);
	}

	/**
	 * Install listeners for checkbox tree
	 * 
	 * @param treeViewer CheckboxTreeViewer
	 */
	private void installTreeListeners(CheckboxTreeViewer treeViewer) {
		DefaultCheckAttributeBehavior.install(treeViewer.getTree(), getTypeCategories());
		treeViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				setDirty();
			}
		});
	}

	/**
	 * @see com.ibm.team.process.ide.ui.OperationDetailsAspectEditor#saveState(com.ibm.team.foundation.common.util.IMemento)
	 */
	@Override
	public boolean saveState(IMemento memento) {
		saveTargetWorkItemType(memento, this.fTreeViewer.getTree().getItems());

		IMemento options = memento.createChild(IWorkitemNumberingDefinisions.OPTIONS);
		options.putString(IWorkitemNumberingDefinisions.NUMBER_OF_DIGIT, this.fNumberOfDigit);

		return true;
	}

	/**
	 * Save target workitem type
	 * 
	 * @param memento Momento
	 * @param items TreeItems
	 */
	private static void saveTargetWorkItemType(IMemento memento, TreeItem[] items) {
		IMemento types = null;
		for (TreeItem item : items) {
			TreeElement element = (TreeElement) item.getData();
			if (item.getChecked()) {
				if (types == null)
					types = memento.createChild(IWorkitemNumberingDefinisions.TYPES);
				IMemento type = types.createChild(IWorkitemNumberingDefinisions.TYPE);
				saveTargetAttribute(type, item.getItems());
				if (element != null)
					addToMemento(type, element);
			}
		}
	}

	/**
	 * Save target attributes
	 * 
	 * @param type Memento
	 * @param items TreeItems
	 */
	private static void saveTargetAttribute(IMemento type, TreeItem[] items) {
		for (TreeItem item : items) {
			TreeElement element = (TreeElement) item.getData();
			if (element != null && item.getChecked()) {
				IMemento attribute = type.createChild(IWorkitemNumberingDefinisions.ATTRIBUTE);
				addToMemento(attribute, element);
			}
		}

	}

	/**
	 * Add id attribute to memento
	 * 
	 * @param memento Memento
	 * @param element TreeElement
	 */
	private static void addToMemento(IMemento memento, TreeElement element) {
		Object realElement = element.getElement();
		if (realElement instanceof Type) {
			memento.putString(IWorkitemNumberingDefinisions.ID, ((Type) realElement).getIdentifier());
		} else if (realElement instanceof CustomAttribute) {
			memento.putString(IWorkitemNumberingDefinisions.ID, ((CustomAttribute) realElement).getIdentifier());
		}
	}

	/**
	 * @see com.ibm.team.process.ide.ui.OperationDetailsAspectEditor#restoreState(com.ibm.team.foundation.common.util.IMemento)
	 */
	@Override
	public void restoreState(IMemento memento) {
		this.fWorkitemTypeToRestore = memento;
	}

	private void restore(IMemento memento) {

		IMemento types = memento.getChild(IWorkitemNumberingDefinisions.TYPES);
		restoreTree(types);

		IMemento options = memento.getChild(IWorkitemNumberingDefinisions.OPTIONS);
		if (options != null) {
			String numberOfDigit = options.getString(IWorkitemNumberingDefinisions.NUMBER_OF_DIGIT);
			this.fNumberOfDigitCombo.select(Arrays.asList(this.fNumberOfDigitOptions).indexOf(numberOfDigit));
			this.fNumberOfDigit = numberOfDigit;
		}
	}

	/**
	 * Restore tree from process configuration
	 * @param memento Memento
	 */
	private void restoreTree(IMemento memento) {

		List<TreeElement> checkedElements = new ArrayList<TreeElement>();
		if (memento != null) {

			IMemento[] types = memento.getChildren(IWorkitemNumberingDefinisions.TYPE);
			for (IMemento type : types) {

				String typeId = type.getString(IWorkitemNumberingDefinisions.ID);
				TreeElement typeElement = find(this.fRoot, typeId);
				if (typeElement != null)
					checkedElements.add(typeElement);
				for (IMemento attribute : type.getChildren(IWorkitemNumberingDefinisions.ATTRIBUTE)) {
					String attributeId = attribute.getString(IWorkitemNumberingDefinisions.ID);
					TreeElement attributeElement = findElement(typeId, attributeId);
					if (attributeElement != null)
						checkedElements.add(attributeElement);
				}

			}
		}

		this.fTreeViewer.setCheckedElements(checkedElements.toArray());
	}

	/**
	 * Find element from Tree
	 * @param type Workitem type
	 * @param attribute Attribute
	 * @return Find result
	 */
	private TreeElement findElement(String type, String attribute) {

		TreeElement typeElement = find(this.fRoot, type);
		return find(typeElement, attribute);
	}

	/**
	 * Find element from Tree with id
	 * @param root Tree
	 * @param id ID
	 * @return Find result
	 */
	private static TreeElement find(TreeElement root, String id) {
		if (root != null) {
			for (TreeElement element : root.getChildren()) {
				ModeledElement modeledElement = (ModeledElement) element.getElement();
				if (modeledElement.getIdentifier().equals(id))
					return element;
			}
		}
		return null;
	}

	/**
	 * Build tree
	 * @return Tree
	 * @throws TeamRepositoryException
	 */
	private TreeElement buildTree() throws TeamRepositoryException {
		TreeElement root = new TreeElement(null, null);
		QueryEditorPresentation presentation = findPresentation();
		for (TypeCategory typeCategory : getTypeCategories()) {
			List<CustomAttribute> attributes = typeCategory.getCustomAttributes();
			for (Type type : typeCategory.getTypes()) {
				TreeElement typeElement = root.addChild(type);
				for (CustomAttribute attribute : attributes) {
					if (isSupportedAttribute((IProjectAreaHandle) this.fProcessItem, attribute, presentation)) {
						typeElement.addChild(attribute);
					}
				}
			}
		}
		return root;
	}

	/**
	 * Judge if custom attribute is queryable
	 * @param projectArea Project Area
	 * @param attribute Custom attribute
	 * @param presentation Presentation for workitem query
	 * @return result
	 */
	private static boolean isSupportedAttribute(IProjectAreaHandle projectArea, CustomAttribute attribute,
			QueryEditorPresentation presentation) {
		if (attribute.getIdentifier().equals(Settings.WorkItemNumberingAspectEditor_WORKITEM_NUMBER_ATTRIBUTE_KEY))
			return false;
		List<String> supportedAttributeTypes = Arrays
				.asList(Settings.WorkItemNumberingAspectEditor_SUPPORTED_ATTRIBUTE_TYPE.split(COMMA_STRING));
		return supportedAttributeTypes.contains(attribute.getType());
	}

	/**
	 * Get workitem type category
	 * @return List of workitem type category
	 */
	private List<TypeCategory> getTypeCategories() {
		if (this.fTypeCategories == null) {
			ModelElement workItemTypes = getSite().getConfigurationData(TypeManager.CONFIG_ID);
			if (workItemTypes != null) {
				this.fTypeCategories = TypeManager.readTypeCategories(workItemTypes);
			} else {
				this.fTypeCategories = Collections.emptyList();
			}
		}
		return this.fTypeCategories;
	}

	/**
	 * Find presentation for workitem query
	 * @return Presentation for workitem query
	 * @throws TeamRepositoryException
	 */
	private QueryEditorPresentation findPresentation() throws TeamRepositoryException {
		IQueryType queryType = QueryTypeRegistry.getQueryType(QueryTypes.WORK_ITEM_QUERY);
		ITeamRepository repository = (ITeamRepository) this.fProcessItem.getOrigin();
		IQueryClient queryClient = (IQueryClient) repository.getClientLibrary(IQueryClient.class);
		return ((QueryCommon) queryClient).findConfiguration((IProjectAreaHandle) this.fProcessItem, queryType,
				new NullProgressMonitor());
	}

	/**
	 * @see com.ibm.team.process.ide.ui.ProcessAspectEditor#dispose()
	 */
	@Override
	public void dispose() {
		if (this.fResourceManager != null) {
			this.fResourceManager.dispose();
			this.fResourceManager = null;
		}
	}

}
