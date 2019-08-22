package gama_analyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import msi.gama.metamodel.agent.GamlAgent;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;
import msi.gaml.species.ISpecies;

public class GroupIdRuleList extends GroupIdRule {

	String nom;
	List<GamlAgent> liste = null;

	public List<GamlAgent> getListe() {
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

	@SuppressWarnings ("unchecked")
	@Override
	public IList<IAgent> update(final IScope scope, IList<IAgent> liste) {
		liste = (IList) scope.getGlobalVarValue(nom);
		return liste;
	}

	@SuppressWarnings ("unchecked")
	public IList<IAgent> updatea(final IScope scope, IList<IAgent> liste) {

		liste = (IList<IAgent>) new ArrayList<IAgent>(); // FALSE
		final Map<String, ISpecies> especes = scope.getModel().getAllSpecies();
		System.out.println("especes= " + especes);
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
		System.out.println("liste= " + liste);
		return liste;
	}

}
