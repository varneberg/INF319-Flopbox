package encryption;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.io.IOException;

public class ClientSSE {


    private static int blockSize = 24;
    private static int m = blockSize/2;
    private String[] s_values;
    private String key;
    private HashMap<String, String> lookup;

    public ClientSSE(String key){
        this.key = key;
    }

    private void generateS(){
        Random random = new Random(Integer.parseInt(key));
        RandomString randomStringGenerator = new RandomString(m,random);


    }

    public String[] generateSearchToken(String keyword){
        keyword = correctLength(keyword);
        String L = keyword.substring(0,blockSize-m);
        int k = L.hashCode();
        String[] token = new String[] {keyword, Integer.toString(k)};
        return token;
    }



    public String[] decryptFile(){
        return null;
    }

    private String decryptBlock(){
        return null;
    }

    public void setLookup(File lookup) throws IOException {
        try {
            File toRead = new File("lookup");
            FileInputStream fis = new FileInputStream(toRead);
            ObjectInputStream ois = new ObjectInputStream(fis);

            HashMap<String,String> mapInFile=(HashMap<String,String>)ois.readObject();

            ois.close();
            fis.close();

            this.lookup = mapInFile;

            //print All data in MAP
            for(Map.Entry<String,String> m :mapInFile.entrySet()){
                System.out.println(m.getKey()+" : "+m.getValue());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public File getLookup() throws IOException {
        File lookupFile = new File("lookup.txt");
        try {
            FileOutputStream fos = new FileOutputStream(lookupFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(lookup);
            oos.flush();
            oos.close();
            fos.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
        return lookupFile;
    }

    public File encryptFile(File clear) {

        if(lookup == null){
            lookup = new HashMap<String,String>();
        }
        int numberOfFiles = lookup.size();
        String seed = key + numberOfFiles;

        Random random = new Random(seed.hashCode());
        RandomString randomStringGenerator = new RandomString(m,random);

        File encrypted = new File(clear.getName());
        try {

            Scanner fileReader = new Scanner(clear);
            System.out.println(fileReader.hasNextLine());
            FileWriter fileWriter = new FileWriter(encrypted);
            while (fileReader.hasNextLine()) {
                String data = fileReader.nextLine();
                String[] words = data.split(" ");


                for(String word : words){
                    String encryptedWord = encryptWord(word,randomStringGenerator);
                    fileWriter.write(encryptedWord);
                }
            }
            fileWriter.close();
            fileReader.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        lookup.put(Integer.toString(encrypted.hashCode()), seed);

        return encrypted;
    }

    private String encryptWord(String word, RandomString randomStringGenerator) {
        word = correctLength(word);
        String L = word.substring(0,blockSize-m);
        String R = word.substring(blockSize-m);
        int k = L.hashCode();
        String s = randomStringGenerator.nextString();

        Random random = new Random(k);
        RandomString fsGenerator = new RandomString(blockSize-m,random);
        String fs = fsGenerator.nextString();

        StringXORer xorer = new StringXORer();
        String T1 = xorer.encode(L, s);
        String T2 = xorer.encode(R, fs);
        String C = T1 + T2;
        return C;
    }

    public class StringXORer {

        public String encode(String s, String key) {
            return base64Encode(xorWithKey(s.getBytes(), key.getBytes()));
        }

        public String decode(String s, String key) {
            return new String(xorWithKey(base64Decode(s), key.getBytes()));
        }

        private byte[] xorWithKey(byte[] a, byte[] key) {
            byte[] out = new byte[a.length];
            for (int i = 0; i < a.length; i++) {
                out[i] = (byte) (a[i] ^ key[i%key.length]);
            }
            return out;
        }

        private byte[] base64Decode(String s) {

            byte[] decodedBytes = Base64.getDecoder().decode(s);
            return decodedBytes;
        }

        private String base64Encode(byte[] bytes) {
            String s = Base64.getEncoder().encodeToString(bytes).replaceAll("\\s", "");
            return s;
        }
    }


    private String correctLength(String keyword) {
        while(keyword.length() < blockSize){
            keyword += "*";
        }
        while (keyword.length() > blockSize){
            keyword = keyword.substring(0, keyword.length() - 1);
        }

        return keyword;
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
