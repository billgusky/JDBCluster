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
package org.jdbcluster.clustertype;

import org.jdbcluster.metapersistence.cluster.Cluster;

/**
 * interface ClusterType defines methods to get a specific 
 * ClusterType and it's name.
 * @author Philipp Noggler
 * @author Christopher Schmidt
 * 
 */

public interface ClusterType {
	
	/**
	 * gets a specific ClusterType by given CTName
	 * @param CTName specifies the ClusterType's name
	 * @return String
	 */
	public String getClusterType(String CTName);
	
	/**
	 * gets the specific Cluster Class by a given name
	 * @return Class of corresponding cluster
	 */
	public Class<? extends Cluster> getClusterClass();
	
	/**
	 * gets the ClusterType name
	 * @return String
	 */
	public String getName();
	
	/**
	 * gets the configured Cluster Class Name as a String
	 * @deprecated use {@link ClusterType.getClusterClass()} instead
	 * @return Class Name of Cluster Object
	 */
	@Deprecated
	public String getClusterClassName();
}
