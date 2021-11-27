all:
	mvn package
	java -javaagent:agent/target/jmtrace-agent-1.0.jar -jar example/target/heavysort-1.0.jar
#java -javaagent:target/jmtrace-1.0.jar -jar output/example.jar

test: example/Main.java 
	java -jar ./output/example.jar

example/Main.java:
	javac src/test/resources/example/*.java -d ./
	jar -cfm output/example.jar src/test/resources/example/MANIFEST.MF example/*.class
	rd /s /q example

clean:
	mvn clean
	Remove-Item ./example/*.class
	rm output/example.jar

