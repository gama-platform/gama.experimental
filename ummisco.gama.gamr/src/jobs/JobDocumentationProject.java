package jobs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import msi.gama.runtime.GAMA;
 
public class JobDocumentationProject extends JobDocumentation {

	/**
	 * The project concerned by this job
	 */
	public IProject project;
	String model="";
	String exp="";
	/**
	 * Constructor using a given project (will be saved in a default documentation
	 * folder )
	 * 
	 * @param project
	 *            {@code IProject} the project to document
	 */
	public JobDocumentationProject(IProject project) {
		super(project.getLocation().toOSString());
		this.project = project;
	}

	/**
	 * Constructor using a given project (will be saved in a given documentation
	 * folder )
	 * 
	 * @param project
	 *            {@code IProject} the project to document
	 * @param path
	 *            {@code String} the path (String) in which the documentation will
	 *            be saved
	 */
	public JobDocumentationProject(IProject project, String path, String m, String e) {
		super(path);
		this.project = project;
		model=m;
		exp=e;
	}

	public void pack(String sourceDirPath, String zipFilePath) throws IOException {
		System.out.println(System.getProperty("user.dir"));
		Files.deleteIfExists(Paths.get(zipFilePath));
		Path p = Files.createFile(Paths.get(zipFilePath));
		try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
			Path pp = Paths.get(sourceDirPath);
			Files.walk(pp).filter(path -> !Files.isDirectory(path)).forEach(path -> {
				ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
				try {
					zs.putNextEntry(zipEntry);
					Files.copy(path, zs);
					zs.closeEntry();
				} catch (IOException e) {
					System.err.println(e);
				}
			});
		}
	}
	public void saveIni(String dirPath) {
		new File(dirPath+"/.metadata").mkdir();

		final File ini = new File(dirPath+"/.metadata/launcher.ini"); 
		try {
			final List<String> contents = new ArrayList<>();
			if (ini != null) {
				contents.add("-model");
				contents.add(model.substring(project.getName().length()+2));
				contents.add("-experiment");
				contents.add(exp);
				
				try (final FileOutputStream os = new FileOutputStream(ini);
						final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));) {
					for (final String line : contents) {
						writer.write(line);
						writer.newLine();
					}
					writer.flush();
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to run the job in background
	 */
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		if (project != null) {

			// WrappedResource file_to_convert =
			// ummisco.gama.ui.navigator.contents.ResourceManager.getInstance().findWrappedInstanceOf(project);
			// Object[] childrenResources = file_to_convert.getNavigatorChildren().clone();
			try {
				saveIni(project.getLocation().toOSString());
				pack(project.getLocation().toOSString(), directory+"/" + project.getName() + ".gamr");
				GAMA.getGui().tell("Gamr created!");
				new File(project.getLocation().toOSString()+"/.metadata/launcher.ini").delete();
				new File(project.getLocation().toOSString()+"/.metadata").delete();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// this.dispose();
		return Status.OK_STATUS;
	}
}
