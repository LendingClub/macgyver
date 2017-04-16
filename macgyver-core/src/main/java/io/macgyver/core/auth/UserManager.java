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
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;

import org.lendingclub.neorx.NeoRxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.lambdaworks.crypto.SCryptUtil;

import io.reactivex.Observable;
import io.reactivex.functions.Function;


public class UserManager {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	NeoRxClient neo4j;

	public Optional<User> getInternalUser(final String id) {

		String q = "match (u:User) where u.username={username} return u.username, 'dummy' as dummy";

		JsonNode n = neo4j.execCypher(q, "username", id.toLowerCase())
				.blockingFirst(null);
		if (n != null) {

			User u = new User();
			u.username = n.get("u.username").asText();

			u.roles = ImmutableList.copyOf(findRolesForUser(id.toLowerCase()));

			return Optional.of(u);
		}

		return Optional.absent();
	}

	public boolean authenticate(String username, String password) {
		try {
			String q = "match (u:User) where u.username={username} return u.scryptHash";
			ObjectNode n = new ObjectMapper().createObjectNode();
			n.put("username", username);
			JsonNode userNode = neo4j.execCypher(q, "username", username)
					.blockingFirst(null);
			if (userNode != null) {

				String hashValue = Strings.emptyToNull(userNode.asText());
				if (hashValue == null) {
					return false;
				}
				try {
					return SCryptUtil.check(password,
							Strings.nullToEmpty(hashValue));
				} catch (IllegalArgumentException e) {
					// if the hash is invalid, we'll get an
					// IllegalArgumentException
					// This could happen if the hashed password was set to
					// something to prevent login
					// no need to log a whole stack trace for this
					logger.info("auth error: " + e.toString());
					return false;
				}

			} else {
				return false;
			}
		}

		catch (Exception e) {
			logger.warn("auth error", e);
			return false;
		}

	}

	public void setPassword(String username, String password) {

		String hash = SCryptUtil.scrypt(password, 4096, 8, 1);

		String c = "match (u:User) where u.username={username} set u.scryptHash={hash}";

		neo4j.execCypher(c, "username", username, "hash", hash);

	}

	public void setRoles(String username, List<String> roles) {

		for (String role : roles) {
			addRoleToUser(username, role);
		}

	}

	public User createUser(String username, List<String> roles) {

		if (getInternalUser(username).isPresent()) {
			throw new IllegalArgumentException("user already exists: "
					+ username);
		}
		username = username.trim().toLowerCase();

		String cypher = "create (u:User {username:{username}}) return u";
		neo4j.execCypher(cypher, "username", username);

		setRoles(username, roles);
		User u = new User();
		u.username = username;
		u.roles = Lists.newArrayList();

		return u;

	}

	@PostConstruct
	public void initializeGraphDatabase() {
		try {

			String cipher = "CREATE CONSTRAINT ON (u:User) ASSERT u.username IS UNIQUE";
			neo4j.execCypher(cipher);

		} catch (Exception e) {
			logger.warn(e.toString());
		}

		try {

			String cipher = "CREATE CONSTRAINT ON (r:Role) ASSERT r.name IS UNIQUE";
			neo4j.execCypher(cipher);

		} catch (Exception e) {
			logger.warn(e.toString());
		}

		if (neo4j.checkConnection()) {
			seedRoles();
			Optional<User> admin = getInternalUser("admin");
			if (admin.isPresent()) {
				logger.debug("admin user already exists");
			} else {
				logger.info("adding admin user");
				List<String> roleList = Lists.newArrayList();

				createUser("admin", roleList);
				
				addUserToGroup("MACGYVER_ADMIN","admin");
				setPassword("admin", "admin");

			}

			migrateRolesForUser("admin");
		}

	}

	public Collection<String> findRolesForUser(String username) {

		HashSet<String> roles = new HashSet<>();

		// First grab all the users with roles directly attached to them
		neo4j.execCypher(
				"match (u:User{username:{username}})-[:HAS_ROLE]-(r:Role) return distinct r.name as role_name",
				"username", username)
				.flatMap(jsonNodeToString())
				.forEach(role -> roles.add(role));

		// Now grab roles that are indirectly attached via groups
		neo4j.execCypher(
				"match (u:User {username:{username}})-[:HAS_MEMBER]-(g:Group)-[]-(r:Role) return r.name as role_name",
				"username", username)
				.flatMap(jsonNodeToString())
				.forEach(role -> roles.add(role));
		
		// Add the groups themselves as well
		neo4j.execCypher(
				"match (u:User {username:{username}})-[:HAS_MEMBER]-(g:Group) return g.name as group_name",
				"username", username)
				.flatMap(jsonNodeToString())
				.forEach(role -> roles.add(InternalGroupRoleTranslator.normalizeUpperCase("GROUP_"+role)));
		// Note that this will NOT include roles for externally managed groups
		return roles;

	}

