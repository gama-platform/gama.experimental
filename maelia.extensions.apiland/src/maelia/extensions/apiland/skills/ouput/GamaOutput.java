package maelia.extensions.apiland.skills.ouput;

import fr.inra.sad.bagap.apiland.capfarm.model.CoverUnit;
import fr.inra.sad.bagap.apiland.capfarm.model.territory.Parcel;
import fr.inra.sad.bagap.apiland.capfarm.simul.CoverLocationModel;
import fr.inra.sad.bagap.apiland.capfarm.simul.GlobalCoverLocationModel;
import fr.inra.sad.bagap.apiland.core.time.Instant;
import fr.inra.sad.bagap.apiland.simul.OutputAnalysis;
import fr.inra.sad.bagap.apiland.simul.Simulation;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IList;
import msi.gama.util.IMap;
import msi.gaml.types.Types;

public class GamaOutput extends OutputAnalysis {
		
	IMap<String,IList<String>> res = GamaMapFactory.create();
	
	public IMap<String,IList<String>> getRes(){
		return res;
	}
	
	@Override
	public void close(Simulation simulation){
		if(!simulation.isCancelled()){
			for(CoverLocationModel model : (GlobalCoverLocationModel) simulation.model().get("agriculture")){
				System.out.println(model.getCoverAllocator().getCode());
				for(Parcel p : model.getCoverAllocator().parcels()){
					IList<String> l = GamaListFactory.create(Types.STRING);
	
					Instant t = simulation.manager().start();
					while(t.isBefore(simulation.manager().end()) || t.equals(simulation.manager().end())){
						l.add(((CoverUnit) p.getAttribute("cover").getValue(t)).getName());
						t = simulation.manager().delay().next(t);
					}					
					
					res.put(p.getId(),l);

				}
			}
		}
	}
}
