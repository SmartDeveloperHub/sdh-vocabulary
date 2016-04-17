package org.smartdeveloperhub.vocabulary.util;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;

public class TestHelper {

	static final Path    ROOT    = Paths.get("src","test","resources","vocabulary");
	static final URI     BASE    = URI.create("http://www.smartdeveloperhub.org/vocabulary/");
	static final Context CONTEXT = Context.create(BASE,ROOT);

	static Model load(final String relativePath) throws IOException {
		final Path file=ROOT.resolve(relativePath);
		final Model model=ModelFactory.createDefaultModel();
		final RDFReader reader=model.getReader("TURTLE");
		reader.setProperty("error-mode", "strict-fatal");
		reader.
			read(
				model,
				new FileReader(
					file.toFile()),
					CONTEXT.getCanonicalNamespace(file).toString());
		return model;
	}

	static String uriRef(final String ontology, final String localPart) {
		return BASE.resolve(ontology+"#"+localPart).toString();
	}

	static Path moduleLocation(final String name) {
		return ROOT.resolve(name);
	}

}
