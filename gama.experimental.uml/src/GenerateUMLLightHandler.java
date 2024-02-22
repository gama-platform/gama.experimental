import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.ui.navigator.view.contents.WrappedGamaFile;
import gama.ui.navigator.view.contents.WrappedResource;


public class GenerateUMLLightHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		
		//Define the output directory
		DirectoryDialog directorySelector = new DirectoryDialog(Display.getCurrent().getActiveShell());
		directorySelector.setMessage("Select a directory as a location for Class Diagram");
		directorySelector.setText("Directory for Class Diagram");
		
		
		if (selection != null & selection instanceof IStructuredSelection) {
           
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
            Object o = strucSelection.getFirstElement();
            if(o instanceof WrappedResource)
            {
            	WrappedResource file_to_convert = (WrappedResource) o;
                 
            	if(o instanceof WrappedGamaFile)
             	{
                    directorySelector.setFilterPath(file_to_convert.getResource().getLocation().toOSString());
             		String selectedDirectoryName = directorySelector.open();
             		//Do for a model
             		JobUMLConverterLight myJob = new JobUMLConverterLight(((WrappedGamaFile) o), selectedDirectoryName);
             		myJob.schedule();
             	}
            }
           
    	}
		
		return null;
	}

	

}
