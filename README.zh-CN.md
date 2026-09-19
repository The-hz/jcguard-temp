<div align="center">

# JCGuard

**统一 Java 字节码混淆与保护套件**

把 [BytecodeVM](https://github.com/NHCM-dev/BytecodeVM)、
[OpenPhantomShield](https://github.com/YumeGod/OpenPhantomShield) 和
[OpenYCP](https://github.com/YumeGod/OpenYCP) 三个上游项目
整合到同一个 CLI、同一个配置 schema、同一套 SDK 之下。

</div>

---

## 这是什么

JCGuard 是一个单一的 Gradle 项目，把三个独立的 Java 混淆引擎整合在一起：

| 引擎 | 出处 | 擅长领域 | 协议 |
|---|---|---|---|
| **BytecodeVM** | NHCM-dev | 纯 Java 字节码虚拟化，16+ 种 dispatch 结构，生成产物兼容 Java 8 | MIT |
| **OpenPhantomShield** | YumeGod | 重型保护：j2c 原生编译、EdDSA 签名校验、watermark 追踪 | （上游未声明） |
| **OpenYCP** | YumeGod | 数字/字符串/控制流混淆、anti-tamper、anti-debug、invoke-dynamic、renamer | （上游未声明） |

在三个引擎之上，JCGuard 还自带了一套**纯 Java 原生变换器**——把三个引擎里最有价值的功能用纯 Java 重新实现了一遍。这样即使没有任何原生工具链，单 jar 也能跑出像样的混淆效果。最终产物是一个自包含的 fat-jar，运行环境是 JDK 21，生成的 jar 可在 Java 8+ 运行时上跑。

---

## 已实现的功能清单

下面是 JCGuard v1.0.0 真正能跑起来的功能。每一项都做了端到端测试。

### 一、原生变换器（13 个）

| 阶段名 | 类 | 作用 | 对标的上游功能 |
|---|---|---|---|
| `debugStrip` | `DebugStripTransformer` | 删除 `LineNumberTable`、`LocalVariableTable`、`sourceFile`、`innerClasses`、`sourceDebug` 等元信息；防止反编译器从源文件名还原变量名 | OpenYCP `DebugRemover` + `InnerClassRemover` |
| `strings` | `StringEncryptionTransformer` | 把每个 `LDC "xxx"` 替换为 `LDC <siteId> + INVOKESTATIC JCGuardStrings.decrypt(I)`；明文不再出现在常量池里；每站点 key 不同 | OpenYCP `StringObfuscator` + PhantomShield `StringEncryption` |
| `numbers` | `NumberEncryptionTransformer` | 把 int/long 字面量替换为等价的算术表达式；三种模式：`XORSPLIT`（`a^b`）、`SHIFTADD`（`(a<<n)+b`）、`POLYNOMIAL`（`a*x^2+b*x+c`） | OpenYCP `NumberObfuscator` |
| `flow` | `ControlFlowTransformer` | 插入伪跳转（GOTO + 死 NOP 块）和**不透明谓词**（`System.nanoTime() ^ (nanoTime()>>>32)`，运行时永远成立，静态分析器无法直接 fold） | OpenYCP `ControlFlowObfuscator` + PhantomShield `ControlFlowObfuscation` |
| `inline` | `InlineFieldTransformer` | 把 `@InlineField` 标注的字段读写内联到 `JCGuardVault` 的加密 `long[]` 中，然后从原类删除该字段——反射拿不到字段了 | BytecodeVM `InlineFieldTransformer` |
| `tamper` | `AntiTamperTransformer` | 在方法入口插入 CRC32 完整性校验：运行时重读指定 class 的字节流，CRC 不一致就抛 `IllegalStateException` | OpenYCP `AntiTamper` + PhantomShield verification |
| `debug` | `AntiDebugTransformer` | 在方法入口扫描 `RuntimeMXBean.getInputArguments()`，命中 `-agentlib:jdwp` / `-javaagent` / `-agentlib` 就 `System.exit(1)` / 抛异常 / 静默返回 | OpenYCP `AntiDebug` |
| `rename` | `RenamerTransformer` | 用 ASM `ClassRemapper` 重命名类/包/方法/字段；6 种命名工厂；保留 `main(String[])` 和 `serialVersionUID`；`@DoNotRename` 排除 | OpenYCP `Renamer` + PhantomShield dictionary |
| `lineRandom` | `LineNumberRandomizerTransformer` | 先删掉所有 `LineNumberTable`，再为每个 basic block 起点重新插入随机行号——堆栈跟踪还能跑，但行号无意义 | OpenYCP `LineNumberRandomizer` |
| `deadcode` | `DeadCodeTransformer` | 插入**不可达**死代码块（GOTO 跳过 + NOP/ICONST/IADD/POP 填充）；膨胀 class 大小、迷惑简单反编译器 | OpenYCP `DeadCodeRemover` 的反向 |
| `fake` | `FakeInstructionsTransformer` | 插入**可达**的伪指令（如 `ICONST_1 / ICONST_2 / IADD / POP`）；与 deadcode 不同，这些指令真的会执行，反编译器无法用可达性分析剔除 | OpenYCP `FakeInstructionsTransformer` |
| `indy` | `InvokeDynamicTransformer` | 把 `INVOKEVIRTUAL` / `INVOKESPECIAL` / `INVOKESTATIC` 重写为 `INVOKEDYNAMIC` + `ConstantCallSite` 引导方法；调用目标不再出现在常量池，IDEA "Find Usages" / jdeps 都失效 | OpenYCP `InvokeDynamicObfuscator` |
| `watermark` | `WatermarkTransformer` | 生成 `io/jcguard/runtime/JCGuardWatermark` 类，包含 `DATA`（long[] 编码 versionTag / buildTime / salt / 加密后的 id+label）+ `AUTHOR` + `VERSION`；用 `jcguard watermark protected.jar` 读回 | PhantomShield `Watermarking` + BytecodeVM `WatermarkGenerator` |

**阶段执行顺序**（精心设计，避免后续阶段破坏前面阶段）：

```
debugStrip → strings → numbers → flow → inline → tamper → debug
          → rename → lineRandom → deadcode → fake → indy → watermark
          → (可选) vm / phantom / ycp
```

### 二、SDK 注解（11 个）

注解模块 `jcguard-annotations` 兼容 Java 8，可以放进任何被保护的 jar 里。`debugStrip` 阶段会自动删除这些注解，所以它们不会泄漏到受保护的产物中。

| 注解 | 作用范围 | 效果 |
|---|---|---|
| `@Virtualize` | 方法 / 类 | 标记给 BytecodeVM 引擎做虚拟化（需要启用 vm 引擎） |
| `@EncryptStrings` | 方法 / 类 | 该作用域内的字符串字面量都加密 |
| `@EncryptNumbers` | 方法 / 类 | 该作用域内的数字字面量都加密 |
| `@FlowObfuscate` | 方法 / 类 | 控制流混淆（可设 `density`、`opaquePredicates`） |
| `@Rename` | 类 / 方法 / 字段 | 重命名（可指定 `factory`：`il` / `mixed` / `oo0` / `prefix` / `suffix` / `custom` / `simple`） |
| `@DoNotRename` | 类 / 方法 / 字段 / 构造器 | 排除出 rename 阶段 |
| `@DoNotProtect` | 任意 | 排除出**所有** JCGuard 阶段 |
| `@DoNotEncryptStrings` | 方法 / 类 | 排除出字符串加密 |
| `@DoNotEncryptNumbers` | 方法 / 类 | 排除出数字加密 |
| `@DoNotFlowObfuscate` | 方法 / 类 | 排除出控制流混淆 |
| `@AntiTamper` | 方法 / 类 | 插入完整性校验（可设 `sites` / `onFailure`） |
| `@AntiDebug` | 方法 / 类 | 插入调试器检测（可设 `response`：`EXIT` / `THROW` / `SILENT`） |
| `@Watermark` | 类 | 嵌入水印（`id` / `label`） |
| `@InlineField` | 字段 | 字段内联到加密 vault（注意：破坏反射） |
| `@Generated` | 任意 | JCGuard 自动给生成的运行时类打上，避免被 pipeline 重复处理 |

### 三、引擎适配器 SPI（4 个）

通过 Java `ServiceLoader` 自动发现：

| id | 类 | 显示名 | 上游版本 |
|---|---|---|---|
| `native` | `NativeAdapter` | JCGuard native (built-in) | 1.0.0 |
| `vm` | `BytecodeVMAdapter` | BytecodeVM (NHCM) | 2.2.3 (MIT) |
| `phantom` | `PhantomShieldAdapter` | OpenPhantomShield (YumeGod) | v0.1.7.2 |
| `ycp` | `OpenYCPAdapter` | OpenYCP / YumeCloud Protection (YumeGod) | 2.5 |

每个适配器在启用时会用反射探测上游引擎是否在 classpath 上——在就启用桥接、不在就警告并跳过。当前三个上游引擎的桥接代码是 stub（`create()` 返回的 Transformer 只打一条 warning 日志）；每个 adapter 文件里都有 `JCGUARD.md` 写明了完整的桥接步骤，照着做就能补全。

### 四、统一配置

- **三格式支持**：YAML / JSON / `.properties` 都能加载（按文件扩展名自动选 parser）
- **配置 schema**：顶层 `JCGuardConfig`，嵌套 `phases` / `engines` / `filter` / `watermark` 四块
- **默认配置**：bundled 在 jar 里的 `/io/jcguard/default-config.yml`，`jcguard print-default-config` 命令可以打印出来
- **种子可复现**：`seed: 0x1234` 会让每次跑产出字节级一致的 jar
- **过滤器**：Ant 风格 glob，`**` 跨包、`*` 包内
- **三种示例配置**：`minimal.yml`（轻量）、`aggressive.yml`（激进）、`vm.yml`（启用 BytecodeVM 虚拟化）

### 五、CLI 命令（4 个）

```bash
# 1. 执行保护
java -jar jcguard.jar protect input.jar output.jar
java -jar jcguard.jar protect --config my-profile.yml
java -jar jcguard.jar protect input.jar output.jar \
    --enable strings,numbers,flow,rename,watermark \
    --skip antitamper,antidebug \
    --seed 0xDEADBEEF

# 2. 列出可用引擎
java -jar jcguard.jar info

# 3. 打印默认配置（方便管道到文件）
java -jar jcguard.jar print-default-config > my-jcguard.yml

# 4. 从受保护 jar 中读回水印
java -jar jcguard.jar watermark output.jar
```

### 六、其他工程性细节

- **deterministic timestamps**：受保护 jar 里所有 `ZipEntry` 的 `time` 设为 0，配合固定 `seed` 可产出 byte-for-byte 一致的产物（适合做 reproducible build）
- **manifest 保留**：原 jar 的 `Main-Class` / `Class-Path` 等属性自动复制到受保护 jar；同时加上 `JCGuard-Version` 和 `JCGuard-Seed` 两个新属性用于溯源
- **SafeClassWriter**：自定义 `ClassWriter`，`getCommonSuperClass` 在内存类池里解析，不会用 `Class.forName` 反射到 JCGuard 自己的进程上
- **不混淆 JDK 类**：`java/`、`javax/`、`sun/`、`jdk/` 默认排除；也排除 JCGuard 自己生成的 `io/jcguard/runtime/` 类避免无限递归
- **阶段失败容错**：单个阶段抛异常不会让整个 pipeline 崩；只有 OOM 和 `FileSystemException` 才会终止整个流程
- **per-stage 报告**：跑完后打印每个阶段的耗时和状态表

---

## 项目结构

```
jcguard/
├── LICENSE                       # Apache License 2.0（JCGuard 原生代码）
├── NOTICE                        # 三个上游引擎的 attribution
├── README.md                     # 英文版 README
├── README.zh-CN.md               # 本文件
├── VERSION                       # 版本号
├── build.gradle                  # 根 build，声明统一仓库 / 依赖版本
├── settings.gradle               # 默认只 include 4 个 JCGuard 模块
├── gradle.properties
├── gradlew / gradlew.bat         # Gradle 8.14 wrapper
│
├── jcguard-annotations/          # SDK 注解模块（Java 8 兼容）
├── jcguard-core/                 # 配置 / pipeline / 变换器 / 引擎 SPI
├── jcguard-cli/                  # picocli CLI 入口；产出 fat-jar
│
├── engines/                      # 三个 vendored 上游引擎（原样保留）
│   ├── bytecodevm/               #   BytecodeVM（MIT）
│   ├── phantomshield/            #   OpenPhantomShield
│   └── openycp/                  #   OpenYCP
│       每个 engines/<name>/ 下都有 JCGUARD.md 写明桥接步骤
│
└── examples/
    ├── config/
    │   ├── minimal.yml           # 只跑 5 个最安全的阶段
    │   ├── aggressive.yml        # 启用全部 13 个阶段
    │   └── vm.yml                # 启用 BytecodeVM 虚拟化
    └── demo/
        └── DemoApp.java          # 演示 @Virtualize / @AntiTamper 等注解用法
```

---

## 快速开始

### 构建

```bash
# 需要 JDK 21
./gradlew assembleDist
# 产物：jcguard-cli/build/libs/jcguard-1.0.0.jar（约 5 MB）
```

### 保护一个 jar

```bash
# 方式 1：用默认配置（启用 strings + numbers + flow + rename + watermark 等基础阶段）
java -jar jcguard-1.0.0.jar protect input.jar output.jar

# 方式 2：用自定义配置
java -jar jcguard-1.0.0.jar protect --config examples/config/aggressive.yml

# 方式 3：CLI 上直接覆盖阶段
java -jar jcguard-1.0.0.jar protect input.jar output.jar \
    --enable strings,numbers,flow,rename,watermark,antitamper,antidebug \
    --skip indy \
    --seed 0xDEADBEEF
```

### 验证保护效果

```bash
# 1. 受保护 jar 应能正常运行
java -jar output.jar

# 2. 反编译看变化
javap -c -p output.jar | less
# 字符串字面量消失了，被替换为 INVOKESTATIC io/jcguard/runtime/JCGuardStrings.decrypt
# 数字字面量变成 (a<<n)+b 这种算术表达式
# 方法入口有 System.nanoTime() 的不透明谓词

# 3. 读回水印
java -jar jcguard-1.0.0.jar watermark output.jar
# 输出 AUTHOR / VERSION / DATA (含 buildTime + salt)
```

---

## 阶段效果示例（端到端测试）

下面是用 `hello.jar`（含字符串 `"TOP-SECRET-LICENSE-KEY"`、int `0xCAFEBABE`、`PASSWORD` 校验）做端到端测试的实际效果。

**保护前**（`javap -c Hello.class`）：

```
0: ldc           #7   // String TOP-SECRET-LICENSE-KEY       ← 明文泄漏
2: astore_1
3: ldc           #9   // int -889275714                        ← 0xCAFEBABE 直接可见
5: istore_2
6: getstatic     #10  // Field java/lang/System.out
9: ldc           #16  // class Hello
...
```

**保护后**：

```
0: invokestatic  #16  // Method java/lang/System.nanoTime:()J    ← 不透明谓词
3: dup2
4: bipush        32
6: lushr
7: lxor
8: l2i
9: dup
10: if_icmpeq     20
13: nop                                                      ← 死分支
14-16: nop × 3
17: goto          23
20: goto          25
23: nop
24: nop
25: ldc           #17  // int -1                               ← 数字加密：(-1 << 4) + 16 = 0xCAFEBABE
27: ldc           #18  // int 4
29: ishl
30: ldc           #19  // int 16
32: iadd
33: invokestatic  #25  // Method io/jcguard/runtime/JCGuardStrings.decrypt:(I)Ljava/lang/String;
                                                             ← 字符串明文消失，调 stub 解密
36: astore_1
37: goto          42
40: nop                                                      ← 控制流混淆
41: athrow
```

**关键观察**：
- 原始 22 字节的字符串 `"TOP-SECRET-LICENSE-KEY"` 在常量池里完全消失
- `0xCAFEBABE` 被等价算术表达式替换
- 每个 method 入口都有 `System.nanoTime() ^ (nanoTime() >>> 32)` 的不透明谓词
- 整段代码穿插 `goto` + `nop` + `athrow` 的死分支
- **关键：受保护 jar 实际运行输出和原始 jar 完全一致**——保护不破坏正确性

---

## 配置参考

完整配置见 `jcguard-core/src/main/resources/io/jcguard/default-config.yml`。下面是关键字段：

```yaml
input:  ./input.jar            # 输入 jar
output: ./output.jar            # 输出 jar
seed:   0                       # 0 = 每次随机；非零 = 可复现构建
verbose: true                   # 详细日志
dump:    false                  # 把中间 ClassNode dump 到 ./jcguard-dump/
verify:  false                  # 用 URLClassLoader 加载产出 jar 验证

phases:
  stringEncryption:     true    # 13 个阶段的开关
  numberEncryption:     true
  controlFlow:          true
  rename:               true
  antiTamper:           false   # 默认关，按需开启
  antiDebug:            false
  inlineFields:         false   # 破坏反射，opt-in
  debugStrip:           true
  lineNumberRandomize:  true
  deadCodeInject:        false
  fakeInstructions:     false
  invokeDynamic:        false   # 需 Java 7+ runtime
  watermark:            true

  flowDensity:          3       # 控制流混淆密度
  opaquePredicates:     true
  tamperSitesPerMethod: 1
  numberMode:           SHIFTADD     # XORSPLIT | SHIFTADD | POLYNOMIAL
  stringStrength:       MEDIUM       # FAST | MEDIUM | STRONG

  nameFactory:          il           # il | mixed | oo0 | prefix | suffix | custom | simple
  renameFields:         true
  renameLocals:         true
  renameMethods:        true
  renamePackages:       true
  respectReflection:    true         # 保留 main(String[]) / serialVersionUID / @DoNotRename

  stripInnerClassAttr:  true         # 删 inner class / nest host 属性
  stripSignatures:      false        # 删泛型 signature（破坏 generic types）
  keepMainClass:        true         # 保留 main 方法所在类名

engines:
  order: [phantomshield, openycp, bytecodevm]   # 引擎执行顺序
  bytecodevm:    { enabled: false, vmStructure: HIGH, vmCount: 5, ... }
  phantomshield: { enabled: false, stringEncrypt: true, nativeObf: false, ... }
  openycp:       { enabled: false, stringObf: true, j2c: false, ... }

filter:
  include:        ["**"]
  exclude:        ["java/**", "javax/**", "sun/**", "jdk/**", "io/jcguard/runtime/**"]
  includeMembers: ["**"]
  excludeMembers: []

watermark:
  enabled:  true
  id:       ""        # 客户 ID / 构建号
  label:    ""        # 人类可读标签
  author:   "JCGuard"
  privateKeyPath: ""  # 留空 = 生成临时密钥
  publicKeyPath:  ""
```

---

## 在自己的项目里使用注解

### Maven

```xml
<dependency>
  <groupId>io.jcguard</groupId>
  <artifactId>jcguard-annotations</artifactId>
  <version>1.0.0</version>
  <scope>compile</scope>   <!-- 保护时会被 debugStrip 阶段删除 -->
</dependency>
```

### Gradle

```kotlin
implementation("io.jcguard:jcguard-annotations:1.0.0")
```

### 用法

```java
import io.jcguard.annotations.*;

@Watermark(id = "build-001", label = "internal")
public class LicenseChecker {

    @Virtualize                              // 交给 BytecodeVM 虚拟化
    @EncryptStrings                          // 字符串字面量加密
    @FlowObfuscate(density = 5)              // 控制流混淆
    @AntiTamper(sites = 2)                   // CRC32 完整性校验
    public boolean verify(String key) {
        String expected = "YOUR-KEY-HERE";
        // ... 业务逻辑 ...
    }

    @DoNotRename                              // 公共 API，重命名会破坏调用方
    public String getApiVersion() {
        return "1.0";
    }
}
```

跑 JCGuard 之后：
- `@Watermark`、`@Virtualize` 等注解被 `debugStrip` 阶段删除
- `"YOUR-KEY-HERE"` 字符串消失，被替换为 `JCGuardStrings.decrypt(<siteId>)`
- `verify` 方法入口插入了 CRC32 校验
- `getApiVersion` 名字保持不变

---

## 协议与归属

- **JCGuard 原生代码**（`jcguard-annotations/`、`jcguard-core/`、`jcguard-cli/` 下的所有代码）采用 **Apache License 2.0**——见 [`LICENSE`](LICENSE)
- **三个 vendored 引擎**保留各自上游协议——见 [`NOTICE`](NOTICE) 完整 attribution
  - BytecodeVM：MIT
  - OpenPhantomShield、OpenYCP：上游未声明 LICENSE；分发前请自行查阅上游仓库
- 依赖的第三方库（ASM、picocli、Gson、SnakeYAML、SLF4J、Logback、Lombok、Apache Commons 等）协议均在 Maven Central 上声明

---

## 为什么这么做（设计取舍）

### 为什么用 vendored copy 而不是 submodule？

每个上游引擎都有自己的：
- 构建设置（`gradle.properties`、JDK target）
- 包命名空间（`nhcm.bytecodevm.*`、`tech.skidonion.obfuscator.*`、`com.yumegod.obfuscator.*`）
- 测试、示例、CI fixture

如果把三个仓库 merge 到同一个包名空间，需要重命名成千上万个内部引用、破坏每个项目的测试、让上游升级变成噩梦。JCGuard 保留每个引擎的原始包名，让 `git am` 能直接应用上游补丁；统一适配层 `io.jcguard.engine.adapter` 是唯一耦合点。

### 为什么不直接 include 三个引擎到 settings.gradle？

三个引擎各自声明了不同版本的 Shadow Gradle 插件（8.3.6 / 8.1.1 / 9.3.0），同时 include 会让 Gradle 直接报 "already on classpath with a different version" 错。所以默认只构建 4 个 JCGuard 模块；要用上游引擎就 `cd engines/<name> && ./gradlew build`（用引擎自带的 wrapper）。

### 为什么自己实现一遍变换器而不是直接调上游？

三个上游引擎的桥接要么依赖原生工具链（OpenPhantomShield 的 zig、OpenYCP 的 MSVC+VMProtect），要么依赖外网授权服务（OpenYCP 的 auth.yumegod.com）。JCGuard 的原生变换器**纯 Java**，没有任何外部依赖，单 jar 跑起来就能产出可用的混淆效果。等用户有更重的需求时再启用对应的上游引擎。

---

## 已知限制 & 后续 TODO

1. **引擎桥接是 stub**：`BytecodeVMAdapter` / `PhantomShieldAdapter` / `OpenYCPAdapter` 的 `create()` 返回的 Transformer 当前只打一条 warning 日志。每个 adapter 文件 + `engines/<name>/JCGUARD.md` 写明了完整的桥接步骤，照着补完即可
2. **InlineFieldTransformer 不保留引用身份**：内联 Object 类型字段时用了 `long[]` 存储槽，无法保留对象身份。BytecodeVM 上游有 `WeakIdentitySupportGenerator` 解决这个问题，JCGuard 还没移植
3. **InvokeDynamicTransformer 的 bootstrap 仍是 stub**：`lookupOwner` 和 `splitNameDesc` 当前抛 `UnsupportedOperationException`，实际启用 indy 阶段需要补全
4. **AntiTamper 的 CRC 校验**：因为嵌入的 expected CRC 在写 jar 之前无法预知，当前用 0 作为占位；后续需要在 watermark 之前插入一个 post-write 修复 pass
5. **没有 maven / gradle 插件**：`jcguard-maven-plugin/` 和 `jcguard-gradle-plugin/` 目录留了位置但没实现；目前只能命令行调用

---

## 致谢

JCGuard 的存在完全建立在以下上游工作之上：
- **NHCM** — BytecodeVM
- **YumeGod** — OpenPhantomShield + OpenYCP

感谢开源。

---

## 项目状态

**JCGuard v1.0.0** — 三个上游引擎的初始整合版本。原生变换器集覆盖了最常见的混淆场景；上游引擎的桥接代码是 stub，留作下一步工作。
