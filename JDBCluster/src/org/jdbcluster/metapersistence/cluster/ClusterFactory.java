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
package org.jdbcluster.metapersistence.cluster;

import java.util.List;

import org.jdbcluster.JDBClusterUtil;
import org.jdbcluster.clustertype.ClusterType;
import org.jdbcluster.clustertype.ClusterTypeBase;
import org.jdbcluster.clustertype.ClusterTypeFactory;
import org.jdbcluster.dao.Dao;
import org.jdbcluster.exception.ClusterTypeException;
import org.jdbcluster.exception.ConfigurationException;
import org.jdbcluster.exception.PrivilegeException;
import org.jdbcluster.metapersistence.annotation.DaoLink;
import org.jdbcluster.metapersistence.security.user.IUser;
import org.jdbcluster.privilege.PrivilegeChecker;
import org.jdbcluster.privilege.PrivilegeCheckerImpl;
import org.jdbcluster.privilege.PrivilegedCluster;
import org.springframework.util.Assert;

/**
 * class ClusterFactory is responsible for creating instances
 * of type Cluster. <b>Static</b> privileges are checked <b>after</b>
 * calling ClusterInterceptor
 * @author Philipp Noggler
 * @author FaKod
 *
 */
public abstract class ClusterFactory {
	
	private static ClusterInterceptor clusterInterceptor;
	
	/**
	 * creates an instance of a Cluster
	 * @param ct specifies the ClusterType class that should be returned
	 * @param user the User object.
	 * @return Cluster
	 */
	public static <T extends ICluster> T newInstance(ClusterType ct, IUser user) {
		return newInstance(ct, null, user);
	}
	
	/**
	 * no need to create ClusterType instance
	 * @param clusterType cluster type string as configured
	 * @param The user object.
	 * @return cluster instance
	 */
	public static <T extends ICluster> T newInstance(String clusterType, IUser user) {
		ClusterType ct = ClusterTypeFactory.newInstance(clusterType);
		return newInstance(ct, null, user);
	}
	
	/**
	 * no need to create ClusterType instance
	 * @param clusterType cluster type string as configured
	 * @param The user object.
	 * @return cluster instance
	 */
	public static <T extends ICluster> T newInstance(String clusterType, Object dao, IUser user) {
		ClusterType ct = ClusterTypeFactory.newInstance(clusterType);
		return newInstance(ct, dao, user);
	}
	
	/**
	 * creates and return the configured ClusterInterceptor
	 * @return ClusterInterceptor
	 */
	public static ClusterInterceptor getClusterInterceptor() {
		if(clusterInterceptor==null) {
			String ciStr = ClusterTypeBase.getClusterTypeConfig().getClusterInterceptorClassName();
			if(ciStr!=null && ciStr.length()>0 )
				clusterInterceptor = (ClusterInterceptor) JDBClusterUtil.createClassObject(ciStr);
			else
				clusterInterceptor = (ClusterInterceptor) JDBClusterUtil.createClassObject(DefaultClusterInterceptor.class.getName());
		}
		return clusterInterceptor;
	}
	
