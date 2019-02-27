package com.ibm.jp.ise.workitem.ide.ui.aspecteditor.type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.osgi.util.NLS;

import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.ModeledElement;
import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.editor.Messages;

/**
 * Workitem Type
 */
public class TypeCategory extends ModeledElement {
	private final String fCategoryId;
	private final List<Type> fTypes;
	private final List<CustomAttribute> fCustomAttributes;
	/**
	 * Costructor
	 * 
	 * @param categoryId Workitem category id
	 */
	public TypeCategory(String categoryId) {
		this.fCategoryId = categoryId;
		this.fTypes = new ArrayList<Type>();
		this.fCustomAttributes = new ArrayList<CustomAttribute>();
	}

	/**
	 * Add workitem type to list
	 * 
	 * @param type Workitem type
	 */
	public void addType(Type type) {
		this.fTypes.add(type);
	}

	/**
	 * Get list of workitem type
	 * @return List of workitem type
	 */
	public List<Type> getTypes() {
		return this.fTypes;
	}

	/**
	 * Get list of custom attribute
	 * @return List of custom attribute
	 */
	public List<CustomAttribute> getCustomAttributes() {
		return this.fCustomAttributes;
	}

	/**
	 * Add custom attribute to list
	 * @param customAttribute List of custom attribute
	 */
	public void addCustomAttribute(CustomAttribute customAttribute) {
		this.fCustomAttributes.add(customAttribute);
	}

	/**
	 * @see com.ibm.jp.ise.workitem.ide.ui.aspecteditor.ModeledElement#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return this.fCategoryId;
	}

	/**
	 * @see com.ibm.jp.ise.workitem.ide.ui.aspecteditor.ModeledElement#getName()
	 */
	@Override
	public String getName() {
		String name = getTypeCategoryDisplayName(this);
		if (name == null) {
			return super.getName();
		}
		return name;
	}

	/**
	 * Get display name of workitem type category
	 * @param category Workitem type category
	 * @return Display name of workitem type category
	 */
	public static String getTypeCategoryDisplayName(TypeCategory category) {
		if (category == null || category.getTypes() == null || category.getTypes().isEmpty())
			return null;
		Iterator<Type> iter = category.getTypes().iterator();
		String list = iter.next().getName();
		while (iter.hasNext()) {
			list = NLS.bind(Messages.TypeCategory_LIST_OF_TYPES, list, iter.next().getName());
		}
		return NLS.bind(Messages.TypeCategory_CATEGORYID_LIST, list, category.getIdentifier());
	}

	/**
	 * Workitem type
	 */
	public class Type extends ModeledElement {
		private String fId;
		private String fName;
		private String fIcon;

		/**
		 * Constructor
		 * @param id ID
		 * @param name Name
		 * @param icon Icon
		 */
		public Type(String id, String name, String icon) {
			this.fId = id;
			this.fName = name;
			this.fIcon = icon;
		}

		/**
		 * Set id
		 * @param id  ID
		 */
		public void setId(String id) {
			this.fId = id;
		}

		/**
		 * Set name
		 * @param name Name
		 */
		public void setName(String name) {
			this.fName = name;
		}

		/**
		 * Set icon
		 * @param icon Icon
		 */
		public void setIcon(String icon) {
			this.fIcon = icon;
		}

		/**
		 * Get id
		 * @return ID
		 */
		public String getId() {
			return this.fId;
		}

		/**
		 * @see com.ibm.jp.ise.workitem.ide.ui.aspecteditor.ModeledElement#getName()
		 */
		@Override
		public String getName() {
			return this.fName;
		}

		/**
		 * Get icon
		 * @return icon
		 */
		public String getIcon() {
			return this.fIcon;
		}

		/**
		 * @see com.ibm.jp.ise.workitem.ide.ui.aspecteditor.ModeledElement#getIdentifier()
		 */
		@Override
		public String getIdentifier() {
			return getId();
		}

		/**
		 * Get workitem type category
		 * @return Workitem type category
		 */
		public TypeCategory getCategory() {
			return TypeCategory.this;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getName();
		}
	}

	/**
	 * Model for custom attribute
	 */
	public static class CustomAttribute extends ModeledElement {
		private String fId;
		private String fName;
		private String fType;

		/**
		 * Custom attribute
		 * @param id ID
		 * @param name Name
		 * @param type Attribute type
		 */
		public CustomAttribute(String id, String name, String type) {
			this.fId = id;
			this.fName = name;
			this.fType = type;
		}

		/**
		 * A quick helper method for debugging purposes to actually get a string
		 * representation of the object.
		 */
		@Override
		public String toString() {
			return "\nName: " + this.fName + "\nId: " + this.fId; //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * @see com.ibm.jp.ise.workitem.ide.ui.aspecteditor.ModeledElement#getIdentifier()
		 */
		@Override
		public String getIdentifier() {
			return getId();
		}

		/**
		 * Get id
		 * @return ID
		 */
		public String getId() {
			return this.fId;
		}

		/**
		 * @see com.ibm.jp.ise.workitem.ide.ui.aspecteditor.ModeledElement#getName()
		 */
		@Override
		public String getName() {
			return this.fName;
		}
		/**
		 * Get attribute type
		 * @return Attribute type
		 */
		public String getType() {
			return this.fType;
		}

		/**
		 * Set id
		 * @param id ID
		 */
		public void setId(String id) {
			this.fId = id;
		}

		/**
		 * Set name
		 * @param name Name
		 */
		public void setName(String name) {
			this.fName = name;
		}
		/**
		 * Set attribute type
		 * @param type Attribute type
		 */
		public void setType(String type) {
			this.fType = type;
		}

	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}
}
