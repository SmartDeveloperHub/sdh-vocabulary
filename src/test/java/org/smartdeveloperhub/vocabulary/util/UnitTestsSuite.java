package org.smartdeveloperhub.vocabulary.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	CatalogTest.class,
	CatalogsTest.class,
	VocabularyHelperTest.class,
	PropertyDefinitionTest.class
})
public class UnitTestsSuite {
}
