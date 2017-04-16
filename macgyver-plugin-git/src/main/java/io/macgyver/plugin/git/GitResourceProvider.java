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
package io.macgyver.plugin.git;

import io.macgyver.core.resource.Resource;
import io.macgyver.core.resource.ResourceMatcher;
import io.macgyver.core.resource.ResourceProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class GitResourceProvider extends ResourceProvider {

	Logger logger = LoggerFactory.getLogger(GitResourceProvider.class);

	Optional<GitRepository> gitRepository = Optional.absent();

	long lastRefreshTime = 0;

	long fetchIntervalMillis = TimeUnit.SECONDS.toMillis(300);

	Git git;
	Repository repo;

	String ref = "refs/heads/master";

	protected GitResourceProvider() {

	}

	public GitResourceProvider(String url, String username, String password) {
		GitRepository gitRepository = new GitRepository();
		gitRepository.setUrl(url);
		gitRepository.setUsername(username);
		gitRepository.setPassword(password);
		this.gitRepository = Optional.of(gitRepository);
	}

	public GitResourceProvider(String url) {
		this(url, null, null);
	}

	public GitResourceProvider(GitRepository r) {
		gitRepository = Optional.of(r);
	}

	public GitRepository getGitRepository() {

		return gitRepository.get();

	}

	public String getGitRef() {
		return ref;
	}

	public ObjectId resolveRef(String ref) throws IOException {
		Preconditions.checkNotNull(ref);
		ensureLocalClone();
		return repo.resolve(ref);
	}

	public void setGitRef(String ref) {
		this.ref = ref;

	}

	public synchronized void ensureLocalClone() throws IOException {
		GitRepository gitRepository = getGitRepository();
		try {
			if (git != null) {
				return;
			}
			File dir = Files.createTempDir();

			logger.info("cloning {} into {}", gitRepository.getUrl(), dir);

			CloneCommand cc = Git.cloneRepository()
					.setURI(gitRepository.getUrl()).setBare(true)
					.setDirectory(dir).setCloneAllBranches(true);

			if ((!Strings.isNullOrEmpty(gitRepository.getUsername()))
					|| (!Strings.isNullOrEmpty(gitRepository.getPassword()))) {
				cc = cc.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
						Strings.nullToEmpty(gitRepository.getUsername()),
						Strings.nullToEmpty(gitRepository.getPassword())));
			}
			git = cc.call();
			repo = git.getRepository();

		} catch (GitAPIException e) {
			throw new IOException(e);
		}

	}

	public synchronized void fetch() throws IOException {
		long t0 = System.currentTimeMillis();
		GitRepository gitRepository = getGitRepository();

		try {

			ensureLocalClone();
			FetchCommand fc = git.fetch();

			if ((!Strings.isNullOrEmpty(gitRepository.getUsername()))
					|| (!Strings.isNullOrEmpty(gitRepository.getPassword()))) {
				fc = fc.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
						Strings.nullToEmpty(gitRepository.getUsername()),
						Strings.nullToEmpty(gitRepository.getPassword())));
			}
			fc.call();
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			long t1 = System.currentTimeMillis();
			logger.info("fetch took {} ms", t1 - t0);
		}

	}

	@Override
	public Iterable<Resource> findResources(ResourceMatcher matcher)
			throws IOException {
		Preconditions.checkNotNull(matcher);
		refreshIfNecessary();
		ObjectId headCommit = repo.resolve(getGitRef());

		if (headCommit == null) {
			throw new IOException("ref not found: " + getGitRef());
		}
		TreeWalk tw = null;
		RevWalk rw = new RevWalk(repo);
		try {
			RevCommit rc = rw.parseCommit(headCommit);

			RevTree tree = rc.getTree();

			tw = new TreeWalk(repo);
			tw.addTree(tree);
			tw.setRecursive(true);
			List<Resource> list = Lists.newArrayList();
			while (tw.next()) {

				GitResourceImpl gri = new GitResourceImpl(this,
						tw.getObjectId(0), tw.getPathString());
				if (matcher.matches(gri)) {
					list.add(gri);
				}

			}
			return list;
		} finally {
			if (tw != null) {
				tw.release();
			}
			if (rw != null) {
				rw.release();
			}
		}

	}

	@Override
	public Resource getResourceByPath(String path) throws IOException {

		refreshIfNecessary();
		TreeWalk tw = null;
		RevWalk rw = null;
		try {
			ObjectId headCommit = repo.resolve(getGitRef());
			rw = new RevWalk(repo);

			tw = TreeWalk.forPath(repo, path, rw.parseCommit(headCommit)
					.getTree());
			if (tw == null) {
				throw new FileNotFoundException(path + " not found in ref "
						+ getGitRef() + " (" + headCommit.getName() + ")");
			}
			ObjectId id = tw.getObjectId(0);
			GitResourceImpl gri = new GitResourceImpl(this, id, path);
			return gri;
		} finally {
			if (tw != null) {
				tw.release();
			}
			if (rw != null) {
				rw.release();
			}
		}
	}

	public void close() {
		if (repo != null) {
			repo.close();
		}
		if (git != null) {
			git.close();
		}
	}

	public synchronized void refreshIfNecessary() throws IOException {
		ensureLocalClone();

		long timeSinceRefresh = System.currentTimeMillis() - lastRefreshTime;

		if (timeSinceRefresh > getFetchIntervalMillis()) {
			logger.debug("fetching updates from git repo");
			lastRefreshTime = System.currentTimeMillis();
			fetch();
		}

	}

	public long getFetchIntervalMillis() {
		return fetchIntervalMillis;
	}

	@Override
	public synchronized void refresh() throws IOException {
		ensureLocalClone();
		fetch();
	}

	public void setFetchIntervalSecs(int secs) {
		fetchIntervalMillis = TimeUnit.SECONDS.toMillis(secs);
	}
}
