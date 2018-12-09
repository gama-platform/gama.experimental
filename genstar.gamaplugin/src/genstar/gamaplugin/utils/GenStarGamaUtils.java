package genstar.gamaplugin.utils;

import org.graphstream.graph.Edge;
import core.metamodel.io.GSSurveyType;
import core.metamodel.value.IValue;
import core.metamodel.value.binary.BooleanValue;
import core.metamodel.value.numeric.ContinuousValue;
import core.metamodel.value.numeric.IntegerValue;
import core.metamodel.value.numeric.RangeValue;
import core.util.data.GSEnumDataType;
import genstar.gamaplugin.types.GamaPopGenerator;
import genstar.gamaplugin.types.GamaRange;
import genstar.gamaplugin.types.GamaRangeType;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.graph.GamaGraph;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import spin.SpinNetwork;

public class GenStarGamaUtils {
	public static GSSurveyType toSurveyType(String type) {
		if (type.equals("ContingencyTable"))
			return GSSurveyType.ContingencyTable;
		if (type.equals("GlobalFrequencyTable"))
			return GSSurveyType.GlobalFrequencyTable;
		if (type.equals("LocalFrequencyTable"))
			return GSSurveyType.LocalFrequencyTable;
		return GSSurveyType.Sample;
	}
	
	@SuppressWarnings("rawtypes")
	public static GSEnumDataType toDataType(final IType type, final boolean ordered) {
		int t = type.id();
		if (t == IType.FLOAT)
			return GSEnumDataType.Continue;
		if (t == IType.INT)
			return GSEnumDataType.Integer;
		if (t == IType.BOOL)
			return GSEnumDataType.Boolean;
		if (t == GamaRangeType.id  )
			return GSEnumDataType.Range;
		if (ordered)
			return GSEnumDataType.Order;
		return GSEnumDataType.Nominal; 
	}

	static public Object toGAMAValue(IScope scope, IValue val, boolean checkEmpty) {
		GSEnumDataType type= val.getType();
		if (checkEmpty && val.equals(val.getValueSpace().getEmptyValue())) return toGAMAValue(scope, val.getValueSpace().getEmptyValue(), false);
		if (type == GSEnumDataType.Boolean) {
			return ((BooleanValue) val).getActualValue();
		}
		if (type == GSEnumDataType.Continue) {
			if (val instanceof RangeValue) return toGAMARange(val);
			return ((ContinuousValue) val).getActualValue ();
		}
		if (type == GSEnumDataType.Integer) {
			if (val instanceof RangeValue) return toGAMARange(val);
			return ((IntegerValue) val).getActualValue();
		}
		if (type == GSEnumDataType.Range) {
			return toGAMARange(val);
		}
		return val.getStringValue();
	}
	
	static GamaRange toGAMARange(IValue val) {
		RangeValue rVal = (RangeValue) val;
		return new GamaRange(rVal.getBottomBound().doubleValue(), rVal.getTopBound().doubleValue());
	}
	
	// TODO Ben : à remettre si le précédent ne marche pas :-)
//	static GamaRange toGAMARange(IValue val) {
//		
//		Number[] vals = ((RangeValue) val).getActualValue();
//		if (vals.length == 0) return null;
//		Number rangeMin = vals[0];
//		Number rangeMax = vals.length > 1 ? vals[1] : Double.MAX_VALUE;
//		return new GamaRange(rangeMin.doubleValue(), rangeMax.doubleValue());
//	}

	@SuppressWarnings("rawtypes")
	public static Object toGAMAValue(IScope scope, IValue valueForAttribute, boolean checkEmpty, IType type) {
		Object gamaValue = toGAMAValue(scope, valueForAttribute, checkEmpty);
		if(type != null && gamaValue instanceof GamaRange) {
			return ((GamaRange) gamaValue).cast(scope, type);
		}
		return gamaValue;
	}
	
	
	public static GamaGraph<IAgent,IShape> toGAMAGraph(IScope scope, SpinNetwork net, GamaPopGenerator gen) {
		if(gen.getAgents().isEmpty())
			return null;
		
		IType<?> nodeType = gen.getAgents().stream().findFirst().orElse(null).getGamlType(); 	
		GamaGraph<IAgent,IShape> gamaNetwork = new GamaGraph<>(scope, net.isDirected(),nodeType,Types.GEOMETRY);
		
		for(IAgent agt : gen.getAgents()) {
			gamaNetwork.addVertex(agt);
		}
		
		for(Edge e : net.getLinks()) {
			IAgent sourceAgt = gen.getAgent(net.getDemoEntityNode(e.getNode0()));
			IAgent targetAgt = gen.getAgent(net.getDemoEntityNode(e.getNode1()));
			
			gamaNetwork.addEdge(sourceAgt, targetAgt);
		}

		return gamaNetwork;
	}	
	
}
