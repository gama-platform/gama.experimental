package gama.experimental.markdown.jobs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;

import gama.experimental.markdown.markdownSyntactic.IParser;
import gama.experimental.markdown.markdownSyntactic.LightModel;
import gama.experimental.markdown.markdownSyntactic.MarkdownModelDocumentor;
import gama.experimental.markdown.markdownSyntactic.MarkdownTools;
import gama.ui.navigator.view.contents.WrappedFolder;
import gama.ui.navigator.view.contents.WrappedGamaFile;
import gama.ui.navigator.view.contents.WrappedProject;
import gama.ui.navigator.view.contents.WrappedResource;
import gaml.compiler.gaml.indexer.GamlResourceIndexer;

/**
 *
 * @author damienphilippon Date : 19 Dec 2017 Abstract class used to do a Documentation job (will be derived by Project,
 *         ModelProject and ModelIndependent)
 */
public abstract class JobDocumentation extends WorkspaceJob {
	/**
	 * The directory that will be used to save the file(s)
	 */
	public String directory;

	/**
	 * Variable representing all the species and the link to the documentation files that will present them
	 */
	Map<String, String> speciesLink = new HashMap<>();
	/**
	 * Variable representing all the experiments and the link to the documentation files that will present them
	 */
	Map<String, String> experimentsLink = new HashMap<>();

	/**
	 * The list of all the models already done by the process
	 */
	ArrayList<String> modelsDone = new ArrayList<>();

	/**
	 * The path to the index file
	 */
	public String indexPath;

	/**
	 * Constructor of JobDocumentation that will use the given string as an output directory
	 *
	 * @param directory
	 *            {@code String} the path (string) of the output directory
	 */
	public JobDocumentation(final String directory) {
		super(IParser.JOB_NAME);
		this.directory = directory;

		// The index is defined at the base of the directory file
		this.indexPath = directory + File.separator + IParser.MARKDOWN_LABEL_INDEX + ".md";
	}

