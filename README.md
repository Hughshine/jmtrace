# jmtrace

Hijack jvm's class-loading (as a javaagent) to instrument these instructions (`getstatic/putstatic/getfield/putfield/*aload/*astore`), tracing shared memory accesses.
## build & usage 

(Tested) Platform: windows / linux (`ubuntu20.04`)

(You should install) Prerequisite: jdk11([windows](https://stackoverflow.com/questions/52511778/how-to-install-openjdk-11-on-windows), [ubuntu](https://stackoverflow.com/questions/52504825/how-to-install-jdk-11-under-ubuntu)), [maven](https://maven.apache.org/install.html)

Dependency: [asm8](https://asm.ow2.io/)

build & test this project:

```shell
mvn package  # build agent & sample jar 
make test  # run sample with `make`
# or you can run: 
# java -javaagent:agent/target/jmtrace-agent-1.0.jar -jar example/target/heavysort-1.0.jar  
mvn clean  # clean everything up
```

sample output:

```shell
# R/W <thrdid> <objectid> <field/array access>
W 1 3327bd23adc5b902 java.lang.Object[5]
W 1 714236659e28baf9 example/HeavySort$SortSubsequence.data
W 1 71423665e5426092 example/HeavySort$SortSubsequence.lo
W 1 71423665e5426010 example/HeavySort$SortSubsequence.hi
R 13 78c03f1f9e28baf9 example/HeavySort$SortSubsequence.data
R 18 714236659e28baf9 example/HeavySort$SortSubsequence.data
...
```

We also provide a simple `Makefile`:

```shell
make
make test
make clean
```

And a command line interface `jmtrace` (for both windows & linux users): 

```
# in linux shell
$ chmod +x jmtrace
$ ./jmtrace -jar [your-jar].jar <your-params>

# in windows powershell
$ jmtrace -jar [your-jar].jar <your-params>

# example:
$ ./jmtrace -jar example/target/heavysort-1.0.jar
```

## related tools

* jvm's support for `-javaagent`: `premain` method & `java.lang.Instrument`. Interface given by jvm to hijack jvm bytecode class-loading procedure.
* `ASM`: A library for jvm bytecode handling.
