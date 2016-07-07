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
package org.smartdeveloperhub.vocabulary.ci.spi;

public abstract class RDFUtil {

	private static final class NullRDFUtil extends RDFUtil {

		private static final String NO_DELEGATE_REGISTERED_YET = "No delegate registered yet";

		@Override
		protected int localNameIndex(String uri) {
			throw new IllegalStateException(NO_DELEGATE_REGISTERED_YET);
		}

		@Override
		protected String encodeURI(String label) {
			throw new IllegalStateException(NO_DELEGATE_REGISTERED_YET);
		}

		@Override
		protected String encodeString(String label, boolean isLong) {
			throw new IllegalStateException(NO_DELEGATE_REGISTERED_YET);
		}

	}

	private static RDFUtil DELEGATE=new NullRDFUtil();

	public static void setDelegate(RDFUtil delegate) {
		if(delegate==null) {
			RDFUtil.DELEGATE=new NullRDFUtil();
		} else {
			RDFUtil.DELEGATE=delegate;
		}
	}

	private static RDFUtil getDelegate() {
		return RDFUtil.DELEGATE;
	}

	public static int getLocalNameIndex(String uri) {
		return getDelegate().localNameIndex(uri);
	}

	public static String encodeLongString(String label) {
		return getDelegate().encodeString(label,true);
	}

	public static String encodeString(String label) {
		return getDelegate().encodeString(label,false);
	}


	protected abstract int localNameIndex(String uri);

	protected abstract String encodeString(String label, boolean isLong);

	protected abstract String encodeURI(String label);

	public static String encodeURIString(String string) {
		return getDelegate().encodeURI(string);
	}

}
