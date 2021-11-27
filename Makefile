all:
	mvn package

test:
	java -javaagent:agent/target/jmtrace-agent-1.0.jar -jar example/target/heavysort-1.0.jar

clean:
	mvn clean

