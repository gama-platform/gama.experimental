/*******************************************************************************************************
 *
 * GamaGeoJsonFile.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.experimental.cim.files;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.citygml4j.cityjson.CityJSONContext;
import org.citygml4j.cityjson.CityJSONContextException;
import org.citygml4j.cityjson.reader.CityJSONInputFactory;
import org.citygml4j.cityjson.reader.CityJSONReadException;
import org.citygml4j.cityjson.reader.CityJSONReader;
import org.citygml4j.core.model.building.Building;
import org.citygml4j.core.model.core.AbstractCityObject;
import org.citygml4j.core.model.core.AbstractCityObjectProperty;
import org.citygml4j.core.model.core.CityModel;
import org.citygml4j.core.model.generics.GenericOccupiedSpace;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryType;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.annotations.precompiler.IConcept;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.file.GamaGisFile;
import gama.gaml.types.IType;
import gama.gaml.types.Types;


/**
 * The Class GamaGeoJsonFile.
 */
@file (
		name = "cityjson",
		extensions = { "json" },
		buffer_type = IType.LIST,
		buffer_content = IType.GEOMETRY,
		buffer_index = IType.INT,
		concept = { IConcept.GIS, IConcept.FILE },
		doc = @doc ("Represents geospatial files written using the GeoJSON format. The internal representation is a list of geometries"))
public class GamaCityJsonFile extends GamaGisFile {
	
	/** 
	 * Instantiates a new gama geo json file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read a cityjson file (https://www.cityjson.org)",
			examples = { @example (
					value = "file f <- geojson_file(\"file.json\");",
					isExecutable = false) })
	public GamaCityJsonFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName, (Integer) null);
		
		
	}

	/**
	 * Instantiates a new gama geo json file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 */
	@doc (
			value = "This file constructor allows to read a cityjson file and specifying the coordinates system code, as an int",
			examples = { @example (
					value = "file f <- geojson_file(\"file.json\", 32648);",
					isExecutable = false) })
	public GamaCityJsonFile(final IScope scope, final String pathName, final Integer code) {
		super(scope, pathName, code);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new gama geo json file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 */
	@doc (
			value = "This file constructor allows to read a geojson file and specifying the coordinates system code (epg,...,), as a string",
			examples = { @example (
					value = "file f <- geojson_file(\"file.json\", \"EPSG:32648\");",
					isExecutable = false) })
	public GamaCityJsonFile(final IScope scope, final String pathName, final String code) {
		super(scope, pathName, code);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new gama geo json file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param withZ
	 *            the with Z
	 */
	@doc (
			value = "This file constructor allows to read a geojson file and take a potential z value (not taken in account by default)",
			examples = { @example (
					value = "file f <- geojson_file(\"file.json\", true);",
					isExecutable = false) })
	public GamaCityJsonFile(final IScope scope, final String pathName, final boolean withZ) {
		super(scope, pathName, (Integer) null, withZ);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new gama geo json file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @param withZ
	 *            the with Z
	 */
	@doc (
			value = "This file constructor allows to read a geojson file, specifying the coordinates system code, as an int and take a potential z value (not taken in account by default)",
			examples = { @example (
					value = "file f <- geojson_file(\"file.json\",32648, true);",
					isExecutable = false) })
	public GamaCityJsonFile(final IScope scope, final String pathName, final Integer code, final boolean withZ) {
		super(scope, pathName, code, withZ);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new gama geo json file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @param withZ
	 *            the with Z
	 */
	@doc (
			value = "This file constructor allows to read a geojson file, specifying the coordinates system code (epg,...,), as a string and take a potential z value (not taken in account by default",
			examples = { @example (
					value = "file f <- geojson_file(\"file.json\", \"EPSG:32648\",true);",
					isExecutable = false) })
	public GamaCityJsonFile(final IScope scope, final String pathName, final String code, final boolean withZ) {
		super(scope, pathName, code, withZ);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		setBuffer(GamaListFactory.<IShape> create(Types.GEOMETRY));
		readShapes(scope);
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		final Map<String, String> attributes = new LinkedHashMap<>();
		final SimpleFeatureCollection store = getFeatureCollection(scope);
		final java.util.List<AttributeDescriptor> att_list = store.getSchema().getAttributeDescriptors();
		for (final AttributeDescriptor desc : att_list) {
			String type;
			if (desc.getType() instanceof GeometryType) {
				type = "geometry";
			} else {
				type = Types.get(desc.getType().getBinding()).toString();
			}
			attributes.put(desc.getName().getLocalPart(), type);
		}

		return GamaListFactory.wrap(Types.STRING, attributes.keySet());
	}

	@Override
	protected SimpleFeatureCollection getFeatureCollection(final IScope scope) {
		try {
				
	        CityJSONContext context = CityJSONContext.newInstance();
			
	        CityJSONInputFactory in = context.createCityJSONInputFactory();

	       
	        CityModel cityModel = null;
	        try (CityJSONReader reader = in.createCityJSONReader(getFile(scope).toPath())) {
	            cityModel = (CityModel) reader.next();
	        } catch (CityJSONReadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        cityModel.getFeatureMembers().forEach(i -> System.out.println(i));
	        
	        Map<String, Integer> cityObjects = new TreeMap<>();
	        for (AbstractCityObjectProperty cityObjectMember : cityModel.getCityObjectMembers()) {
	            AbstractCityObject cityObject = cityObjectMember.getObject();
	            
	            System.out.println("Lower corner of CIM: "+cityModel.getBoundedBy().getEnvelope().getLowerCorner().getValue());
				System.out.println("CIMObject is "+cityObject.getId());
				
	            String sn = cityObject.getClass().getSimpleName();
	            switch (sn) {
				case "Building" -> { 
					Building b = (Building) cityObject;
					System.out.println(b.getADEProperties());
				}
				case "GenericOccupiedSpace" -> { 
					GenericOccupiedSpace b = (GenericOccupiedSpace) cityObject;
					System.out.println(b.getADEProperties());
					
				}
				default ->
				throw new IllegalArgumentException("Unexpected city object: " + sn);
				}
	            
	            cityObjects.merge(cityObject.getClass().getSimpleName(), 1, Integer::sum);
	        }

	        cityObjects.forEach((key, value) -> System.out.println(key + ": " + value + " instance(s)"));

		} catch (CityJSONContextException e) {
				e.printStackTrace();
		}

		return null;
	}

}
