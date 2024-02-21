package gama.experimental.matlab.gaml.files;

import gama.annotations.precompiler.IConcept;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.core.common.geometry.Envelope3D;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.file.GamaFile;
import gama.gaml.operators.Strings;
import gama.gaml.types.IContainerType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

@file (
	name = "matlab",
	extensions = { "m" },
	buffer_type = IType.LIST,
	buffer_content = IType.STRING,
	buffer_index = IType.INT,
	concept = { IConcept.FILE},
	doc = @doc ("Represents a matlb script file. The internal contents is the set of the script lines."))
@SuppressWarnings ({ "unchecked" })
public class MatlabFile extends GamaFile<IList<String>, String> {

	@doc("Initialisation of a matlab file: it reads the file and store line by line all the script statements")
	public MatlabFile(IScope scope, String pathName) throws GamaRuntimeException {
		super(scope, pathName);

		fillBuffer(scope);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see msi.gama.util.GamaFile#fillBuffer()
	 */
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) { return; }
		try (BufferedReader in = new BufferedReader(new FileReader(getFile(scope)))) {
			final StringBuilder sb = new StringBuilder();	
			
			// Continue with the core of the file
			String str = in.readLine();
			while (str != null) {
				sb.append(str);
				sb.append(System.lineSeparator());
				str = in.readLine();
			}
			final IList<String> contents = GamaListFactory.create(Types.STRING);
			contents.add(sb.toString());	
			setBuffer(contents);
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}		

	@Override
	public String _stringValue(final IScope scope) throws GamaRuntimeException {
		getContents(scope);
		final StringBuilder sb = new StringBuilder(getBuffer().length(scope) * 200);
		for (final String s : getBuffer().iterable(scope)) {
			sb.append(s).append(Strings.LN);
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}	
	
	@Override
	public Envelope3D computeEnvelope(IScope scope) {
		return null;
	}
	
	@Override
	public IContainerType<?> getGamlType() {
		return Types.FILE.of(Types.INT, Types.STRING);
	}
	
}