	public Collection<String> findRolesForGroup(String group) {

		return neo4j
				.execCypher(
						"match (g:Group{name:{name}})-[:HAS_ROLE]-(r:Role) return distinct r.name as role_name",
						"name", group)
				.flatMap(UserManager.jsonNodeToString()).toList().blockingGet();

	}

	public void seedRoles() {
		addRole(MacGyverRole.ROLE_MACGYVER_ADMIN.toString(),
				"MacGyver Administrator");
		addRole(MacGyverRole.ROLE_MACGYVER_USER.toString(), "MacGyver User");
		addRole(MacGyverRole.ROLE_MACGYVER_SHELL.toString(),
				"MacGyver Shell Access");
		addRole(MacGyverRole.ROLE_NEO4J_READ.toString(),
				"Read Access to Neo4j Console");
		addRole(MacGyverRole.ROLE_NEO4J_WRITE.toString(),
				"Read-Write Access Neo4j Console");
		
		addGroup("MACGYVER_ADMIN","Macgyver Admin");
		
		addRoleToGroup("MACGYVER_ADMIN", MacGyverRole.ROLE_MACGYVER_ADMIN.toString());
		addRoleToGroup("MACGYVER_ADMIN", MacGyverRole.ROLE_MACGYVER_SHELL.toString());
		addRoleToGroup("MACGYVER_ADMIN", MacGyverRole.ROLE_MACGYVER_USER.toString());
		addRoleToGroup("MACGYVER_ADMIN", MacGyverRole.ROLE_NEO4J_READ.toString());
		addRoleToGroup("MACGYVER_ADMIN", MacGyverRole.ROLE_NEO4J_WRITE.toString());
		
		
		
	}

	public void migrateRolesForUser(String username) {
		JsonNode n = neo4j
				.execCypher("match (u:User {username: {username}}) return u",
						"username", username).blockingFirst();

		for (JsonNode s : Lists.newArrayList(n.path("roles").iterator())) {
			String roleName = s.asText();
			logger.info("adding role={} to user={}", roleName, username);
			addRoleToUser(username, roleName);

		}

	}

	public void addGroup(String name, String description) {
		String cypher = "merge (g:Group {name:{name}}) ON CREATE SET g.description={description} return g";
		neo4j.execCypher(cypher, "name", name, "description", description);
	}

	public void addRole(String name, String description) {
		String cypher = "merge (r:Role {name:{name}}) ON CREATE SET r.description={description} return r";
		neo4j.execCypher(cypher, "name", name, "description", description);
	}

	public void addRoleToUser(String user, String role) {

		String cypher = "match (u:User {username:{username}}),(r:Role {name:{role}}) MERGE (u)-[x:HAS_ROLE]-(r) return u,r";

		neo4j.execCypher(cypher, "username", user, "role", role);
	}

	public void addRoleToGroup(String group, String role) {
		String cypher = "match (g:Group {name:{name}}),(r:Role {name:{role}}) MERGE (g)-[x:HAS_ROLE]-(r) return g,r";

		neo4j.execCypher(cypher, "name", group, "role", role);
	}
	public void addUserToGroup(String group, String username) {
		String cypher = "match (g:Group {name:{name}}),(u:User {username:{username}}) MERGE (g)-[x:HAS_MEMBER]-(u) return x";
		neo4j.execCypher(cypher, "username",username,"name",group);
	}
	
	public static Function<JsonNode, Observable<String>> jsonNodeToString() {
		return new Function<JsonNode, Observable<String>>() {

			@Override
			public Observable<String> apply(JsonNode t1) {
				if (t1==null) {
					return Observable.just(null);
				}
				else if (t1 instanceof NullNode) {
					return Observable.just(null);
				}
				else {
					return Observable.just(t1.asText());
				}
			}
			
		};
		
	}
}
