public class Hello {
   public static void main(String[] var0) {
      String var1 = "TOP-SECRET-LICENSE-KEY";
      int var2 = -889275714;
      System.out.println("Hello from " + Hello.class.getSimpleName());
      System.out.println("Secret length: " + var1.length());
      System.out.println("Code: " + Integer.toHexString(var2));
      if (verify("PASSWORD")) {
         System.out.println("Access granted.");
      } else {
         System.out.println("Access denied.");
      }
   }

   static boolean verify(String var0) {
      String var1 = "PASSWORD";
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
