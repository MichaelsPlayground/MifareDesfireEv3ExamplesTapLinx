package de.androidcrypto.taplinxexample;

import javax.crypto.SecretKey;

public class DesfireKey {

    /**
     * this class hold all data of a Mifare DESFire key
     */
    private static final String TAG = DesfireKey.class.getName();

    private String keyType = ""; // DES or AES
    private int keyNumber;
    private String keyName; // e.g. 1 read&write access key
    private String keyNameShort; // e.g. read&write
    private byte[] desKey; // length 8 bytes
    private byte[] tdesKey; // length 16 bytes
    private byte[] aesKey; // length 16 bytes
    private boolean isDefaultKey = false; // true when filled with nulls
    private boolean isMasterApplicationKey = false; // true when it is keyNumber 0 AND Master Application Key
    private boolean isAesKey = false;
    private boolean isDesKey = false;
    private SecretKey tdesSecretKey;
    private SecretKey aesSecretKey;

    public DesfireKey(String keyType, int keyNumber, String keyName, String keyNameShort, byte[] desKey, byte[] aesKey, boolean isDefaultKey, boolean isMasterApplicationKey, boolean isAesKey, boolean isDesKey) {
        this.keyType = keyType;
        this.keyNumber = keyNumber;
        this.keyName = keyName;
        this.keyNameShort = keyNameShort;
        this.desKey = desKey;
        this.aesKey = aesKey;
        this.isDefaultKey = isDefaultKey;
        this.isMasterApplicationKey = isMasterApplicationKey;
        this.isAesKey = isAesKey;
        this.isDesKey = isDesKey;
    }

    public DesfireKey(String keyType, int keyNumber, String keyName, String keyNameShort, byte[] desKey, byte[] tdesKey, byte[] aesKey, boolean isDefaultKey, boolean isMasterApplicationKey, boolean isAesKey, boolean isDesKey, SecretKey tdesSecretKey, SecretKey aesSecretKey) {
        this.keyType = keyType;
        this.keyNumber = keyNumber;
        this.keyName = keyName;
        this.keyNameShort = keyNameShort;
        this.desKey = desKey;
        this.tdesKey = tdesKey;
        this.aesKey = aesKey;
        this.isDefaultKey = isDefaultKey;
        this.isMasterApplicationKey = isMasterApplicationKey;
        this.isAesKey = isAesKey;
        this.isDesKey = isDesKey;
        this.tdesSecretKey = tdesSecretKey;
        this.aesSecretKey = aesSecretKey;
    }
}
