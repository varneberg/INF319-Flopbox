package encryption;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;


public class ClientSSE {

    private static int blockSize = 24;
    private static int m = blockSize/2;
    private String key;
    private HashMap<String, String> lookup;
    private static char filler = '*';
    private static String tmpFolder = "./src/main/resources/tmp/";
    private GFG gfg;

    /*
    initializes the sse with a secret key
     */
    public ClientSSE(String key){
        this.key = key;
        gfg = new GFG(blockSize, key.hashCode());
    }

    /*
    generates a search token to be sent to the server. includes encrypted search word and key k
    input: keyword to be encrypted and made into a search token
    returns the token as a string
     */
    public String generateSearchToken(String keyword){
        keyword = correctLength(keyword);

        keyword = gfg.permute(false, keyword);
        String L = keyword.substring(0,blockSize-m);
        int k = L.hashCode();


        String token = keyword + k;
        return token;
    }

    /*
    adds the values of 2 bytes together to an int, and converts back to a byte
    works like the modulo operation
     */
    private byte f2plus(byte a, byte b){
        Integer c = a + b;
        return c.byteValue();
    }

    /*
    subtracts the values of 2 bytes from each other to an int, and converts back to a byte
    works like the modulo operation
     */
    private byte f2minus(byte a, byte b){
        Integer c = a - b;
        return c.byteValue();
    }

    /*
    decrypts a file encrypted by the same user, uses the lookup table
    input: encrypted = file to be decrypted
    returns decrypted file
     */
    public File decryptFile(File encrypted){
        String hashed = Integer.toString(encrypted.hashCode());
        if (!lookup.containsKey(hashed)){
            System.out.println("key missing in lookup");
            return encrypted;
        }
        String seed = lookup.get(hashed);
        Random random = new Random(seed.hashCode());
        RandomString randomStringGenerator = new RandomString(m,random);

        File decrypted = new File(tmpFolder + encrypted.getName());

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

    /*
    decrypts block,used during decrypting
    input: word= block to be decrypted, randomStringGenerator = generator which generates s
     */
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

        String W = gfg.permute(true, X);

        return W;
    }

