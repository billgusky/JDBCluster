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
package org.jdbcluster.domain;

import java.util.HashMap;
import java.util.List;

import org.dom4j.Node;

/**
 * Maps Domain Entry Values to SlaveDomainIds and 
 * SlaveDomainIds to a ValidEntryList
 * 1. Map: Key   -> entries (fe. 'RED', 'BLUE') of the domain domainId (fe. 'Colors')
 *         Value -> 2. Map which holds...
 * 2. Map: Key   -> configured slave domainIds
 *         Value -> the configured validation entries for the domain domainId
 * @author FaKod
 */
final class EntrySet extends HashMap<String,HashMap<String, ValidEntryList>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8732982067055787566L;
	
	/**
	 * the current DomainId this Map is for
	 */
	String domainId;
	
	/**
	 * Constructs and filles HashMap(s) 
	 * @param domainId the DomainId the entries are belonging to
	 * @param domEntryNodes the List of (entry) Nodes
	 */
	EntrySet(String domainId, List<Node> domEntryNodes) {
		this.domainId = domainId;	
		fill(domEntryNodes, this);
	}

	/**
	 * filles structure
	 * @param domEntryNodes
	 */
	@SuppressWarnings("unchecked") 
	void fill(List<Node> domEntryNodes, HashMap<String,HashMap<String, ValidEntryList>> topList) {
		for( Node n : domEntryNodes) {
			String domEntryValue = n.valueOf("@value");
			String domEntrySlaveDomain = n.valueOf("@slavedomainid");
			
			HashMap<String, ValidEntryList> slaveDomainMap = topList.get(domEntryValue);
			if(slaveDomainMap==null) {
				slaveDomainMap = new HashMap<String, ValidEntryList>();
				topList.put(domEntryValue, slaveDomainMap);
			}
			ValidEntryList  validValueList = slaveDomainMap.get(domEntrySlaveDomain);
			if(validValueList==null) {
				List<Node> validEntries = n.selectNodes("valid");
				List<Node> invalidEntries = n.selectNodes("invalid");
				List<Node> addMasterEntries = n.selectNodes("additionalmaster");
				validValueList = new ValidEntryList(validEntries, invalidEntries, addMasterEntries);
				slaveDomainMap.put(domEntrySlaveDomain, validValueList);
			}
		}
	}	
}
