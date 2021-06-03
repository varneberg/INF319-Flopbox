package encryption;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    private byte f2plus(byte a, byte b){
        Integer c = a + b;
        return c.byteValue();
    }
    private byte f2minus(byte a, byte b){
        Integer c = a - b;
        return c.byteValue();
    }

    public boolean checkMatch(File encrypted, String searchToken) throws IOException {
        String keyword = searchToken.substring(0, blockSize);
        String k = searchToken.substring(blockSize);

        Scanner fileReader = new Scanner(encrypted);

        String fileString = Files.readString(Paths.get(encrypted.getAbsolutePath()));


        String L = keyword.substring(0,blockSize-m);
        byte[] LBytes = L.getBytes(Charset.forName("ISO-8859-1"));

        String R = keyword.substring(m);
        byte[] RBytes = R.getBytes(Charset.forName("ISO-8859-1"));

        Random random = new Random(Integer.parseInt(k));
        RandomString fkGenerator = new RandomString(blockSize-m,random);
        String fk = fkGenerator.nextString();


        //while (fileReader.hasNextLine()) {
        for (int j = 0; j <= fileString.length() - 1;) {

            String word = fileString.substring(j, j + blockSize);



                String c1 = word.substring(0,blockSize-m);
                byte[] c1Bytes = c1.getBytes(Charset.forName("ISO-8859-1"));

                String c2 = word.substring(m);
                byte[] c2Bytes = c2.getBytes(Charset.forName("ISO-8859-1"));

                byte[] sBytes = new byte[c1Bytes.length];
                for (int i =0;i<c1Bytes.length;i++){
                    sBytes[i] = f2minus(c1Bytes[i] , LBytes[i]);
                }


                byte[] fkBytes = fk.getBytes(Charset.forName("ISO-8859-1"));

                String c = new String(fkBytes, Charset.forName("ISO-8859-1"));

                byte[] fs = new byte[c1Bytes.length];
                for (int i =0;i<c1Bytes.length;i++){
                    fs[i] = f2plus(sBytes[i] , fkBytes[i]);
                }

                byte[] fsr = new byte[c2Bytes.length];
                for (int i =0;i<fsr.length;i++){
                    fsr[i] = f2plus(fs[i] , RBytes[i]);
                }

                String a = new String(c2Bytes, Charset.forName("ISO-8859-1"));
                String b = new String(fsr, Charset.forName("ISO-8859-1"));


                if(a.equals(b)){
                    return true;
                }

                j = j + blockSize;


            }
        //}
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
