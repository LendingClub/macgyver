/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.plugin.cmdb.catalog;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.beust.jcommander.internal.Sets;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;

import ch.qos.logback.core.helpers.Transform;
import io.macgyver.core.MacGyverException;
import io.macgyver.core.event.MacGyverEventPublisher;
import io.macgyver.core.resource.Resource;
import io.macgyver.core.resource.ResourceMatcher;
import io.macgyver.core.resource.ResourceProvider;
import io.macgyver.core.service.ServiceRegistry;
import io.macgyver.core.util.HJson;
import io.macgyver.core.util.Neo4jUtil;
import io.macgyver.neorx.rest.NeoRxClient;
import io.macgyver.plugin.cmdb.catalog.AbstractCatalogLoader.EntitiyDefinitionWriter;
import io.macgyver.plugin.cmdb.catalog.AbstractCatalogLoader.ProviderMapper;
import io.macgyver.plugin.cmdb.catalog.AbstractCatalogLoader.ResourceMapper;
import io.macgyver.plugin.git.GitRepository;
import io.macgyver.plugin.git.GitRepositoryServiceFactory;
import io.macgyver.plugin.git.GitResourceProvider;
import reactor.bus.EventBus;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public abstract class AbstractCatalogLoader {

	List<ResourceProvider> providers = Lists.newCopyOnWriteArrayList();

	@Autowired
	NeoRxClient neo4j;

	@Autowired
	ServiceRegistry serviceRegistry;

	@Autowired
	MacGyverEventPublisher publisher;

	String neo4jLabel;

	String alternateKey = null;

	Logger logger = LoggerFactory.getLogger(getClass());

	Pattern jobPattern = Pattern.compile("DO_NOT_MATCH");

	List<Func1<ObjectNode, Observable<ObjectNode>>> transformers = new CopyOnWriteArrayList<>();

	public <T extends AbstractCatalogLoader> T withNodeLabel(String label) {
		this.neo4jLabel = label;
		return (T) this;
	}

	public String getEntryType() {
		return neo4jLabel;
	}

	public <T extends AbstractCatalogLoader> T withDirName(String dirName) {
		jobPattern = Pattern.compile(".*" + dirName + ".*?\\/((.+?)\\.[h]*json)");
		return (T) this;
	}

	public <T extends AbstractCatalogLoader> T withNeoRxClient(NeoRxClient x) {
		this.neo4j = x;
		return (T) this;
	}

	public <T extends AbstractCatalogLoader> T withEventPublisher(io.macgyver.core.event.MacGyverEventPublisher publisher) {
		this.publisher = publisher;
		return (T) this;
	}

	public <T extends AbstractCatalogLoader> T withAlternateNodeKey(String alternateKey) {
		this.alternateKey = alternateKey;
		return (T) this;
	}

	public void clearTransforms() {
		transformers.clear();
	}

	public <T extends AbstractCatalogLoader> T addTransform(Func1<ObjectNode, Observable<ObjectNode>> x) {
		transformers.add(x);
		return (T) this;
	}

	public <T extends AbstractCatalogLoader> T withResourceProvider(ResourceProvider p) {
		providers.add(p);
		return (T) this;
	}

	public void clearResourceProviders() {
		providers.clear();
	}

	public class ProviderMapper implements Func1<ResourceProvider, Observable<Resource>> {

		@Override
		public Observable<Resource> call(ResourceProvider t) {
			try {
				t.refresh();
				return t.allResources();
			} catch (IOException e) {
				logger.warn("", e);
			}
			return Observable.empty();
		}

	}

	public class RegexResourceFilter implements Func1<Resource, Boolean> {

		Pattern matchingPattern;

		public RegexResourceFilter(Pattern p) {
			matchingPattern = p;
		}

		public Boolean call(Resource r) {
			try {
				Matcher m = matchingPattern.matcher(r.getPath());
				if (m.matches()) {
					return true;
				}
				return false;
			} catch (IOException | RuntimeException e) {
				logger.warn("problem filtering", e);
			}
			return false;
		}

	}

	public void discoverResourceProviders() {

		if (serviceRegistry != null && serviceRegistry.getServiceDefinitions().containsKey("serviceCatalogGitRepo")) {
			GitRepository repo = serviceRegistry.get("serviceCatalogGitRepo");
			GitResourceProvider p = new GitResourceProvider(repo);
			withResourceProvider(p);
		} else {
			logger.warn("service 'serviceCatalogGitRepo' not found");
		}

	}

	public void importAll() {
		doImportAll();
		doDelete();
	}

	protected void doDelete() {

		Set<String> valuesOnDisk = Sets.newHashSet();

		Observable.from(providers).flatMap(new ProviderMapper()).filter(new RegexResourceFilter(jobPattern))
				.forEach(x -> {

					valuesOnDisk.add(extractNameFromResource(x));

				});
		if (!valuesOnDisk.isEmpty()) {
			// If we have an empty set, it is possible that something failed or
			// was misconfigured
			// Do not delete under this circumstance.

			Set<String> valuesInNeo4j = Sets.newHashSet();
			String cypher = "match (x:" + neo4jLabel + ")  return x.id as id";
			neo4j.execCypherAsList(cypher).forEach(it -> {
				valuesInNeo4j.add(it.asText());
			});

			valuesInNeo4j.removeAll(valuesOnDisk);

			// we should be left with things in neo4j that are not on disk

			valuesInNeo4j.forEach(id -> {
				logger.info("deleting {} id={}", neo4jLabel, id);
				String c = "match (x:" + neo4jLabel + " {id:{id}}) detach delete x";
				neo4j.execCypher(c, "id", id);

			});

		}

	}

	public final void recordParseError(Resource resource, Throwable error) {
		try {
			doRecordParseError(resource, error);
		} catch (RuntimeException e) {
			logger.warn("problem recording parse error", e);
		}
	}

	public String extractNameFromResource(Resource r) {
		try {
			List<String> x = Splitter.on("/").splitToList(r.getPath());
			String s = x.get(x.size() - 1);

			int idx = s.lastIndexOf(".");
			if (idx > 0) {
				s = s.substring(0, idx);
			}
			return s;
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	protected void publish(ServiceCatalogMessage m) {
		if (publisher == null) {
			logger.warn("publisher not set. {} will not be sent", m);
		} else {
			publisher.publish(m);
		}
	}

	public void doRecordParseError(Resource resource, Throwable e) {
		String resourceName = extractNameFromResource(resource);

		String cypher = "merge (j:" + neo4jLabel + " { id :{id}}) set j.error={error},j.updateTs=timestamp() return j";
		JsonNode n = neo4j.execCypher(cypher, "id", resourceName, "error", e.toString()).toBlocking()
				.firstOrDefault(MissingNode.getInstance());

		ServiceCatalogMessage.ErrorMessage m = new ServiceCatalogMessage.ErrorMessage().withErrorMessage(e.toString())
				.withEntryType(getEntryType()).withId(resourceName);
		publish(m);
	}

	public class EntitiyDefinitionWriter implements Action1<ObjectNode> {

		@Override
		public void call(ObjectNode n) {

			try {
				String id = n.get("id").asText();
				logger.info("entity ({}): {}", id, n);
				Preconditions.checkState(neo4j != null, "neo4j not set");
				Preconditions.checkState(!Strings.isNullOrEmpty(neo4jLabel), "neo4j label must be set");

				ObjectNode copy = Neo4jUtil.scrubNonCompliantNeo4jAttributes(n);

				if (!Strings.isNullOrEmpty(alternateKey)) {
					copy.put(alternateKey, id);
				}

				String oldHashVal = neo4j.execCypher("match (a:" + neo4jLabel + " {id:{id}}) return a", "id", id)
						.firstOrDefault(MissingNode.getInstance()).toBlocking().first().path("entryHash").asText();

				try {
					copy.put("entryType", getEntryType());
					String cypher = "merge (j:" + neo4jLabel + " {id:{id}})  set j+={props}, j.updateTs=timestamp()";
					neo4j.execCypher(cypher, "id", n.get("id").asText(), "props", copy);

					cypher = "match (j:" + neo4jLabel + " {id:{id}})  remove j.error";
					neo4j.execCypher(cypher, "id", n.get("id").asText());
				} catch (RuntimeException e) {
					logger.warn("problem processing entity definition", e);
				}

				if (oldHashVal.equals(copy.path("entryHash"))) {
					logger.debug("no change detected in: entryType={} id={}", copy.path("entryType").asText(),
							copy.path("id").asText());
				} else {
					logger.info("change detected in: entryType={} id={}", copy.path("entryType").asText(),
							copy.path("id").asText());
					String cypher = "match (j:" + neo4jLabel + " {id:{id}})  return j";
					ObjectNode defData = (ObjectNode) neo4j.execCypher(cypher, "id", n.get("id").asText()).toBlocking()
							.first();

					ServiceCatalogMessage.UpdateMessage m = (ServiceCatalogMessage.UpdateMessage) new ServiceCatalogMessage.UpdateMessage()
							.withCatalogEntry(defData).withCatalogEntrySource(n);

					publish(m);

				}
			} catch (RuntimeException e) {
				logger.warn("", e);
			}
		}

	}

	public class ResourceMapper implements Func1<Resource, Observable<ObjectNode>> {

		@Override
		public Observable<ObjectNode> call(Resource t) {
			try {

				Matcher m = jobPattern.matcher(t.getPath());
				if (m.matches()) {

					String sourceContent = t.getContentAsString();
					String hash = Hashing.sha1().hashBytes(sourceContent.getBytes()).toString();

					ObjectNode n = (ObjectNode) HJson.parse(t.getContentAsString());
					n.put("entryType", getEntryType());
					n.put("entryHash", hash);
					n.put("id", m.group(2));

					return Observable.just(n);
				}

			} catch (RuntimeException | IOException e) {
				recordParseError(t, e);
			}
			return Observable.empty();
		}

	}

	protected void doImportAll() {

		Observable<ObjectNode> builder = Observable.from(providers).flatMap(new ProviderMapper())
				.flatMap(new ResourceMapper());

		for (Func1<ObjectNode, Observable<ObjectNode>> transformer : transformers) {
			builder = builder.flatMap(transformer);
		}

		builder.forEach(new EntitiyDefinitionWriter());
	}
}
