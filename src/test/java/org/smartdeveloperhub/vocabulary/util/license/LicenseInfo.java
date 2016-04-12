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
 *   Artifact    : org.smartdeveloperhub.vocabulary:sdh-vocabulary:0.3.0-SNAPSHOT
 *   Bundle      : sdh-vocabulary-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.vocabulary.util.license;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Example
 * <pre>{@code
 * {
 *   "legalcode": "[\"en\", ]",
 *   "label": "GNU General Public License",
 *   "title": "GNU General Public License",
 *   "uri": "http://purl.org/NET/rdflicense/gpl2.0",
 *   "version": "2.0",
 *   "seeAlso": "http://gnu.org/licenses/gpl-2.0.html"
 * }}</pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	LicenseInfo.LEGAL_CODE,
	LicenseInfo.LABEL,
	LicenseInfo.TITLE,
	LicenseInfo.URI,
	LicenseInfo.VERSION,
	LicenseInfo.SEE_ALSO
})
final class LicenseInfo {

	static final String LEGAL_CODE = "legalcode";
	static final String LABEL      = "label";
	static final String TITLE      = "title";
	static final String URI        = "uri";
	static final String VERSION    = "version";
	static final String SEE_ALSO   = "seeAlso";

	@JsonProperty(LEGAL_CODE)
	private String legalCode;
	@JsonProperty(LABEL)
	private String label;
	@JsonProperty(TITLE)
	private String title;
	@JsonProperty(URI)
	private String uri;
	@JsonProperty(VERSION)
	private String version;
	@JsonProperty(SEE_ALSO)
	private String seeAlso;

	LicenseInfo() {
		// Package-private
	}

	public String getLegalCode() {
		return this.legalCode;
	}
	public void setLegalCode(final String legalCode) {
		this.legalCode = legalCode;
	}
	public String getLabel() {
		return this.label;
	}
	public void setLabel(final String label) {
		this.label = label;
	}
	public String getTitle() {
		return this.title;
	}
	public void setTitle(final String title) {
		this.title = title;
	}
	public String getUri() {
		return this.uri;
	}
	public void setUri(final String uri) {
		this.uri = uri;
	}
	public String getVersion() {
		return this.version;
	}
	public void setVersion(final String version) {
		this.version = version;
	}
	public String getSeeAlso() {
		return this.seeAlso;
	}
	public void setSeeAlso(final String seeAlso) {
		this.seeAlso = seeAlso;
	}
}