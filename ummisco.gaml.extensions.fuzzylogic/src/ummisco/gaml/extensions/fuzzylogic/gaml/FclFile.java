package ummisco.gaml.extensions.fuzzylogic.gaml;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.annotations.precompiler.IConcept;
import gama.core.common.geometry.Envelope3D;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IList;
import gama.core.util.file.GamaFile;
import gama.core.util.file.GamaFileMetaData;
import gama.gaml.operators.Strings;
import gama.gaml.types.IContainerType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.RuleBlock;

@file (
		name = "fcl",
		extensions = { "fcl" },
		buffer_type = IType.LIST,
		buffer_content = IType.STRING,
		buffer_index = IType.INT,
		concept = { IConcept.FILE, IConcept.SAVE_FILE },
		doc = @doc ("Represents a fuzzy logic system in fic format. The internal contents is a string at index 0"))
@SuppressWarnings("unchecked")
public class FclFile extends GamaFile<IList<String>, String> {

	public static class FclInfo extends GamaFileMetaData { 

		public int nbRules;
		public int nbVariables;

		public FclInfo(final String fileName, final long modificationStamp) {
			super(modificationStamp);

	        FIS fis = FIS.load(fileName,true);	 
	       	        
	        for(FunctionBlock fb : fis ) {
	        	nbVariables += fb.getVariables().size();
	        	
	        	for(RuleBlock rb : fb.getRuleBlocks().values()) {
	        		nbRules += rb.getRules().size();
	        	}
	        }
		}

//		public FclInfo(final String propertyString) {
//			super(propertyString);
//
//			final String[] segments = split(propertyString);
//			savedModel = segments[1];
//			savedExperiment = segments[2];
//			savedCycle = Integer.valueOf(segments[3]);
//		}

		@Override
		public String getDocumentation() {
			final StringBuilder sb = new StringBuilder();
			sb.append("Number of variables: ").append(nbVariables).append(Strings.LN);			
			sb.append("Number of rules: ").append(nbRules).append(Strings.LN);
			return sb.toString();
		}

		@Override
		public String getSuffix() {
			return "Variables: " + nbVariables + " | Rules: " + nbRules;
		}

		@Override
		public void appendSuffix(final StringBuilder sb) {
			sb.append("Variables: ").append(nbVariables).append(SUFFIX_DEL);			
			sb.append("Rules: ").append(nbRules);

		}

		/**
		 * @return
		 */
		@Override
		public String toPropertyString() {
			return super.toPropertyString() + DELIMITER + nbVariables + DELIMITER + nbRules;
		}
	}
	
	
	@doc ("Constructor for FCL (Fuzzy Control Language, specification IEC 61131 part 7) files: read the content.")
	public FclFile(IScope scope, String pn) throws GamaRuntimeException {
		super(scope, pn);
	}

	@Override
	public Envelope3D computeEnvelope(IScope scope) {
		return null;
	}

	@Override
	public IContainerType<?> getGamlType() {
		return Types.FILE.of(Types.INT, Types.STRING);
	}

}
