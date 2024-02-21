package gama.experimental.markdown.jobs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import gama.ui.navigator.view.contents.WrappedGamaFile;

/**
 * 
 * @author damienphilippon
 * Date : 19 Dec 2017
 * Class used to do a Documentation job for a Model (build only the model, not its dependencies), should be used
 * after validation of the model semantics to update only the documentation of the edited model (but doesn't update
 * the index of the project in which the model is)
 */
public class JobDocumentationModelProject extends JobDocumentation{
	/**
	 * The file of the model
	 */
	public WrappedGamaFile file;
	
	/**
	 * Constructor using a given model file and a given output directory
	 * @param aFile {@code WrappedGamaFile} the file containing the model
	 * @param directory {@code String} the string representing the path for the outputs
	 */
	public JobDocumentationModelProject(WrappedGamaFile aFile, String directory) {
		super(directory);
		this.file = aFile;
	}
	
	/**
	 * Method to run the job in background
	 */
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		generateForWrappedGamaFile(this.file);
		return Status.OK_STATUS;
	}
	
	
}
