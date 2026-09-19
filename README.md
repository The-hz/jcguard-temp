# JCGuard temp repo

Quick dump of JCGuard v1.0.0 build + two smoke tests that run the obfuscator end-to-end.

## Files

| Path | What |
|---|---|
| `JCGuard-1.0.0.jar` | Pre-built fat-jar (5 MB). Run with `java -jar JCGuard-1.0.0.jar ...` |
| `smoke-test/` | First test: minimal Hello program |
| `control-flow/` | Second test: program with 7 control-flow structures (recursion, nested if, switch, multi-level loops, try-catch, etc.) |
| `README.md` | This file |

## smoke-test/ — Hello jar

6 phases enabled (debugStrip, strings, numbers, flow, lineRandom, watermark).
6 ms total runtime. Confirms string/number encryption + opaque predicate + watermark.

| File | Description |
|---|---|
| `hello-original.jar` | 1.4 KB test jar (1 class) |
| `hello-protected.jar` | 3.5 KB jar after JCGuard |
| `jcguard-test.yml` | Config used for the run |
| `decompile-comparison.txt` | `javap -c -p` before vs after |
| `vineflower/original/Hello.java` | Vineflower 1.10.1 decompile of original |
| `vineflower/protected/Hello.java` | Vineflower 1.10.1 decompile of protected |
| `vineflower/protected/JCGuardStrings.java` | JCGuard-generated string decryptor stub |
| `vineflower/protected/JCGuardWatermark.java` | JCGuard-generated watermark class |

## control-flow/ — ControlFlowDemo jar

8 phases enabled (debugStrip, strings, numbers, flow, lineRandom, deadcode, fake, watermark).
39 ms total runtime. Tests that JCGuard preserves correctness across:
- recursion (`fib`)
- nested if-else (`grade`)
- switch (`classify`)
- 3-level loops with break/continue (`sumSquares`)
- complex boolean short-circuit (`ping`)
- nested try-catch-finally (`safeDivide`)
- while + arrays (`toRoman`)

| File | Description |
|---|---|
| `cfdemo-original.jar` | 2.5 KB original (1 class, 7 methods) |
| `cfdemo-protected.jar` | 5.7 KB jar after JCGuard |
| `cf-jcguard.yml` | Config used for the run (flowDensity=8, POLYNOMIAL number mode, deadcode + fake enabled) |
| `decompile-comparison.txt` | `javap -c -p` before vs after (46 KB) |
| `vineflower/original/ControlFlowDemo.java` | Vineflower 1.10.1 decompile of original |
| `vineflower/protected/ControlFlowDemo.java` | Vineflower 1.10.1 decompile of protected |

## How to reproduce

```bash
# 1) JCGuard v1.0.0 fat-jar — run any subcommand
java -jar JCGuard-1.0.0.jar --help
java -jar JCGuard-1.0.0.jar info
java -jar JCGuard-1.0.0.jar print-default-config > my.yml

# 2) Verify protected jars still run identically to originals
java -jar smoke-test/hello-protected.jar
java -jar control-flow/cfdemo-protected.jar

# 3) Read back the watermark from any protected jar
java -jar JCGuard-1.0.0.jar watermark smoke-test/hello-protected.jar
java -jar JCGuard-1.0.0.jar watermark control-flow/cfdemo-protected.jar

# 4) Reproduce the obfuscation runs
java -jar JCGuard-1.0.0.jar protect --config smoke-test/jcguard-test.yml
java -jar JCGuard-1.0.0.jar protect --config control-flow/cf-jcguard.yml

# 5) Decompile with Vineflower 1.10.1 to see the before/after
#    (download separately from https://github.com/Vineflower/vineflower)
java -jar vineflower.jar smoke-test/hello-original.jar out-original/
java -jar vineflower.jar smoke-test/hello-protected.jar out-protected/
```

## What JCGuard actually did

Both runs confirm these phases work end-to-end:

| Phase | Effect |
|---|---|
| `debugStrip` | Remove `LineNumberTable`, `LocalVariableTable`, `sourceFile`, inner-class attrs |
| `strings` | Replace every `LDC "..."` with `INVOKESTATIC JCGuardStrings.decrypt(<siteId>)` — cleartext disappears from constant pool |
| `numbers` | Replace int/long literals with arithmetic expressions: `XORSPLIT` / `SHIFTADD` / `POLYNOMIAL` (control-flow test used POLYNOMIAL → `a*x² + b*x + c`) |
| `flow` | Insert opaque predicate (`System.nanoTime() ^ (nanoTime() >>> 32)`) at every method entry + bogus `goto/nop/athrow` dead branches |
| `lineRandom` | Replace line number table with random values — stack traces show meaningless line numbers |
| `deadcode` | Insert unreachable dead code blocks (GOTO + NOP/ICONST/IADD/POP) to inflate class file size |
| `fake` | Insert **reachable** fake instructions (e.g. `ICONST_1 / ICONST_2 / IADD / POP`) that actually execute but have no effect |
| `watermark` | Embed `io/jcguard/runtime/JCGuardWatermark.class` with versionTag + buildTime + salt + encrypted id+label |

### Smoke test results

| | Original | Protected | Change |
|---|---|---|---|
| hello.jar size | 1.4 KB | 3.5 KB | +158% |
| cfdemo.jar size | 2.5 KB | 5.7 KB | +128% |
| `fib()` bytecode length | 11 bytes | 80+ bytes | +700% |
| String literals in constant pool | visible | 0 | all encrypted |
| Numeric literals | visible | 0 direct | all arithmetic-encoded |
| Method entries with opaque predicate | 0 | 100% | every method |
| **Output correctness** | ✓ | ✓ | identical to original |

## Notes

- Both protected jars run **identically** to their originals — JCGuard preserves program semantics.
- Each JCGuard run uses a random master seed, so two runs on the same input produce different ciphertexts.
- Pin the seed with `seed: 0x...` in the config for reproducible builds.
- Vineflower 1.10.1 (released 2024-04) is the most recent version and recovers most of the obfuscated control flow structure; older decompilers (CFR, Procyon, Fernflower) will produce more fragmented output.
