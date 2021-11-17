# jmtrace

Hijack jvm's class-loading to instrument these instructions (`getstatic/putstatic/getfield/putfield/*aload/*astore`), tracing shared memory accesses.

## build

### config project in ide

### maven

## usage 

```
$ jmtrace -jar something.jar "hello world"
R 1032 b026324c6904b2a9 cn.edu.nju.ics.Foo.someField
W 1031 e7df7cd2ca07f4f1 java.lang.Object[0]
W 1031 e7df7cd2ca07f4f2 java.lang.Object[1]
...
# R/W <thrdid> <objectid> <field/array access>
```


## related tools

* javaagent
* `java.lang.Instrument`
* `ASM`

## TODO 

ASM 接口

构建系统调整

git出问题了