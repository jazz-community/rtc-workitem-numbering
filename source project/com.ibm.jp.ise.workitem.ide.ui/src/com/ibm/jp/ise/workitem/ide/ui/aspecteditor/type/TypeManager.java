package com.ibm.jp.ise.workitem.ide.ui.aspecteditor.type;

import java.util.ArrayList;
import java.util.List;


import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.type.TypeCategory.CustomAttribute;
import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.type.TypeCategory.Type;
import com.ibm.team.process.common.ModelElement;

/**
 * Workitem type
 */
public class TypeManager {
	
	/** Process configuration id */
	public static final String CONFIG_ID = "com.ibm.team.workitem.configuration.workItemTypes"; //$NON-NLS-1$

	/** id attribute */
	public static final String ID= "id"; //$NON-NLS-1$

	/** category attribute */
	public static final String CATEGORY= "category"; //$NON-NLS-1$

	/** name attribute */
	public static final String NAME= "name"; //$NON-NLS-1$

	/** icon attribute */
	public static final String ICON= "icon"; //$NON-NLS-1$

	/** type element */
	public static final String TYPE= "type"; //$NON-NLS-1$

	/** customAttributes element */
	private static final String CUSTOMATTRIBUTES= "customAttributes"; //$NON-NLS-1$

	/** customAttributes element */
	private static final String CUSTOMATTRIBUTE= "customAttribute"; //$NON-NLS-1$

	/** attributeDefinitions element */
	private static final String ATTRIBUTE_DEFINITIONS = "attributeDefinitions"; //$NON-NLS-1$

	/** attributeDefinition element */
	static final String ATTRIBUTE_DEFINITION = "attributeDefinition"; //$NON-NLS-1$
		
	/**
	 * Read workitem type category
	 * @param configurationElement Model for process configuration
	 * @return List of workitem type category
	 */
	@SuppressWarnings("unchecked")
	public static List<TypeCategory> readTypeCategories(ModelElement configurationElement) {
		List<TypeCategory> typeCategories= new ArrayList<TypeCategory>();
		List<Type> sortedTypes= new ArrayList<Type>();
		List<CustomAttribute> attributeDefinitions= new ArrayList<CustomAttribute>();
		if (configurationElement != null) {
			for (ModelElement element: (List<ModelElement>)configurationElement.getChildElements()) {
				// read attribute definitions
				if (ATTRIBUTE_DEFINITIONS.equals(element.getName())) {
					if (element.getChildElements() != null) {
						for (ModelElement e: (List<ModelElement>)element.getChildElements()) {
							if (ATTRIBUTE_DEFINITION.equals(e.getName())) {
								readAttributeDefinition(e, attributeDefinitions);
							}
						}
					}
				}
			}
			
			for (ModelElement element: (List<ModelElement>)configurationElement.getChildElements()) {
				if (TYPE.equals(element.getName())) {
					readType(element, typeCategories, sortedTypes);
				} else if (CUSTOMATTRIBUTES.equals(element.getName())) {
					readCustomAttributes(element, typeCategories, attributeDefinitions);
				}
			}
		}
		return typeCategories;
	}
	
	/**
	 * Factory interface to create a new "type" instace since there are multiple places
	 * that want to parse a type from the XML, but may want to instantiate different classes from it.
	 * Pass to {@link TypeManager#readTypeElement(ModelElement, TypeFactory)}.
	 * <p>
	 * Feels like overkill. Could the same "Type" classes be shared instead? TypeCategory.Type == EditorIdBindingPart.Type?
	 * @param <T> the class of "type" objects this returns.
	 */
	public static interface TypeFactory<T> {
		/**
		 * Create workitem type
		 * @param id ID
		 * @param name Name
		 * @param iconUrl Icon URL
		 * @return Workitem type
		 */
		T newType(String id, String name, String iconUrl);
	}
	
	/**
	 * Read type element
	 * @param typeElement type element
	 * @param typeFactory Workitem type factory
	 * @return Workitem type
	 */
	public static <T> T readTypeElement(ModelElement typeElement, TypeFactory<T> typeFactory) {
		// read in type
		String id= typeElement.getAttribute(ID);
		String name= typeElement.getAttribute(NAME);
		String icon= typeElement.getAttribute(ICON);
		if ("".equals(icon)) { //$NON-NLS-1$
			icon= null;
		}
		
		return typeFactory.newType(id, name, icon);
	}
	
	/**
	 * Read type element
	 * @param element type element
	 * @param categories Workitem type category
	 * @param sortedTypes sorted list of workitem type
	 */
	private static void readType(ModelElement element, List<TypeCategory> categories, List<Type> sortedTypes) {
		String cat= element.getAttribute(CATEGORY);
		TypeCategory category= null;
		for (TypeCategory tc: categories) {
			if (tc.getIdentifier().equals(cat)) {
				category= tc;
				break;
			}
		}
		if (category == null) {
			category= new TypeCategory(cat);
			categories.add(category);
		}
		
		final TypeCategory finalCategory = category;
		final TypeFactory<Type> typeFactory = new TypeFactory<Type>() {
			/**
			 * @see com.ibm.jp.ise.workitem.ide.ui.aspecteditor.type.TypeManager.TypeFactory#newType(java.lang.String, java.lang.String, java.lang.String)
			 */
			@Override
			public Type newType(String id, String name, String iconUrl) {
				return finalCategory.new Type(id, name, iconUrl);
			}
		};
		
		final Type type = readTypeElement(element, typeFactory);
		category.addType(type);
		sortedTypes.add(type);
	}
	
	/**
	 * Read attributeDefinition element
	 * @param element attributeDefinition element
	 * @param attributeDefinitions List of attribute definition
	 */
	private static void readAttributeDefinition(ModelElement element, List<CustomAttribute> attributeDefinitions) {
		String id= element.getAttribute(ID);
		String name= element.getAttribute(NAME);
		String type= element.getAttribute(TYPE);
		attributeDefinitions.add(new CustomAttribute(id, name, type));
	}
	
	/**
	 * Read customAttribute element
	 * @param element customAttributes element
	 * @param categories Workitem type category
	 * @param attributeDefinitions List of attribute definition
	 */
	@SuppressWarnings("null")
	private static void readCustomAttributes(ModelElement element, List<TypeCategory> categories, List<CustomAttribute> attributeDefinitions) {
		String cat= element.getAttribute(CATEGORY);
		TypeCategory category= null;
		for (TypeCategory tc: categories) {
			if (tc.getIdentifier().equals(cat)) {
				category= tc;
				break;
			}
		}
		if (element.getChildElements() != null) {
			for (ModelElement e: (List<ModelElement>)element.getChildElements()) {
				if (CUSTOMATTRIBUTE.equals(e.getName())) {
					//read in custom attribute
					String id= e.getAttribute(ID);
					CustomAttribute attr= findExistingAttribute(attributeDefinitions, id);
					if (attr != null) category.addCustomAttribute(attr);
				}
			}
		}
	}
	
	/**
	 * Find existing attribute
	 * @param attributeDefinitions List of attribute definition
	 * @param id ID
	 * @return result
	 */
	public static CustomAttribute findExistingAttribute(List<CustomAttribute> attributeDefinitions, String id) {
		for (CustomAttribute c: attributeDefinitions) {
			if (c.getId().equals(id)) {
				return c;
			}
		}
		return null;
	}
	
}
