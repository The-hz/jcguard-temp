package io.jcguard.runtime;

import io.jcguard.annotations.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

// $VF: synthetic class
@Generated
public final class JCGuardWatermark {
   public static final long[] DATA = new long[]{65536L, 1789797434L, -4104097708253789906L, 1L, -12729L, 2L, -14784L, -39L};
   public static final String AUTHOR = "JCGuard";
   public static final String VERSION = "1.0.0";

   public static Map<String, String> info() {
      LinkedHashMap var0 = new LinkedHashMap();
      var0.put("salt", "0xc70b50e0f809c12e");
      var0.put("build-time", "1789797434");
      var0.put("version", "1.0.0");
      var0.put("author", "JCGuard");
      var0.put("label", "smoke-test");
      var0.put("id", "test-001");
      return var0;
   }
}
