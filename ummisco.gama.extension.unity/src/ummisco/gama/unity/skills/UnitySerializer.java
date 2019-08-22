package ummisco.gama.unity.skills;

import com.thoughtworks.xstream.XStream;
import com.vividsolutions.jts.geom.Polygon;

import msi.gama.common.geometry.GamaCoordinateSequence;
import msi.gama.common.geometry.GamaCoordinateSequenceFactory;
import msi.gama.common.geometry.GamaGeometryFactory;
import msi.gama.extensions.messaging.GamaMailbox;
import msi.gama.extensions.messaging.GamaMessage;
import msi.gama.metamodel.agent.MinimalAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.GamaShape;
import msi.gama.metamodel.shape.IShape;
import msi.gama.util.GamaColor;
import msi.gama.util.GamaColor.NamedGamaColor;
import msi.gama.util.GamaList;
import msi.gaml.types.GamaIntegerType;
import msi.gaml.types.GamaListType;
import msi.gaml.types.GamaMessageType;
import msi.gaml.types.GamaNoType;
import msi.gaml.types.GamaStringType;
import msi.gaml.types.ParametricType;
import ummisco.gama.unity.messages.PropertyTopicMessage;

public class UnitySerializer {

	public static XStream xstream;
	
	public UnitySerializer () {
		
	}
	
	public UnitySerializer (XStream xs) {
		this.xstream = xs;
	}
	
	public void SetSerializer(XStream xs) {
		this.xstream = xs;
	}
	
	
	
	public String toXML(Object message) {
		return xstream.toXML(message);
	}
	
	
	public String agentShapeToXML(Object message) {
		
		

		//xstream.omitField(PropertyTopicMessage.class, "value"); // to Omit a specific field
		//xstream.aliasType("xsi:type=\"GamaShape\"", GamaShape.class);
		// xstream.aliasType("xsi:type", Object.class);
		// xstream.useAttributeFor("sender", type);
	
	/*	
		xstream.alias("GamaMessage", GamaMessage.class);
		xstream.alias("GamaShape", GamaShape.class);
		xstream.alias("geometry", Polygon.class);		
		xstream.alias("factory", GamaGeometryFactory.class);
		xstream.alias("GamaCoordinateSequence", GamaCoordinateSequence.class);
		xstream.alias("GamaCoordinateSequenceFactory", GamaCoordinateSequenceFactory.class);
	
		xstream.alias("MinimalAgent", MinimalAgent.class);
		xstream.alias("GamaStringType", GamaStringType.class);
		xstream.alias("GamaNoType", GamaNoType.class);
		xstream.alias("GamaColor", GamaColor.class);
		xstream.alias("GamaList", GamaList.class);
		xstream.alias("ParametricType", ParametricType.class);
		xstream.alias("GamaListType", GamaListType.class);
		xstream.alias("GamaMessageType", GamaMessageType.class);
		xstream.alias("GamaIntegerType", GamaIntegerType.class);
		xstream.alias("GamaMailbox", GamaMailbox.class);
		
		xstream.omitField(GamaGeometryFactory.class, "precisionModel"); // 
		xstream.omitField(GamaGeometryFactory.class, "factory"); // 
		xstream.omitField(GamaGeometryFactory.class, "factory"); // 
	*/	
		
		xstream.alias("geometryType", msi.gama.metamodel.shape.IShape.Type.class);
		xstream.alias("color", GamaColor.class);
		xstream.alias("NamedGamaColor", msi.gama.util.GamaColor.NamedGamaColor.class);
		xstream.alias("GamaPoint", GamaPoint.class); 
		xstream.alias("UnityAgent", UnityAgent.class); 
		//xstream.alias("GamaMessage", GamaMessage.class); 


		return xstream.toXML(message);
	}
	
	
	
	
	
}
