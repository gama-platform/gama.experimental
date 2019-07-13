package gama_analyzer;

import java.util.Iterator;
import java.util.Map;

import msi.gama.metamodel.agent.GamlAgent;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gaml.species.ISpecies;

public class GroupIdRuleSpecies extends GroupIdRule {

	String nom;
	IList<GamlAgent> liste = null;

	public IList<GamlAgent> getListe() {
		return liste;
	}

	public void setListe(final IList<GamlAgent> liste) {
		this.liste = liste;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(final String nom) {
		this.nom = nom;
	}

	@Override
	public IList<IAgent> update(final IScope scope, IList<IAgent> liste) {

		liste = GamaListFactory.create(); // ???
		final Map<String, ISpecies> especes = scope.getModel().getAllSpecies();
		final Iterator<String> it = especes.keySet().iterator();
		while (it.hasNext()) {
			final String s = it.next();
			if (s.equals(nom)) {
				final Iterator<IAgent> monde = (Iterator<IAgent>) especes.get(s).iterable(scope).iterator();
				while (monde.hasNext()) {
					liste.add(monde.next());
				}
			}

		}
		return liste;
	}
}
