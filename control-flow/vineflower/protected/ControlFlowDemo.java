import io.jcguard.runtime.JCGuardStrings;

public class ControlFlowDemo {
   public static void main(String[] var0) {
      int var10000 = 1 + 2;
      long var3 = System.nanoTime();
      if ((int)(var3 ^ var3 >>> 32) != (int)(var3 ^ var3 >>> 32)) {
         ;
      }

      System.out.println(JCGuardStrings.decrypt(7 * 99 * 99 + 20 * 99 + -70587));
      System.out.println("fib(10) = " + fib(10));
      System.out.println("grade(85) = " + grade(85));
      System.out.println("classify(7) = " + classify(7));
      System.out.println("sumSquares(5) = " + sumSquares(5));
      System.out.println("ping(3, 5) = " + ping(3, 5));

      try {
         int var1 = safeDivide(100, 0);
         System.out.println("safeDivide(100, 0) = " + var1);
      } catch (ArithmeticException var2) {
         System.out.println("safeDivide(100, 0) threw: " + var2.getMessage());
      }

      System.out.println("toRoman(2024) = " + toRoman(2024));
   }

   static int fib(int var0) {
      int var10000 = 1 + 2;
      long var1 = System.nanoTime();
      int var10001 = 1 + 2;
      if ((int)(var1 ^ var1 >>> 32) != (int)(var1 ^ var1 >>> 32)) {
         ;
      }

      return var0 < 2 ? var0 : fib(var0 - 1) + fib(var0 - 2);
   }

   static char grade(int var0) {
      int var10000 = 1 + 2;
      long var1 = System.nanoTime();
      if ((int)(var1 ^ var1 >>> 32) != (int)(var1 ^ var1 >>> 32)) {
         ;
      }

      if (var0 >= 90) {
         return 'A';
      } else if (var0 >= 80) {
         return 'B';
      } else if (var0 >= 70) {
         return 'C';
      } else {
         return (char)(var0 >= 60 ? 'D' : 'F');
      }
   }

   static String classify(int var0) {
      int var10000 = 1 + 2;
      long var4 = System.nanoTime();
      if ((int)(var4 ^ var4 >>> 32) != (int)(var4 ^ var4 >>> 32)) {
         ;
      }
      String var1 = switch (var0) {
         case 1, 2, 3, 4, 5 -> JCGuardStrings.decrypt(1 * 99 * 99 + 4 * 99 + -10196);
         case 6, 7 -> JCGuardStrings.decrypt(1 * 49 * 49 + 58 * 49 + -5241);
         default -> JCGuardStrings.decrypt(7 * 77 * 77 + 52 * 77 + -45504);
      };
      StringBuilder var2 = new StringBuilder();

      for (int var3 = 0; var3 < var0; var3++) {
         var2.append(var1.charAt(0));
      }

      return var2.toString();
   }

   static int sumSquares(int var0) {
      int var10000 = 1 + 2;
      long var5 = System.nanoTime();
      if ((int)(var5 ^ var5 >>> 32) != (int)(var5 ^ var5 >>> 32)) {
         ;
      }

      int var1 = 0;

      for (int var2 = 0; var2 < var0; var2++) {
         for (int var3 = 0; var3 < var0; var3++) {
            if (var2 * var3 <= 10) {
               for (int var4 = 0; var4 < var0; var4++) {
                  if (var2 + var3 + var4 > 100) {
                     return var1;
                  }

                  var1 += var2 + var3 + var4;
               }
            }
         }
      }

      return var1;
   }

   static boolean ping(int var0, int var1) {
      int var10000 = 1 + 2;
      long var2 = System.nanoTime();
      if ((int)(var2 ^ var2 >>> 32) != (int)(var2 ^ var2 >>> 32)) {
         ;
      }

      if ((var0 <= 0 || var1 <= 0 || (var0 + var1) % 2 != 0) && var0 != var1) {
         return false;
      } else if (var0 > var1) {
         return var0 - var1 < 10;
      } else {
         return var0 < var1 ? var1 - var0 < 10 : true;
      }
   }

   static int safeDivide(int var0, int var1) {
      int var10000 = 1 + 2;
      long var16 = System.nanoTime();
      if ((int)(var16 ^ var16 >>> 32) != (int)(var16 ^ var16 >>> 32)) {
         ;
      }

      byte var3;
      try {
         int var2 = var0 / var1;

         try {
            return var2 + 1;
         } finally {
            System.out.println("  inner finally r=" + var2);
         }
      } catch (ArithmeticException var13) {
         var3 = -1;
      } finally {
         System.out.println("  outer finally a=" + var0 + " b=" + var1);
      }

      return var3;
   }

   static String toRoman(int var0) {
      int var10000 = 1 + 2;
      long var5 = System.nanoTime();
      if ((int)(var5 ^ var5 >>> 32) != (int)(var5 ^ var5 >>> 32)) {
         ;
      }

      int[] var1 = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
      String[] var2 = new String[]{
         JCGuardStrings.decrypt(5 * 25 * 25 + 51 * 25 + -4396),
         JCGuardStrings.decrypt(3 * 103 * 103 + 28 * 103 + -34706),
         JCGuardStrings.decrypt(7 * 74 * 74 + 21 * 74 + -39880),
         JCGuardStrings.decrypt(7 * 107 * 107 + 5 * 107 + -80671),
         JCGuardStrings.decrypt(8 * 39 * 39 + 38 * 39 + -13642),
         JCGuardStrings.decrypt(3 * 97 * 97 + 48 * 97 + -32874),
         JCGuardStrings.decrypt(3 * 65 * 65 + 55 * 65 + -16240),
         JCGuardStrings.decrypt(5 * 79 * 79 + 16 * 79 + -32458),
         JCGuardStrings.decrypt(6 * 75 * 75 + 40 * 75 + -36738),
         JCGuardStrings.decrypt(7 * 29 * 29 + 61 * 29 + -7643),
         JCGuardStrings.decrypt(5 * 91 * 91 + 39 * 91 + -44940),
         JCGuardStrings.decrypt(1 * 50 * 50 + 38 * 50 + -4385),
         JCGuardStrings.decrypt(8 * 56 * 56 + 6 * 56 + -25408)
      };
      StringBuilder var3 = new StringBuilder();

      for (int var4 = 0; var0 > 0; var4++) {
         while (var0 >= var1[var4]) {
            var3.append(var2[var4]);
            var0 -= var1[var4];
         }
      }

      return var3.toString();
   }
}
