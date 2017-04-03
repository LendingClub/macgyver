## Changes

### 1.7.0 - 2017-04-02
* switch to use Neo4j BOLT protocol
* neorx 2.0.1
* mercator 1.4.1

### 1.6.9 - 2017-02-21
* mercator should share neo4j client with macgyver

### 1.6.8 - 2017-02-21
* mercator 1.1.1

### 1.6.7 - 2017-02-20
* fix service catalog change detection
* fix service catalog entries wiht missing appId

### 1.6.6 - 2017-02-20 
* mercator 1.1.0 

### 1.6.5 - 2017-02-20
* mercator 1.0.6 - fix for incremental ASG scans

### 1.6.4 - 2017-02-19
* mercator 1.0.5
* expoose ReflexBus from EventSystem

### 1.6.3
* Minor extensibility improvements for scanning

### 1.6.2
* Mercator 1.0.4
* Fix delimiting of AWS regions
* AWS SDK 1.11.93

### 1.6.1
* Mercator 1.0.2

### 1.6.0
* Migrate scanning functionality to mercator


### 1.5.9
* Fix Security Group Scanner

### 1.5.7
* Add NewRelic message types

### 1.5.4
* reflex 1.4.2

### 1.5.3
* core reflex thread pool default size of 50
* expose convenience method for creating concurrent subscribers

### 1.5.2
* reflex 1.4.1

### 1.5.1
* move to reflex 1.0.6

### 1.5.0
* elimiate ProjectReactor and switch to RxJava+reflex 

### 1.4.7 
* fix deletion of launch config 

### 1.4.4
* Added cpu cores and memory to VSphere scanning
* made access log and conditional logging configurable

### 1.4.3
* Do not throw exception when logging invalid json in JdbcEventWriter
* Add access log suppression attribute to app checkIn
* Fix "macgyver login" CLI
* Add jmespath-java

### 1.4.2
* OkHttp 3.4.1
* OkRest 3.1.0
* NeoRx 1.3.4
* Do not log MissingNode JSON payloads
* Do not expire old tokens when refreshing tokens

### 1.4.1
* JdbcEventLog Fixes

### 1.4.0
* Refactor Event Processing Model

### 1.3.9 
* Add RxJava JDBC

### 1.3.8
* expose OkRest/OkHttp from jenkins client
* upgrade Jenkins to OkRest/OkHttp 3.x

### 1.3.6
* removing relationships between ELB-ASG when ASGs are detached from ELB

### 1.3.5
* Add SpringFox + Swagger
* Enable API Tokens to be sent via X-API-KEY
* Support for config to be loaded via mechanisms other than services.groovy (spring config, neo4j, json)
* JUnit Integration tests run with the "junit" profile
* src/test/resources/ext no longer used (dedicated junit profile used instead)

### 1.3.4
* formal support for sub-commands

### 1.3.3
* Roll back AWS SDK to 1.10.76 (due to memory leak)

### 1.3.2
* Cypher compatability issues with neo4j 3.x

### 1.3.1
* Added functionality for environment-specific script execution

### 1.3.0
* Remove Ignite (may use it later but not for clustering)
* CLI Enhancmeents
* Spring Boot 1.3.3 -> 1.3.5
* Spring Securty 4.0.3 -> 4.1.0
* Spring Framework 4.2.4 -> 4.2.6

### 1.2.1
* Improve help for CLI
* Improve feedback on successful authentication
* Advance AWS version

### 1.2.0
* Eliinate Vaadin

### 1.1.5
* Fix CLI getToken()

### 1.1.4
* Fix Memory Leak - https://github.com/LendingClub/macgyver/pull/301

### 1.1.3
* Add quickstart functionality to bootstrap macgyver
* curl | bash support for CLI install

### 1.1.2
* allow ArrayNode types into neo4j while still blocking ObjectNode

### 1.1.1
* simple CLI enhancement

### 1.1.0
* refactor check-in processing
* refactor service catalog

### 1.0.9
* be smart about how/when we update AppInstance during checkIn

### 1.0.8
* fix broken cli in 1.0.7

### 1.0.7
* add cli

### 1.0.6
* fix neo4j embedded browser

### 1.0.5
* rx-aws 1.2.0
* neorx 1.3.0 (uses OkHttp 3.2.0)

### 1.0.2
* Expose hook URL path in GitHubWebHookMessage

### 1.0.1
* Reactor event refactoring
* GitHub web hook event cleanup
* Got bored of version <1

### 0.107.0
* Add Project Reactor
* Simplified the Distributed Event Log

### 0.105.0

* vastly improved scheduler functionality
* ScheduledTask nodes can now have scheduledBy=manual set which will prevent the script's cron/enabled flags from being used
* Scanning AWS ELBs now pulls tags properly
* added rapidoid-u

### 0.104.0
* added distributed event log

### 0.92.0

* expose Neo4j through UI with role-based access controls
* refactor role management
* upgrade versions:
  * vaadin 7.4.8 -> 7.5.2
  * spring-boot 1.2.4.RELEASE->1.2.5.RELEASE
  * spring 4.1.6.RELEASE -> 4.1.7.RELEASE

### 0.91.0
* upgrade to jdk 8

### 0.90.5
* fix update timestamp for ComputeInstance updates

### 0.90.4
* basic working cloudstack plugin

### 0.86.1
* add MockWebServer (from Square OkHttp)
* upgrade junit to 4.12
* upgrade slf4j to 1.7.10

### 0.86.0 
* A10 will automatically detect active/standby state
* upgrade vaadin 7.3.10 -> 7.4.0
* add clojure jsr223 support
* upgrade jython and jruby
* upgrade spring 4.1.4.RELEASE -> 4.1.5.RELEASE
* upgrade gradle 2.2.1 -> 2.3
* rename macgyver-jython -> macgyver-plugin-jython
* rename macgyver-jruby -> macgyver-plugin-jruby

### 0.85.0
* add jenkins plugin
* add github webhook support
* add handlebars template MVC support
* remove javax.json
* upgrade versions:
  * aspectjweaver - 1.5.4 -> 1.8.5
  * hazelcast - 3.2.5 -> 3.4.1
  * jackson - 2.4.3 -> 2.5.1
  * joda-time - 2.3 -> 2.7
  * groovy - 2.3.9 -> 2.4.0
  * assertj - 1.7.0 -> 1.7.1
  * okhttp - 2.1.0 -> 2.2.0
  * vaadin - 7.3.7 -> 7.3.10
  * spring - 4.1.4.RELEASE
  * spring boot - 1.1.8.RELEASE -> 1.2.1.RELEASE

### 0.84.0
* #109 - Auto-refresh VIJava ServiceInstance sessions
* #111 - add basic JIRA integration
* add support for Retrofit

### 0.81.1
* CertChecker can match hostnames on wildcard cert

### 0.81.0
* Add CertChecker

### 0.80.2
* Fix broken AppInstance checkIn

### 0.80.1
* NeoRx related fixes

### 0.80.0
* Switched to NeoRx

### 0.76.0
* Added AssertJ for fluent unit test assertions
* Refactor of Leftronic client to add flexibility

### 0.75.0
* Fixed dependency conflicts with bouncycastle 

### 0.74.0
* Upgraded SSHJ to 0.10.0
* Better configuration of SSH authentication




