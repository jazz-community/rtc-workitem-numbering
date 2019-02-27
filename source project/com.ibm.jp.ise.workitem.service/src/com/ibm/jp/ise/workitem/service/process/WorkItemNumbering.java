package com.ibm.jp.ise.workitem.service.process;

import java.util.HashMap;
import java.util.Map;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

import com.ibm.jp.ise.workitem.common.IWorkitemNumberingDefinisions;
import com.ibm.team.process.common.IProcessConfigurationElement;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.process.common.advice.AdvisableOperation;
import com.ibm.team.process.common.advice.runtime.IOperationParticipant;
import com.ibm.team.process.common.advice.runtime.IParticipantInfoCollector;
import com.ibm.team.repository.common.IAuditable;
import com.ibm.team.repository.common.IExtensibleItem;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.service.AbstractService;
import com.ibm.team.repository.service.IRepositoryItemService;
import com.ibm.team.workitem.common.IAuditableCommon;
import com.ibm.team.workitem.common.IQueryCommon;
import com.ibm.team.workitem.common.ISaveParameter;
import com.ibm.team.workitem.common.expression.AttributeExpression;
import com.ibm.team.workitem.common.expression.IQueryableAttribute;
import com.ibm.team.workitem.common.expression.IQueryableAttributeFactory;
import com.ibm.team.workitem.common.expression.QueryableAttributes;
import com.ibm.team.workitem.common.expression.SelectClause;
import com.ibm.team.workitem.common.expression.SortCriteria;
import com.ibm.team.workitem.common.expression.Statement;
import com.ibm.team.workitem.common.expression.Term;
import com.ibm.team.workitem.common.expression.Term.Operator;
import com.ibm.team.workitem.common.model.AttributeOperation;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.common.model.ItemProfile;
import com.ibm.team.workitem.common.query.IQueryResult;
import com.ibm.team.workitem.common.query.IResolvedResult;
import com.ibm.team.workitem.service.IAuditableServer;
import com.ibm.team.workitem.service.IQueryServer;
import com.ibm.team.workitem.service.IWorkItemServer;

/**
 * Workitem Numbering follow-up action
 */
public class WorkItemNumbering extends AbstractService implements IOperationParticipant {
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String SLASH_STRING = "/"; //$NON-NLS-1$
	/** Logger */
	public static Log fLogger = LogFactory.getLog(WorkItemNumbering.class);
	private static Map<String, Integer> cachedWorkItemNumber;
	static {
		cachedWorkItemNumber = new HashMap<String, Integer>();
	}

	/**
	 * Main method of workitem numbering follow-up action
	 * 
	 * @see com.ibm.team.process.common.advice.runtime.IOperationParticipant#run(com.ibm.team.process.common.advice.AdvisableOperation,
	 *      com.ibm.team.process.common.IProcessConfigurationElement,
	 *      com.ibm.team.process.common.advice.runtime.IParticipantInfoCollector,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(AdvisableOperation operation, IProcessConfigurationElement participantConfig,
			IParticipantInfoCollector collector, IProgressMonitor monitor) throws TeamRepositoryException {
		Object data = operation.getOperationData();
		if (data instanceof ISaveParameter) {
			ISaveParameter saveParameter = (ISaveParameter) data;
			IProjectAreaHandle projectAreaHandle = operation.getProcessArea().getProjectArea();
			workitemNumber(saveParameter, projectAreaHandle, participantConfig, monitor);
		}
	}

	/**
	 * Number workitem number
	 * 
	 * @param saveParameter Save parameter for workitem
	 * @param projectAreaHandle Project Area Handle
	 * @param participantConfig ProcessConfiguration
	 * @param monitor Monitor
	 * @throws TeamRepositoryException
	 */
	private void workitemNumber(ISaveParameter saveParameter, IProjectAreaHandle projectAreaHandle,
			IProcessConfigurationElement participantConfig, IProgressMonitor monitor) throws TeamRepositoryException {
		IAuditable newAuditable = saveParameter.getNewState();
		if (newAuditable instanceof IWorkItem) {
			IWorkItem newWorkItem = (IWorkItem) newAuditable;
			IWorkItemServer workItemServer = getWorkItemServer();
			if (isNumberingTargetWorkItem(participantConfig, newWorkItem)) {
				IRepositoryItemService itemService = getRepositoryItemService();
				IProjectArea projectArea = (IProjectArea) itemService.fetchItem(projectAreaHandle, null);
				IAttribute workItemNumberAttribute = workItemServer.findAttribute(projectArea,
						Settings.WORKITEM_NUMBER_ATTRIBUTE_KEY, monitor);
				if (workItemNumberAttribute == null) {
					throw new TeamRepositoryException(Messages.WORKITEM_NUMBER_ATTRIBUTE_NOT_FOUND);
				}

				boolean isWorkItemTypeChanged = false;
				isWorkItemTypeChanged = isWorkItemTypeChanged(saveParameter, newWorkItem, workItemServer, monitor);
				try {
					if (!isWorkItemTypeChanged && newWorkItem.getValue(workItemNumberAttribute) != null
							&& !newWorkItem.getValue(workItemNumberAttribute).equals(EMPTY_STRING)) {
						return;
					}
				} catch(AssertionFailedException e) {
					throw new TeamRepositoryException(Messages.WORKITEM_NUMBER_ATTRIBUTE_NOT_CONFIGURATION);
				}
				int newWorkitemNumber = getNewWorkItemNumber(participantConfig, projectArea, newWorkItem,
						workItemNumberAttribute, monitor);
				String numberOfDigit = getNumberOfDigit(participantConfig);
				IWorkItem workitemCopy = (IWorkItem) workItemServer.getAuditableCommon()
						.resolveAuditable(newWorkItem, IWorkItem.FULL_PROFILE, monitor).getWorkingCopy();
				workitemCopy.setValue(workItemNumberAttribute, String.format(
						NLS.bind(Messages.FORMAT_NUMBER_OF_DIGIT, numberOfDigit), new Integer(newWorkitemNumber)));

				workItemServer.saveWorkItem2(workitemCopy, null, null);
			}
		}
	}

