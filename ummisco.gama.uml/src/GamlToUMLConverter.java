import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.common.util.URI;

import msi.gama.lang.gaml.indexer.GamlResourceIndexer;
import msi.gaml.compilation.GAML;
import msi.gaml.compilation.ast.ISyntacticElement;
import msi.gaml.compilation.ast.ISyntacticElement.SyntacticVisitor;
import msi.gaml.types.Types;
import ummisco.gama.ui.navigator.contents.WrappedGamaFile;

public class GamlToUMLConverter {

	ISyntacticElement model;
	Iterator<URI> importedModelsURIs;
	VisitorForSpeciesChildren visitorForSpeciesChildren;
	VisitorForSpeciesFacets visitorForSpeciesFacets;
	public XMLStringBuilder umlText;
	public Map<String, ISyntacticElement> species;
	public Map<String, Map<String, ISyntacticElement>> attributes;
	public Map<String, Map<String, ISyntacticElement>> operations;
	public Map<String, Map<String, ISyntacticElement>> associations;
	public Map<String, Map<String, ISyntacticElement>> compositions;
	public Map<String, String> generalizations;
	public ArrayList<String> speciesName;
	public int id = 0;
	SyntacticVisitor visitorForSpecies;

	/**
	 * Constructor of GamlToUMLConverter
	 *
	 * @param modelFile
	 *            {@code WrappedGamaFile} the Wrapped Gama File containing the model
	 */
	public GamlToUMLConverter(final WrappedGamaFile modelFile, final boolean light) {
		this.model = GAML.getContents(URI.createURI(modelFile.getResource().getLocationURI().toString()));
		importedModelsURIs =
				(Iterator<URI>) GamlResourceIndexer.allImportsOf(URI.createURI(modelFile.getResource().getRawLocationURI().toString()));
		umlText = new XMLStringBuilder();
		species = new HashMap<>();
		attributes = new HashMap<>();
		operations = new HashMap<>();
		associations = new HashMap<>();
		compositions = new HashMap<>();
		generalizations = new HashMap<>();
		this.speciesName = new ArrayList<>();
		visitorForSpeciesChildren = new VisitorForSpeciesChildren(this);
		visitorForSpeciesFacets = new VisitorForSpeciesFacets(this);
		visitorForSpecies = element -> {
			if (element.isSpecies() || element.getKeyword().equals(IParser.GAMA_KEYWORD_GRID)) {
				if (speciesName.contains(element.getName()) == false) {
					id = id + 1;
					// System.out.println(element.getName()+" is not in "+species);
					species.put(Integer.toString(id), element);
					speciesName.add(element.getName());
					visitorForSpeciesChildren.setSpecies(Integer.toString(id));
					visitorForSpeciesFacets.setSpecies(Integer.toString(id));
					element.visitAllChildren(visitorForSpeciesChildren);
					element.visitFacets(visitorForSpeciesFacets);
					element.visitSpecies(visitorForSpecies);
				}

			}
		};
		loadSpecies();
		generateBeginning();
		generateSpecies(light);
		generateEnding();

	}

	/**
	 * Method to load the species of a model (even the ones imported)
	 */
	public void loadSpecies() {

		while (importedModelsURIs.hasNext()) {
			final URI tmpUri = importedModelsURIs.next();
			final ISyntacticElement tmpModel = GAML.getContents(tmpUri);
			tmpModel.visitSpecies(visitorForSpecies);
			tmpModel.visitGrids(visitorForSpecies);
		}
		model.visitSpecies(visitorForSpecies);
		model.visitGrids(visitorForSpecies);
	}

	public String getIdSpecies(final String name) {
		for (final String aKey : species.keySet()) {
			if (species.get(aKey).getName().equals(name)) { return aKey; }
		}
		return "";
	}

	public String getIdAttributeFromAssociation(final String species, final String name) {
		for (final String aKey : attributes.get(species).keySet()) {
			if (attributes.get(species).get(aKey).getName().equals(name)) { return aKey; }
		}
		return "";
	}

	public String getIdAssociationFromAttribute(final String species, final String name) {
		for (final String aKey : associations.get(species).keySet()) {
			if (associations.get(species).get(aKey).getName().equals(name)) { return aKey; }
		}
		return "";
	}

	public String getIdParent(final String name) {
		// System.out.println("Looking for parent of : "+name);
		if (generalizations.containsKey(name)) { return getIdSpecies(generalizations.get(name)); }
		return "";
	}

