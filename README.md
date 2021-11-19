# jmtrace

Hijack jvm's class-loading to instrument these instructions (`getstatic/putstatic/getfield/putfield/*aload/*astore`), tracing shared memory accesses.

## build & usage 

automated test:

```shell
# wrap `maven` with Makefile currently...
make test  # build example jar 
make  # build agent and test it with example jar
```

or use command for any `jar` (TODO):

```
$ jmtrace -jar something.jar "hello world"
# R/W <thrdid> <objectid> <field/array access>
```


## related tools

* jvm's support for `-javaagent`: `premain` method & `java.lang.Instrument`. Interface given by jvm to hijack jvm bytecode class-loading procedure.
* `ASM`: A library for jvm bytecode handling. 

## TODO 

- [ ] `Makefile -> full maven` 
- [ ] doc & report

## Takeaway

1. `maven` & `jar` 
2. `javaagent`
3. ASM's mechanism & jvm

> Man will get better understanding with jvm and the difference between static and dynamic analysis :D
