package ummisco.gaml.extensions.fuzzylogic.gaml.skills;

import java.util.Map;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.file.GamaFile;
import msi.gaml.compilation.ISymbol;
import msi.gaml.skills.Skill;
import msi.gaml.statements.Facets;
import msi.gaml.statements.Facets.Facet;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Variable;

@doc ("The fuzzy_logic skill is intended to define the actions of an agent reasoning using a Fuzzy logic Inference System (FIS).")
@skill ( name = "fuzzy_logic",
		 concept = { IConcept.SKILL}) // , IConcept.REASONING 
// @SuppressWarnings("rawtypes")
public class FuzzylogicSkills extends Skill {

	public static final String FL_FIS = "fl_fis";
	public static final String FL_VARIABLES = "fl_vars";
	
	@action(name = "fl_init_fis", 
			args = {
				@arg(name = IKeyword.FROM, type = IType.FILE, optional = false, doc = @doc("The file containing the Fuzzy Logic Inference System."))			
			}, doc = @doc("Action that initialize the FIS from a FCL file."))
	public void initFIS(final IScope scope) throws GamaRuntimeException {
		final IAgent agt = scope.getAgent();
		GamaFile fclFile = (GamaFile) scope.getArg(IKeyword.FROM, IType.FILE);
		FIS f = FIS.load(fclFile.getPath(scope),true) ;
		agt.setAttribute(FL_FIS, f);
	}	

	@action(name = "fl_set_variable", args = {
			@arg(name = IKeyword.VAR, type = IType.STRING, optional = false, doc = @doc(".")),
			@arg(name = IKeyword.VALUE, type = IType.STRING, optional = false, doc = @doc("."))
	}, doc = @doc("Action that associates a GAML attribute from a species with a variable from the FIS."))
	public void setVariable(final IScope scope) throws GamaRuntimeException {
		final IAgent agt = scope.getAgent();
	
		String varGAML = (String) scope.getArg(IKeyword.VAR,IType.STRING);
		String varFIS = (String) scope.getArg(IKeyword.VALUE,IType.STRING);
			
		Map<String,String> vars = (Map) agt.getAttribute(FL_VARIABLES);
		if(vars == null) {
			vars = GamaMapFactory.create(Types.STRING, Types.STRING);
		}
		
		vars.put(varGAML, varFIS);
		agt.setAttribute(FL_VARIABLES,vars);			
	}	
	
	@action(name = "fl_evaluate", 
			doc = @doc("Action that evaluates the FIS."))
	public void evaluate(final IScope scope) throws GamaRuntimeException {
		final IAgent agt = scope.getAgent();
		FIS fis = (FIS) agt.getAttribute(FL_FIS);	
		Map<String,String> vars = (Map) agt.getAttribute(FL_VARIABLES);

		if(vars == null) {
			throw GamaRuntimeException.error("Variables of the FIS have not been associated with agent attributes", scope);
		}
		
        // Set inputs from the agent attributes
		for(String nameVarGAML : vars.keySet()) {
	        fis.setVariable(vars.get(nameVarGAML), (Double) agt.getAttribute(nameVarGAML));			
		}

        // Evaluate the FIS
        fis.evaluate();		
	}
	
	@action(name = "fl_get_output", args = {
			@arg(name = IKeyword.VAR, type = IType.STRING, optional = false, doc = @doc("The name of the output we want to take the value."))}, 
			doc = @doc("Action that returns the value of the given output FIS."))
	public Double getOutput(final IScope scope) throws GamaRuntimeException {
		final IAgent agt = scope.getAgent();
		FIS fis = (FIS) agt.getAttribute(FL_FIS);
		String varFISOutput = (String) scope.getArg(IKeyword.VAR,IType.STRING);		
		
		if(fis == null) {
			throw GamaRuntimeException.error("The FIS has not been initialized.", scope);
		}
				    
	    return fis.getVariable(varFISOutput).getValue();
	}		
}
