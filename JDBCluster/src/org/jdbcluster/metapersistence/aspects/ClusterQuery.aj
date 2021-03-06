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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.jdbcluster.clustertype.ClusterType;
import org.jdbcluster.metapersistence.cluster.Cluster;
import org.jdbcluster.metapersistence.cluster.ClusterFactory;
import org.jdbcluster.template.hibernate.HibernateQuery;

/**
 * Aspect used after a native (fe. hibernate) query was
 * executed. We have to convert Dao objects
 * to Cluster objects
 * @author Christopher Schmidt
 */
public aspect ClusterQuery extends ClusterBaseAspect {
	
	private Logger logger = Logger.getLogger(ClusterQuery.class);
	
	pointcut resultList(HibernateQuery q):
		execution(* HibernateQuery+.list(..)) && 
		within(HibernateQuery) && 
		target(q);
	
	pointcut resultListUnique(HibernateQuery q):
		execution(* HibernateQuery+.listUnique(..)) && 
		within(HibernateQuery) && 
		target(q);

	@SuppressWarnings("unchecked")
	@SuppressAjWarnings("adviceDidNotMatch")
	Object around(HibernateQuery q):resultList(q) {
		ClusterType ct = q.getClusterType();
		List dAOResultSet = (List) proceed(q);
		List<Cluster> clusterResultSet = new ArrayList<Cluster>();
				
		if(logger.isDebugEnabled())
			logger.debug("Query result converting " + dAOResultSet.size() + " elements to Cluster");
		
		for (Object dao : dAOResultSet) {
			Cluster c = ClusterFactory.newInstance(ct, dao, q.getUser());
			clusterResultSet.add(c);
		}

		return clusterResultSet;
	}
	
	@SuppressWarnings("unchecked")
	@SuppressAjWarnings("adviceDidNotMatch")
	Object around(HibernateQuery q):resultListUnique(q) {
		ClusterType ct = q.getClusterType();
		List dAOResultSet = (List) proceed(q);
		List<Cluster> clusterResultSet = new ArrayList<Cluster>();
		
		/*
		 * create unique result
		 */
		Set uniqueSet = new HashSet();
		
		for (Object dao : dAOResultSet) {
			if(uniqueSet.add(dao)) { 
				Cluster c = ClusterFactory.newInstance(ct, dao, q.getUser());
				clusterResultSet.add(c);
			}
		}
		
		if(logger.isDebugEnabled())
			logger.debug("Query result converting " + clusterResultSet.size() + "unique elements to Cluster");
	
		return clusterResultSet;
	}
}