	/**
	 * Judge if workitem type is changed
	 * 
	 * @param saveParameter Save parameter for workitem
	 * @param newWorkItem Workitem after saving
	 * @param workItemServer Workitem service
	 * @param monitor Monitor
	 * @return result
	 * @throws TeamRepositoryException
	 */
	private static boolean isWorkItemTypeChanged(ISaveParameter saveParameter, IWorkItem newWorkItem,
			IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
		boolean isWorkItemTypeChanged = false;
		IAuditable oldAuditable = saveParameter.getOldState();
		if (oldAuditable instanceof IWorkItem) {
			IWorkItem oldWorkItem = (IWorkItem) oldAuditable;
			IWorkItemType oldWorkItemType = workItemServer.findWorkItemType(oldWorkItem.getProjectArea(),
					oldWorkItem.getWorkItemType(), monitor);
			isWorkItemTypeChanged = oldWorkItemType != null
					&& !oldWorkItemType.getIdentifier().equals(newWorkItem.getWorkItemType());
		}
		return isWorkItemTypeChanged;
	}

	/**
	 * Get new workitem number
	 * 
	 * @param participantConfig Process configuration
	 * @param projectArea Project Area
	 * @param workItem Workitem
	 * @param workItemNumberAttribute Workitem number attribute
	 * @param monitor Monitor
	 * @return New workitem number
	 * @throws TeamRepositoryException
	 */
	private int getNewWorkItemNumber(IProcessConfigurationElement participantConfig, IProjectArea projectArea,
			IWorkItem workItem, IAttribute workItemNumberAttribute, IProgressMonitor monitor)
			throws TeamRepositoryException {

		synchronized (cachedWorkItemNumber) {
			int newWorkItemNumber = 1;
			if (isExistCache(participantConfig, projectArea, workItem, monitor)) {
				newWorkItemNumber = getCachedWorkItemNumber(participantConfig, projectArea, workItem, monitor) + 1;
			} else {
				IQueryableAttribute projectAreaAttribute = findAttribute(projectArea, IWorkItem.PROJECT_AREA_PROPERTY,
						monitor);
				AttributeExpression projectAreaExpression = new AttributeExpression(projectAreaAttribute,
						AttributeOperation.EQUALS, projectArea);

				Term term = new Term(Operator.AND);
				term.add(projectAreaExpression);

				IQueryableAttribute workItemTypeQueryableAttribute = findAttribute(projectArea, IWorkItem.TYPE_PROPERTY, monitor);
				AttributeExpression workItemTypeExpression = new AttributeExpression(workItemTypeQueryableAttribute,
						AttributeOperation.EQUALS, workItem.getWorkItemType());
				term.add(workItemTypeExpression);
				IQueryableAttribute workItemNumberQueryableAttribute = findAttribute(projectArea, Settings.WORKITEM_NUMBER_ATTRIBUTE_KEY, monitor);
				AttributeExpression workItemNumberExpression = new AttributeExpression(workItemNumberQueryableAttribute,
						AttributeOperation.NOT_EQUALS, EMPTY_STRING);
				term.add(workItemNumberExpression);
				IQueryableAttribute workItemIdQueryableAttribute = findAttribute(projectArea, IWorkItem.ID_PROPERTY, monitor);
				AttributeExpression workItemIdExpression = new AttributeExpression(workItemIdQueryableAttribute,
						AttributeOperation.NOT_EQUALS, new Integer(workItem.getId()));
				term.add(workItemIdExpression);
				addCustomAttributeTerm(participantConfig, projectArea, workItem, term, monitor);
				Statement statement = new Statement(new SelectClause(), term, new SortCriteria[] { new SortCriteria(
						IWorkItem.ID_PROPERTY, false) });

				IQueryCommon queryService = getQueryCommon();
				ItemProfile<IWorkItem> profile = getProfile();
				IQueryResult<IResolvedResult<IWorkItem>> result = queryService.getResolvedExpressionResults(
						projectArea, statement, profile);
				result.setLimit(1);

				if (result.getResultSize(monitor).getTotal() > 0) {
					IWorkItem resultWorkItem = result.next(monitor).getItem();
					String workItemNumber;
					try {
						workItemNumber = (String) resultWorkItem.getValue(workItemNumberAttribute);
						newWorkItemNumber = Integer.parseInt(workItemNumber) + 1;

						double numberOfDigit = Double.parseDouble(getNumberOfDigit(participantConfig));
						double maxWorkItemNumber = Math.pow(10D, numberOfDigit) - 1;
						if (maxWorkItemNumber <= newWorkItemNumber) {
							newWorkItemNumber = 1;
						}
					} catch (NumberFormatException e) {
						throw new TeamRepositoryException(NLS.bind(Messages.LAST_WORKITEM_NUMBER_NOT_NUMERICAL, new Integer(resultWorkItem.getId())));
					}
				} else {
					newWorkItemNumber = 1;
				}
			}
			cachedWorkItemNumber.put(getCacheKey(participantConfig, projectArea, workItem, monitor), new Integer(
					newWorkItemNumber));

			return newWorkItemNumber;

		}
	}

