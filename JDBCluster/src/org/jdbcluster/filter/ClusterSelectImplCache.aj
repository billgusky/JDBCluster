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
package org.jdbcluster.filter;

import org.jdbcluster.clustertype.ClusterType;
import org.jdbcluster.metapersistence.aspects.XmlCache;
import org.jdbcluster.metapersistence.aspects.XmlCachable;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Implementing cache of class ClusterSelectImpl
 * @author FaKod
 */
public aspect ClusterSelectImplCache extends XmlCache {
	
	/**
	 * mark class as cachable
	 */
	declare parents : ClusterSelectImpl implements XmlCachable;

	/**
	 * do nothing cause of special treatment
	 */
	public pointcut xmlCache0(XmlCachable xmlc);
	public pointcut xmlCache1(XmlCachable xmlc, String str);
	public pointcut xmlCache2(XmlCachable xmlc, String str, String str2);

	/**
	 * pointcut for ClusterType and String parameter methods
	 */
	pointcut doCache(XmlCachable xmlc, ClusterType ct, String str):
		execution(public * XmlCachable+.get*(ClusterType, String)) && 
		within(ClusterSelectImpl) &&
		args(ct, str) &&
		target(xmlc);
	
	/**
	 * pointcut for ClusterType and two String parameter methods
	 */
	pointcut doCache2(XmlCachable xmlc, ClusterType ct, String str, String str2):
		execution(public * XmlCachable+.get*(ClusterType, String, String)) && 
		within(ClusterSelectImpl) &&
		args(ct, str, str2) &&
		target(xmlc);

	/**
	 * around advice for ClusterType and String parameter methods
	 */
	Object around(XmlCachable xmlc, ClusterType ct, String str) : doCache(xmlc, ct, str) {
		MethodSignature ms = (MethodSignature) thisJoinPoint.getSignature();
		if(isInCache(xmlc, ms.getMethod(), ct.getName(), str))
			return getCache(xmlc, ms.getMethod(), ct.getName(), str);
		
		Object o = proceed(xmlc, ct, str);
		fillCache(xmlc, ms.getMethod(), o, ct.getName(), str);
		return o;
	}
	
	/**
	 * around advice for ClusterType and two String parameter methods
	 */
	Object around(XmlCachable xmlc, ClusterType ct, String str, String str2) : doCache2(xmlc, ct, str, str2) {
		MethodSignature ms = (MethodSignature) thisJoinPoint.getSignature();
		if(isInCache(xmlc, ms.getMethod(), ct.getName(), str, str2))
			return getCache(xmlc, ms.getMethod(), ct.getName(), str, str2);
		
		Object o = proceed(xmlc, ct, str, str2);
		fillCache(xmlc, ms.getMethod(), o, ct.getName(), str, str2);
		return o;
	}
}
