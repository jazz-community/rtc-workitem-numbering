package com.ibm.jp.ise.workitem.ide.ui.aspecteditor;


import com.ibm.jp.ise.workitem.ide.ui.aspecteditor.editor.WorkItemNumberingAspectEditor;
import com.ibm.team.process.ide.ui.IProcessAspectEditorFactory;
import com.ibm.team.process.ide.ui.ProcessAspectEditor;

/**
 * Aspect Editor Factory
 */
public class WorkItemNumberingAspectEditorFactory implements IProcessAspectEditorFactory {

    /**
     * Create aspect editor
     * @see com.ibm.team.process.ide.ui.IProcessAspectEditorFactory#createProcessAspectEditor(java.lang.String)
     */
    @Override
	public ProcessAspectEditor createProcessAspectEditor(String processAspectId) {
            return new WorkItemNumberingAspectEditor();
    }

}
