package org.smartdeveloperhub.vocabulary.util;

import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

public class VocabularyHelperTest {

	@Test
	public void testClasses() throws Exception {
		final Result<Catalog> result=
			Catalogs.
				loadFrom(
					Paths.get("src","main","resources","vocabulary"),
					TestHelper.BASE);
		final Catalog catalog=result.get();
		final Module ci = catalog.resolve(URI.create("v1/ci"));
		final VocabularyHelper helper = VocabularyHelper.create(ci);
		dumpValues("classes", helper.classes());
		dumpValues("object properties", helper.objectProperties());
		dumpValues("datatype properties", helper.datatypeProperties());
		dumpValues("individuals", helper.individuals());
		dumpValues("URIrefs", helper.uriRefs());
	}

	private void dumpValues(final String type, final List<String> values) {
		if(values.isEmpty()) {
			System.out.printf("No %s available%n",type);
		} else {
			System.out.printf("%d %s available%n",values.size(),type);
			for(final String value:values) {
				System.out.printf("- %s%n",value);
			}
		}
	}

}
