import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ummisco.gama.ui.navigator.contents.WrappedGamaFile;

public class JobUMLConverterLight extends WorkspaceJob 
{
	WrappedGamaFile modelFile;
	String directory;
	public JobUMLConverterLight(WrappedGamaFile modelFile, String directory) {
		super(IParser.JOB_NAME_TO_UML);
		this.modelFile=modelFile;
		this.directory=directory;
	}

	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		GamlToUMLConverter gamlConverter = new GamlToUMLConverter(this.modelFile,true);
		gamlConverter.save(directory+"/"+this.modelFile.getName()+".uml");
		gamlConverter.dispose();
		gamlConverter=null;
		return Status.OK_STATUS;
	}
	
}
