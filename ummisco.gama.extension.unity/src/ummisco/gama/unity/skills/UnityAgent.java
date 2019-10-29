package ummisco.gama.unity.skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import msi.gama.metamodel.agent.MinimalAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.GamaShape;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.metamodel.shape.IShape;
import msi.gama.util.GamaColor;
import msi.gama.util.GamaPair;
import msi.gama.util.IList;
import msi.gama.util.IMap.IPairList;
import ummisco.gama.dev.utils.DEBUG;
import ummisco.gama.unity.data.type.AgentAttribute;
import msi.gama.util.GamaColor.NamedGamaColor;

public class UnityAgent {

		public String agentName;
		public String species;
		public Object geometryType;
		public Object vertices;
		public GamaPoint location;
		
		public  ArrayList<AgentAttribute> attributes ; 
		
		
				
	

		

		public Object color;
		public Object height;
	
		
		public UnityAgent() {
			
		}
				
		static {
			DEBUG.ON();
		}
		public void getUnityAgent(MinimalAgent miniAgent) {
			this.agentName = miniAgent.getName();
			this.species = miniAgent.getSpecies().getName();
			this.geometryType = miniAgent.getGeometricalType();
			this.vertices = getPointsList(miniAgent.getGeometry().getPoints());
			this.location = (GamaPoint) miniAgent.getLocation();
			
			
			this.attributes = new ArrayList<AgentAttribute>();
			
		//	GamaShape gs = (GamaShape) miniAgent.getGeometry();
		//	GamaPairList pairs = gs.getAttributes().getPairs();

			IPairList pairs = miniAgent.getOrCreateAttributes().getPairs();
			
			System.out.println("Agent name is : "+this.agentName);
			
			for(Object e :pairs) {
				GamaPair gp = (GamaPair) e;
				
				switch ((String) gp.getKey()) {
					case "color":
						this.color = gp.getValue();
						break;
					case "height": 
						this.height = gp.getValue();
					break;
					
					default:
						final AgentAttribute agAtt = new AgentAttribute(e.getClass().getName(), (String) gp.getKey(), ""+gp.getValue());
						attributes.add(agAtt);
					break;
				}
				
					
				//GamaColor color = gp.getKey().
				DEBUG.OUT("Object type is : "+e.getClass());
				DEBUG.OUT("Object key is : "+gp.getKey());
				DEBUG.OUT("Object value is : "+gp.getValue());
				DEBUG.OUT("Object Gaml type is : "+gp.getGamlType());		
			}
			
			DEBUG.OUT("Attributes list size ios : "+attributes.size());		
			
					
				
		}
		
		
		
		public ArrayList<GamaPoint> getPointsList(IList<? extends ILocation> list){
			ArrayList<GamaPoint> pointsList = new ArrayList<GamaPoint>();
			for(ILocation l :list) {
				GamaPoint p = new GamaPoint(l.getX(), l.getY(), l.getZ());
				pointsList.add(p);
			}
			return pointsList;
		}
		
		public GamaColor getGamaColor() {
			
			GamaColor red = new GamaColor(217, 72, 33);

			return red;
		}
}