	public void generateBeginning() {
		umlText.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		umlText.nextLine();
		umlText.append(
				"<uml:Model xmi:version=\"2.1\" xmlns:xmi=\"http://schema.omg.org/spec/XMI/2.1\" xmlns:uml=\"http://www.eclipse.org/uml2/3.0.0/UML\" xmi:id=\"_NpznoOUmEeeAeY96UuZjHg\" name=\""
						+ this.model.getName() + "\">");
	}

	public void generateEnding() {
		umlText.nextLine();
		umlText.append("</uml:Model>");
	}

	public void generateSpecies(final boolean light) {
		// System.out.println("GENERALIZATIONS + "+this.generalizations);
		for (final String aSpecies : species.keySet()) {
			// System.out.println("DOING SPECIES "+aSpecies+" "+species.get(aSpecies).getName());
			umlText.nextLine();
			umlText.append("\t <" + IParser.XML_TAG_CLASS + " xmi:type=\"" + IParser.XML_KEYWORD_CLASS + "\" "
					+ IParser.XML_KEYWORD_ID + "=\"" + aSpecies + "\" name=\"" + species.get(aSpecies).getName()
					+ "\">");
			final String idParent = getIdParent(aSpecies);
			if (idParent.equals("") == false) {
				umlText.nextLine();
				this.id = this.id + 1;
				umlText.append("\t \t <" + IParser.XML_TAG_GENERALIZATION + " xmi:id=\"" + this.id + "\" general=\""
						+ idParent + "\"/>");
			}
			if (light == false) {
				if (attributes.containsKey(aSpecies)) {
					for (final String anAttribute : attributes.get(aSpecies).keySet()) {
						umlText.nextLine();
						umlText.append("\t \t <" + IParser.XML_TAG_ATTRIBUTE + " " + IParser.XML_KEYWORD_ID + "=\""
								+ anAttribute + "\" name=\"" + attributes.get(aSpecies).get(anAttribute).getName()
								+ "\" visibility=\"public\" isUnique=\"false\"");
						final String type = attributes.get(aSpecies).get(anAttribute).getKeyword();
						if (Types.get(type).toString().equals("unknown")) {
							umlText.append(" type=\"" + getIdSpecies(type) + "\" association=\""
									+ getIdAssociationFromAttribute(aSpecies,
											attributes.get(aSpecies).get(anAttribute).getName())
									+ "\">");
						} else {
							umlText.append(">");
							umlText.nextLine();
							umlText.append(
									"\t \t \t <" + IParser.XML_TAG_TYPE + " xmi:type=\"uml:PrimitiveType\" href=\""
											+ IParser.MAP_BUILT_IN_TYPES.get(type) + "\"/>");
						}
						umlText.nextLine();
						umlText.append("\t \t </" + IParser.XML_TAG_ATTRIBUTE + ">");
					}
				}
				if (operations.containsKey(aSpecies)) {
					for (final String anOperation : operations.get(aSpecies).keySet()) {
						umlText.nextLine();
						umlText.append("\t \t <" + IParser.XML_TAG_OPERATION + " " + IParser.XML_KEYWORD_ID + "=\""
								+ anOperation + "\" name=\"" + operations.get(aSpecies).get(anOperation).getName()
								+ "\" visibility=\"public\" >");
						if (operations.get(aSpecies).get(anOperation).hasFacet(IParser.GAMA_KEYWORD_TYPE)
								&& operations.get(aSpecies).get(anOperation).getExpressionAt(IParser.GAMA_KEYWORD_TYPE)
										.toString().equals("null") == false) {
							final String type = operations.get(aSpecies).get(anOperation)
									.getExpressionAt(IParser.GAMA_KEYWORD_TYPE).toString();
							this.id = this.id + 1;
							umlText.nextLine();
							umlText.append("\t \t \t <" + IParser.XML_TAG_PARAMETER + " xmi:id=\"" + this.id
									+ "\"  isUnique=\"false\" direction=\"return\" ");
							if (Types.get(type).toString().equals(IParser.GAMA_KEYWORD_UNKNOWN)) {
								umlText.append("type=\"" + getIdSpecies(type) + "\" >");
							} else {
								umlText.append(">");
								umlText.nextLine();
								umlText.append("\t \t \t \t <" + IParser.XML_TAG_TYPE
										+ " xmi:type=\"uml:PrimitiveType\" href=\""
										+ IParser.MAP_BUILT_IN_TYPES.get(type) + "\"/>");
							}
							umlText.nextLine();
							umlText.append("\t \t \t </" + IParser.XML_TAG_PARAMETER + ">");
						}
						final SyntacticVisitor visitorForArgs = element -> {
							if (element.getKeyword().equals(IParser.GAMA_KEYWORD_ARG)) {
								id = id + 1;
								final String type = element.getExpressionAt(IParser.GAMA_KEYWORD_TYPE).toString();
								umlText.nextLine();
								umlText.append("\t \t \t <" + IParser.XML_TAG_PARAMETER + " xmi:id=\"" + id
										+ "\"  isUnique=\"false\"");

								if (Types.get(type).toString().equals(IParser.GAMA_KEYWORD_UNKNOWN)) {
									umlText.append(" type=\"" + getIdSpecies(type) + "\" >");
								} else {
									umlText.append(">");
									umlText.nextLine();
									umlText.append("\t \t \t \t <" + IParser.XML_TAG_TYPE
											+ " xmi:type=\"uml:PrimitiveType\" href=\""
											+ IParser.MAP_BUILT_IN_TYPES.get(type) + "\"/>");
								}
								umlText.nextLine();
								umlText.append("\t \t \t </" + IParser.XML_TAG_PARAMETER + ">");
							}
						};
						operations.get(aSpecies).get(anOperation).visitAllChildren(visitorForArgs);
						umlText.nextLine();
						umlText.append("\t \t </" + IParser.XML_TAG_OPERATION + ">");
					}
				}

			} else {
				if (attributes.containsKey(aSpecies)) {
					for (final String anAttribute : attributes.get(aSpecies).keySet()) {
						final String type = attributes.get(aSpecies).get(anAttribute).getKeyword();
						if (Types.get(type).toString().equals("unknown")) {
							umlText.nextLine();
							umlText.append("\t \t <" + IParser.XML_TAG_ATTRIBUTE + " " + IParser.XML_KEYWORD_ID + "=\""
									+ anAttribute + "\" name=\"" + attributes.get(aSpecies).get(anAttribute).getName()
									+ "\" visibility=\"public\" isUnique=\"false\"");
							umlText.append(" type=\"" + getIdSpecies(type) + "\" association=\""
									+ getIdAssociationFromAttribute(aSpecies,
											attributes.get(aSpecies).get(anAttribute).getName())
									+ "\">");
							umlText.nextLine();
							umlText.append("\t \t </" + IParser.XML_TAG_ATTRIBUTE + ">");
						}
					}
				}
			}

			umlText.nextLine();
			umlText.append("\t </" + IParser.XML_TAG_CLASS + ">");
		}
		for (final String aSpecies : associations.keySet()) {
			for (final String anAssociationId : associations.get(aSpecies).keySet()) {
				umlText.nextLine();
				this.id = this.id + 1;
				umlText.append("\t <" + IParser.XML_TAG_ASSOCIATION + " xmi:type=\"" + IParser.XML_KEYWORD_ASSOCIATION
						+ "\" xmi:id=\"" + anAssociationId + "\" memberEnd=\""
						+ getIdAttributeFromAssociation(aSpecies,
								associations.get(aSpecies).get(anAssociationId).getName())
						+ " " + Integer.toString(this.id) + "\">");
				umlText.nextLine();
				umlText.append(
						"\t \t <" + IParser.XML_TAG_END + " xmi:id=\"" + this.id + "\" visibility=\"public\" type=\""
								+ aSpecies + "\" association=\"" + anAssociationId + "\" >");
				umlText.append("\t \t </" + IParser.XML_TAG_END + " >");
				umlText.nextLine();
				umlText.append("\t </" + IParser.XML_TAG_ASSOCIATION + ">");
			}
		}

	}

	public void save(final String path) {
		FileWriter fw;
		try {
			fw = new FileWriter(path);
			fw.write(umlText.toString());
			fw.flush();
			fw.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void dispose() {

		model.dispose();
		model = null;
		importedModelsURIs = null;
		visitorForSpeciesChildren = null;
		visitorForSpeciesFacets = null;
		umlText = null;
		species = null;
		attributes = null;
		operations = null;
		associations = null;
		generalizations = null;
	}
}
