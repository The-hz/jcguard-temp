package io.jcguard.runtime;

import io.jcguard.annotations.Generated;
import java.nio.charset.StandardCharsets;

@Generated
public final class JCGuardStrings {
   private static final String[] CIPHERS = new String[5];
   private static final int[] SITE_SALT = new int[5];
   private static final long MASTER_SEED = -2020739001939761846L;

   JCGuardStrings() {
   }

   static {
      CIPHERS[0] = "\u008b\u007f\u0097\u0011\u0092úC\rµó!\u0095z\u0005¹ï\\\u001dÞm\u0089à";
      SITE_SALT[0] = -470489963;
      CIPHERS[1] = "7É,×.Hê£";
      SITE_SALT[1] = 2109971756;
      CIPHERS[2] = "î#Ô)Â¼PHò¶\u0012Ý&R¢";
      SITE_SALT[2] = -543549465;
      CIPHERS[3] = "¶{\u008cq\u009aä\b\u0013½áM\u0094\u007f@";
      SITE_SALT[3] = 961710526;
      CIPHERS[4] = "o\u0091t\u008fv\u0010²û";
      SITE_SALT[4] = -1691811215;
   }

   public static String decrypt(int var0) {
      String var1 = CIPHERS[var0];
      byte[] var2 = var1.getBytes(StandardCharsets.ISO_8859_1);
      byte[] var3 = new byte[var2.length];
      byte[] var5 = buildKey(var0);

      for (int var4 = 0; var4 < var2.length; var4++) {
         var3[var4] = (byte)(var2[var4] ^ var5[var4 % var5.length] ^ var4 & -1);
      }

      return new String(var3, StandardCharsets.UTF_8);
   }

   private static byte[] buildKey(int var0) {
      int var1 = SITE_SALT[var0];
      long var2 = MASTER_SEED;
      byte[] var4 = new byte[32];

      for (int var5 = 0; var5 < 32; var5++) {
         var4[var5] = (byte)(var0 ^ var1 ^ var5 ^ (int)(var2 >> (var5 & 31)));
      }

      return var4;
   }
}