	/**
	 * Add custom attribute as workitem query condition
	 * 
	 * @param participantConfig Process configuration
	 * @param projectArea Project area
	 * @param workItem Workitem
	 * @param term Workitem query condition
	 * @param monitor Monitor
	 * @throws TeamRepositoryException
	 */
	private void addCustomAttributeTerm(IProcessConfigurationElement participantConfig, IProjectArea projectArea,
			IWorkItem workItem, Term term, IProgressMonitor monitor) throws TeamRepositoryException {
		IProcessConfigurationElement types = getChild(participantConfig, IWorkitemNumberingDefinisions.TYPES);
		if (types != null) {
			for (IProcessConfigurationElement type : types.getChildren()) {

				String typeId = type.getAttribute(IWorkitemNumberingDefinisions.ID);

				if (workItem.getWorkItemType().equals(typeId)) {
					for (IProcessConfigurationElement attribute : type.getChildren()) {
						String attributeId = attribute.getAttribute(IWorkitemNumberingDefinisions.ID);
						IAttribute customAttribute = getWorkItemServer().findAttribute(projectArea, attributeId,
								monitor);
						Object customAttributeValue = workItem.getValue(customAttribute);
						IQueryableAttribute querableCustomAttribute = findAttribute(projectArea, attributeId, monitor);
						AttributeExpression customAttributeExpression = new AttributeExpression(
								querableCustomAttribute, AttributeOperation.EQUALS, customAttributeValue);
						term.add(customAttributeExpression);
					}
					return;
				}
			}
		}
	}

	/**
	 * Get final workitem number from cache
	 * 
	 * @param participantConfig Process configuration
	 * @param projectArea Project Area
	 * @param workItem Workitem
	 * @param monitor Monitor
	 * @return Final workitem number
	 * @throws TeamRepositoryException
	 */
	private int getCachedWorkItemNumber(IProcessConfigurationElement participantConfig, IProjectArea projectArea,
			IWorkItem workItem, IProgressMonitor monitor) throws TeamRepositoryException {

		String cacheKey = getCacheKey(participantConfig, projectArea, workItem, monitor);
		return cachedWorkItemNumber.get(cacheKey).intValue();
	}

	/**
	 * Judge if workitem number cache exists
	 * 繝ｯ繝ｼ繧ｯ繧｢繧､繝�Β逡ｪ蜿ｷ縺ｮ繧ｭ繝｣繝�す繝･縺悟ｭ伜惠縺吶ｋ縺句愛螳壹☆繧�
	 * 
	 * @param participantConfig Process configuration
	 * @param projectArea Project area
	 * @param workItem Workitem
	 * @param monitor Monitor
	 * @return result
	 * @throws TeamRepositoryException
	 */
	private boolean isExistCache(IProcessConfigurationElement participantConfig, IProjectArea projectArea,
			IWorkItem workItem, IProgressMonitor monitor) throws TeamRepositoryException {
		String cacheKey = getCacheKey(participantConfig, projectArea, workItem, monitor);
		return cachedWorkItemNumber.containsKey(cacheKey.toString());
	}

