/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbcluster.metapersistence.aspects;

import org.jdbcluster.template.*;
import org.jdbcluster.metapersistence.cluster.*;

/**
 * @author Christopher Schmidt
 * calles to save(), update() etc. have to use cluster objects
 */
public aspect ClusterSessionAspect extends ClusterBaseAspect {

	pointcut sessionOperation(Object o): 
		(call(* SessionTemplate+.*(Object)) && args(o));
	
	Object around(Object o): sessionOperation(o) {
		if(o instanceof ClusterBase) {
			return proceed(((ClusterBase)o).getDao());
		}
		throw new IllegalArgumentException("Please use only ClusterBase Objects"); 
    }
}
