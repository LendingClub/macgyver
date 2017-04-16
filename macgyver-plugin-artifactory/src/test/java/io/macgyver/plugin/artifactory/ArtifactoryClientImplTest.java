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
package io.macgyver.plugin.artifactory;

import io.macgyver.okrest.OkRestLoggingInterceptor;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import com.squareup.okhttp.mockwebserver.MockWebServer;

public class ArtifactoryClientImplTest {

	@Rule
	public MockWebServer mockServer = new MockWebServer();

	@Test
	public void testMockAql() throws InterruptedException{
		
		String  aql = "items.find\n" + 
				"(\n" + 
				"    {\n" + 
				"        \"$and\":\n" + 
				"        [\n" + 
				"            {\"repo\":{\"$eq\":\"central-cache\"}},\n" + 
				"            {\"name\":{\"$match\":\"guava-18*.jar\"}}\n" + 
			
				"        ]\n" + 
				"    }\n" + 
				").include(\"*\")";
		
		
		String response = "{\n" + 
				"    \"range\": {\n" + 
				"        \"end_pos\": 4,\n" + 
				"        \"start_pos\": 0,\n" + 
				"        \"total\": 4\n" + 
				"    },\n" + 
				"    \"results\": [\n" + 
				"        {\n" + 
				"            \"actual_md5\": \"a2b3b121c9e5fcdf15f68055e5cd84a9\",\n" + 
				"            \"actual_sha1\": \"418be347c254422b51adee3cacb10e3f69e279ec\",\n" + 
				"            \"created\": \"2014-10-21T10:47:18.717-07:00\",\n" + 
				"            \"created_by\": \"myuser\",\n" + 
				"            \"depth\": 6,\n" + 
				"            \"id\": 2790739,\n" + 
				"            \"modified\": \"2014-08-25T11:48:35.000-07:00\",\n" + 
				"            \"modified_by\": \"myuser\",\n" + 
				"            \"name\": \"guava-18.0-javadoc.jar\",\n" + 
				"            \"original_md5\": \"a2b3b121c9e5fcdf15f68055e5cd84a9\",\n" + 
				"            \"original_sha1\": \"418be347c254422b51adee3cacb10e3f69e279ec\",\n" + 
				"            \"path\": \"com/google/guava/guava/18.0\",\n" + 
				"            \"repo\": \"central-cache\",\n" + 
				"            \"size\": 5185956,\n" + 
				"            \"type\": \"file\",\n" + 
				"            \"updated\": \"2014-10-21T10:47:18.871-07:00\"\n" + 
				"        },\n" + 
				"        {\n" + 
				"            \"actual_md5\": \"9475fa46958a75ef885d21a45f4bd1b9\",\n" + 
				"            \"actual_sha1\": \"ad97fe8faaf01a3d3faacecd58e8fa6e78a973ca\",\n" + 
				"            \"created\": \"2014-09-10T09:26:46.155-07:00\",\n" + 
				"            \"created_by\": \"myuser\",\n" + 
				"            \"depth\": 6,\n" + 
				"            \"id\": 1848104,\n" + 
				"            \"modified\": \"2014-08-25T11:48:35.000-07:00\",\n" + 
				"            \"modified_by\": \"myuser\",\n" + 
				"            \"name\": \"guava-18.0-sources.jar\",\n" + 
				"            \"original_md5\": \"9475fa46958a75ef885d21a45f4bd1b9\",\n" + 
				"            \"original_sha1\": \"ad97fe8faaf01a3d3faacecd58e8fa6e78a973ca\",\n" + 
				"            \"path\": \"com/google/guava/guava/18.0\",\n" + 
				"            \"repo\": \"central-cache\",\n" + 
				"            \"size\": 1277909,\n" + 
				"            \"type\": \"file\",\n" + 
				"            \"updated\": \"2014-09-10T09:26:46.187-07:00\"\n" + 
				"        },\n" + 
				"        {\n" + 
				"            \"actual_md5\": \"947641f6bb535b1d942d1bc387c45290\",\n" + 
				"            \"actual_sha1\": \"cce0823396aa693798f8882e64213b1772032b09\",\n" + 
				"            \"created\": \"2014-09-08T10:45:37.779-07:00\",\n" + 
				"            \"created_by\": \"myuser\",\n" + 
				"            \"depth\": 6,\n" + 
				"            \"id\": 1662383,\n" + 
				"            \"modified\": \"2014-08-25T11:48:34.000-07:00\",\n" + 
				"            \"modified_by\": \"myuser\",\n" + 
				"            \"name\": \"guava-18.0.jar\",\n" + 
				"            \"original_md5\": \"947641f6bb535b1d942d1bc387c45290\",\n" + 
				"            \"original_sha1\": \"cce0823396aa693798f8882e64213b1772032b09\",\n" + 
				"            \"path\": \"com/google/guava/guava/18.0\",\n" + 
				"            \"repo\": \"central-cache\",\n" + 
				"            \"size\": 2256213,\n" + 
				"            \"type\": \"file\",\n" + 
				"            \"updated\": \"2014-09-08T10:45:37.896-07:00\"\n" + 
				"        },\n" + 
				"        {\n" + 
				"            \"actual_md5\": \"a8efd08d9dda2ab593ae7eaa99170475\",\n" + 
				"            \"actual_sha1\": \"9bd0d5bc8a4269bb2b5584d5498e281633c677eb\",\n" + 
				"            \"created\": \"2014-09-02T12:31:15.949-07:00\",\n" + 
				"            \"created_by\": \"myuser\",\n" + 
				"            \"depth\": 6,\n" + 
				"            \"id\": 1441564,\n" + 
				"            \"modified\": \"2014-08-05T12:16:43.000-07:00\",\n" + 
				"            \"modified_by\": \"myuser\",\n" + 
				"            \"name\": \"guava-18.0-rc1.jar\",\n" + 
				"            \"original_md5\": \"a8efd08d9dda2ab593ae7eaa99170475\",\n" + 
				"            \"original_sha1\": \"9bd0d5bc8a4269bb2b5584d5498e281633c677eb\",\n" + 
				"            \"path\": \"com/google/guava/guava/18.0-rc1\",\n" + 
				"            \"repo\": \"central-cache\",\n" + 
				"            \"size\": 2256450,\n" + 
				"            \"type\": \"file\",\n" + 
				"            \"updated\": \"2014-09-02T12:31:16.178-07:00\"\n" + 
				"        }\n" + 
				"    ]\n" + 
				"}\n" + 
				"";
		
		mockServer.enqueue(new MockResponse().setBody(response).addHeader("Content-type","application/json"));
		
		ArtifactoryClient c = new ArtifactoryClient.Builder().url(mockServer.url("/artifactory").toString()).credentials("grateful", "dead").build();
	

		
		
		JsonNode n = c.searchAQL().aql(aql).execute();
		
		RecordedRequest rr = mockServer.takeRequest();
		
		Assertions.assertThat(rr.getHeader("Content-type")).isEqualTo("text/plain");
		Assertions.assertThat(rr.getHeader("Authorization")).isEqualTo("Basic Z3JhdGVmdWw6ZGVhZA==");
		Assertions.assertThat(rr.getBody().readUtf8()).contains("$and").contains("guava-18*.jar");
		
		// ensure we got the response we expect
		Assertions.assertThat(n.path("results").get(0).get("actual_md5").asText()).isEqualTo("a2b3b121c9e5fcdf15f68055e5cd84a9");
	}