	/**
	 * Get key of cache
	 * 
	 * @param participantConfig Process configuration
	 * @param projectArea Project area
	 * @param workItem Workitem
	 * @param monitor Monitor
	 * @return Key of cache
	 * @throws TeamRepositoryException
	 */
	private String getCacheKey(IProcessConfigurationElement participantConfig, IProjectArea projectArea,
			IWorkItem workItem, IProgressMonitor monitor) throws TeamRepositoryException {
		StringBuffer cacheKey = new StringBuffer(projectArea.getItemId().toString());
		cacheKey.append(SLASH_STRING);
		cacheKey.append(workItem.getWorkItemType());
		IProcessConfigurationElement types = getChild(participantConfig, IWorkitemNumberingDefinisions.TYPES);
		if (types != null) {
			for (IProcessConfigurationElement type : types.getChildren()) {

				String typeId = type.getAttribute(IWorkitemNumberingDefinisions.ID);
				if (workItem.getWorkItemType().equals(typeId)) {
					for (IProcessConfigurationElement attribute : type.getChildren()) {
						String attributeId = attribute.getAttribute(IWorkitemNumberingDefinisions.ID);
						IAttribute customAttribute = getWorkItemServer().findAttribute(projectArea, attributeId,
								monitor);
						Object customAttributeValue = workItem.getValue(customAttribute);
						cacheKey.append(SLASH_STRING);
						cacheKey.append(customAttributeValue);
					}
					break;
				}
			}
		}
		return cacheKey.toString();
	}

	/**
	 * Find queryable attribute
	 * 
	 * @param projectArea Project area
	 * @param attributeId Attribute id
	 * @param monitor Monitor
	 * @return Queryable attribute
	 * @throws TeamRepositoryException
	 */
	private IQueryableAttribute findAttribute(IProjectAreaHandle projectArea, String attributeId,
			IProgressMonitor monitor) throws TeamRepositoryException {
		IQueryableAttributeFactory factory = QueryableAttributes.getFactory(IWorkItem.ITEM_TYPE);
		return factory.findAttribute(projectArea, attributeId, getAuditableCommon(), monitor);
	}

	/**
	 * Judge if workitem is numbering target
	 * 
	 * @param participantConfig Process configuration
	 * @param workItem Workitem
	 * @return result
	 */
	private static boolean isNumberingTargetWorkItem(IProcessConfigurationElement participantConfig, IWorkItem workItem) {

		IProcessConfigurationElement types = getChild(participantConfig, IWorkitemNumberingDefinisions.TYPES);
		if (types != null) {
			for (IProcessConfigurationElement type : types.getChildren()) {

				String typeId = type.getAttribute(IWorkitemNumberingDefinisions.ID);

				if (workItem.getWorkItemType().equals(typeId)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get number of digit
	 * @param participantConfig Process configuration
	 * @return Number of digit
	 */
	private static String getNumberOfDigit(IProcessConfigurationElement participantConfig) {
		IProcessConfigurationElement options = getChild(participantConfig, IWorkitemNumberingDefinisions.OPTIONS);
		String numberOfDigit = options.getAttribute(IWorkitemNumberingDefinisions.NUMBER_OF_DIGIT);
		return numberOfDigit;
	}

	/**
	 * Get child element
	 * @param element Process configuration
	 * @param name  Name of element
	 * @return Element of process configuration
	 */
	private static IProcessConfigurationElement getChild(IProcessConfigurationElement element, String name) {
		for (IProcessConfigurationElement cfg : element.getChildren()) {
			if (cfg.getName().equals(name))
				return cfg;
		}
		return null;
	}

	/**
	 * Get profile for workitem
	 * 
	 * @return Profile for workitem
	 */
	private static ItemProfile<IWorkItem> getProfile() {
		return IWorkItem.SMALL_PROFILE.createExtension(IWorkItem.CUSTOM_ATTRIBUTES_PROPERTY,
				IExtensibleItem.TIMESTAMP_EXTENSIONS_QUERY_PROPERTY);
	}

	/**
	 * Get workitem service
	 * 
	 * @return IWorkItemServer
	 */
	private IWorkItemServer getWorkItemServer() {
		return getService(IWorkItemServer.class);
	}

	/**
	 * Get Auditable service
	 * 
	 * @return IAuditableCommon
	 */
	private IAuditableCommon getAuditableCommon() {
		return getService(IAuditableServer.class);
	}

	/**
	 * Get Query service
	 * 
	 * @return IQueryCommon
	 */
	private IQueryCommon getQueryCommon() {
		return getService(IQueryServer.class);
	}

	/**
	 * Get RepositoryItem service
	 * 
	 * @return IRepositoryItemService
	 */
	private IRepositoryItemService getRepositoryItemService() {
		return getService(IRepositoryItemService.class);
	}

}
