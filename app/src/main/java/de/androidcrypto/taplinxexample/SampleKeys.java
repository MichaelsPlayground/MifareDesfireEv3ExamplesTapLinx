package de.androidcrypto.taplinxexample;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SampleKeys {
    /**
     * This class generates the key data for the application
     * There are key numbers from 0 to 4 for DES and AES and DEFAULT and CHANGED
     */

    private List<DesfireKey> keyList = new ArrayList<>();

    // private key constants
    private final byte[] MASTER_APPLICATION_KEY_DES_DEFAULT = Utils.hexStringToByteArray("0000000000000000");
    private final byte[] MASTER_APPLICATION_KEY_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] MASTER_APPLICATION_KEY_DES = Utils.hexStringToByteArray("DD00000000000000");
    private final byte[] MASTER_APPLICATION_KEY_AES = Utils.hexStringToByteArray("AA000000000000000000000000000000");
    private final byte MASTER_APPLICATION_KEY_NUMBER = (byte) 0x00;
    private final byte[] APPLICATION_ID_DES = Utils.hexStringToByteArray("A1A2A3");
    private final byte[] APPLICATION_KEY_MASTER_DES_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_MASTER_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000"); // default AES key with 16 nulls
    private final byte[] APPLICATION_KEY_MASTER_DES = Utils.hexStringToByteArray("D000000000000000");
    private final byte[] APPLICATION_KEY_MASTER_AES = Utils.hexStringToByteArray("A0000000000000000000000000000000");
    private final byte APPLICATION_KEY_MASTER_NUMBER = (byte) 0x00;
    private final byte APPLICATION_MASTER_KEY_SETTINGS = (byte) 0x0f; // amks
    private final byte KEY_NUMBER_RW = (byte) 0x01;
    private final byte[] APPLICATION_KEY_RW_DES_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_RW_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] APPLICATION_KEY_RW_DES = Utils.hexStringToByteArray("D100000000000000");
    private final byte[] APPLICATION_KEY_RW_AES = Utils.hexStringToByteArray("A1000000000000000000000000000000");
    private final byte APPLICATION_KEY_RW_NUMBER = (byte) 0x01;
    private final byte[] APPLICATION_KEY_CAR_DES_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_CAR_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] APPLICATION_KEY_CAR_DES = Utils.hexStringToByteArray("D200000000000000");
    private final byte[] APPLICATION_KEY_CAR_AES = Utils.hexStringToByteArray("A2000000000000000000000000000000");
    private final byte APPLICATION_KEY_CAR_NUMBER = (byte) 0x02;

    private final byte[] APPLICATION_KEY_R_DES_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_R_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] APPLICATION_KEY_R_DES = Utils.hexStringToByteArray("D300000000000000");
    private final byte[] APPLICATION_KEY_R_AES = Utils.hexStringToByteArray("A3000000000000000000000000000000");
    private final byte APPLICATION_KEY_R_NUMBER = (byte) 0x03;

    private final byte[] APPLICATION_KEY_W_DES_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_W_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] APPLICATION_KEY_W_DES = Utils.hexStringToByteArray("D400000000000000");
    private final byte[] APPLICATION_KEY_W_AES = Utils.hexStringToByteArray("A4000000000000000000000000000000");
    private final byte APPLICATION_KEY_W_NUMBER = (byte) 0x04;

    // Master Application Keys 0
    private final DesfireKey MAKDD = setDesKey("DES", MASTER_APPLICATION_KEY_NUMBER, "MASTER_APPLICATION_KEY_DES_DEFAULT", "master app", MASTER_APPLICATION_KEY_DES_DEFAULT, true, true);
    private final DesfireKey MAKDC = setDesKey("DES", MASTER_APPLICATION_KEY_NUMBER, "MASTER_APPLICATION_KEY_DES", "master app", MASTER_APPLICATION_KEY_DES, false, true);
    private final DesfireKey MAKAD = setAesKey("AES", MASTER_APPLICATION_KEY_NUMBER, "MASTER_APPLICATION_KEY_AES_DEFAULT", "master app", MASTER_APPLICATION_KEY_AES_DEFAULT, true, true);
    private final DesfireKey MAKAC = setAesKey("AES", MASTER_APPLICATION_KEY_NUMBER, "MASTER_APPLICATION_KEY_AES", "master app", MASTER_APPLICATION_KEY_AES, false, true);
    // Application Master Key 0
    private final DesfireKey AMK0DD = setDesKey("DES", APPLICATION_KEY_MASTER_NUMBER, "APPLICATION_KEY_MASTER_DES_DEFAULT", "app master", MASTER_APPLICATION_KEY_DES_DEFAULT, true, true);
    private final DesfireKey AMK0DC = setDesKey("DES", APPLICATION_KEY_MASTER_NUMBER, "APPLICATION_KEY_MASTER_DES", "app master", MASTER_APPLICATION_KEY_DES, false, true);
    private final DesfireKey AMK0AD = setAesKey("AES", APPLICATION_KEY_MASTER_NUMBER, "APPLICATION_KEY_MASTER_AES_DEFAULT", "app master", MASTER_APPLICATION_KEY_AES_DEFAULT, true, true);
    private final DesfireKey AMK0AC = setAesKey("AES", APPLICATION_KEY_MASTER_NUMBER, "APPLICATION_KEY_MASTER_AES", "app master", MASTER_APPLICATION_KEY_AES, false, true);


    private DesfireKey setDesKey(String keyType, int keyNumber, String keyName, String keyNameShort, byte[] desKey, boolean isDefaultKey, boolean isMasterApplicationKey) {
        byte[] tdesKey = new byte[16];
        System.arraycopy(desKey, 0, tdesKey, 0, 8);
        System.arraycopy(desKey, 0, tdesKey, 8, 8);
        SecretKey tdesSecretKey = new SecretKeySpec(tdesKey, 0, tdesKey.length, "TDES");
        DesfireKey desfireKey = new DesfireKey(keyType, keyNumber, keyName, keyNameShort, desKey, tdesKey, null, isDefaultKey, isMasterApplicationKey, false, true, tdesSecretKey, null);
        keyList.add(desfireKey);
        return desfireKey;
    }

    private DesfireKey setAesKey(String keyType, int keyNumber, String keyName, String keyNameShort, byte[] aesKey, boolean isDefaultKey, boolean isMasterApplicationKey) {
        SecretKey aesSecretKey = new SecretKeySpec(aesKey, 0, aesKey.length, "AES");
        DesfireKey desfireKey = new DesfireKey(keyType, keyNumber, keyName, keyNameShort, null, null, aesKey, isDefaultKey, isMasterApplicationKey, true, false, null, aesSecretKey);
        keyList.add(desfireKey);
        return desfireKey;
    }


}
