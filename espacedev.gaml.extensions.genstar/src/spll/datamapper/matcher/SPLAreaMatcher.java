package spll.datamapper.matcher;

import msi.gama.metamodel.shape.IShape;

public class SPLAreaMatcher implements ISPLMatcher<String, Double> {

	private double area;
	
	private final String variable;

	private IShape entity;
	
	protected SPLAreaMatcher(IShape entity, String variable){
		this(entity, variable, 1d);
	}
	
	protected SPLAreaMatcher(IShape entity, String variable, double area){
		this.entity = entity;
		this.variable = variable;
		this.area = area;
	}
	
	@Override
	public boolean expandValue(Double area){
		this.area += area;
		return true;
	}
	
	@Override
	public Double getValue(){
		return area;
	}
	
	@Override
	public String getVariable(){
		return variable;
	}

	@Override
	public IShape getEntity() {
		return entity;
	}
	
	// -------------------------------------------------- //
	
	@Override
	public String toString() {
		return entity.toString()+" => ["+getVariable()+" = "+area+"]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(area);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((variable == null) ? 0 : variable.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SPLAreaMatcher other = (SPLAreaMatcher) obj;
		if (Double.doubleToLongBits(area) != Double.doubleToLongBits(other.area))
			return false;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity))
			return false;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}
	
	
	
}
