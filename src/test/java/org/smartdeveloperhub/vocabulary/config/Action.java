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
package org.smartdeveloperhub.vocabulary.config;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

final class Action {

	private final String field;
	private final Object value;
	private final Map<String,Object> current;
	private final Map<String,Object> parent;
	private final String ctx;

	private Action(final String field, final Object value, final Map<String,Object> target, final Map<String,Object> parent, final String ctx) {
		this.field = field;
		this.value = value;
		this.current = target;
		this.parent = parent;
		this.ctx = ctx;
	}

	private String context(final String field) {
		return this.ctx+(this.ctx.isEmpty()?this.ctx:".")+field;
	}

	Map<String,Object> execute() {
		final Map<String,Object> overrides=Maps.newLinkedHashMap();
		if(this.field!=null) {
			if(this.value!=null) {
				final Object previous=this.current.put(this.field,this.value);
				addOverrides(overrides,this.ctx,previous);
			} else if(this.parent!=null){
				final Object previous = this.parent.put(this.field,this.current);
				addOverrides(overrides,this.ctx,previous);
			}
		}
		return overrides;
	}

	Action override(final String field, final Object value) {
		return new Action(field,value,this.current,null,context(field));
	}

	@SuppressWarnings("unchecked")
	Action merge(final String field) {
		final String currentProperty=context(field);
		final Object object=this.current.get(field);
		if(object instanceof Map) {
			return new Action(field,null,(Map<String,Object>)object,null,currentProperty);
		} else {
			return new Action(field,null,Maps.<String,Object>newLinkedHashMap(),this.current,currentProperty);
		}
	}

	static Action extend(final String ctx, final Map<String,Object> current) {
		return new Action(null,null,current,null,Strings.nullToEmpty(ctx));
	}

	private static void addOverrides(final Map<String, Object> overrides,final String property,final Object value) {
		if(value==null) {
			return;
		}
		if(value instanceof Map) {
			@SuppressWarnings("unchecked")
			final Map<String,Object> composite=(Map<String,Object>)value;
			for(final Entry<String,Object> entry:composite.entrySet()) {
				final String key = property+(property.isEmpty()?"":".")+entry.getKey();
				addOverrides(overrides,key,entry.getValue());
			}
		} else {
			overrides.put(property,value);
		}
	}

}