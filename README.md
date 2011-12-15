BaseX Web
=========
This is a sneak-peak at an XQuery-driven Web Application Server.

Requirements
------------
[Java 1.6](http://java.com/getjava/index.jsp "Download Free Java Software") and [Maven](http://maven.apache.org/ "Maven - 
    Welcome to Apache Maven") are highly recommended to run the project. 
    
You have to install voldemort locally, as it has no public maven repository. Execute the following commands:

		$ mvn install:install-file -Dfile=voldemort-0.90.1.jar -DgroupId=voldemort -DartifactId=voldemort-core -Dversion=0.90.1 -Dpackaging=jar -DgeneratePom=true
		$ mvn install:install-file -Dfile=voldemort-contrib-0.90.1.jar -DgroupId=voldemort -DartifactId=voldemort-contrib -Dversion=0.90.1 -Dpackaging=jar -DgeneratePom=true


When starting Voldemort, you need to define a store named "basex-web". A single-node example stores.xml file could look like this:

		<stores>
		  <store>
		    <name>basex-web</name>
		    <persistence>bdb</persistence>
		    <description>baseX store</description>
		    <owners>basex@basex.org </owners>
		    <routing>client</routing>
		    <replication-factor>1</replication-factor>
		    <required-reads>1</required-reads>
		    <required-writes>1</required-writes>
		    <key-serializer>
		      <type>string</type>
		    </key-serializer>
		    <value-serializer>
		      <type>java-serialization</type>
		    </value-serializer>
		  </store>
		  <view>
		    <name>basex-view</name>
		    <view-of>basex-web</view-of>
		    <owners>basex@basex.org</owners>
		    <view-class>
		      voldemort.store.views.UpperCaseView
		    </view-class>
		    <value-serializer>
		      <type>string</type>
		    </value-serializer>
		    <transforms-serializer>
		        <type>java-serialization</type>
		    </transforms-serializer>
		  </view>
		</stores>


You could then start Voldemort by running

<code>
$ ./bin/voldemort-server.sh config/single_node_cluster/
</code>

Usage
-----
To get the demo application up and running 
either clone this project (or even better fork it):
<code>
$ git clone git@github.com:micheee/basex-web.git
</code> 
Once cloned, change to the checked out project and run
<code>
$ mvn jetty:run
</code>
this will fetch all needed packages and start a webserver at [localhost:8080](http://localhost:8080 "Inline XQuery in your Browser").

The default page will contain some help information to get you started. 

The source files that power your webapplication reside in `src/main/webapp`. 

BaseX is able to parse both, `*.htm(l)` files that contain *inline xquery* like 

* `<?xq 1 to 10 ?>` (see [index.html](http://localhost:8080/index.html) for an example)

as well as *.xq(y) files in pure functional mode.

* see [index.xq](http://localhost:8080/index.xq) for an example
