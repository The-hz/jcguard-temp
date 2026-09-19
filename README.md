# JCGuard temp repo

Quick dump of JCGuard v1.0.0 build + a smoke test that runs the obfuscator against `hello.jar`.

## Files

| File | What |
|---|---|
| `JCGuard-1.0.0.jar` | Pre-built fat-jar (5 MB). Run with `java -jar JCGuard-1.0.0.jar ...` |
| `hello-original.jar` | Test program before obfuscation (1.4 KB, 1 class) |
| `hello-protected.jar` | Same program after JCGuard obfuscation (3.5 KB, 3 classes) |
| `jcguard-test.yml` | Config used for the run |
| `decompile-comparison.txt` | `javap -c -p` of original vs protected `Hello.class` |
| `README.zh-CN.md` | Full Chinese README documenting all features |

## Run

```bash
# 1) protected jar should still produce the same output as original
java -jar hello-protected.jar
# → Hello from Hello
# → Secret length: 22
# → Code: cafebabe
# → Access granted.

# 2) read back the JCGuard watermark
java -jar JCGuard-1.0.0.jar watermark hello-protected.jar

# 3) protect some other jar
java -jar JCGuard-1.0.0.jar protect your-app.jar your-app-protected.jar \
    --enable strings,numbers,flow,rename,watermark \
    --seed 0xDEADBEEF
```

## What JCGuard actually did to hello.jar

- Encrypted every string literal (`"TOP-SECRET-LICENSE-KEY"`, `"PASSWORD"`, etc.) → replaced `LDC "..."` with `INVOKESTATIC io/jcguard/runtime/JCGuardStrings.decrypt(I)`
- Encrypted the int `0xCAFEBABE` → `(-1 << 7) + 128` arithmetic expression
- Inserted an opaque predicate (`System.nanoTime() ^ (nanoTime() >>> 32)`) at every method entry
- Inserted unreachable `goto + nop + athrow` dead branches throughout the bytecode
- Randomised the `LineNumberTable` so stack traces show meaningless line numbers
- Stripped debug info (source file, local variable table, inner class attrs)
- Embedded a watermark class `io/jcguard/runtime/JCGuardWatermark.class` with author/version/salt

See `decompile-comparison.txt` for the full before/after `javap` dump.