	/**
	 * Method to run the job in background
	 */
	@Override
	public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
		return Status.OK_STATUS;
	}

	/**
	 * Recursive method to generate the models (will loop trough the folders)
	 *
	 * @param theResource
	 *            {@code WrappedResource} the wrapped resource to loop on (project or folder)
	 */
	public void generateForWrappedResource(final WrappedResource<?, ?> theResource) {
		final IPath pathProject = theResource.getProject().getResource().getLocation();
		final IPath pathResource = theResource.getResource().getLocation();
		final IPath pathResourceToProject = pathResource.makeRelativeTo(pathProject);

		if (theResource instanceof WrappedFolder || theResource instanceof WrappedProject) {
			final File newDirectory = new File(directory + File.separator + pathResourceToProject);
			if (Files.exists(newDirectory.toPath()) == false) {
				try {
					Files.createDirectory(newDirectory.toPath());
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			Object[] childrenResources = theResource.getNavigatorChildren().clone();
			for (final Object aResource : childrenResources) {
				if (aResource instanceof WrappedResource) {
					if (((WrappedResource<?, ?>) aResource).countModels() > 0) {
						generateForWrappedResource((WrappedResource<?, ?>) aResource);
					}
				}
			}
			childrenResources = null;
		} else {
			if (theResource instanceof WrappedGamaFile) {
				WrappedGamaFile gamaFile = (WrappedGamaFile) theResource;

				generateForWrappedGamaFile(gamaFile);
				gamaFile = null;
			}
		}
	}

	/**
	 * Method to release the memory used by the variables of the job
	 */
	public void dispose() {
		this.experimentsLink = null;
		this.speciesLink = null;
		this.modelsDone = null;
	}

	/**
	 * Method to generate the index of the job (can be for the whole project or a model)
	 */
	public void generateIndex() {
		StringBuilder mDText = new StringBuilder();
		mDText.append(MarkdownTools.addHeader(IParser.MARKDOWN_LABEL_INDEX));
		mDText.append(MarkdownTools.goBeginLine());
		mDText.append(MarkdownTools.addLine());
		mDText.append(MarkdownTools.goBeginLine());
		mDText.append(
				MarkdownTools.addSubHeader(IParser.MARKDOWN_LABEL_SPECIES + " (" + speciesLink.keySet().size() + ")"));

		// Sorts the species alphabetically
		List<String> species = new ArrayList<>(speciesLink.keySet());
		Collections.sort(species, String.CASE_INSENSITIVE_ORDER);
		for (final String aSpecies : species) {
			mDText.append(MarkdownTools.goBeginLine());
			mDText.append(MarkdownTools.addLink(aSpecies, speciesLink.get(aSpecies)));
		}
		species = null;

		mDText.append(MarkdownTools.goBeginLine());
		mDText.append(MarkdownTools.goBeginLine());
		mDText.append(MarkdownTools.addLine());
		mDText.append(MarkdownTools.goBeginLine());

		mDText.append(MarkdownTools
				.addSubHeader(IParser.MARKDOWN_LABEL_EXPERIMENTS + " (" + experimentsLink.keySet().size() + ")"));

		// Sorts the experiments alphabetically
		List<String> experiments = new ArrayList<>(experimentsLink.keySet());
		Collections.sort(experiments, String.CASE_INSENSITIVE_ORDER);
		for (final String anExperiment : experiments) {
			mDText.append(MarkdownTools.goBeginLine());
			mDText.append(MarkdownTools.addLink(anExperiment, experimentsLink.get(anExperiment)));
		}
		experiments = null;

		// Save the markdown text to the file and set the markdown text to null
		try (final FileWriter fw = new FileWriter(indexPath);) {
			fw.write(mDText.toString());
			fw.flush();
			mDText = null;
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to generate the documentation of a given file (can be the documentation of a whole model)
	 *
	 * @param aFile
	 *            {@code WrappedGamaFile} the file that will be used for documentation
	 */
	public void generateForWrappedGamaFile(final WrappedGamaFile aFile) {

		// Loop trough the imported models of the file and build the links between their species and the documentation
		Map<URI, String> importedUris =
				GamlResourceIndexer.allImportsOf(URI.createURI(aFile.getResource().getLocationURI().toString()));
		for(final URI tmpUri : importedUris.keySet()){
			if (modelsDone.contains(tmpUri.toString()) == false) {
				modelsDone.add(tmpUri.toString());
				LightModel model = new LightModel(tmpUri);

				final IPath importedModel = new Path(tmpUri.toFileString().replace("file:", ""));
				final IPath relativisedPath = importedModel.makeRelativeTo(new Path(indexPath));
				for (final String aSpecies : model.speciesLink.keySet()) {
					String linkToSpecies = model.speciesLink.get(aSpecies);
					linkToSpecies = FilenameUtils.removeExtension(relativisedPath.toString()
							.replaceFirst(".." + File.separator, "").replaceFirst(".." + File.separator, "")) + ".md#"
							+ linkToSpecies;
					this.speciesLink.put(aSpecies, linkToSpecies);
				}
				for (final String anExperiment : model.experimentsLink.keySet()) {
					String linkToExperiment = model.experimentsLink.get(anExperiment);
					linkToExperiment = FilenameUtils.removeExtension(relativisedPath.toString()
							.replaceFirst(".." + File.separator, "").replaceFirst(".." + File.separator, "")) + ".md#"
							+ linkToExperiment;
					this.experimentsLink.put(anExperiment, linkToExperiment);
				}

				model.dispose();
				model = null;
			}
		}
		importedUris = null;

		// Add the species and experiments of the model
		final LightModel model = new LightModel(URI.createURI(aFile.getResource().getLocationURI().toString()));
		if (modelsDone.contains(aFile.getResource().getRawLocationURI().toString()) == false) {
			modelsDone.add(aFile.getResource().getRawLocationURI().toString());
			final IPath importedModel = aFile.getResource().getLocation();
			final IPath relativisedPath = importedModel.makeRelativeTo(new Path(indexPath));
			for (final String aSpecies : model.speciesLink.keySet()) {
				String linkToSpecies = model.speciesLink.get(aSpecies);
				linkToSpecies =
						FilenameUtils.removeExtension(relativisedPath.toString().replaceFirst(".." + File.separator, "")
								.replaceFirst(".." + File.separator, "")) + ".md#" + linkToSpecies;
				this.speciesLink.put(aSpecies, linkToSpecies);
			}
			for (final String anExperiment : model.experimentsLink.keySet()) {
				String linkToExperiment = model.experimentsLink.get(anExperiment);
				linkToExperiment =
						FilenameUtils.removeExtension(relativisedPath.toString().replaceFirst(".." + File.separator, "")
								.replaceFirst(".." + File.separator, "")) + ".md#" + linkToExperiment;
				this.experimentsLink.put(anExperiment, linkToExperiment);
			}
		}

		// Build the folders tree
		IPath pathProject = aFile.getProject().getResource().getLocation();
		IPath pathResource = aFile.getResource().getLocation();
		String pathResourceToProject =
				FilenameUtils.removeExtension(pathResource.makeRelativeTo(pathProject).toOSString());
		new File(directory + File.separator + pathResourceToProject + ".md").getParentFile().mkdirs();

		// Generate the documentation of the model
		final MarkdownModelDocumentor modelDoc =
				new MarkdownModelDocumentor(aFile, directory + File.separator + pathResourceToProject + ".md");
		modelDoc.generateMarkdown();
		modelDoc.saveMarkdown();

		// Empty the variable to release memory
		pathProject = null;
		pathResource = null;
		pathResourceToProject = null;
	}

}