	@Test
	public void testMockGavcSearch() throws IOException, InterruptedException {

		String response = "{\n"
				+ "  \"results\" : [ {\n"
				+ "    \"repo\" : \"lib-release\",\n"
				+ "    \"path\" : \"/mygroup/myartifact/1.0.1/myartifact-1.0.1.jar\",\n"
				+ "    \"created\" : \"2015-01-06T18:50:11.270-08:00\",\n"
				+ "    \"createdBy\" : \"deployuser\",\n"
				+ "    \"lastModified\" : \"2015-01-06T18:50:11.262-08:00\",\n"
				+ "    \"modifiedBy\" : \"deployuser\",\n"
				+ "    \"lastUpdated\" : \"2015-01-06T18:50:11.262-08:00\",\n"
				+ "    \"properties\" : { },\n"
				+ "    \"downloadUri\" : \"https://artifactory.example.com/artifactory/lib-release/mygroup/myartifact/1.0.1/myartifact-1.0.1.jar\",\n"
				+ "    \"mimeType\" : \"application/java-archive\",\n"
				+ "    \"size\" : \"42429\",\n"
				+ "    \"checksums\" : {\n"
				+ "      \"sha1\" : \"b8b79fcae449ad6b300c26957ebc98ab362b2e6c\",\n"
				+ "      \"md5\" : \"dc0bf7c2bfde4ac73a2c9561e8b11901\"\n"
				+ "    },\n"
				+ "    \"originalChecksums\" : {\n"
				+ "      \"sha1\" : \"b8b79fcae449ad6b300c26957ebc98ab362b2e6c\",\n"
				+ "      \"md5\" : \"dc0bf7c2bfde4ac73a2c9561e8b11901\"\n"
				+ "    },\n"
				+ "    \"uri\" : \"https://artifactory.example.com/artifactory/api/storage/lib-release/mygroup/myartifact/1.0.1/myartifact-1.0.1.jar\"\n"
				+ "  }, {\n"
				+ "    \"repo\" : \"lib-release\",\n"
				+ "    \"path\" : \"/mygroup/myartifact/1.0.1/myartifact-1.0.1.pom\",\n"
				+ "    \"created\" : \"2015-01-06T18:50:11.294-08:00\",\n"
				+ "    \"createdBy\" : \"deployuser\",\n"
				+ "    \"lastModified\" : \"2015-01-06T18:50:11.294-08:00\",\n"
				+ "    \"modifiedBy\" : \"deployuser\",\n"
				+ "    \"lastUpdated\" : \"2015-01-06T18:50:11.294-08:00\",\n"
				+ "    \"properties\" : { },\n"
				+ "    \"downloadUri\" : \"https://artifactory.example.com/artifactory/lib-release/mygroup/myartifact/1.0.1/myartifact-1.0.1.pom\",\n"
				+ "    \"mimeType\" : \"application/x-maven-pom+xml\",\n"
				+ "    \"size\" : \"6395\",\n"
				+ "    \"checksums\" : {\n"
				+ "      \"sha1\" : \"b0b25aeb7671ecfafe9e1bb6ee496bd3f72ce414\",\n"
				+ "      \"md5\" : \"009095fb9116b284949be888686955aa\"\n"
				+ "    },\n"
				+ "    \"originalChecksums\" : {\n"
				+ "      \"sha1\" : \"b0b25aeb7671ecfafe9e1bb6ee496bd3f72ce414\",\n"
				+ "      \"md5\" : \"009095fb9116b284949be888686955aa\"\n"
				+ "    },\n"
				+ "    \"uri\" : \"https://artifactory.example.com/artifactory/api/storage/lib-release/mygroup/myartifact/1.0.1/myartifact-1.0.1.pom\"\n"
				+ "  } ]\n" + "}";
		mockServer
				.enqueue(new MockResponse()
						.setBody(response)
						.addHeader("Content-type",
								"application/vnd.org.jfrog.artifactory.search.GavcSearchResult+json")
						.setResponseCode(200));

		ArtifactoryClient c = new ArtifactoryClient.Builder().url(mockServer.url("/artifactory")
				.toString()).credentials( "scott", "tiger").build();
		

		JsonNode n = c.searchGAVC().artifact("myartifact").version("1.0.1")
				.withInfo(true).withProperties(true).inRepo("lib-release")
				.execute();
		RecordedRequest rr = mockServer.takeRequest();

		Assertions
				.assertThat(rr.getPath())
				.isEqualTo(
						"/artifactory/api/search/gavc?a=myartifact&v=1.0.1&repos=lib-release");
		Assertions.assertThat(rr.getHeader("authorization")).isEqualTo(
				"Basic c2NvdHQ6dGlnZXI=");
		Assertions.assertThat(rr.getHeader("X-Result-Detail")).isEqualTo(
				"info, properties");

		mockServer
				.enqueue(new MockResponse()
						.setBody(response)
						.addHeader("Content-type",
								"application/vnd.org.jfrog.artifactory.search.GavcSearchResult+json")
						.setResponseCode(200));

		c = new ArtifactoryClient.Builder().url(mockServer.url("/artifactory")
				.toString()).credentials( "scott", "tiger").build();
		
		n = c.searchGAVC().artifact("myartifact").version("1.0.1")
				.withInfo(true).withProperties(true).inRepo("lib-release")
				.execute();
		rr = mockServer.takeRequest();
		
		Assertions
				.assertThat(rr.getPath())
				.isEqualTo(
						"/artifactory/api/search/gavc?a=myartifact&v=1.0.1&repos=lib-release");
		Assertions.assertThat(rr.getHeader("authorization")).isEqualTo(
				"Basic c2NvdHQ6dGlnZXI=");
		Assertions.assertThat(rr.getHeader("X-Result-Detail")).isEqualTo(
				"info, properties");

		
		
		// *************
		
		mockServer
				.enqueue(new MockResponse()
						.setBody(response)
						.addHeader("Content-type",
								"application/vnd.org.jfrog.artifactory.search.GavcSearchResult+json")
						.setResponseCode(200));

		c = new ArtifactoryClient.Builder().url(mockServer.url("/artifactory")
				.toString()).credentials( "scott", "tiger").build();
	

		n = c.searchGAVC().artifact("myartifact").version("1.0.1")
				.withInfo(true).withProperties(false).inRepo("lib-release")
				.execute();
		rr = mockServer.takeRequest();
	
		Assertions
				.assertThat(rr.getPath())
				.isEqualTo(
						"/artifactory/api/search/gavc?a=myartifact&v=1.0.1&repos=lib-release");
		Assertions.assertThat(rr.getHeader("authorization")).isEqualTo(
				"Basic c2NvdHQ6dGlnZXI=");
		Assertions.assertThat(rr.getHeader("X-Result-Detail")).isEqualTo(
				"info");
		
	// *************
		
		mockServer
				.enqueue(new MockResponse()
						.setBody(response)
						.addHeader("Content-type",
								"application/vnd.org.jfrog.artifactory.search.GavcSearchResult+json")
						.setResponseCode(200));

		c = new ArtifactoryClient.Builder().url(mockServer.url("/artifactory")
				.toString()).credentials( "scott", "tiger").build();
		

		n = c.searchGAVC().artifact("myartifact").version("1.0.1")
				.withInfo(false).withProperties(false).inRepo("lib-release")
				.execute();
		rr = mockServer.takeRequest();
	
		Assertions
				.assertThat(rr.getPath())
				.isEqualTo(
						"/artifactory/api/search/gavc?a=myartifact&v=1.0.1&repos=lib-release");
		Assertions.assertThat(rr.getHeader("authorization")).isEqualTo(
				"Basic c2NvdHQ6dGlnZXI=");
		Assertions.assertThat(rr.getHeader("X-Result-Detail")).isNull();

	}
	
