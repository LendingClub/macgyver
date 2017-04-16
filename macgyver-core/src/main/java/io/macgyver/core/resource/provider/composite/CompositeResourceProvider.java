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
package io.macgyver.core.resource.provider.composite;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import io.macgyver.core.resource.Resource;
import io.macgyver.core.resource.ResourceMatcher;
import io.macgyver.core.resource.ResourceProvider;

public class CompositeResourceProvider extends ResourceProvider {

	Logger logger = LoggerFactory.getLogger(CompositeResourceProvider.class);
	List<ResourceProvider> resourceLoaders = Lists.newCopyOnWriteArrayList();

	public CompositeResourceProvider() {

	}

	public void replaceResourceProvider(ResourceProvider rp) {
		logger.info("removing all providers of {}", rp.getClass());
		List<ResourceProvider> temp = Lists.newArrayList();
		for (ResourceProvider p : resourceLoaders) {
			if (rp.getClass().isAssignableFrom(p.getClass())) {

				temp.add(p);
			}
		}
		resourceLoaders.removeAll(temp);

		addResourceLoader(rp);
	}

	public void insertResourceLoader(ResourceProvider loader) {
		addResourceLoader(loader, true);
	}

	public void addResourceLoader(ResourceProvider loader) {
		addResourceLoader(loader, false);
	}

	private void addResourceLoader(ResourceProvider loader, boolean insert) {
		Preconditions.checkNotNull(loader);
		if (resourceLoaders.contains(loader)) {
			logger.warn("resource loader already registered: {}", loader);
			return;
		}
		if (insert) {
			resourceLoaders.add(0, loader);
		} else {
			resourceLoaders.add(loader);
		}
	}

	@Override
	public Iterable<Resource> findResources(ResourceMatcher matcher) throws IOException {
		
		Set<String> paths = new HashSet<>();
		
		List<Resource> list = Lists.newArrayList();

		for (ResourceProvider loader : resourceLoaders) {
			for (Iterator<Resource> iter = loader.findResources(matcher).iterator(); iter.hasNext(); ) {
				Resource resource = iter.next();
				if (paths.add(resource.getPath())) {
					list.add(resource);
				}
			}
		}

		return list;
	}

	@Override
	public Resource getResourceByPath(String path) throws IOException {
		for (ResourceProvider loader : resourceLoaders) {

			try {
				Resource r = loader.getResourceByPath(path);
				if (r != null) {
					return r;
				}
			} catch (FileNotFoundException e) {

			}

		}
		throw new FileNotFoundException("resource not found: " + path);

	}

	@Override
	public void refresh() throws IOException {
		for (ResourceProvider p : resourceLoaders) {
			p.refresh();
		}

	}

	public Optional<Resource> findResourceByHash(String hash) throws IOException {
		Preconditions.checkNotNull(hash);
		for (Resource r : findResources()) {

			if (r.getHash().equals(hash)) {
				return Optional.of(r);
			}
		}
		return Optional.absent();
	}
}
