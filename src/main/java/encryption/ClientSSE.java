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



    public File decryptFile(File encrypted){
        String hashed = Integer.toString(encrypted.hashCode());
        if (!lookup.containsKey(hashed)){
            System.out.println("key missing in lookup");
            return encrypted;
        }
        String seed = lookup.get(hashed);
        Random random = new Random(seed.hashCode());
        RandomString randomStringGenerator = new RandomString(m,random);

        File decrypted = new File("decrypted.txt");

        try {

            Scanner fileReader = new Scanner(encrypted);
            System.out.println(fileReader.hasNextLine());
            FileWriter fileWriter = new FileWriter(decrypted);
            while (fileReader.hasNextLine()) {
                String data = fileReader.nextLine();
                String[] words = data.split("(?<=\\G.{" + blockSize + "})");

                for(String word : words){
                    String decryptedWord = decryptBlock(word,randomStringGenerator);
                    decryptedWord = decryptedWord.replace("*", "");
                    decryptedWord += " ";
                    fileWriter.write(decryptedWord);
                }
            }
            fileWriter.close();
            fileReader.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return decrypted;
    }

    private String decryptBlock(String word, RandomString randomStringGenerator){
        String C1 = word.substring(0,blockSize-m);
        String C2 = word.substring(blockSize-m);

        String s = randomStringGenerator.nextString();

        byte[] sBytes = s.getBytes(StandardCharsets.UTF_8);
        byte[] cipherBytes1 = C1.getBytes(StandardCharsets.UTF_8);

        byte[] LBytes = new byte[cipherBytes1.length];

        for (int i =0;i<cipherBytes1.length;i++){
            LBytes[i] = (byte) (cipherBytes1[i] ^ sBytes[i]);
        }

        String L = new String(LBytes, StandardCharsets.UTF_8);

        int k = L.hashCode();
        Random random = new Random(k);
        RandomString fsGenerator = new RandomString(blockSize-m,random);
        String fs = fsGenerator.nextString();


        byte[] cipherBytes2 = C2.getBytes(StandardCharsets.UTF_8);
        byte[] fsBytes = fs.getBytes(StandardCharsets.UTF_8);

        byte[] RBytes = new byte[cipherBytes2.length];

        for (int i =0;i<cipherBytes2.length;i++){
            RBytes[i] = (byte) (cipherBytes2[i] ^ fsBytes[i]);
        }

        String R = new String(RBytes, StandardCharsets.UTF_8);


        String T = L + R;

        return T;
    }

    public void setLookup(File lookup) throws IOException {
        try {
            File toRead = new File("lookup.txt");
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

        byte[] clearBytes1 = L.getBytes(StandardCharsets.UTF_8);
        byte[] clearBytes2 = R.getBytes(StandardCharsets.UTF_8);

        byte[] sBytes = s.getBytes(StandardCharsets.UTF_8);
        byte[] fsBytes = fs.getBytes(StandardCharsets.UTF_8);

        byte[] T1 = new byte[clearBytes1.length];
        byte[] T2 = new byte[clearBytes2.length];

        for (int i =0;i<clearBytes1.length;i++){
            T1[i] = (byte) (clearBytes1[i] ^ sBytes[i]);
        }
        for (int i =0;i<clearBytes2.length;i++){
            T2[i] = (byte) (clearBytes2[i] ^ fsBytes[i]);
        }

        String T1string = new String(T1, StandardCharsets.UTF_8);
        String T2string = new String(T2, StandardCharsets.UTF_8);


        String C = T1string + T2string;
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