	/**
	 * creates an instance of a Cluster
	 * Cluster interceptor is <b>not called</b> if dao!=null
	 * Cluster privileges are <b>not checked</b> if dao!=null 
	 * @param ct specifies the ClusterType class that should be returned
	 * @param dao dao object to be presetted
	 * @param The user object.
	 * @return Cluster
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ICluster> T newInstance(ClusterType ct, Object dao, IUser user) {
		Cluster cluster = newInstance(getClusterClass(ct), dao, user);
		cluster.setClusterType(ct);
		return (T) cluster;
	}
	
	/**
	 * creates an instance of a Cluster
	 * Cluster interceptor is <b>not called</b> if dao!=null
	 * Cluster privileges are <b>not checked</b> if dao!=null 
	 * If dao is not null its assumed that the dao object is persistent 
	 * @param clusterClass class of cluster
	 * @param dao dao object to be presetted
	 * @param The user object.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Cluster> T newInstance(Class<? extends Cluster> clusterClass, Object dao, IUser user) {
		return newInstance(clusterClass, dao, dao!=null, user);
	}
	
	/**
	 * creates an instance of a Cluster
	 * Cluster interceptor is <b>not called</b> if dao!=null
	 * Cluster privileges are <b>not checked</b> if dao!=null 
	 * @param clusterClass class of cluster
	 * @param dao dao object to be presetted
	 * @param daoIsPersistent if dao Object is persistent dont call interceptor and pivilegeInterceptor
	 * @param user saves the given User object into the newly created Cluster object.
	 * @return the new Cluster instance.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Cluster> T newInstance(Class<? extends ICluster> clusterClass, Object dao, boolean daoIsPersistent, IUser user) {
		
		Assert.notNull(clusterClass, "Class<?> may not be null");
		
		PrivilegeChecker pc = PrivilegeCheckerImpl.getInstance();
		Cluster cluster = null;
		try {
			//create a new instance with given classname
			cluster = (Cluster) clusterClass.newInstance();
		} catch (InstantiationException e) {
			throw new ClusterTypeException("specified class [" + clusterClass.getName() + "] object cannot be instantiated because it is an interface or is an abstract class", e);
		} catch (IllegalAccessException e) {
			throw new ClusterTypeException("the currently executed ctor for class [" + clusterClass.getName() + "] does not have access", e);
		} 
		if(user != null) {
			cluster.setUser(user);
		}
		if(dao!=null)
			cluster.setDao(dao);
		else
		{
			DaoLink classAnno = clusterClass.getAnnotation(DaoLink.class);
			if (classAnno != null)
				cluster.setDao(Dao.newInstance(classAnno.dAOClass()));
		}
		
		if(!daoIsPersistent) {
			/*
			 * call of cluster interceptor
			 */
			if(!getClusterInterceptor().clusterNew(cluster))
				throw new ConfigurationException("ClusterInterceptor [" + getClusterInterceptor().getClass().getName() + "] returned false" );
			
			/*
			 * privilege check (only static privileges are checked)
			 */
			if(cluster instanceof PrivilegedCluster) {
				if(!pc.userPrivilegeIntersect(user, (PrivilegedCluster)cluster))
					throw new PrivilegeException("No sufficient privileges for new Cluster class [" + clusterClass.getName() + "]");
			}
		}
		cluster.setClusterType(ClusterTypeFactory.newInstance(clusterClass));
		return (T) cluster;
	}
	/**
	 * get the cluster class object
	 * @param ct specifies the ClusterType class that should be returned
	 * @return Class<? extends Cluster> of ClusterType
	 */
	@SuppressWarnings("unchecked")
	public static Class<? extends Cluster> getClusterClass(ClusterType ct) {
		
		Assert.notNull(ct, "ClusterType may not be null");
		
		String className = ClusterTypeBase.getClusterTypeConfig().getClusterClassName(ct.getName());
		if (className == null) {
			throw new ConfigurationException("unknown ClusterType [" + ct.getName() + "]");
		}

		Class<? extends Cluster> clusterClass;
		clusterClass = ct.getClusterClass();
		
		return clusterClass;
	}
	
	/**
	 *  get the cluster class object
	 * @param clusterType specifies the ClusterType class that should be returned
	 * @return Class<? extends Cluster> of ClusterType
	 */
	public static Class<? extends Cluster> getClusterClass(String clusterType) {
		
		Assert.notNull(clusterType, "String clusterType may not be null");
		
		ClusterType ct = ClusterTypeFactory.newInstance(clusterType);
		return getClusterClass(ct);
	}
	
	/**
	 * get the cluster class object from a Dao instance
	 * @param dao Dao instance to search cluster
	 * @return Class<? extends Cluster> class of corresponding cluster
	 */
	@SuppressWarnings("unchecked")
	public static Class<? extends Cluster> getClusterFromDao(Object dao) {
		
		Assert.notNull(dao, "Dao dao may not be null");
		
		List<String> clusterIDs = ClusterTypeBase.getClusterTypeConfig().getClusterIDs();
		for( String s : clusterIDs) {
			Class<? extends Cluster> clusterClass = null;
			String className = ClusterTypeBase.getClusterTypeConfig().getClusterClassName(s);
			try {
				clusterClass = (Class<? extends Cluster>) Class.forName(className);
				DaoLink classAnno = clusterClass.getAnnotation(DaoLink.class);
				if (classAnno != null) {
					if(classAnno.dAOClass() == dao.getClass())
						return clusterClass;
				}
			} catch (ClassNotFoundException e) {
				throw new ClusterTypeException("no definition for the class [" + className + "] with the specified name could be found", e);
			}
		}
		return null;
	}
	
}
