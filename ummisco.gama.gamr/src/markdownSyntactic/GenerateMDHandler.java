package markdownSyntactic;

import java.util.Collection;

import javax.swing.JOptionPane;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import jobs.JobDocumentation;
import jobs.JobDocumentationModelIndependent;
import jobs.JobDocumentationProject;
import msi.gama.runtime.GAMA;
import msi.gama.util.file.GamlFileInfo;
import msi.gama.util.file.IGamaFileMetaData;
import ummisco.gama.ui.navigator.NavigatorLabelProvider;
import ummisco.gama.ui.navigator.contents.WrappedGamaFile;
import ummisco.gama.ui.navigator.contents.WrappedProject;
import ummisco.gama.ui.navigator.contents.WrappedResource;

/**
 * 
 * @author damienphilippon Date : 19 Dec 2017 Class used go handle the command
 */
public class GenerateMDHandler extends AbstractHandler {
	/**
	 * The job that will be executed
	 */
	JobDocumentation myJob;

	private FilteredResourcesSelectionDialog dialog;

	/**
	 * Method that will execute the job (will ask for a location)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveWorkbenchWindow(event).getShell();
		final IContainer p = ResourcesPlugin.getWorkspace().getRoot();
		dialog = new FilteredResourcesSelectionDialog(shell, false, p, Resource.FILE);
		dialog.setInitialPattern("*.gaml");
		dialog.setTitle("Choose a gaml model in project " + p.getName());
		if (dialog.open() == Window.OK) {
			final Object[] result = dialog.getResult();
			if (result.length == 1) {
				final IResource res = (IResource) result[0];
				System.out.println(res.getFullPath().toString());

				final IGamaFileMetaData data = GAMA.getGui().getMetaDataProvider().getMetaData(res, false, true);
				if (data != null && data instanceof GamlFileInfo) {
					String[] exp = (String[]) ((GamlFileInfo) data).getExperiments().toArray();
					LabelProvider ss = new LabelProvider();
					ElementListSelectionDialog l = new ElementListSelectionDialog(shell, ss);
					l.setMessage("Selection the experiment to be launch in GAMR");
					l.setElements(((GamlFileInfo) data).getExperiments().toArray());
					int rres = l.open();
					if (rres == Window.OK) {
						Object[] objs = l.getResult();
						System.out.println(objs[0]);
					}
					return null;
					// String input = (String) JOptionPane.showInputDialog(null, "Choose now...",
					// "The Choice of a Lifetime", JOptionPane.QUESTION_MESSAGE, null, // Use
					// // default
					// // icon
					// exp, // Array of choices
					// exp[0]); // Initial choice
					// System.out.println(input);
				}

				// modelChooser.setText(res.getFullPath().toString());
			}
		}
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();

		// Define the output directory
		DirectoryDialog directorySelector = new DirectoryDialog(Display.getCurrent().getActiveShell());
		directorySelector.setMessage("Select a directory as a location for documentation");
		directorySelector.setText("Directory for Documentation");

		if (selection != null & selection instanceof IStructuredSelection) {

			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			Object o = strucSelection.getFirstElement();
			if (o instanceof WrappedResource) {
				WrappedResource file_to_convert = (WrappedResource) o;

				// If the selected object is a project, then creates a Job for project
				if (o instanceof WrappedProject) {
					// Do for a project
					directorySelector
							.setFilterPath(file_to_convert.getProject().getResource().getLocation().toOSString());
					String selectedDirectoryName = directorySelector.open();
					myJob = new JobDocumentationProject(((WrappedProject) o).getResource(), selectedDirectoryName);
					myJob.schedule();
				}
				// else
				// {
				// //If the selected object is a GamaFile, then creates a Job for Independent
				// Model (with includes, index etc.)
				// if(o instanceof WrappedGamaFile)
				// {
				// directorySelector.setFilterPath(file_to_convert.getResource().getLocation().toOSString());
				// String selectedDirectoryName = directorySelector.open();
				// //Do for a model
				// myJob = new JobDocumentationModelIndependent(((WrappedGamaFile) o),
				// selectedDirectoryName);
				// myJob.schedule();
				// }
				// }
			}

		}

		return null;
	}
}
