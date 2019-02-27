package com.ibm.jp.ise.workitem.ide.ui.aspecteditor;

/**
 * Model class for process configuration.
 */
public abstract class ModeledElement {

	/**
	 * Get identifier
	 * @return identifier
	 */
	public abstract String getIdentifier();
	
	/**
	 * Get name
	 * @return name
	 */
	public String getName() {
		return getIdentifier();
	}
}
