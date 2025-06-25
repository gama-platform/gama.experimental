/*******************************************************************************************************
 *
 * Predicate.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.experimental.types;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import gama.core.common.interfaces.IValue;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class Predicate.
 */
public class ContentRetriever implements IValue {
 
	dev.langchain4j.rag.content.retriever.ContentRetriever contentRetriever = null;
	
	@Override
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(),"content_retriever", contentRetriever);
	}

	public ContentRetriever(Path path) {
		List<Document> documents = null;
		try {
			documents = FileSystemDocumentLoader.loadAllFromDirectory(
			          path
			    );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		if (documents!=null && documents.size() > 0) {
			

			// Here, we create an empty in-memory store for our documents and their
			// embeddings.
			InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
			// Here, we are ingesting our documents into the store.
			// Under the hood, a lot of "magic" is happening, but we can ignore it for now.
			EmbeddingStoreIngestor.ingest(documents, embeddingStore);
			
			// Lastly, let's create a content retriever from an embedding store.
			contentRetriever = EmbeddingStoreContentRetriever.from(embeddingStore);
			
			
		}
	}




	@Override
	public String toString() { 
		return "content_retriever(" + contentRetriever.toString() +")";
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return toString();
	}

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return toString();
	}

	

	@Override
	public int hashCode() {
		return contentRetriever.hashCode();
	} 

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final ContentRetriever other = (ContentRetriever) obj;
		return contentRetriever.equals(other.contentRetriever); 
	}

	/**
	 * Method getType()
	 *
	 * @see gama.core.common.interfaces.ITyped#getGamlType()
	 */
	@Override
	public IType<?> getGamlType() { return Types.get(ContentRetrieverType.id); }



	/*public static PathMatcher glob(String glob) {
		return FileSystems.getDefault().getPathMatcher("glob:" + glob);
	}*/



	@Override
	public IValue copy(IScope scope) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

}
