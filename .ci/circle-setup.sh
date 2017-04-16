#!/bin/bash


sudo service mongodb stop
sudo service mysql stop
sudo service couchdb stop
sudo service couchbase-server stop
sudo service mysql stop
sudo service zookeeper stop
sudo service redis-server stop
sudo service rabbitmq-server stop
sudo service memcached stop
sudo service postgresql stop

#sudo echo "wrapper.java.maxmemory=1750" >>/var/lib/neo4j/conf/neo4j-wrapper.conf 
#sudo echo "wrapper.java.initmemory=256" >>/var/lib/neo4j/conf/neo4j-wrapper.conf 


cd
curl 'https://neo4j.com/artifact.php?name=neo4j-community-3.1.3-unix.tar.gz' -o neo4j.tgz
tar zxvf neo4j.tgz
cd neo4j-community*

JAVA_HOME=$(find /usr/lib/jvm  -maxdepth 1 -type d -name 'jdk1.8*' | head -1)
PATH=$JAVA_HOME/bin:$PATH
export JAVA_HOME PATH

cat <<EOF >>./conf/neo4j.conf
dbms.security.auth_enabled=false
dbms.memory.pagecache.size=100m
EOF

./bin/neo4j start
