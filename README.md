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

ASM 接口:event是什么，谁在消耗，visit时做了什么？

构建系统调整：
1. 对maven plugin机制不熟悉
2. 不知道为什么不把asm加入jar包，用作javaagent时会报反射错误（ClassVisitor not found）
3. 去除Makefile

git出问题了: 可能是因为onedrive在不同的电脑上... 可能新电脑装了gnuwin也导致了一些问题