    /*
    updates the lookup table with the given lookup file
    input: lookup = lookup file
     */
    public void setLookup(File lookup) {
        try {
            File toFile = new File(tmpFolder + ".lookupDecrypted");
            FileEncryptor.decryptFile(lookup, toFile,  key);
            File toRead = toFile;

            FileInputStream fis = new FileInputStream(toRead);
            ObjectInputStream ois = new ObjectInputStream(fis);

            HashMap<String,String> mapInFile=(HashMap<String,String>)ois.readObject();

            ois.close();
            fis.close();

            this.lookup = mapInFile;

            toFile.delete();
            lookup.delete();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
    returns the lookup table as a file
     */
    public File getLookup() {
        File lookupFile = new File(tmpFolder + ".lookupClear");

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
        File toFile = null;
        try {
            toFile = new File(tmpFolder+".lookup");
            FileEncryptor.encryptFile(lookupFile, toFile, key);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        lookupFile.delete();

        return toFile;
    }

    /*
    encrypts a file with the sse algorithm.
    input: clear = file to be encrypted
    returns the encrypted file
     */
    public File encryptFile(File clear) {
        if(lookup == null){
            lookup = new HashMap<String,String>();
        }
        int numberOfFiles = lookup.size();
        String seed = key + numberOfFiles;

        Random random = new Random(seed.hashCode());
        RandomString randomStringGenerator = new RandomString(m,random);

        File encrypted = new File(tmpFolder + clear.getName());
        try {

            Scanner fileReader = new Scanner(clear);
            fileReader.hasNextLine();
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

    /*
    encrypts a block, used during encryption of file.
    input: word = block to be encrypted, randomStringGenerator = generator that produces s
     */
    private String encryptWord(String word, RandomString randomStringGenerator) {
        word = correctLength(word);

        word = gfg.permute(false, word);

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

    /*
    converts a word to the length of the block size. uses * as filler characters
    input: keyword = string to convert
    returns string of correct length
     */
    private String correctLength(String keyword) {
        while(keyword.length() < blockSize){
            keyword += filler;
        }
        while (keyword.length() > blockSize){
            keyword = keyword.substring(0, keyword.length() - 1);
        }

        return keyword;
    }

    /*
    class to generate random strings, used to generate s
     */
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

    /*
    class to encrypt a file with a password
     */
    public static class FileEncryptor {

        //Arbitrarily selected 8-byte salt sequence:
        private static final byte[] salt = {
                (byte) 0x43, (byte) 0x76, (byte) 0x95, (byte) 0xc7,
                (byte) 0x5b, (byte) 0xd7, (byte) 0x45, (byte) 0x17
        };

        private static Cipher makeCipher(String pass, Boolean decryptMode) throws GeneralSecurityException{

            //Use a KeyFactory to derive the corresponding key from the passphrase:
            PBEKeySpec keySpec = new PBEKeySpec(pass.toCharArray());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            //Create parameters from the salt and an arbitrary number of iterations:
            PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 42);

            /*Dump the key to a file for testing: */
            FileEncryptor.keyToFile(key);

            //Set up the cipher:
            Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");

            //Set the cipher mode to decryption or encryption:
            if(decryptMode){
                cipher.init(Cipher.ENCRYPT_MODE, key, pbeParamSpec);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, key, pbeParamSpec);
            }

            return cipher;
        }


        /**Encrypts one file to a second file using a key derived from a passphrase:**/
        public static void encryptFile(File fromFile, File toFile, String pass)
                throws IOException, GeneralSecurityException{
            byte[] decData;
            byte[] encData;
            File inFile = fromFile;
            //Generate the cipher using pass:
            Cipher cipher = FileEncryptor.makeCipher(pass, true);

            //Read in the file:
            FileInputStream inStream = new FileInputStream(inFile);

            int blockSize = 8;
            //Figure out how many bytes are padded
            int paddedCount = blockSize - ((int)inFile.length()  % blockSize );

            //Figure out full size including padding
            int padded = (int)inFile.length() + paddedCount;

            decData = new byte[padded];


            inStream.read(decData);

            inStream.close();

            //Write out padding bytes as per PKCS5 algorithm
            for( int i = (int)inFile.length(); i < padded; ++i ) {
                decData[i] = (byte)paddedCount;
            }

            //Encrypt the file data:
            encData = cipher.doFinal(decData);


            //Write the encrypted data to a new file:
            FileOutputStream outStream = new FileOutputStream(toFile);
            outStream.write(encData);
            outStream.close();
        }


        /**Decrypts one file to a second file using a key derived from a passphrase:**/
        public static void decryptFile(File fromFile, File toFile, String pass)
                throws GeneralSecurityException, IOException{
            byte[] encData;
            byte[] decData;
            File inFile = fromFile;

            //Generate the cipher using pass:
            Cipher cipher = FileEncryptor.makeCipher(pass, false);

            //Read in the file:
            FileInputStream inStream = new FileInputStream(inFile );
            encData = new byte[(int)inFile.length()];
            inStream.read(encData);
            inStream.close();
            //Decrypt the file data:
            decData = cipher.doFinal(encData);

            //Figure out how much padding to remove

            int padCount = (int)decData[decData.length - 1];

            //Naive check, will fail if plaintext file actually contained
            //this at the end
            //For robust check, check that padCount bytes at the end have same value
            if( padCount >= 1 && padCount <= 8 ) {
                decData = Arrays.copyOfRange( decData , 0, decData.length - padCount);
            }

            //Write the decrypted data to a new file:



            FileOutputStream target = new FileOutputStream(toFile);
            target.write(decData);
            target.close();
        }

        /**Record the key to a text file for testing:**/
        private static void keyToFile(SecretKey key){
            try {
                File keyFile = new File("C:\\keyfile.txt");
                FileWriter keyStream = new FileWriter(keyFile);
                String encodedKey = "\n" + "Encoded version of key:  " + key.getEncoded().toString();
                keyStream.write(key.toString());
                keyStream.write(encodedKey);
                keyStream.close();
            } catch (IOException e) {
                System.err.println("Failure writing key to file");
                e.printStackTrace();
            }

        }
    }
    

}

/*
class used to permute initial block before encrypting
 */
class GFG {
    ArrayList<Integer> p;
    ArrayList<Integer> ip;
    Random rand;

    /*
    creates a gfg object with given length and a secret key
     */
    public GFG(int length, int key) {
        rand = new Random(key);
        p = generateRandom(length);
        ip = inversePermutation(p);
    }

    /*
    permutes a string with the key provided during initialization
    input: inverse = tru or false depending on if the function should permute or return the string to the original form.
    inn = the string to be permuted.
    returns string after permutation
     */
    public String permute(boolean inverse, String inn) {
        char[] chars = inn.toCharArray();

        char[] newChar = new char[chars.length];

        for (int i = 0; i <= chars.length - 1; i++) {
            if (inverse) {
                newChar[ip.get(i)] = chars[i];
            } else {
                newChar[p.get(i)] = chars[i];
            }
        }
        return new String(newChar);
    }

    /*
    gets the next number during permutation
     */
    private int getNum(ArrayList<Integer> v) {
        // Size of the vector
        int n = v.size();

        // Make sure the number is within
        // the index range
        int index = rand.nextInt(n);

        // Get random number from the vector
        int num = v.get(index);

        // Remove the number from the vector
        v.set(index, v.get(n - 1));
        v.remove(n - 1);

        // Return the removed number
        return num;
    }

    /*
    generates a random string on length n
     */
    private ArrayList<Integer> generateRandom(int n) {
        ArrayList<Integer> v = new ArrayList<>(n);

        // Fill the vector with the values
        // 1, 2, 3, ..., n
        for (int i = 0; i < n; i++)
            v.add(i + 1);

        // While vector has elements
        // get a random number from the vector and print it
        ArrayList<Integer> p = new ArrayList<>();
        while (v.size() > 0) {
            p.add(getNum(v) - 1);
        }

        return p;
        // While vector has elements
        // get a random number from the vector and print it

    }

    // function to find inverse permutations
    private ArrayList<Integer> inversePermutation(ArrayList<Integer> arr) {

        // to store element to index mappings
        //ArrayList<Integer> arr2 = new ArrayList<>(arr.size());
        int arr2[] = new int[arr.size()];

        // Inserting position at their
        // respective element in second array
        for (int i = 0; i < arr.size(); i++)
            arr2[arr.get(i)] = i;

        ArrayList<Integer> arr3 = new ArrayList<>();
        for (int i : arr2) {
            arr3.add(i);
        }

        return arr3;
    }
}

