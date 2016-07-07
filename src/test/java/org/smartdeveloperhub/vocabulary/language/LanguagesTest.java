/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015-2016 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.smartdeveloperhub.vocabulary:sdh-vocabulary:0.4.0-SNAPSHOT
 *   Bundle      : sdh-vocabulary-0.4.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.vocabulary.language;

import java.util.Locale;

import org.junit.Test;

public class LanguagesTest {

	@Test
	public void testGetInstance() throws Exception {
		System.out.printf("es ---> %s%n",Languages.getInstance().uri("es"));
		System.out.printf("es --[ENGLISH]--> %s%n",Languages.getInstance().localizedName("es",Locale.ENGLISH));
		System.out.printf("es --[FRENCH ]--> %s%n",Languages.getInstance().localizedName("es",Locale.FRENCH));
		System.out.printf("es --[GERMAN ]--> %s%n",Languages.getInstance().localizedName("es",Locale.GERMAN));
		System.out.printf("es --[CHINESE]--> %s%n",Languages.getInstance().localizedName("es",Locale.CHINESE));
		System.out.printf("spa --> %s%n",Languages.getInstance().uri("spa"));
		System.out.printf("spa --[ENGLISH]--> %s%n",Languages.getInstance().localizedName("spa",Locale.ENGLISH));
		System.out.printf("spa --[FRENCH ]--> %s%n",Languages.getInstance().localizedName("spa",Locale.FRENCH));
		System.out.printf("spa --[GERMAN ]--> %s%n",Languages.getInstance().localizedName("spa",Locale.GERMAN));
		System.out.printf("spa --[CHINESE]--> %s%n",Languages.getInstance().localizedName("spa",Locale.CHINESE));
	}

}
