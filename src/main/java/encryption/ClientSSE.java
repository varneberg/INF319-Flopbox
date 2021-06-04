package encryption;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;
import java.io.IOException;

public class ClientSSE {

    private static int blockSize = 24;
    private static int m = blockSize/2;
    private String key;
    private HashMap<String, String> lookup;
    private static char filler = '*';
    private static String tmpFolder = "./src/main/resources/tmp/";

    public ClientSSE(String key){
        this.key = key;
    }

    public String generateSearchToken(String keyword){
        keyword = correctLength(keyword);
        String L = keyword.substring(0,blockSize-m);
        int k = L.hashCode();

        String token = keyword + k;
        return token;
    }

    private byte f2plus(byte a, byte b){
        Integer c = a + b;
        return c.byteValue();
    }

    private byte f2minus(byte a, byte b){
        Integer c = a - b;
        return c.byteValue();
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

        File decrypted = new File(tmpFolder + "decrypted.txt");

        try {

            String fileString = Files.readString(Paths.get(encrypted.getAbsolutePath()));

            FileWriter fileWriter = new FileWriter(decrypted);

            for (int i = 0; i <= fileString.length() - 1;) {
                String word = fileString.substring(i, i + blockSize);
                String decryptedWord = decryptBlock(word,randomStringGenerator);
                decryptedWord = decryptedWord.replace("*", "");
                decryptedWord += " ";
                fileWriter.write(decryptedWord);

                i = i + blockSize;
            }

            fileWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return decrypted;
    }

    private String decryptBlock(String word, RandomString randomStringGenerator){
        String C1 = word.substring(0,blockSize-m);
        String C2 = word.substring(m);

        String s = randomStringGenerator.nextString();

        byte[] sBytes = s.getBytes(Charset.forName("ISO-8859-1"));
        byte[] cipherBytes1 = C1.getBytes(Charset.forName("ISO-8859-1"));

        byte[] LBytes = new byte[cipherBytes1.length];

        for (int i =0;i<cipherBytes1.length;i++){
            LBytes[i] = f2minus(cipherBytes1[i] , sBytes[i]);
        }

        String L = new String(LBytes, Charset.forName("ISO-8859-1"));

        int k = L.hashCode();
        Random random = new Random(k);
        RandomString fkGenerator = new RandomString(blockSize-m,random);
        String fk = fkGenerator.nextString();

        byte[] cipherBytes2 = C2.getBytes(Charset.forName("ISO-8859-1"));
        byte[] fkBytes = fk.getBytes(Charset.forName("ISO-8859-1"));

        byte[] fsBytes = new byte[fkBytes.length];
        for (int i =0;i<fsBytes.length;i++){
            fsBytes[i] = f2plus(fkBytes[i] , sBytes[i]);
        }

        byte[] RBytes = new byte[cipherBytes2.length];

        for (int i =0;i<cipherBytes2.length;i++){
            RBytes[i] = f2minus(cipherBytes2[i] , fsBytes[i]);
        }

        String R = new String(RBytes, Charset.forName("ISO-8859-1"));

        String X = L + R;
        return X;
    }

    public void setLookup(File lookup) {
        try {
            File toRead = new File(tmpFolder + ".lookup");
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

    public File getLookup() {
        File lookupFile = new File(tmpFolder + ".lookup");
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

        File encrypted = new File(tmpFolder + "encrypted.txt");
        try {

            Scanner fileReader = new Scanner(clear);
            fileReader.hasNextLine();
            //System.out.println(fileReader.hasNextLine());
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
        String R = word.substring(m);
        int k = L.hashCode();

        String s = randomStringGenerator.nextString();

        Random random = new Random(k);
        RandomString fkGenerator = new RandomString(blockSize-m,random);
        String fk = fkGenerator.nextString();

        byte[] clearBytes1 = L.getBytes(Charset.forName("ISO-8859-1"));
        byte[] clearBytes2 = R.getBytes(Charset.forName("ISO-8859-1"));

        byte[] sBytes = s.getBytes(Charset.forName("ISO-8859-1"));
        byte[] fkBytes = fk.getBytes(Charset.forName("ISO-8859-1"));

        byte[] fsBytes = new byte[fkBytes.length];
        for (int i =0;i<fsBytes.length;i++){
            fsBytes[i] = f2plus(sBytes[i] , fkBytes[i]);
        }

        byte[] C1 = new byte[clearBytes1.length];
        byte[] C2 = new byte[clearBytes2.length];

        for (int i =0;i<clearBytes1.length;i++){
            C1[i] = f2plus(clearBytes1[i] , sBytes[i]);
        }
        for (int i =0;i<clearBytes2.length;i++){
            C2[i] = f2plus(clearBytes2[i] , fsBytes[i]);
        }

        String C1string = new String(C1, Charset.forName("ISO-8859-1"));
        String C2string = new String(C2, Charset.forName("ISO-8859-1"));

        String C = C1string + C2string;
        return C;
    }

    private String correctLength(String keyword) {
        while(keyword.length() < blockSize){
            keyword += filler;
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