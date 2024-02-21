package gama.experimental.markdown.markdownSyntactic;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.experimental.markdown.jobs.JobDocumentation;
import gama.experimental.markdown.jobs.JobDocumentationModelIndependent;
import gama.experimental.markdown.jobs.JobDocumentationProject;
import gama.ui.navigator.view.contents.WrappedGamaFile;
import gama.ui.navigator.view.contents.WrappedProject;
import gama.ui.navigator.view.contents.WrappedResource;

/**
 * 
 * @author damienphilippon
 * Date : 19 Dec 2017
 * Class used go handle the command
 */
public class GenerateMDHandler extends AbstractHandler {
	/**
	 * The job that will be executed 
	 */
	JobDocumentation myJob;
	
	/**
	 * Method that will execute the job (will ask for a location)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		
		//Define the output directory
		DirectoryDialog directorySelector = new DirectoryDialog(Display.getCurrent().getActiveShell());
		directorySelector.setMessage("Select a directory as a location for documentation");
		directorySelector.setText("Directory for Documentation");
		
		
		if (selection != null & selection instanceof IStructuredSelection) {
           
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
            Object o = strucSelection.getFirstElement();
            if(o instanceof WrappedResource)
            {
            	WrappedResource file_to_convert = (WrappedResource) o;
                 
            	//If the selected object is a project, then creates a Job for project
         		if(o instanceof WrappedProject)
                 {
                 	//Do for a project
                    directorySelector.setFilterPath(file_to_convert.getProject().getResource().getLocation().toOSString());
             		String selectedDirectoryName = directorySelector.open();
                 	myJob = new JobDocumentationProject(((WrappedProject) o).getResource(), selectedDirectoryName);
             		myJob.schedule();
                 }
                 else
                 {
                	//If the selected object is a GamaFile, then creates a Job for Independent Model (with includes, index etc.)
                 	if(o instanceof WrappedGamaFile)
                 	{
                        directorySelector.setFilterPath(file_to_convert.getResource().getLocation().toOSString());
                 		String selectedDirectoryName = directorySelector.open();
                 		//Do for a model
                 		myJob = new JobDocumentationModelIndependent(((WrappedGamaFile) o), selectedDirectoryName);
                 		myJob.schedule();
                 	}
                 }
            }
           
    	}
		
		return null;
	}
}
