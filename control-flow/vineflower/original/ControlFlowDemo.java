public class ControlFlowDemo {
   public static void main(String[] var0) {
      System.out.println("=== ControlFlowDemo ===");
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
      return var0 < 2 ? var0 : fib(var0 - 1) + fib(var0 - 2);
   }

   static char grade(int var0) {
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
      String var1 = switch (var0) {
         case 1, 2, 3, 4, 5 -> "weekday";
         case 6, 7 -> "weekend";
         default -> "invalid";
      };
      StringBuilder var2 = new StringBuilder();

      for (int var3 = 0; var3 < var0; var3++) {
         var2.append(var1.charAt(0));
      }

      return var2.toString();
   }

   static int sumSquares(int var0) {
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
      if ((var0 <= 0 || var1 <= 0 || (var0 + var1) % 2 != 0) && var0 != var1) {
         return false;
      } else if (var0 > var1) {
         return var0 - var1 < 10;
      } else {
         return var0 < var1 ? var1 - var0 < 10 : true;
      }
   }

   static int safeDivide(int var0, int var1) {
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
      int[] var1 = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
      String[] var2 = new String[]{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
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
