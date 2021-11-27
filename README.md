# jmtrace

Hijack jvm's class-loading (as a javaagent) to instrument these instructions (`getstatic/putstatic/getfield/putfield/*aload/*astore`), tracing shared memory accesses.

## build & usage 

build & test this project:

```shell
maven package  # build agent & sample jar 
java -javaagent:agent/target/jmtrace-agent-1.0.jar -jar example/target/heavysort-1.0.jar  # run sample
# clean everything up
maven clean
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

We also provide a `Makefile`:

```shell
make
make test
make clean
```

And a command line interface `jmtrace` (in python): 

```
$ jmtrace --help
...
$ jmtrace -jar hello.jar "hello world"
# run agent with any executable jar
```

***

## related tools

* jvm's support for `-javaagent`: `premain` method & `java.lang.Instrument`. Interface given by jvm to hijack jvm bytecode class-loading procedure.
* `ASM`: A library for jvm bytecode handling.

## Takeaway

1. `maven` & `jar` 
2. `javaagent`
3. ASM's mechanism & jvm
