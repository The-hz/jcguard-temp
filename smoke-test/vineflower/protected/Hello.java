import io.jcguard.runtime.JCGuardStrings;

public class Hello {
   public static void main(String[] var0) {
      long var10000 = System.nanoTime();
      if ((int)(var10000 ^ var10000 >>> 32) != (int)(var10000 ^ var10000 >>> 32)) {
         ;
      }

      String var1 = JCGuardStrings.decrypt((-1 << 7) + 128);
      int var2 = (-55579733 << 4) + 14;
      System.out.println("Hello from " + Hello.class.getSimpleName());
      System.out.println("Secret length: " + var1.length());
      System.out.println("Code: " + Integer.toHexString(var2));
      if (verify(JCGuardStrings.decrypt((1 << 2) + -3))) {
         System.out.println(JCGuardStrings.decrypt((1 << 7) + -126));
      } else {
         System.out.println(JCGuardStrings.decrypt((1 << 2) + -1));
      }
   }

   static boolean verify(String var0) {
      long var10000 = System.nanoTime();
      if ((int)(var10000 ^ var10000 >>> 32) != (int)(var10000 ^ var10000 >>> 32)) {
         ;
      }

      String var1 = JCGuardStrings.decrypt((1 << 4) + -12);
      if (var0.length() != var1.length()) {
         return false;
      } else {
         int var2 = 0;

         for (int var3 = 0; var3 < var0.length(); var3++) {
            var2 |= var0.charAt(var3) ^ var1.charAt(var3);
         }

         return var2 == 0;
      }
   }
}
