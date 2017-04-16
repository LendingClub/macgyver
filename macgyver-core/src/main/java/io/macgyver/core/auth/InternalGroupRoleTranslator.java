/**
 * Copyright 2017 Lending Club, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.core.auth;

import java.util.Collection;

import org.lendingclub.neorx.NeoRxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


/**
 * This class will map granted authorities from an upstream source (probably
 * LDAD/AD) and map them onto MacGyver-specific roles.
 * 
 * @author rschoening
 *
 */
public class InternalGroupRoleTranslator extends GrantedAuthoritiesTranslator {

	Logger log = LoggerFactory.getLogger(InternalGroupRoleTranslator.class);
	@Autowired
	NeoRxClient neo4j;

	@SuppressWarnings("unchecked")
	@Override
	protected void translate(Collection<? extends GrantedAuthority> source,
			Collection<? extends GrantedAuthority> target) {

		source.stream().forEach(sourceAuthority -> {
			
			mapSourceToTarget(sourceAuthority, target);
			((Collection<GrantedAuthority>) target).add(new SimpleGrantedAuthority("GROUP_"+normalizeUpperCase(sourceAuthority.getAuthority())));
		});

	}

	@SuppressWarnings("unchecked")
	protected void mapSourceToTarget(GrantedAuthority sourceAuthority,
			Collection<? extends GrantedAuthority> target) {

		String sourceName = sourceAuthority.getAuthority();

	
		neo4j.execCypher(
				"match (g:Group{name:{name}})-[:HAS_ROLE]-(r:Role) return distinct r.name as role_name",
				"name", sourceName).flatMap(UserManager.jsonNodeToString())
				.forEach(roleName -> {
					
					log.info("found role_name: {}",roleName);
					GrantedAuthority sg = new SimpleGrantedAuthority(roleName);

					((Collection<GrantedAuthority>) target).add(sg);
				});
		
	}
	
	public static String normalizeUpperCase(String s) {
		return s.replaceAll("[^A-Za-z0-9]", "_").trim().toUpperCase();
	}
}
