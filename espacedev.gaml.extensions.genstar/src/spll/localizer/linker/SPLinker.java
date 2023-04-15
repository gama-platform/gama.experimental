package spll.localizer.linker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import spll.localizer.constraint.ISpatialConstraint;
import spll.localizer.distribution.ISpatialDistribution;

/**
 * General implementation for spatial linker - meant to link entity with a spatial entity
 * using a pre-defined distribution and optional spatial constraints
 * 
 * @author kevinchapuis
 *
 */
public class SPLinker implements ISPLinker<IShape> {
	 
	private ISpatialDistribution<IShape> distribution;
	private List<ISpatialConstraint> constraints;
	private ConstraintsReleaseRule rule;
	
	public SPLinker(ISpatialDistribution<IShape> distribution) {
		this.distribution = distribution;
		this.constraints = new ArrayList<>();
		this.rule = ConstraintsReleaseRule.PRIORITY;
	}
	
	public SPLinker(ISpatialDistribution<IShape> distribution, ConstraintsReleaseRule rule) {
		this(distribution);
		this.rule = rule;
	}

	@Override 
	public Optional<IShape> getCandidate(IScope scope, IAgent entity,
			IList<IShape> candidates) {

		IList<IShape> filteredCandidates = this.filter(scope, candidates);

		return filteredCandidates.isEmpty() ? Optional.empty() : 
			Optional.ofNullable(distribution.getCandidate(scope, entity, filteredCandidates));
	}
	
	@Override
	public Map<IAgent, Optional<IShape>>  getCandidates(IScope scope,IList<IAgent> entities,
			IList<IShape> candidates) {	
		
		Map<IAgent, Optional<IShape>> res = entities.stream()
				.collect(Collectors.toMap(Function.identity(), e -> this.getCandidate(scope, e,candidates)));
		
		Collection<IAgent> unbindedEntities = new LinkedHashSet<>();
		for(IAgent e : res.keySet()) { 
			if(!res.get(e).isPresent()) unbindedEntities.add(e);
			else constraints.forEach(c -> c.updateConstraint(res.get(e).get()));
		}
		
		if(!unbindedEntities.isEmpty()) {
			IList<IShape> filteredCandidates = null;
			do {
				filteredCandidates = this.filterWithRelease(scope, candidates);
				res.clear();
				for(IAgent e : unbindedEntities) {
					Optional<IShape> oNest = Optional.ofNullable(
							distribution.getCandidate(scope, e, filteredCandidates.copy(scope))); 
					if(oNest.isPresent()) {
						res.put(e, oNest); 
						constraints.stream().forEach(c -> c.updateConstraint(oNest.get()));
					}
				}
				unbindedEntities = res.keySet().stream().filter(e -> !res.get(e).isPresent()).toList();
			} while (!unbindedEntities.isEmpty() || this.constraints.stream().allMatch(ISpatialConstraint::isConstraintLimitReach));
		}
		return res;
	}
	
	@Override
	public void setDistribution(ISpatialDistribution<IShape> distribution) {
		this.distribution = distribution;
	}

	@Override
	public ISpatialDistribution<IShape> getDistribution() {
		return distribution;
	}
	
	@Override
	public IList<IShape> filterWithRelease(IScope scope,
			IList<IShape> candidates) {
		IList<IShape> filteredCandidates = candidates.copy(scope);
		List<ISpatialConstraint> scs = constraints.stream().sorted(
				(c1,c2) -> Integer.compare(c1.getPriority(), c2.getPriority()))
				.toList();
		switch(rule) {
		case LINEAR:
			do {
				IList<IShape> newFilteredCandidates =filteredCandidates.copy(scope);
				for(ISpatialConstraint sc : scs.stream()
						.filter(c -> !c.isConstraintLimitReach())
						.toList()) {
					newFilteredCandidates = sc.getCandidates(scope, filteredCandidates);
					if(newFilteredCandidates.isEmpty()) {
						sc.relaxConstraint(newFilteredCandidates); 
						newFilteredCandidates = sc.getCandidates(scope, newFilteredCandidates);
					}
				}
				if(!newFilteredCandidates.isEmpty()) {
					return newFilteredCandidates;
				}
			} while(scs.stream().noneMatch(c -> !c.isConstraintLimitReach()));
			return GamaListFactory.EMPTY_LIST;
		default:
			for(ISpatialConstraint sc : scs) {
				IList<IShape> newFilteredCandidates = sc.getCandidates(scope, filteredCandidates);
				if(newFilteredCandidates.isEmpty()) {
					do {
						sc.relaxConstraint(filteredCandidates);
						newFilteredCandidates = sc.getCandidates(scope, filteredCandidates);
					} while(!newFilteredCandidates.isEmpty() &&
							!sc.isConstraintLimitReach());
				}
				if(newFilteredCandidates.isEmpty())
					return GamaListFactory.EMPTY_LIST;
				filteredCandidates = newFilteredCandidates;
			}
			return filteredCandidates;
		}
		
	}
	
	@Override
	public IList<IShape> filter(IScope scope,
			IList<IShape> candidates) {
		IList<IShape> filteredCandidates = candidates.copy(scope);
		List<ISpatialConstraint> scs = constraints.stream().sorted(
				(c1,c2) -> Integer.compare(c1.getPriority(), c2.getPriority()))
				.toList();
		for(ISpatialConstraint sc : scs) {
			filteredCandidates = sc.getCandidates(scope, filteredCandidates);
			if(filteredCandidates.isEmpty()) 
				return GamaListFactory.create();
		}
		return filteredCandidates;	 
	}

	@Override
	public void setConstraints(List<ISpatialConstraint> constraints) {
		this.constraints = constraints;
	}
	
	@Override
	public void addConstraints(ISpatialConstraint... constraints) {
		this.constraints.addAll(Arrays.asList(constraints));
		
	}

	@Override
	public List<ISpatialConstraint> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	@Override
	public ConstraintsReleaseRule getConstraintsReleaseRule() {
		return this.rule;
	}

	@Override
	public void setConstraintsReleaseRule(ConstraintsReleaseRule rule) {
		this.rule = rule;
	}

}