	@Test
	public void testPropertySearch() throws IOException, InterruptedException {

		String response = "{\n"
				+ "  \"results\" : [ {\n"
				+ "    \"repo\" : \"lib-release\",\n"
				+ "    \"path\" : \"/mygroup/myartifact/1.0.1/myartifact-1.0.1.jar\",\n"
				+ "    \"created\" : \"2015-01-06T18:50:11.270-08:00\",\n"
				+ "    \"createdBy\" : \"deployuser\",\n"
				+ "    \"lastModified\" : \"2015-01-06T18:50:11.262-08:00\",\n"
				+ "    \"modifiedBy\" : \"deployuser\",\n"
				+ "    \"lastUpdated\" : \"2015-01-06T18:50:11.262-08:00\",\n"
				+ "    \"properties\" : { },\n"
				+ "    \"downloadUri\" : \"https://artifactory.example.com/artifactory/lib-release/mygroup/myartifact/1.0.1/myartifact-1.0.1.jar\",\n"
				+ "    \"mimeType\" : \"application/java-archive\",\n"
				+ "    \"size\" : \"42429\",\n"
				+ "    \"checksums\" : {\n"
				+ "      \"sha1\" : \"b8b79fcae449ad6b300c26957ebc98ab362b2e6c\",\n"
				+ "      \"md5\" : \"dc0bf7c2bfde4ac73a2c9561e8b11901\"\n"
				+ "    },\n"
				+ "    \"originalChecksums\" : {\n"
				+ "      \"sha1\" : \"b8b79fcae449ad6b300c26957ebc98ab362b2e6c\",\n"
				+ "      \"md5\" : \"dc0bf7c2bfde4ac73a2c9561e8b11901\"\n"
				+ "    },\n"
				+ "    \"uri\" : \"https://artifactory.example.com/artifactory/api/storage/lib-release/mygroup/myartifact/1.0.1/myartifact-1.0.1.jar\"\n"
				+ "  }, {\n"
				+ "    \"repo\" : \"lib-release\",\n"
				+ "    \"path\" : \"/mygroup/myartifact/1.0.1/myartifact-1.0.1.pom\",\n"
				+ "    \"created\" : \"2015-01-06T18:50:11.294-08:00\",\n"
				+ "    \"createdBy\" : \"deployuser\",\n"
				+ "    \"lastModified\" : \"2015-01-06T18:50:11.294-08:00\",\n"
				+ "    \"modifiedBy\" : \"deployuser\",\n"
				+ "    \"lastUpdated\" : \"2015-01-06T18:50:11.294-08:00\",\n"
				+ "    \"properties\" : { },\n"
				+ "    \"downloadUri\" : \"https://artifactory.example.com/artifactory/lib-release/mygroup/myartifact/1.0.1/myartifact-1.0.1.pom\",\n"
				+ "    \"mimeType\" : \"application/x-maven-pom+xml\",\n"
				+ "    \"size\" : \"6395\",\n"
				+ "    \"checksums\" : {\n"
				+ "      \"sha1\" : \"b0b25aeb7671ecfafe9e1bb6ee496bd3f72ce414\",\n"
				+ "      \"md5\" : \"009095fb9116b284949be888686955aa\"\n"
				+ "    },\n"
				+ "    \"originalChecksums\" : {\n"
				+ "      \"sha1\" : \"b0b25aeb7671ecfafe9e1bb6ee496bd3f72ce414\",\n"
				+ "      \"md5\" : \"009095fb9116b284949be888686955aa\"\n"
				+ "    },\n"
				+ "    \"uri\" : \"https://artifactory.example.com/artifactory/api/storage/lib-release/mygroup/myartifact/1.0.1/myartifact-1.0.1.pom\"\n"
				+ "  } ]\n" + "}";
		
		mockServer
				.enqueue(new MockResponse()
						.setBody(response)
						.addHeader("Content-type",
								"application/vnd.org.jfrog.artifactory.search.MetadataSearchResult+json")
						.setResponseCode(200));

		ArtifactoryClient c = new ArtifactoryClient.Builder().url(mockServer.url(
				"/artifactory").toString()).credentials("scott", "tiger").build();
	

		JsonNode n = c.searchProperties().property("artifactId","myartifact")
				.withInfo(true).withProperties(true).inRepo("lib-release")
				.execute();
		RecordedRequest rr = mockServer.takeRequest();
	
		Assertions
				.assertThat(rr.getPath())
				.isEqualTo(
						"/artifactory/api/search/prop?artifactId=myartifact&repos=lib-release");
		Assertions.assertThat(rr.getHeader("authorization")).isEqualTo(
				"Basic c2NvdHQ6dGlnZXI=");
		Assertions.assertThat(rr.getHeader("X-Result-Detail")).isEqualTo(
				"info, properties");

		
	}
}
