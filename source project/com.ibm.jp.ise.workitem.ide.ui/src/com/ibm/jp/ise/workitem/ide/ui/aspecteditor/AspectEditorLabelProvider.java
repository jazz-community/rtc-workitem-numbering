package com.ibm.jp.ise.workitem.ide.ui.aspecteditor;


import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.type.TypeCategory.CustomAttribute;
import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.type.TypeCategory.Type;
import com.ibm.team.jface.JazzResources;
import com.ibm.team.process.ide.ui.ProcessAspect;

import com.ibm.team.workitem.ide.ui.internal.ImagePool;
import com.ibm.team.workitem.ide.ui.internal.aspecteditor.AspectEditorUtil;

/**
 * LabelProvider Implementation
 */
@SuppressWarnings("restriction")
public class AspectEditorLabelProvider extends LabelProvider {

	private ProcessAspect fAspect;
	private ResourceManager fResourceManager;

	/**
	 * Constructor
	 * 
	 * @param aspect
	 *            ProcessAspect
	 * @param resourceManager
	 *            ResourceManager
	 */
	public AspectEditorLabelProvider(ProcessAspect aspect, ResourceManager resourceManager) {
		this.fAspect = aspect;
		this.fResourceManager = resourceManager;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ModeledElement) {
			return getModeledElementLabel((ModeledElement) element);
		}
		return element.toString();
	}

	@Override
	public Image getImage(final Object element) {
		if (element instanceof Type) {
			Type type = (Type) element;
			return getImage(type, type.getIcon());
		}
		if (element instanceof CustomAttribute) {
			return JazzResources.getImageWithDefault(this.fResourceManager, ImagePool.CUSTOM_ATTRIBUTE_ICON);
		}
		return null;
	}

	/**
	 * Get label of ModeledElement
	 * @param element ModeledElement
	 * @return label of ModeledElement
	 */
	private static String getModeledElementLabel(ModeledElement element) {
		return element.getName();
	}

	/**
	 * Get image
	 * @param element target Element
	 * @param iconUrl url of the image icon
	 * @return image
	 */
	private Image getImage(final Object element, String iconUrl) {
		if (iconUrl != null) {
			return AspectEditorUtil.getImage(this.fAspect.getProcessContainerWorkingCopy(), iconUrl, this.fResourceManager,
					new Runnable() {
						@Override
						public void run() {
							// blank block
						}
					});
		}
		return null;
	}
}
