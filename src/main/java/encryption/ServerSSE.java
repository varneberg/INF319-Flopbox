package encryption;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

public class ServerSSE {

    int blockSize = 24;
    int m = blockSize/2;

    public ServerSSE(){

    }

    public boolean checkMatch(File encrypted, String searchToken) throws FileNotFoundException {
        String keyword = searchToken.substring(0, blockSize);
        String k = searchToken.substring(blockSize);



        Scanner fileReader = new Scanner(encrypted);

        String L = keyword.substring(0,blockSize-m);
        byte[] LBytes = L.getBytes(StandardCharsets.UTF_8);

        String R = keyword.substring(m);
        byte[] RBytes = R.getBytes(StandardCharsets.UTF_8);

        Random random = new Random(Integer.parseInt(k));
        RandomString fsGenerator = new RandomString(blockSize-m,random);
        String fk = fsGenerator.nextString();

        int j =0;
        while (fileReader.hasNextLine()) {
            String data = fileReader.nextLine();
            String[] words = data.split("(?<=\\G.{" + blockSize + "})");


            for (String word : words) {







                String c1 = word.substring(0,blockSize-m);
                byte[] c1Bytes = c1.getBytes(StandardCharsets.UTF_8);

                String c2 = word.substring(m);
                byte[] c2Bytes = c2.getBytes(StandardCharsets.UTF_8);

                byte[] s = new byte[c1Bytes.length];
                for (int i =0;i<c1Bytes.length;i++){
                    s[i] = (byte) (c1Bytes[i] ^ LBytes[i]);
                }


                byte[] fkBytes = fk.getBytes(StandardCharsets.UTF_8);

                byte[] sf = new byte[c1Bytes.length];
                for (int i =0;i<c1Bytes.length;i++){
                    sf[i] = (byte) (s[i] ^ fkBytes[i]);
                }

                byte[] rc2 = new byte[c2Bytes.length];
                for (int i =0;i<rc2.length;i++){
                    rc2[i] = (byte) (sf[i] ^ RBytes[i]);
                }

                String a = new String(sf, StandardCharsets.UTF_8);
                String b = new String(rc2, StandardCharsets.UTF_8);
                //String c = new String(fkBytes, StandardCharsets.UTF_8);
                System.out.println(" w: "+word + " L: " + L + " K: " + k + " sf: " + a + " c2: " + b);

                System.out.println(j);
                j++;
                if(a.equals(b)){
                    return true;
                }


            }
        }
        return false;
    }


    public static class RandomString {

        /**
         * Generate a random string.
         */
        public String nextString() {
            for (int idx = 0; idx < buf.length; ++idx)
                buf[idx] = symbols[random.nextInt(symbols.length)];
            return new String(buf);
        }

        public static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        public static final String lower = upper.toLowerCase(Locale.ROOT);

        public static final String digits = "0123456789";

        public static final String alphanum = upper + lower + digits;

        private final Random random;

        private final char[] symbols;

        private final char[] buf;

        public RandomString(int length, Random random, String symbols) {
            if (length < 1) throw new IllegalArgumentException();
            if (symbols.length() < 2) throw new IllegalArgumentException();
            this.random = Objects.requireNonNull(random);
            this.symbols = symbols.toCharArray();
            this.buf = new char[length];
        }

        /**
         * Create an alphanumeric string generator.
         */
        public RandomString(int length, Random random) {
            this(length, random, alphanum);
        }

        /**
         * Create an alphanumeric strings from a secure generator.
         */
        public RandomString(int length) {
            this(length, new SecureRandom());
        }

        /**
         * Create session identifiers.
         */
        public RandomString() {
            this(21);
        }

    }

}
