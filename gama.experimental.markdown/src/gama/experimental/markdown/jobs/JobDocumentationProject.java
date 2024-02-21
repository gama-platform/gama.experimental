package gama.experimental.markdown.jobs;

import java.io.File;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import gama.experimental.markdown.markdownSyntactic.IParser;
import gama.ui.navigator.view.contents.WrappedGamaFile;
import gama.ui.navigator.view.contents.WrappedResource;
/**
 * 
 * @author damienphilippon
 * Date : 19 Dec 2017
 * Class used to do a Documentation job for a Project (will defined an Index of all the species and experiments of the
 * projects, and do the documentation of all the models contained in the project)
 */
public class JobDocumentationProject extends JobDocumentation{
	
	/**
	 * The project concerned by this job
	 */
	public IProject project;

	/**
	 * Constructor using a given project (will be saved in a default documentation folder )
	 * @param project {@code IProject} the project to document
	 */
	public JobDocumentationProject(IProject project)
	{
		super(project.getLocation().toOSString()+File.pathSeparator+IParser.PROJECT_DEFAULT_FOLDER);
		this.project= project;
	}
	/**
	 * Constructor using a given project (will be saved in a given documentation folder )
	 * @param project {@code IProject} the project to document
	 * @param path {@code String} the path (String) in which the documentation will be saved
	 */public JobDocumentationProject(IProject project, String path)
	{
		super(path);
		this.project= project;
	}
	
	
	
	 /**
	 * Method to run the job in background
	 */
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		if(project!=null)
		{
			WrappedResource file_to_convert = gama.ui.navigator.view.contents.ResourceManager.getInstance().findWrappedInstanceOf(project);
			Object[] childrenResources = file_to_convert.getNavigatorChildren().clone();
			for(Object aResource : childrenResources)
			{
				if(aResource instanceof WrappedResource)
				{
					if(((WrappedResource) aResource).countModels()>0)
					{
						generateForWrappedResource((WrappedResource) aResource);
					}
				}
			}
			childrenResources=null;
			generateIndex();
		}
		this.dispose();
		return Status.OK_STATUS;
	}
}
