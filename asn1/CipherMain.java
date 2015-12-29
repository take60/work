import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public class CipherMain {

    public static void main(String[] args) throws Exception {
        String text = "stratasingapore";
        byte[] plainBytes = text.getBytes("UTF-8");
        System.out.println("Plain");
        for (byte b : plainBytes) {
            System.out.printf("%02x ", b);
        }
        System.out.println("\n");

        // 暗号化処理
        byte[] encryptedBytes = Sub.encrypt(plainBytes);
        System.out.println("Encrypted(16バイトの倍数にパディング)");
        for (byte b : encryptedBytes) {
            System.out.printf("%02x ", b);
        }
        System.out.println("\n");

        // 復号処理
        byte[] decryptedBytes = Sub.decrypt(encryptedBytes);
        System.out.println("Decrypted");
        for (byte b : decryptedBytes) {
            System.out.printf("%02x ", b);
        }
	System.out.println("\n");
    }
}

class Sub {
    // 鍵データ
    private static final String KEY = "1234567890123456";
    // 初期化ベクトル
    private static final String IV = "abcdefghijklmnop";
    // 暗号化アルゴリズム
    private static final String ALGORITHM = "AES";
    // 暗号化アルゴリズム/暗号化モード/パディング方式
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /*
     * sha-256 hash
     */
    public static byte[] sha256hash(String key) throws Exception {
    	MessageDigest md = MessageDigest.getInstance("SHA-256");
	md.update(key.getBytes());
	byte[] hashbytes = md.digest();
	return hashbytes;
    }
    /**
     * 暗号化
     * @param bytes 暗号化するバイト配列
     * @return 暗号化したバイト配列
     * @throws Exception
     */
    public static byte[] encrypt(byte[] bytes) throws Exception {
        // 鍵データと初期化ベクトルを生成する
        //byte[] byteKey = KEY.getBytes("UTF-8");
        byte[] byteKey = sha256hash(KEY);
        byte[] byteIv = IV.getBytes("UTF-8");
        SecretKeySpec secretKeySpec = new SecretKeySpec(byteKey, ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(byteIv);

        // 暗号化を行う
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        //cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
	cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedBytes = cipher.doFinal(bytes);
        return encryptedBytes;
    }

    /**
     * 復号
     * @param bytes 復号するバイト配列
     * @return 復号したバイト配列
     * @throws Exception
     */
    public static byte[] decrypt(byte[] bytes) throws Exception {
        // 鍵データと初期化ベクトルを生成する
        //byte[] byteKey = KEY.getBytes("UTF-8");
        byte[] byteKey = sha256hash(KEY);
        byte[] byteIv = IV.getBytes("UTF-8");
        SecretKeySpec secretKeySpec = new SecretKeySpec(byteKey, ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(byteIv);

        // 復号を行う
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        //cipher.init(Cipher.DECRYPT_MODE, secretKeySpec,ivParameterSpec);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] byteResult = cipher.doFinal(bytes);
        return byteResult;
    }
}
