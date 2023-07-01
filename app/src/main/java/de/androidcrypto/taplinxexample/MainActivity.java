package de.androidcrypto.taplinxexample;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.nxp.nfclib.CardType;
import com.nxp.nfclib.KeyType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.defaultimpl.KeyData;
import com.nxp.nfclib.desfire.DESFireEV3File;
import com.nxp.nfclib.desfire.DESFireFactory;
import com.nxp.nfclib.desfire.EV1KeySettings;
import com.nxp.nfclib.desfire.EV1PICCConfigurationSettings;
import com.nxp.nfclib.desfire.EV2PICCConfigurationSettings;
import com.nxp.nfclib.desfire.EV3ApplicationKeySettings;
import com.nxp.nfclib.desfire.EV3PICCConfigurationSettings;
import com.nxp.nfclib.desfire.IDESFireEV1;
import com.nxp.nfclib.desfire.IDESFireEV2;
import com.nxp.nfclib.desfire.IDESFireEV3;
import com.nxp.nfclib.exceptions.InvalidResponseLengthException;
import com.nxp.nfclib.exceptions.NxpNfcLibException;
import com.nxp.nfclib.exceptions.PICCException;
import com.nxp.nfclib.exceptions.SecurityException;
import com.nxp.nfclib.exceptions.UsageException;
import com.nxp.nfclib.interfaces.IKeyData;
import com.nxp.nfclib.utils.NxpLogUtils;
import com.nxp.nfclib.utils.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private static final String TAG = MainActivity.class.getName();

    /**
     * Package Key.
     */
    static String packageKey = "07e7a6e1091d445f60ce756883b42ef2";
    // Persisting first open: 1688060183131
    // Persisting first open: 1688067416793
    /**
     * NxpNfclib instance.
     */
    private NxpNfcLib libInstance = null;

    /**
     * Cipher instance.
     */
    private Cipher cipher = null;
    /**
     * Iv.
     */
    private IvParameterSpec iv = null;

    /**
     * Desfire card object.
     */

    private IDESFireEV1 desFireEV1;

    private IDESFireEV2 desFireEV2;

    private IDESFireEV3 desFireEV3;

    /**
     * section for sample keys
     */

    private IKeyData objKEY_2KTDES_ULC = null;
    private IKeyData objKEY_2KTDES = null;
    private IKeyData objKEY_AES128 = null;
    private byte[] default_ff_key = null;
    private IKeyData default_zeroes_key = null;

    private static final String ALIAS_KEY_AES128 = "key_aes_128";
    private static final String ALIAS_KEY_2KTDES = "key_2ktdes";
    private static final String ALIAS_KEY_2KTDES_ULC = "key_2ktdes_ulc";
    private static final String ALIAS_DEFAULT_FF = "alias_default_ff";
    private static final String ALIAS_KEY_AES128_ZEROES = "alias_default_00";
    private static final String EXTRA_KEYS_STORED_FLAG = "keys_stored_flag";

    private CardType mCardType = CardType.UnknownCard;

    private static final String KEY_APP_MASTER = "This is my key  ";
    /**
     * bytes key.
     */
    private byte[] bytesKey = new byte[16]; // 16 null bytes for AES

    private com.google.android.material.textfield.TextInputEditText output, errorCode;
    private com.google.android.material.textfield.TextInputLayout errorCodeLayout;

    /**
     * section for temporary actions
     */

    private Button setupCompleteApplication, standardWriteRead, standardWriteReadDefaultKeys;
    private Button getFileSettingsDesfire;

    /**
     * section for general workflow
     */

    private LinearLayout llGeneralWorkflow;
    private Button tagVersion, keySettings, freeMemory, formatPicc, selectMasterApplication;
    private Button formatPiccTaplinx, changeMasterKeyToAes;

    /**
     * section for application handling
     */
    private LinearLayout llApplicationHandling;
    private Button applicationList, applicationCreate, applicationSelect, applicationDelete;
    private com.google.android.material.textfield.TextInputEditText numberOfKeys, applicationId, applicationSelected;
    private byte[] selectedApplicationId = null;

    /**
     * section for files handling
     */

    private LinearLayout llFiles;

    private Button fileList, fileSelect, fileDelete;
    private com.google.android.material.textfield.TextInputEditText fileSelected;
    private String selectedFileId = "";
    private int selectedFileIdInt = -1;
    private int selectedFileSize;

    /**
     * section for standard & backup file handling
     */

    private LinearLayout llStandardFile;
    private Button fileStandardCreate, fileStandardWrite, fileStandardRead;
    private com.google.android.material.textfield.TextInputEditText fileSize, fileData;
    private RadioButton rbStandardFile, rbBackupFile;
    private com.shawnlin.numberpicker.NumberPicker npStandardFileId;
    RadioButton rbFileStandardPlainCommunication, rbFileStandardMacedCommunication, rbFileStandardEncryptedCommunication;
    private final int MAXIMUM_STANDARD_DATA_CHUNK = 40; // if any data are longer we create chunks when writing

    //private FileSettings selectedFileSettings;


    /**
     * section for value file handling
     */

    private LinearLayout llValueFile;
    private Button fileValueCreate, fileValueCredit, fileValueDebit, fileValueRead;
    RadioButton rbFileValuePlainCommunication, rbFileValueMacedCommunication, rbFileValueEncryptedCommunication;
    private com.shawnlin.numberpicker.NumberPicker npValueFileId;
    private com.google.android.material.textfield.TextInputEditText lowerLimitValue, upperLimitValue, initialValueValue, creditDebitValue;

    /**
     * section for record file handling
     */

    private LinearLayout llRecordFile;
    private Button fileRecordCreate, fileRecordWrite, fileRecordRead;
    private RadioButton rbLinearRecordFile, rbCyclicRecordFile;
    RadioButton rbFileRecordPlainCommunication, rbFileRecordMacedCommunication, rbFileRecordEncryptedCommunication;
    private com.shawnlin.numberpicker.NumberPicker npRecordFileId;
    private com.google.android.material.textfield.TextInputEditText fileRecordSize, fileRecordData, fileRecordNumberOfRecords;

    /**
     * work with encrypted standard files - EXPERIMENTAL
     */

    private LinearLayout llStandardFileEnc;
    private Button fileStandardCreateEnc, fileStandardWriteEnc, manualEncryption;

    /**
     * work with transaction mac files - only available on EV2+
     */

    private LinearLayout llTMacFile;
    private Button fileTMacCreate, fileTMacWrite, fileTMacRead;

    /**
     * section for authentication
     */

    private Button authDM0D, authD0D, authD1D, authD2D, authD3D, authD4D; // auth with default DES keys
    private Button authDM0A, authD0A, authD1A, authD2A, authD3A, authD4A; // auth with default AES keys
    private Button authDM0DC, authD0DC, authD1DC, authD2DC, authD3DC, authD4DC; // auth with changed DES keys
    private Button authDM0AC, authD0AC, authD1AC, authD2AC, authD3AC, authD4AC; // auth with changed AES keys


    /**
     * section for DES authentication
     */

    private Button authKeyDM0, authKeyD0, authKeyD1, authKeyD2, authKeyD3, authKeyD4; // M0 is the Master Application Key

    // changed keys
    private Button authKeyDM0C, authKeyD0C, authKeyD1C, authKeyD2C, authKeyD3C, authKeyD4C; // M0 is the Master Application Key

    /**
     * section for AES authentication
     */

    private Button authKeyAM0, authKeyA0, authKeyA1, authKeyA2, authKeyA3, authKeyA4; // M0 is the Master Application Key
    private Button authKeyAM0Ev2;


    /**
     * section for key handling
     */

    private Button changeKeyDM0, changeKeyD0, changeKeyD1, changeKeyD2, changeKeyD3, changeKeyD4;

    // virtual card key handling
    //private Button authKeyAM0; // M0 is the Master Application Key AES
    private Button changeKeyVc20, authKeyVc20, changeKeyVc21, authKeyVc21;
    private Button keyVersionKeyVc20, keyVersionKeyVc21;

    // changed keys
    private Button changeKeyDM0C, changeKeyD0C, changeKeyD1C, changeKeyD2C, changeKeyD3C, changeKeyD4C;

    // proximity check
    private Button proximityPrepare, proximityCheck, proximityVerify;
    // testing
    private Button createApplication1;


    // constants
    private final byte[] MASTER_APPLICATION_IDENTIFIER = new byte[3]; // '00 00 00'
    private final int MASTER_APPLICATION_IDENTIFIER_INT = 0;
    private final byte[] MASTER_APPLICATION_KEY_DEFAULT = Utils.hexStringToByteArray("0000000000000000");
    private final byte[] MASTER_APPLICATION_KEY_DES_DEFAULT = Utils.hexStringToByteArray("0000000000000000");
    private final byte[] MASTER_APPLICATION_KEY_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] MASTER_APPLICATION_KEY = Utils.hexStringToByteArray("DD00000000000000");
    private final byte MASTER_APPLICATION_KEY_NUMBER = (byte) 0x00;
    private final int MASTER_APPLICATION_KEY_NUMBER_INT = 0;
    private final byte MASTER_APPLICATION_KEY_VERSION = (byte) 0x00;
    private final byte[] APPLICATION_ID_DES = Utils.hexStringToByteArray("A1A2A3");
    private final byte[] DES_DEFAULT_KEY = new byte[8];
    private final byte[] DES_DEFAULT_KEY_TDES = new byte[16];
    private final byte[] DES_DEFAULT_KEY_2KTDES = new byte[24];

    private final byte[] APPLICATION_KEY_MASTER_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_MASTER = Utils.hexStringToByteArray("D000000000000000");
    private final byte APPLICATION_KEY_MASTER_NUMBER = (byte) 0x00;
    private final byte APPLICATION_MASTER_KEY_SETTINGS = (byte) 0x0f; // amks
    private final byte KEY_NUMBER_RW = (byte) 0x01;
    private final byte[] APPLICATION_KEY_RW_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_RW_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000"); // default AES key with 16 nulls
    private final byte[] APPLICATION_KEY_RW = Utils.hexStringToByteArray("D100000000000000");
    private final byte APPLICATION_KEY_RW_NUMBER = (byte) 0x01;
    private final byte[] APPLICATION_KEY_CAR_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_CAR = Utils.hexStringToByteArray("D200000000000000");
    private final byte APPLICATION_KEY_CAR_NUMBER = (byte) 0x02;

    private final byte[] APPLICATION_KEY_R_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_R = Utils.hexStringToByteArray("D300000000000000");
    private final byte APPLICATION_KEY_R_NUMBER = (byte) 0x03;

    private final byte[] APPLICATION_KEY_W_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    //private final byte[] APPLICATION_KEY_W = Utils.hexStringToByteArray("B400000000000000");
    private final byte[] APPLICATION_KEY_W = Utils.hexStringToByteArray("D400000000000000");
    private final byte APPLICATION_KEY_W_NUMBER = (byte) 0x04;

    private final byte[] VIRTUAL_CARD_CONFIG_KEY_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] VIRTUAL_CARD_KEY_CONFIG = Utils.hexStringToByteArray("20200000000000000000000000000000");
    private final int VIRTUAL_CARD_CONFIG_KEY_NUMBER_INT = 32; // 0x20
    private final byte VIRTUAL_CARD_CONFIG_KEY_VERSION = (byte) 0x20; // dec 32
    private final byte[] VIRTUAL_CARD_PROXIMITY_KEY_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] VIRTUAL_CARD_KEY_PROXIMITY = Utils.hexStringToByteArray("20200000000000000000000000000000");
    private final int VIRTUAL_CARD_PROXIMITY_KEY_NUMBER_INT = 33; // 0x21
    private final byte VIRTUAL_CARD_PROXIMITY_KEY_VERSION = (byte) 0x21; // dec 33

    private final byte[] APPLICATION_KEY_CAR_AES = Utils.hexStringToByteArray("A2000000000000000000000000000000");

    private final byte STANDARD_FILE_NUMBER = (byte) 0x01;


    int COLOR_GREEN = Color.rgb(0, 255, 0);
    int COLOR_RED = Color.rgb(255, 0, 0);

    // variables for NFC handling

    private NfcAdapter mNfcAdapter;
    private IsoDep isoDep;
    private byte[] tagIdByte;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        output = findViewById(R.id.etOutput);
        errorCode = findViewById(R.id.etErrorCode);
        errorCodeLayout = findViewById(R.id.etErrorCodeLayout);

        // temporary workflow
        setupCompleteApplication = findViewById(R.id.btnSetupCompleteApplication);
        standardWriteRead = findViewById(R.id.btnStandardFileWriteRead);
        //standardWriteReadDefaultKeys = findViewById(R.id.btnStandardFileWriteReadDefaultKeys);
        getFileSettingsDesfire = findViewById(R.id.btnGetFileSettings);


        // general workflow
        tagVersion = findViewById(R.id.btnGetTagVersion);
        keySettings = findViewById(R.id.btnGetKeySettings);
        freeMemory = findViewById(R.id.btnGetFreeMemory);
        formatPicc = findViewById(R.id.btnFormatPicc);
        selectMasterApplication = findViewById(R.id.btnSelectMasterApplication);
        formatPiccTaplinx = findViewById(R.id.btnFormatPiccTaplinx);
        changeMasterKeyToAes = findViewById(R.id.btnChangeMasterKeyToAes);

        // application handling
        llApplicationHandling = findViewById(R.id.llApplications);
        applicationList = findViewById(R.id.btnListApplications);
        applicationCreate = findViewById(R.id.btnCreateApplication);
        applicationSelect = findViewById(R.id.btnSelectApplication);
        applicationDelete = findViewById(R.id.btnDeleteApplication);
        applicationSelected = findViewById(R.id.etSelectedApplicationId);
        numberOfKeys = findViewById(R.id.etNumberOfKeys);
        applicationId = findViewById(R.id.etApplicationId);

        // files handling
        fileList = findViewById(R.id.btnListFiles);
        fileSelect = findViewById(R.id.btnSelectFile);
        fileDelete = findViewById(R.id.btnDeleteFile);

        // standard & backup file handling
        llStandardFile = findViewById(R.id.llStandardFile);
        fileStandardCreate = findViewById(R.id.btnCreateStandardFile);
        fileStandardWrite = findViewById(R.id.btnWriteStandardFile);
        fileStandardRead = findViewById(R.id.btnReadStandardFile);
        npStandardFileId = findViewById(R.id.npStandardFileId);
        rbStandardFile = findViewById(R.id.rbStandardFile);
        rbBackupFile = findViewById(R.id.rbBackupFile);
        rbFileStandardPlainCommunication = findViewById(R.id.rbFileStandardPlainCommunication);
        rbFileStandardMacedCommunication = findViewById(R.id.rbFileStandardMacedCommunication);
        rbFileStandardEncryptedCommunication = findViewById(R.id.rbFileStandardEncryptedCommunication);
        fileSize = findViewById(R.id.etFileStandardSize);
        fileData = findViewById(R.id.etFileStandardData);
        fileSelected = findViewById(R.id.etSelectedFileId);

        // value file handling
        llValueFile = findViewById(R.id.llValueFile);
        fileValueCreate = findViewById(R.id.btnCreateValueFile);
        fileValueRead = findViewById(R.id.btnReadValueFile);
        fileValueCredit = findViewById(R.id.btnCreditValueFile);
        fileValueDebit = findViewById(R.id.btnDebitValueFile);
        npValueFileId = findViewById(R.id.npValueFileId);
        rbFileValuePlainCommunication = findViewById(R.id.rbFileValuePlainCommunication);
        rbFileValueMacedCommunication = findViewById(R.id.rbFileValueMacedCommunication);
        rbFileValueEncryptedCommunication = findViewById(R.id.rbFileValueEncryptedCommunication);
        lowerLimitValue = findViewById(R.id.etValueLowerLimit);
        upperLimitValue = findViewById(R.id.etValueUpperLimit);
        initialValueValue = findViewById(R.id.etValueInitialValue);
        creditDebitValue = findViewById(R.id.etValueCreditDebitValue);

        // record file handling
        llRecordFile = findViewById(R.id.llRecordFile);
        fileRecordCreate = findViewById(R.id.btnCreateRecordFile);
        fileRecordRead = findViewById(R.id.btnReadRecordFile);
        fileRecordWrite = findViewById(R.id.btnWriteRecordFile);
        npRecordFileId = findViewById(R.id.npRecordFileId);
        rbFileRecordPlainCommunication = findViewById(R.id.rbFileRecordPlainCommunication);
        rbFileRecordMacedCommunication = findViewById(R.id.rbFileRecordMacedCommunication);
        rbFileRecordEncryptedCommunication = findViewById(R.id.rbFileRecordEncryptedCommunication);
        fileRecordSize = findViewById(R.id.etRecordFileSize);
        fileRecordNumberOfRecords = findViewById(R.id.etRecordFileNumberRecords);
        fileRecordData = findViewById(R.id.etRecordFileData);
        rbLinearRecordFile = findViewById(R.id.rbLinearRecordFile);
        rbCyclicRecordFile = findViewById(R.id.rbCyclicRecordFile);

        // transaction mac file handling
        llTMacFile = findViewById(R.id.llTransactionMacFile);
        fileTMacCreate = findViewById(R.id.btnCreateTransactionMacFile);
        fileTMacWrite = findViewById(R.id.btnWriteTransactionMacFile);
        fileTMacRead = findViewById(R.id.btnReadTransactionMacFile);

        // encrypted standard file handling
        llStandardFileEnc = findViewById(R.id.llStandardFileEnc);
        fileStandardCreateEnc = findViewById(R.id.btnCreateStandardFileEnc);
        fileStandardWriteEnc = findViewById(R.id.btnWriteStandardFileEnc);
        manualEncryption = findViewById(R.id.btnManualEnc);

        // authentication handling DES default keys
        authDM0D = findViewById(R.id.btnAuthDM0D);
        authD0D = findViewById(R.id.btnAuthD0D);
        authD1D = findViewById(R.id.btnAuthD1D);
        authD2D = findViewById(R.id.btnAuthD2D);
        authD3D = findViewById(R.id.btnAuthD3D);
        authD4D = findViewById(R.id.btnAuthD4D);

        // authentication handling AES default keys
        authDM0A = findViewById(R.id.btnAuthDM0A);
        authD0A = findViewById(R.id.btnAuthD0A);
        authD1A = findViewById(R.id.btnAuthD1A);
        authD2A = findViewById(R.id.btnAuthD2A);
        authD3A = findViewById(R.id.btnAuthD3A);
        authD4A = findViewById(R.id.btnAuthD4A);

        // authentication handling DES changed keys
        authDM0DC = findViewById(R.id.btnAuthDM0DC);
        authD0DC = findViewById(R.id.btnAuthD0DC);
        authD1DC = findViewById(R.id.btnAuthD1DC);
        authD2DC = findViewById(R.id.btnAuthD2DC);
        authD3DC = findViewById(R.id.btnAuthD3DC);
        authD4DC = findViewById(R.id.btnAuthD4DC);

        // authentication handling AES changed keys
        authDM0AC = findViewById(R.id.btnAuthDM0AC);
        authD0AC = findViewById(R.id.btnAuthD0AC);
        authD1AC = findViewById(R.id.btnAuthD1AC);
        authD2AC = findViewById(R.id.btnAuthD2AC);
        authD3AC = findViewById(R.id.btnAuthD3AC);
        authD4AC = findViewById(R.id.btnAuthD4AC);

        // OLD menu
        // authentication handling
        authKeyDM0 = findViewById(R.id.btnAuthDM0);
        authKeyD0 = findViewById(R.id.btnAuthD0);
        authKeyD1 = findViewById(R.id.btnAuthD1);
        authKeyD2 = findViewById(R.id.btnAuthD2);
        authKeyD3 = findViewById(R.id.btnAuthD3);
        authKeyD4 = findViewById(R.id.btnAuthD4);
        // now with changed keys
        authKeyDM0C = findViewById(R.id.btnAuthDM0C);
        authKeyD0C = findViewById(R.id.btnAuthD0C);
        authKeyD1C = findViewById(R.id.btnAuthD1C);
        authKeyD2C = findViewById(R.id.btnAuthD2C);
        authKeyD3C = findViewById(R.id.btnAuthD3C);
        authKeyD4C = findViewById(R.id.btnAuthD4C);

        // AES authentication handling
        authKeyAM0 = findViewById(R.id.btnAuthAM0);
        authKeyA0 = findViewById(R.id.btnAuthA0);
        authKeyA1 = findViewById(R.id.btnAuthA1);
        authKeyA2 = findViewById(R.id.btnAuthA2);
        authKeyA3 = findViewById(R.id.btnAuthA3);
        authKeyA4 = findViewById(R.id.btnAuthA4);
        authKeyAM0Ev2 = findViewById(R.id.btnAuthAM0Ev2);


        // key handling
        changeKeyDM0 = findViewById(R.id.btnChangeKeyDM0);
        changeKeyD0 = findViewById(R.id.btnChangeKeyD0);
        changeKeyD1 = findViewById(R.id.btnChangeKeyD1);
        changeKeyD2 = findViewById(R.id.btnChangeKeyD2);
        changeKeyD3 = findViewById(R.id.btnChangeKeyD3);
        changeKeyD4 = findViewById(R.id.btnChangeKeyD4);

        // virtual card key handling
        changeKeyVc20 = findViewById(R.id.btnChangeKeyA20);
        authKeyAM0 = findViewById(R.id.btnAuthAM0);
        authKeyVc20 = findViewById(R.id.btnAuthA20);
        keyVersionKeyVc20 = findViewById(R.id.btnGetKeyVersionKeyA20);
        changeKeyVc21 = findViewById(R.id.btnChangeKeyA21);
        authKeyVc21 = findViewById(R.id.btnAuthA21);
        keyVersionKeyVc21 = findViewById(R.id.btnGetKeyVersionKeyA21);

        // now with changed keys
        changeKeyDM0C = findViewById(R.id.btnChangeKeyDM0C);
        changeKeyD0C = findViewById(R.id.btnChangeKeyD0C);
        changeKeyD1C = findViewById(R.id.btnChangeKeyD1C);
        changeKeyD2C = findViewById(R.id.btnChangeKeyD2C);
        changeKeyD3C = findViewById(R.id.btnChangeKeyD3C);
        changeKeyD4C = findViewById(R.id.btnChangeKeyD4C);

        // proximity check
        proximityPrepare = findViewById(R.id.btnProximityPrepare);
        proximityCheck = findViewById(R.id.btnProximityCheck);
        proximityVerify = findViewById(R.id.btnProximityVerify);

        // just for check
        createApplication1 = findViewById(R.id.btnCreateApplication1); // will create an application with number 1

        /* Initialize the library and register to this activity */
        initializeLibrary();

        initializeKeys();

        /* Initialize the Cipher and init vector of 16 bytes with 0xCD */
        initializeCipherinitVector();


        //allLayoutsInvisible(); // default

        // hide soft keyboard from showing up on startup
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);


        applicationCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new application
                clearOutputFields();
                String logString = "create a new application with AES keys";
                writeToUiAppend(output, logString);
                int numberOfKeysInt = Integer.parseInt(numberOfKeys.getText().toString());
                byte[] applicationIdentifier = Utils.hexStringToByteArray(applicationId.getText().toString());
                if ((applicationIdentifier == null) || (applicationIdentifier.length != 3)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong application ID", COLOR_RED);
                    return;
                }
                if ((numberOfKeysInt < 1) || (numberOfKeysInt > 14)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong number of keys (range 1..14)", COLOR_RED);
                    return;
                }
                boolean success = createApplicationEv3(logString, applicationIdentifier, numberOfKeysInt, KeyType.AES128);
                if (success) {
                    writeToUiAppend(output, "SUCCESS for " + printData("new appID", applicationIdentifier) + " with " + numberOfKeysInt + " AES keys");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                } else {
                    writeToUiAppend(output, logString + " not possible for " + printData("new appID", applicationIdentifier) + " with " + numberOfKeysInt + " AES keys");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " FAILURE", COLOR_RED);
                }
            }
        });

        /* old one
        applicationCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new application
                clearOutputFields();
                writeToUiAppend(output, "create a new application");
                int numberOfKeysInt = Integer.parseInt(numberOfKeys.getText().toString());
                byte[] applicationIdentifier = Utils.hexStringToByteArray(applicationId.getText().toString());
                if (applicationIdentifier == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong application ID", COLOR_RED);
                    return;
                }
                if ((numberOfKeysInt < 0) || (numberOfKeysInt > 14)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong number of keys (range 1..14)", COLOR_RED);
                    return;
                }
                //int appIdInt = Utilities.bytesToInt(applicationIdentifier);
                //writeToUiAppend(output, "appId as Integer: " + appIdInt);
                EV3ApplicationKeySettings applicationKeySettings = getApplicationSettingsDefault(numberOfKeysInt);
                try {
                    // important: first select the  Master Application ('0')
                    desFireEV3.selectApplication(0);
                    // depending on MasterKey settings an authentication is necessary, skipped here
                    desFireEV3.createApplication(applicationIdentifier, applicationKeySettings);
                    writeToUiAppend(output, "create a new application done," + printData("new appID", applicationIdentifier));
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "change MasterApplicationKey success", COLOR_GREEN);
                } catch (InvalidResponseLengthException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                } catch (UsageException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "UsageException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                } catch (
                        SecurityException e) { // don't use the java Security Exception but the NXP one
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "SecurityException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                } catch (PICCException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "PICCException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                } catch (Exception e) {
                    writeToUiAppend(output, "Exception occurred... check LogCat");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                }
            }
        });
        */


        applicationSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get all applications and show them in a listview for selection
                clearOutputFields();
                writeToUiAppend(output, "select an application");
                String[] applicationList;

                try {
                    // first select Master Application
                    desFireEV3.selectApplication(0); // todo run this before get applications
                    int[] desfireApplicationIdIntArray = desFireEV3.getApplicationIDs();
                    applicationList = new String[desfireApplicationIdIntArray.length];
                    for (int i = 0; i < desfireApplicationIdIntArray.length; i++) {
                        applicationList[i] = Utilities.byteToHexString(Utilities.intToBytes(desfireApplicationIdIntArray[i], 3));
                    }
                } catch (InvalidResponseLengthException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (UsageException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "UsageException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (
                        SecurityException e) { // don't use the java Security Exception but the NXP one
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "SecurityException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (PICCException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "PICCException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    writeToUiAppend(output, "Exception occurred... check LogCat");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                }

                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Choose an application");

                // add a list
                //String[] animals = {"horse", "cow", "camel", "sheep", "goat"};
                //builder.setItems(animals, new DialogInterface.OnClickListener() {
                builder.setItems(applicationList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        writeToUiAppend(output, "you selected nr " + which + " = " + applicationList[which]);
                        boolean dfSelectApplication = false;
                        try {
                            byte[] aid = Utilities.stringToBytes(applicationList[which]);
                            //byte[] aid = Utils.hexStringToByteArray(applicationList[which]);
                            int aidInt = Utilities.bytesToInt(aid);
                            desFireEV3.selectApplication(aidInt);
                            selectedApplicationId = aid.clone();
                            applicationSelected.setText(applicationList[which]);
                            selectedFileId = "";
                            fileSelected.setText("");
                            writeToUiAppend(output, "selectApplication SUCCESS, selected AID: " + applicationList[which]);
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "selectApplication SUCCESS", COLOR_GREEN);
                        } catch (InvalidResponseLengthException e) {
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
                            e.printStackTrace();
                            return;
                        } catch (UsageException e) {
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "UsageException occurred\n" + e.getMessage(), COLOR_RED);
                            e.printStackTrace();
                            return;
                        } catch (
                                SecurityException e) { // don't use the java Security Exception but the NXP one
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "SecurityException occurred\n" + e.getMessage(), COLOR_RED);
                            e.printStackTrace();
                            return;
                        } catch (PICCException e) {
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "PICCException occurred\n" + e.getMessage(), COLOR_RED);
                            e.printStackTrace();
                            return;
                        } catch (Exception e) {
                            writeToUiAppend(output, "Exception occurred... check LogCat");
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException occurred\n" + e.getMessage(), COLOR_RED);
                            e.printStackTrace();
                            return;
                        }
                    }
                });
                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        /**
         * section for transaction MAC files
         * Note: this  is available on DESFire EV2+ cards and using AES keys in application
         */

        fileTMacCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new transaction mac file
                // get the input and sanity checks
                clearOutputFields();
                writeToUiAppend(output, "create a Transaction MAC file");
                byte fileIdByte = (byte) (npRecordFileId.getValue() & 0xFF);
                int fileIdInt = npRecordFileId.getValue();
                // the number of files on an EV1 tag is limited to 32 (00..31), but we are using the limit for the old D40 tag with a maximum of 15 files (00..14)
                // this limit is hardcoded in the XML file for the fileId numberPicker

                if (fileIdByte > (byte) 0x0f) {
                    // this should not happen as the limit is hardcoded in npFileId
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file ID", COLOR_RED);
                    return;
                }
                try {
                    DESFireEV3File.EV3TransactionMacFileSettings ev3TransactionMacFileSettings = new DESFireEV3File.EV3TransactionMacFileSettings(
                            IDESFireEV1.CommunicationType.Plain, // plain communication
                            (byte) 0x03, (byte) 0x0F, (byte) 0x01, (byte) 0x02, // access keys for read, write (never), read&write, CAR
                            (byte) 0x02, // keyOption 02 = AES
                            (byte) 0x00 // key version
                    );
                    desFireEV3.createFile(fileIdInt, ev3TransactionMacFileSettings);
                    writeToUiAppend(output, "Transaction MAC file created with fileId: " + fileIdInt);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "create Transaction MAC file success", COLOR_GREEN);

                    // Transaction Timer
                    // enablement is done using the SetConfiguration command with option (byte) 0x55
                    EV1PICCConfigurationSettings confByte = null;

                    //SetConfiguration 0x5C Yes Yes Yes but differences in PICC

                    //byte confByte = (byte) 0x55;
                    //desFireEV3.setConfigurationByte(confByte);

                    return;
/*
Command sent to card : 45
Response received : 000F85F2E7A33CFB4A97F4
Command sent to card : CE0000123F0230F82C20E85AC62404FD405B057D7512
Response received : 7E
 */
                } catch (InvalidResponseLengthException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (UsageException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "UsageException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (
                        SecurityException e) { // don't use the java Security Exception but the NXP one
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "SecurityException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (PICCException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "PICCException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    writeToUiAppend(output, "Exception occurred... check LogCat");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                }

                //desFireEV3.commitAndGetTransactionMac();
            }
        });


        authKeyAM0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Master Application Key
                clearOutputFields();
                writeToUiAppend(output, "legacy authentication with AES Master Application Key");
                boolean success = legacyAesAuth(MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, "legacy authentication with MASTER_APPLICATION_KEY_AES_DEFAULT SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "legacy auth with MasterApplicationKey (AES default) SUCCESS", COLOR_GREEN);
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "legacy auth with MasterApplicationKey (AES default) NO SUCCESS", COLOR_RED);
                }
            }
        });

        authKeyA1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Key 1 = Read & Write Access key
                clearOutputFields();
                writeToUiAppend(output, "legacy authentication with AES Key 1 = Read & Write Access key");
                boolean success = legacyAesAuth(APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, "legacy authentication with APPLICATION_KEY_RW_AES_DEFAULT SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "legacy auth with RW access key (AES default) SUCCESS", COLOR_GREEN);
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "legacy auth with RW access key (AES default) NO SUCCESS", COLOR_RED);
                }
            }
        });

        authKeyAM0Ev2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // new EV2First authentication with AES Master Application Key
                clearOutputFields();
                writeToUiAppend(output, "new EV2First authentication with AES Master Application Key");
                boolean success = newAesEv2Auth(MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, "new EV2First authentication with MASTER_APPLICATION_KEY_AES_DEFAULT SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "new EV2First auth with MasterApplicationKey (AES default) SUCCESS", COLOR_GREEN);
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "new EV2First auth with MasterApplicationKey (AES default) NO SUCCESS", COLOR_RED);
                }
/*
new AES EV2First auth and secure messaging
Authenticate EV2 First Command : 710006000000000000
Authenticate EV2 First Command Response : 1E4ED24D113209311EF9DE00977D354A
Command sent to card : 6E4B66EC7DFD37438E
Response received : 00000A002525C5AF6CE39963
 */
            }
        });

        authKeyVc20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Key 32 = VirtualCard Configuration key
                clearOutputFields();
                writeToUiAppend(output, "legacy AES authentication with AES Key 32 = VirtualCard Configuration key");
                boolean success = legacyAesAuth(VIRTUAL_CARD_CONFIG_KEY_NUMBER_INT, VIRTUAL_CARD_CONFIG_KEY_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, "legacy AES authentication with VIRTUAL_CARD_CONFIG_KEY_AES_DEFAULT SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "legacy AES auth with VC Config key (AES default) SUCCESS", COLOR_GREEN);
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "legacy AES auth with VC Config key (AES default) NO SUCCESS", COLOR_RED);
                }
            }
        });

        authKeyVc21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Key 33 = VirtualCard Proximity key
                // new EV2 authentication with AES Key 33 = VirtualCard Proximity key
                clearOutputFields();
                writeToUiAppend(output, "new EV2 AES authentication with AES Key 33 = VirtualCard Proximity key");
                //boolean success = legacyAesAuth(VIRTUAL_CARD_PROXIMITY_KEY_NUMBER_INT, VIRTUAL_CARD_PROXIMITY_KEY_AES_DEFAULT);
                boolean success = newAesEv2Auth(VIRTUAL_CARD_PROXIMITY_KEY_NUMBER_INT, VIRTUAL_CARD_PROXIMITY_KEY_AES_DEFAULT);
                if (success) {
                    //writeToUiAppend(output, "legacy AES authentication with VIRTUAL_CARD_PROXIMITY_KEY_AES_DEFAULT SUCCESS");
                    writeToUiAppend(output, "new EV2 AES authentication with VIRTUAL_CARD_PROXIMITY_KEY_AES_DEFAULT SUCCESS");
                    //writeToUiAppendBorderColor(errorCode, errorCodeLayout, "legacy AES auth with VC Proximity key (AES default) SUCCESS", COLOR_GREEN);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "new EV2 AES auth with VC Proximity key (AES default) SUCCESS", COLOR_GREEN);
                } else {
                    //writeToUiAppendBorderColor(errorCode, errorCodeLayout, "legacy AES auth with VC Proximity key (AES default) NO SUCCESS", COLOR_RED);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "new EV2 AES auth with VC Proximity key (AES default) NO SUCCESS", COLOR_RED);
                }
            }
        });


        changeKeyD2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change key 02 in sample app to an AES key
                clearOutputFields();
                writeToUiAppend(output, "change key 02 from DES default to AES key");

                byte[] APPLICATION_KEY_CAR_DEFAULT2 = new byte[16];

                // authenticate with master application key = 0
                int masterApplicationKeyNumber = 0;

                byte[] TDES_KEY_ZERO = new byte[16]; // 16 bytes even for single des key (double the key)
                KeyData objKEY_TDES_ZERO = new KeyData();
                SecretKeySpec secretKeySpecTDesZero = new SecretKeySpec(TDES_KEY_ZERO, "TDES");
                objKEY_TDES_ZERO.setKey(secretKeySpecTDesZero);

                // test key 02
                desFireEV3.authenticate(APPLICATION_KEY_CAR_NUMBER, IDESFireEV3.AuthType.Native, KeyType.THREEDES, objKEY_TDES_ZERO);
                String authStatus = desFireEV3.getAuthStatus();
                writeToUiAppend(output, "authStatus key 2: " + authStatus);

                // auth with key 0
                desFireEV3.authenticate(masterApplicationKeyNumber, IDESFireEV3.AuthType.Native, KeyType.THREEDES, objKEY_TDES_ZERO);
                authStatus = desFireEV3.getAuthStatus();
                writeToUiAppend(output, "authStatus key 0: " + authStatus);

                //byte[] AES_KEY_CAR = new byte[16]; // 16 bytes even for single des key (double the key)
                KeyData objKEY_AES_CAR = new KeyData();
                SecretKeySpec secretKeySpecAES = new SecretKeySpec(APPLICATION_KEY_CAR_AES, "AES");
                objKEY_AES_CAR.setKey(secretKeySpecAES);

                int keyNumberToChange = APPLICATION_KEY_CAR_NUMBER;
                byte newKeyVersion = 0;
                try {

                    EV1KeySettings keySettings = desFireEV3.getKeySettings();
                    writeToUiAppend(output, "keySettings: " + keySettings.toString());


                    writeToUiAppend(output, "going to change key 02 from DES to AES");
                    authStatus = desFireEV3.getAuthStatus();
                    writeToUiAppend(output, "authStatus2: " + authStatus);
                    writeToUiAppend(output, printData("APPLICATION_KEY_CAR_DEFAULT2", APPLICATION_KEY_CAR_DEFAULT2));
                    writeToUiAppend(output, printData("APPLICATION_KEY_CAR_AES", APPLICATION_KEY_CAR_AES));

                    EV2PICCConfigurationSettings piccKeySettings = new EV2PICCConfigurationSettings();
                    piccKeySettings.setAppDefaultKey(SampleAppKeys.KEY_AES128_PICC_APP_DEFAULT_KEY, (byte) 1);


                    //EV3PICCConfigurationSettings piccKeySettings = new EV3PICCConfigurationSettings();
                    EV3ApplicationKeySettings.Builder ev3ApplicationKeySettings = new EV3ApplicationKeySettings.Builder();

                    desFireEV3.changeKey(keyNumberToChange, KeyType.AES128, APPLICATION_KEY_CAR_DEFAULT2, APPLICATION_KEY_CAR_AES, newKeyVersion);
                    writeToUiAppend(output, "change key 02 from DES to AES done");
                    authStatus = desFireEV3.getAuthStatus();
                    writeToUiAppend(output, "authStatus3: " + authStatus);


                } catch (SecurityException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "SecurityException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                } catch (PICCException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "PICCException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                } catch (Exception e) {
                    writeToUiAppend(output, "IOException occurred... check LogCat");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                }
/*
https://github.com/dfpalomar/TapLinxSample/blob/master/src/main/java/com/nxp/sampletaplinx/MainActivity.java

https://github.com/RfidResearchGroup/proxmark3/blob/master/doc/desfire.md

void changeKey(int cardkeyNumber,
               KeyType keyType,
               byte[] oldKey,
               byte[] newKey,
               byte newKeyVersion)
This method allows to change any key stored on the PICC. If the AID 0x00 is selected (PICC level ), the change applies to the PICC Master Key. As only one PICC Master key is stored on MIFARE DESFire EV1. In all other cases (if the selected AID is not 0x00 ) the change applies to the specified KeyNo within the currently selected application ( represented by it's AID ). On Application level ( the selected AID is not 0x00) it is not possible to change key after application creation. NOTE: oldkey and newKey is taken as byte array instead of IKeyData.This is because changing the the Key in the card require actual key bytes. IKeyData represents the secure key object which may be in the secure environment [ like HSM (Hardware secure module)] where we cant get the key contents always.

Parameters:
cardkeyNumber - key number to change.
keyType - Key type of the new Key
oldKey - old key of length 16/24 bytes depends on key type.
if type is AES128 then, key length should be 16 bytes.
if type is THREEDES then, [0 to 7 ] equal to [ 8 to 15 ] bytes of the 16 byte key.
if type is TWO_KEY_THREEDES then, [0 to 7 ] not equal to [ 8 to 15 ] bytes of the 16 byte key.
if type is THREE_KEY_THREEDES then, key data should be 24 bytes but key data not necessarily follow the pattern explained for THREEDES, TWO_KEY_THREEDES
newKey - new key of length 16/24 bytes depends on key type.
if type is AES128 then, key length should be 16 bytes.
if type is THREEDES then, [0 to 7 ] equal to [ 8 to 15 ] bytes of the 16 byte key.
if type is TWO_KEY_THREEDES then, [0 to 7 ] not equal to [ 8 to 15 ] bytes of the 16 byte key.
if type is THREE_KEY_THREEDES then, key data should be 24 bytes but key data not necessarily follow the pattern explained for THREEDES, TWO_KEY_THREEDES
newKeyVersion - new key version byte.
 */
            }
        });


        /**
         * this method will format the card and change the Master Application Key to an AES default key
         */

        changeMasterKeyToAes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change the Master Application key 0x00 to the AES default key
                clearOutputFields();
                writeToUiAppend(output, "change the Master Application Key to the default AES key");
                // open a confirmation dialog
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                try {
                                    writeToUiAppend(output, "select the Master Application: " + MASTER_APPLICATION_IDENTIFIER_INT);
                                    desFireEV3.selectApplication(MASTER_APPLICATION_IDENTIFIER_INT);
                                    writeToUiAppend(output, "PICC configuration settings, new key version: " + MASTER_APPLICATION_KEY_VERSION + printData(" key", MASTER_APPLICATION_KEY_AES_DEFAULT));
                                    EV3PICCConfigurationSettings piccKeySettings = new EV3PICCConfigurationSettings();
                                    piccKeySettings.setAppDefaultKey(MASTER_APPLICATION_KEY_AES_DEFAULT, MASTER_APPLICATION_KEY_VERSION);
                                    writeToUiAppend(output, "auth the Master Application with the old DES default key: " + printData("default key", DES_DEFAULT_KEY_TDES));
                                    KeyData keyData = getDesKeyFromByteArray(DES_DEFAULT_KEY_TDES);
                                    byte[] keyDataBytes = keyData.getKey().getEncoded();
                                    writeToUiAppend(output, "keyData of the old DES default key: " + printData("keyData", keyDataBytes));
                                    writeToUiAppend(output, "auth the Master Application with keyNumber: " + MASTER_APPLICATION_KEY_NUMBER + " using Native auth");
                                    desFireEV3.authenticate(MASTER_APPLICATION_KEY_NUMBER, IDESFireEV3.AuthType.Native, KeyType.THREEDES, keyData);
                                    writeToUiAppend(output, "set the configuration byte (PICC key settings)");
                                    desFireEV3.setConfigurationByte(piccKeySettings);
                                    writeToUiAppend(output, "change the Master Application key: " + MASTER_APPLICATION_IDENTIFIER_INT);
                                    desFireEV3.changeKey(MASTER_APPLICATION_KEY_NUMBER, KeyType.AES128, MASTER_APPLICATION_KEY_DES_DEFAULT, MASTER_APPLICATION_KEY_AES_DEFAULT, MASTER_APPLICATION_KEY_VERSION);
                                    writeToUiAppend(output, "change of the Master Application Key done," + printData("new key", MASTER_APPLICATION_KEY_AES_DEFAULT));
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "change MasterApplicationKey success", COLOR_GREEN);
                                } catch (InvalidResponseLengthException e) {
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                } catch (UsageException e) {
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "UsageException occurred\n" + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                } catch (
                                        SecurityException e) { // don't use the java Security Exception but the  NXP one
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "SecurityException occurred\n" + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                } catch (PICCException e) {
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "PICCException occurred\n" + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    writeToUiAppend(output, "Exception occurred... check LogCat");
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException occurred\n" + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                // nothing to do
                                writeToUiAppend(output, "change of the MasterKey aborted");
                                break;
                        }
                    }
                };
                final String selectedFolderString = "You are going to change the MasterKey to AES default key." + "\n\n" +
                        "Do you want to proceed ?";
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setMessage(selectedFolderString).setPositiveButton(android.R.string.yes, dialogClickListener)
                        .setNegativeButton(android.R.string.no, dialogClickListener)
                        .setTitle("CHANGE the MasterKey to AES")
                        .show();
        /*
        If you want to use the "yes" "no" literals of the user's language you can use this
        .setPositiveButton(android.R.string.yes, dialogClickListener)
        .setNegativeButton(android.R.string.no, dialogClickListener)
         */


            }
        });

        changeKeyVc20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change the VirtualCard Configuration Key (0x20)
                clearOutputFields();
                writeToUiAppend(output, "change the VirtualCard Configuration Key (0x20) to the default AES key");
                // open a confirmation dialog
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                try {
                                    writeToUiAppend(output, "select the Master Application: " + MASTER_APPLICATION_IDENTIFIER_INT);
                                    desFireEV3.selectApplication(MASTER_APPLICATION_IDENTIFIER_INT);
                                    //writeToUiAppend(output, "PICC configuration settings, new key version: " + MASTER_APPLICATION_KEY_VERSION + printData(" key", MASTER_APPLICATION_KEY_AES_DEFAULT));
                                    //EV3PICCConfigurationSettings piccKeySettings = new EV3PICCConfigurationSettings();
                                    //piccKeySettings.setAppDefaultKey(MASTER_APPLICATION_KEY_AES_DEFAULT, MASTER_APPLICATION_KEY_VERSION);
                                    writeToUiAppend(output, "auth the Master Application with the AES default key: " + printData("default key", MASTER_APPLICATION_KEY_AES_DEFAULT));
                                    KeyData keyData = getAesKeyFromByteArray(MASTER_APPLICATION_KEY_AES_DEFAULT);
                                    byte[] keyDataBytes = keyData.getKey().getEncoded();
                                    writeToUiAppend(output, "keyData of the AES default key: " + printData("keyData", keyDataBytes));
                                    writeToUiAppend(output, "auth the Master Application with keyNumber: " + MASTER_APPLICATION_KEY_NUMBER + " using AES auth");
                                    desFireEV3.authenticate(MASTER_APPLICATION_KEY_NUMBER, IDESFireEV3.AuthType.AES, KeyType.AES128, keyData);
                                    //writeToUiAppend(output, "set the configuration byte (PICC key settings)");
                                    //desFireEV3.setConfigurationByte(piccKeySettings);
                                    writeToUiAppend(output, "change the VirtualCard Configuration Key (0x20): " + VIRTUAL_CARD_CONFIG_KEY_NUMBER_INT);
                                    desFireEV3.changePICCKeys(VIRTUAL_CARD_CONFIG_KEY_NUMBER_INT, KeyType.AES128, VIRTUAL_CARD_CONFIG_KEY_AES_DEFAULT, VIRTUAL_CARD_CONFIG_KEY_AES_DEFAULT, VIRTUAL_CARD_CONFIG_KEY_VERSION);
                                    writeToUiAppend(output, "change of the VirtualCard Configuration Key (0x20) done," + printData("new key", VIRTUAL_CARD_CONFIG_KEY_AES_DEFAULT));
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "change VirtualCard Configuration Key (0x20) success", COLOR_GREEN);


                                } catch (InvalidResponseLengthException e) {
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                } catch (UsageException e) {
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "UsageException occurred\n" + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                } catch (
                                        SecurityException e) { // don't use the java Security Exception but the  NXP one
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "SecurityException occurred\n" + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                } catch (PICCException e) {
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "PICCException occurred\n" + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    writeToUiAppend(output, "Exception occurred... check LogCat");
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException occurred\n" + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                // nothing to do
                                writeToUiAppend(output, "change of the VirtualCard Configuration Key (0x20) aborted");
                                break;
                        }
                    }
                };
                final String selectedFolderString = "You are going to change the VirtualCard Configuration Key to AES default key." + "\n\n" +
                        "Do you want to proceed ?";
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setMessage(selectedFolderString).setPositiveButton(android.R.string.yes, dialogClickListener)
                        .setNegativeButton(android.R.string.no, dialogClickListener)
                        .setTitle("CHANGE the VirtualCard Configuration Key to AES")
                        .show();
            }
        });

        changeKeyVc21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change the VirtualCard Proximity Key (0x21)
                clearOutputFields();
                writeToUiAppend(output, "change the VirtualCard Proximity Key (0x21) to the default AES key");
                // open a confirmation dialog
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                try {
                                    writeToUiAppend(output, "select the Master Application: " + MASTER_APPLICATION_IDENTIFIER_INT);
                                    desFireEV3.selectApplication(MASTER_APPLICATION_IDENTIFIER_INT);
                                    //writeToUiAppend(output, "PICC configuration settings, new key version: " + MASTER_APPLICATION_KEY_VERSION + printData(" key", MASTER_APPLICATION_KEY_AES_DEFAULT));
                                    //EV3PICCConfigurationSettings piccKeySettings = new EV3PICCConfigurationSettings();
                                    //piccKeySettings.setAppDefaultKey(MASTER_APPLICATION_KEY_AES_DEFAULT, MASTER_APPLICATION_KEY_VERSION);
                                    writeToUiAppend(output, "auth the Master Application with the AES default key: " + printData("default key", MASTER_APPLICATION_KEY_AES_DEFAULT));
                                    KeyData keyData = getAesKeyFromByteArray(MASTER_APPLICATION_KEY_AES_DEFAULT);
                                    byte[] keyDataBytes = keyData.getKey().getEncoded();
                                    writeToUiAppend(output, "keyData of the AES default key: " + printData("keyData", keyDataBytes));
                                    writeToUiAppend(output, "auth the Master Application with keyNumber: " + MASTER_APPLICATION_KEY_NUMBER + " using AES auth");
                                    desFireEV3.authenticate(MASTER_APPLICATION_KEY_NUMBER, IDESFireEV3.AuthType.AES, KeyType.AES128, keyData);
                                    // now that we are authenticated we need a second authentication with VC Configuration Key
                                    writeToUiAppend(output, "auth the VirtualCard Config with the AES default key: " + printData("default key", VIRTUAL_CARD_CONFIG_KEY_AES_DEFAULT));
                                    keyData = getAesKeyFromByteArray(VIRTUAL_CARD_CONFIG_KEY_AES_DEFAULT);
                                    keyDataBytes = keyData.getKey().getEncoded();
                                    writeToUiAppend(output, "keyData of the AES default key: " + printData("keyData", keyDataBytes));
                                    writeToUiAppend(output, "auth the VirtualCard Config with keyNumber: " + VIRTUAL_CARD_CONFIG_KEY_NUMBER_INT + " using AES auth");
                                    desFireEV3.authenticate(VIRTUAL_CARD_CONFIG_KEY_NUMBER_INT, IDESFireEV3.AuthType.AES, KeyType.AES128, keyData);
                                    writeToUiAppend(output, "change the VirtualCard Proximity Key (0x21): " + VIRTUAL_CARD_PROXIMITY_KEY_NUMBER_INT);
                                    //desFireEV3.changePICCKeys(VIRTUAL_CARD_PROXIMITY_KEY_NUMBER_INT, KeyType.AES128, VIRTUAL_CARD_PROXIMITY_KEY_AES_DEFAULT, VIRTUAL_CARD_PROXIMITY_KEY_AES_DEFAULT, VIRTUAL_CARD_PROXIMITY_KEY_VERSION);
                                    desFireEV3.changeVCKey(VIRTUAL_CARD_PROXIMITY_KEY_NUMBER_INT, VIRTUAL_CARD_PROXIMITY_KEY_AES_DEFAULT, VIRTUAL_CARD_PROXIMITY_KEY_AES_DEFAULT, VIRTUAL_CARD_PROXIMITY_KEY_VERSION);
                                    writeToUiAppend(output, "change of the VirtualCard Proximity Key (0x21) done," + printData("new key", VIRTUAL_CARD_PROXIMITY_KEY_AES_DEFAULT));
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "change VirtualCard Proximity Key (0x21) success", COLOR_GREEN);
                                } catch (InvalidResponseLengthException e) {
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                } catch (UsageException e) {
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "UsageException occurred\n" + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                } catch (
                                        SecurityException e) { // don't use the java Security Exception but the  NXP one
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "SecurityException occurred\n" + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                } catch (PICCException e) {
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "PICCException occurred\n" + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    writeToUiAppend(output, "Exception occurred... check LogCat");
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException occurred\n" + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                // nothing to do
                                writeToUiAppend(output, "change of the VirtualCard Proximity Key (0x21) aborted");
                                break;
                        }
                    }
                };
                final String selectedFolderString = "You are going to change the VirtualCard Proximity Key to AES default key." + "\n\n" +
                        "Do you want to proceed ?";
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(selectedFolderString).setPositiveButton(android.R.string.yes, dialogClickListener)
                        .setNegativeButton(android.R.string.no, dialogClickListener)
                        .setTitle("CHANGE the VirtualCard Proximity Key to AES")
                        .show();
            }
        });

        proximityPrepare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // prepare for the proximity check
                clearOutputFields();
                writeToUiAppend(output, "prepare for the proximity check");
                try {
                    writeToUiAppend(output, "select the Master Application: " + MASTER_APPLICATION_IDENTIFIER_INT);
                    desFireEV3.selectApplication(MASTER_APPLICATION_IDENTIFIER_INT);

                    /*
                    writeToUiAppend(output, "auth the Master Application with the AES default key: " + printData("default key", MASTER_APPLICATION_KEY_AES_DEFAULT));
                    KeyData keyData = getAesKeyFromByteArray(MASTER_APPLICATION_KEY_AES_DEFAULT);
                    byte[] keyDataBytes = keyData.getKey().getEncoded();
                    writeToUiAppend(output, "keyData of the AES default key: " + printData("keyData", keyDataBytes));
                    writeToUiAppend(output, "new EV2 auth the Master Application with keyNumber: " + MASTER_APPLICATION_KEY_NUMBER + " using AES auth");
                    newAesEv2Auth(MASTER_APPLICATION_KEY_NUMBER_INT, MASTER_APPLICATION_KEY_AES_DEFAULT);
*/
                    // now that we are authenticated we need a second authentication with VC Configuration Key
                    writeToUiAppend(output, "auth the VirtualCard Config with the AES default key: " + printData("default key", VIRTUAL_CARD_CONFIG_KEY_AES_DEFAULT));
                    KeyData keyData = getAesKeyFromByteArray(VIRTUAL_CARD_CONFIG_KEY_AES_DEFAULT);
                    byte[] keyDataBytes = keyData.getKey().getEncoded();
                    writeToUiAppend(output, "keyData of the AES default key: " + printData("keyData", keyDataBytes));
                    writeToUiAppend(output, "auth the VirtualCard Config with keyNumber: " + VIRTUAL_CARD_CONFIG_KEY_NUMBER_INT + " using AES auth");
                    //desFireEV3.authenticate(VIRTUAL_CARD_CONFIG_KEY_NUMBER_INT, IDESFireEV3.AuthType.AES, KeyType.AES128, keyData);
                    legacyAesAuth(VIRTUAL_CARD_PROXIMITY_KEY_NUMBER_INT, VIRTUAL_CARD_PROXIMITY_KEY_AES_DEFAULT);

                    //desFireEV3.authenticate(MASTER_APPLICATION_KEY_NUMBER, IDESFireEV3.AuthType.AES, KeyType.AES128, keyData);

                    KeyData keyDataProx = getAesKeyFromByteArray(VIRTUAL_CARD_PROXIMITY_KEY_AES_DEFAULT);
                    int numberOfProximityIterations = 1; // Needs to be 1, 2, 4, or 8   or 1..8
                    //desFireEV3.proximityCheckEV3(null, numberOfProximityIterations);
                    //desFireEV3.proximityCheckEV3(keyDataProx, numberOfProximityIterations);
                    //desFireEV3.proximityCheck(keyDataProx, numberOfProximityIterations); // not supported in desfireEV3
                    //writeToUiAppendBorderColor(errorCode, errorCodeLayout, "proximity check success", COLOR_GREEN);

                    writeToUiAppend(output, "manual PC prepare");
                    byte prepareCheckCommand = (byte) 0xF0;
                    byte[] prepareCheckWrappedCommand = wrapMessage(prepareCheckCommand, null);
                    writeToUiAppend(output, printData("prepareCheckWrappedCommand", prepareCheckWrappedCommand));
                    byte[] resp = desFireEV3.getReader().transceive(prepareCheckWrappedCommand);
                    writeToUiAppend(output, printData("resp", resp));
                    writeToUiAppend(output, "manual PC check");

/*

                    //writeToUiAppend(output, "PICC configuration settings, new key version: " + MASTER_APPLICATION_KEY_VERSION + printData(" key", MASTER_APPLICATION_KEY_AES_DEFAULT));
                    //EV3PICCConfigurationSettings piccKeySettings = new EV3PICCConfigurationSettings();
                    //piccKeySettings.setAppDefaultKey(MASTER_APPLICATION_KEY_AES_DEFAULT, MASTER_APPLICATION_KEY_VERSION);
                    writeToUiAppend(output, "auth the Master Application with the AES default key: " + printData("default key", MASTER_APPLICATION_KEY_AES_DEFAULT));
                    KeyData keyData = getAesKeyFromByteArray(MASTER_APPLICATION_KEY_AES_DEFAULT);
                    byte[] keyDataBytes = keyData.getKey().getEncoded();
                    writeToUiAppend(output, "keyData of the AES default key: " + printData("keyData", keyDataBytes));
                    writeToUiAppend(output, "auth the Master Application with keyNumber: " + MASTER_APPLICATION_KEY_NUMBER + " using AES auth");
                    desFireEV3.authenticate(MASTER_APPLICATION_KEY_NUMBER, IDESFireEV3.AuthType.AES, KeyType.AES128, keyData);
                    //writeToUiAppend(output, "set the configuration byte (PICC key settings)");
                    //desFireEV3.setConfigurationByte(piccKeySettings);
                    writeToUiAppend(output, "change the VirtualCard Configuration Key (0x20): " + VIRTUAL_CARD_CONFIG_KEY_NUMBER_INT);
                    desFireEV3.changePICCKeys(VIRTUAL_CARD_CONFIG_KEY_NUMBER_INT, KeyType.AES128, VIRTUAL_CARD_CONFIG_KEY_AES_DEFAULT, VIRTUAL_CARD_CONFIG_KEY_AES_DEFAULT, VIRTUAL_CARD_CONFIG_KEY_VERSION);
                    writeToUiAppend(output, "change of the VirtualCard Configuration Key (0x20) done," + printData("new key", VIRTUAL_CARD_CONFIG_KEY_AES_DEFAULT));
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "change VirtualCard Configuration Key (0x20) success", COLOR_GREEN);
*/

                } catch (InvalidResponseLengthException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                } catch (UsageException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "UsageException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                } catch (
                        SecurityException e) { // don't use the java Security Exception but the  NXP one
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "SecurityException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                } catch (PICCException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "PICCException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                } catch (Exception e) {
                    writeToUiAppend(output, "Exception occurred... check LogCat");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException occurred\n" + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                }

            }
        });

        keyVersionKeyVc20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // gets the key version of the VirtualCard Configuration key (0x20)
                clearOutputFields();
                writeToUiAppend(output, "select the Master Application: " + MASTER_APPLICATION_IDENTIFIER_INT);
                desFireEV3.selectApplication(MASTER_APPLICATION_IDENTIFIER_INT);
                int keyVersion = getKeyVersion(VIRTUAL_CARD_CONFIG_KEY_NUMBER_INT);
                writeToUiAppend(output, "The key version for key " + VIRTUAL_CARD_CONFIG_KEY_NUMBER_INT + " is " + keyVersion);
            }
        });

        keyVersionKeyVc21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // gets the key version of the VirtualCard Proximity key (0x21)
                clearOutputFields();
                writeToUiAppend(output, "select the Master Application: " + MASTER_APPLICATION_IDENTIFIER_INT);
                desFireEV3.selectApplication(MASTER_APPLICATION_IDENTIFIER_INT);
                int keyVersion = getKeyVersion(VIRTUAL_CARD_PROXIMITY_KEY_NUMBER_INT);
                writeToUiAppend(output, "The key version for key " + VIRTUAL_CARD_PROXIMITY_KEY_NUMBER_INT + " is " + keyVersion);
            }
        });


        freeMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the free memory on the card
                clearOutputFields();
                String logString = "get the free memory on the tag";
                writeToUiAppend(output, logString);

                int freeMemoryInt = getFreeMemory(logString);
                if (freeMemoryInt > -1) {
                    writeToUiAppend(output, logString + ": " + freeMemoryInt + " bytes");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                } else {
                    writeToUiAppend(output, logString + ": get an ERROR");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " FAILURE", COLOR_RED);
                }
            }
        });

        // testing

        createApplication1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new application
                clearOutputFields();
                String logString = "create a new application 1 with 5 AES keys";
                writeToUiAppend(output, logString);
                int numberOfKeysInt = 5;
                int applicationIdentifierInt = 1;

                boolean success = createApplicationEv3(logString, applicationIdentifierInt, numberOfKeysInt, KeyType.AES128);
                if (success) {
                    writeToUiAppend(output, "SUCCESS for new appID int" + applicationIdentifierInt + " with " + numberOfKeysInt + " AES keys");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                } else {
                    writeToUiAppend(output, logString + " not possible for new appID int" + applicationIdentifierInt + " with " + numberOfKeysInt + " AES keys");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " FAILURE", COLOR_RED);
                }
            }
        });

    }

    /**
     * section for applications
     */

    /**
     * creates a new application for Mifare DESFireEV3 tag with default settings:
     * .setAppKeySettingsChangeable(true)
     * .setAppMasterKeyChangeable(true)
     * .setAuthenticationRequiredForFileManagement(false)
     * .setAuthenticationRequiredForDirectoryConfigurationData(false)
     *
     * @param logString: provide a string for error log
     * @param applicationIdentifier: 3 bytes long array with the application identifier
     * @param numberOfKeysInt: minimum is 1 BUT you should give a minimum of 5 keys as on file creation we will need them. Maximum is 14
     * @return true for create success
     * Note: this methods assumes that we can create a new application without prior authentication with the master application key (setup in PICC settings)
     */
    private boolean createApplicationEv3(String logString, byte[] applicationIdentifier, int numberOfKeysInt, KeyType keyType) {
        // create a new application
        Log.d(TAG, logString + " for " + printData("AID", applicationIdentifier) + " with " + numberOfKeysInt + " keys of type " + keyType.toString());
        // sanity checks
        if ((applicationIdentifier == null) || (applicationIdentifier.length != 3)) {
            Log.e(TAG, logString + " wrong argument: applicationIdentifier is NULL or not of length 3, aborted");
            return false;
        }
        if (numberOfKeysInt < 1) {
            Log.e(TAG, logString + " wrong argument: numberOfKeysInt is < 1, aborted");
            return false;
        }
        if (numberOfKeysInt > 14) {
            Log.e(TAG, logString + " wrong argument: numberOfKeysInt is > 14, aborted");
            return false;
        }
        if (keyType.toString().equals(KeyType.UNKNOWN.toString())) {
            Log.e(TAG, logString + " wrong argument: keyType is UNKNOWN, aborted");
            return false;
        }
        // get the default application settings
        EV3ApplicationKeySettings applicationKeySettings = getApplicationSettingsDefault(numberOfKeysInt, keyType);
        if (applicationKeySettings == null) {
            Log.e(TAG, logString + " the applicationKeysSettings are NULL, aborted");
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " could not get the application settings, aborted", COLOR_RED);
        }
        try {
            // important: first select the Master Application ('0')
            desFireEV3.selectApplication(0);
            // depending on MasterKey settings an authentication is necessary, skipped here
            desFireEV3.createApplication(applicationIdentifier, applicationKeySettings);
            //writeToUiAppend(output, "create a new application done," + printData("new appID", applicationIdentifier));
            //writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout,  logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (UsageException e) {
            Log.e(TAG, logString + " UsageResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " UsageException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (SecurityException e) { // don't use the java Security Exception but the NXP one
            Log.e(TAG, logString + " SecurityException occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SecurityException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (PICCException e) {
            Log.e(TAG, logString + " PICCException occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " PICCException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * creates a new application for Mifare DESFireEV3 tag with default settings:
     * .setAppKeySettingsChangeable(true)
     * .setAppMasterKeyChangeable(true)
     * .setAuthenticationRequiredForFileManagement(false)
     * .setAuthenticationRequiredForDirectoryConfigurationData(false)
     *
     * @param logString: provide a string for error log
     * @param applicationIdentifierInt: an integer for the application number
     * @param numberOfKeysInt: minimum is 1 BUT you should give a minimum of 5 keys as on file creation we will need them. Maximum is 14
     * @return true for create success
     * Note: this methods assumes that we can create a new application without prior authentication with the master application key (setup in PICC settings)
     */
    private boolean createApplicationEv3(String logString, int applicationIdentifierInt, int numberOfKeysInt, KeyType keyType) {
        // create a new application
        byte[] applicationIdentifier = Utilities.intToBytes(applicationIdentifierInt, 3);
        Log.d(TAG, logString + " for " + printData("AID", applicationIdentifier) + " with " + numberOfKeysInt + " keys of type " + keyType.toString());
        // sanity checks
        if ((applicationIdentifier == null) || (applicationIdentifier.length != 3)) {
            Log.e(TAG, logString + " wrong argument: applicationIdentifier is NULL or not of length 3, aborted");
            return false;
        }
        if (numberOfKeysInt < 1) {
            Log.e(TAG, logString + " wrong argument: numberOfKeysInt is < 1, aborted");
            return false;
        }
        if (numberOfKeysInt > 14) {
            Log.e(TAG, logString + " wrong argument: numberOfKeysInt is > 14, aborted");
            return false;
        }
        if (keyType.toString().equals(KeyType.UNKNOWN.toString())) {
            Log.e(TAG, logString + " wrong argument: keyType is UNKNOWN, aborted");
            return false;
        }
        // get the default application settings
        EV3ApplicationKeySettings applicationKeySettings = getApplicationSettingsDefault(numberOfKeysInt, keyType);
        if (applicationKeySettings == null) {
            Log.e(TAG, logString + " the applicationKeysSettings are NULL, aborted");
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " could not get the application settings, aborted", COLOR_RED);
        }
        try {
            // important: first select the Master Application ('0')
            desFireEV3.selectApplication(0);
            // depending on MasterKey settings an authentication is necessary, skipped here
            desFireEV3.createApplication(applicationIdentifier, applicationKeySettings);
            //writeToUiAppend(output, "create a new application done," + printData("new appID", applicationIdentifier));
            //writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout,  logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (UsageException e) {
            Log.e(TAG, logString + " UsageResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " UsageException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (SecurityException e) { // don't use the java Security Exception but the NXP one
            Log.e(TAG, logString + " SecurityException occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SecurityException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (PICCException e) {
            Log.e(TAG, logString + " PICCException occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " PICCException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }


    private byte[] wrapMessage(byte command, byte[] parameters) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write((byte) 0x90);
        stream.write(command);
        stream.write((byte) 0x00);
        stream.write((byte) 0x00);
        if (parameters != null) {
            stream.write((byte) parameters.length);
            stream.write(parameters);
        }
        stream.write((byte) 0x00);
        return stream.toByteArray();
    }


    /**
     * section for authentication
     */

    private boolean legacyAesAuth(int keyNumber, byte[] keyToAuthenticate) {
        try {
            Log.d(TAG, "Authentication with AES legacy");
            writeToUiAppend(output, "AES legacy auth");
            SecretKey originalKey = new SecretKeySpec(keyToAuthenticate, 0, keyToAuthenticate.length, "AES");
            KeyData keyData = new KeyData();
            keyData.setKey(originalKey);
            desFireEV3.authenticate(keyNumber, IDESFireEV1.AuthType.AES, KeyType.AES128, keyData);
            return true;
        } catch (InvalidResponseLengthException e) {
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (UsageException e) {
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "UsageException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (SecurityException e) { // don't use the java Security Exception but the  NXP one
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "SecurityException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (PICCException e) {
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "PICCException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (Exception e) {
            writeToUiAppend(output, "Exception occurred... check LogCat");
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    private boolean newAesEv2Auth(int keyNumber, byte[] keyToAuthenticate) {
        Log.d(TAG, "Authentication with AES EV2First");
        try {
            writeToUiAppend(output, "new AES EV2First auth and secure messaging");
            SecretKey originalKey = new SecretKeySpec(keyToAuthenticate, 0, keyToAuthenticate.length, "AES");
            KeyData keyData = new KeyData();
            keyData.setKey(originalKey);
            byte[] pCDcap2 = new byte[]{0, 0, 0, 0, 0, 0};
            desFireEV3.authenticateEV2First(keyNumber, keyData, pCDcap2);
            desFireEV3.getFreeMemory();
            return true;
        } catch (InvalidResponseLengthException e) {
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (UsageException e) {
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "UsageException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (SecurityException e) { // don't use the java Security Exception but the  NXP one
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "SecurityException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (PICCException e) {
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "PICCException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (Exception e) {
            writeToUiAppend(output, "Exception occurred... check LogCat");
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * section for helper methods
     */

    private EV3ApplicationKeySettings getApplicationSettingsDefault(int maxNumberOfApplicationKeys, KeyType keyType) {
        Log.d(TAG, "get default application settings with maxNumberOfApplicationKeys " + maxNumberOfApplicationKeys + " and keyType " + keyType);
        if (maxNumberOfApplicationKeys < 1) {
            Log.e(TAG, "maxNumberOfApplicationKeys is < 1, aborted");
            return null;
        }
        EV3ApplicationKeySettings.Builder appsetbuilder = new EV3ApplicationKeySettings.Builder();
        appsetbuilder
                .setAppKeySettingsChangeable(true)
                .setAppMasterKeyChangeable(true)
                .setAuthenticationRequiredForFileManagement(false)
                .setAuthenticationRequiredForDirectoryConfigurationData(false)
                .setKeyTypeOfApplicationKeys(keyType)
                .setMaxNumberOfApplicationKeys(maxNumberOfApplicationKeys);
        EV3ApplicationKeySettings appsettings = appsetbuilder.build();
        return appsettings;
    }

    private EV3ApplicationKeySettings getApplicationSettingsDefault(int maxNumberOfApplicationKeys) {
        Log.d(TAG, "get default application settings with maxNumberOfApplicationKeys " + maxNumberOfApplicationKeys);
        if (maxNumberOfApplicationKeys < 1) {
            Log.e(TAG, "maxNumberOfApplicationKeys is < 1, aborted");
            return null;
        }
        EV3ApplicationKeySettings.Builder appsetbuilder = new EV3ApplicationKeySettings.Builder();
        appsetbuilder
                .setAppKeySettingsChangeable(true)
                .setAppMasterKeyChangeable(true)
                .setAuthenticationRequiredForFileManagement(false)
                .setAuthenticationRequiredForDirectoryConfigurationData(false)
                .setKeyTypeOfApplicationKeys(KeyType.AES128)
                .setMaxNumberOfApplicationKeys(maxNumberOfApplicationKeys);
        EV3ApplicationKeySettings appsettings = appsetbuilder.build();
        return appsettings;
    }

    private int getKeyVersion(int keyNumber) {
        int keyVersion = desFireEV3.getKeyVersionFor(keyNumber);
        return keyVersion;
    }

    private int getFreeMemory(String logString) {
        Log.d(TAG, logString);
        int freeMemory = 0;
        try {
            // important: first select the Master Application ('0')
            desFireEV3.selectApplication(0);
            freeMemory = desFireEV3.getFreeMemory();
            return freeMemory;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout,  logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (UsageException e) {
            Log.e(TAG, logString + " UsageResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " UsageException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (SecurityException e) { // don't use the java Security Exception but the NXP one
            Log.e(TAG, logString + " SecurityException occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SecurityException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (PICCException e) {
            Log.e(TAG, logString + " PICCException occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " PICCException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * section for key conversion
     */

    private KeyData getDesKeyFromByteArray(byte[] desKeyBytes) {
        SecretKey originalKey = new SecretKeySpec(desKeyBytes, 0, desKeyBytes.length, "DESede");
        KeyData keyData = new KeyData();
        keyData.setKey(originalKey);
        return keyData;
    }

    private KeyData getAesKeyFromByteArray(byte[] aesKeyBytes) {
        SecretKey originalKey = new SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length, "AES");
        KeyData keyData = new KeyData();
        keyData.setKey(originalKey);
        return keyData;
    }

    /**
     * DESFire Pre Conditions.
     * <p/>
     * PICC Master key should be factory default settings, (ie 16 byte All zero
     * Key ).
     * <p/>
     */
    private void desfireEV1CardLogic() {
        writeToUiAppend(output, "desfireEV1CardLogic Card Detected : " + desFireEV1.getType().getTagName());

        try {
            int timeOut = 2000;
            desFireEV1.getReader().setTimeout(timeOut);
            writeToUiAppend(output,
                    "Version of the Card : "
                            + Utilities.dumpBytes(desFireEV1.getVersion()));
            desFireEV1.selectApplication(0); // todo do this before getApplicationIds
            writeToUiAppend(output,
                    "Existing Applications Ids : " + Arrays.toString(desFireEV1.getApplicationIDs()));

            //desFireEV1.selectApplication(0);

            //desFireEV1.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.THREEDES, objKEY_2KTDES);

            desFireEV1.getReader().close();

            // Set the custom path where logs will get stored, here we are setting the log folder DESFireLogs under
            // external storage.
            String spath = Environment.getExternalStorageDirectory().getPath() + File.separator + "DESFireLogs";
            NxpLogUtils.setLogFilePath(spath);
            // if you don't call save as below , logs will not be saved.
            NxpLogUtils.save();

        } catch (Exception e) {
            writeToUiAppend(output, "IOException occurred... check LogCat");
            e.printStackTrace();
        }

    }

    private void desfireEV2CardLogic() {
        int timeOut = 2000;
        writeToUiAppend(output, "desfireEV2CardLogic Card Detected : " + desFireEV2.getType().getTagName());
        try {
            desFireEV2.getReader().setTimeout(timeOut);
            writeToUiAppend(output, "Version of the Card : "
                    + Utilities.dumpBytes(desFireEV2.getVersion()));
            desFireEV2.selectApplication(0); // todo run this before get applications

            writeToUiAppend(output,
                    "Existing Applications Ids : " + Arrays.toString(desFireEV2.getApplicationIDs()));

            //desFireEV2.selectApplication(0);

            //desFireEV2.authenticate(0, IDESFireEV2.AuthType.Native, KeyType.THREEDES, objKEY_2KTDES);

            // Set the custom path where logs will get stored, here we are setting the log folder DESFireLogs under
            // external storage.
            String spath = Environment.getExternalStorageDirectory().getPath() + File.separator + "DESFireLogs";
            Log.i("LogNXP", "path to logs :" + spath);

            NxpLogUtils.setLogFilePath(spath);
            // if you don't call save as below , logs will not be saved.
            NxpLogUtils.save();

        } catch (Exception e) {
            writeToUiAppend(output, "IOException occurred... check LogCat");
            e.printStackTrace();
        }
    }

    private void desfireEV3CardLogic() {
        int timeOut = 2000;
        writeToUiAppend(output, "desfireEV3CardLogic Card Detected : " + desFireEV3.getType().getTagName());
        try {
            desFireEV3.getReader().setTimeout(timeOut);
            writeToUiAppend(output, "Version of the Card : "
                    + Utilities.dumpBytes(desFireEV3.getVersion()));
            desFireEV3.selectApplication(0); // todo run this before get applications

            writeToUiAppend(output,
                    "Existing Applications Ids : " + Arrays.toString(desFireEV3.getApplicationIDs()));

            int[] appIdsInt = desFireEV3.getApplicationIDs();
            int lastAppIdInt = 0;
            writeToUiAppend(output, "number of applications on the card: " + appIdsInt.length);
            for (int i = 0; i < appIdsInt.length; i++) {
                int appIdInt = appIdsInt[i];
                byte[] appId = Utilities.intToBytes(appIdInt, 3);
                String appIdString = Utilities.dumpBytes(appId);
                writeToUiAppend(output, "i: " + i + " appIdInt: " + appIdInt + " hex: " + appIdString);
                lastAppIdInt = appIdInt;
            }

            /*

            // select the last application
            desFireEV3.selectApplication(lastAppIdInt);
            byte[] fileIds = desFireEV3.getFileIDs();
            writeToUiAppend(output, "appId files: " + Utilities.dumpBytes(fileIds));

            String authStatus = desFireEV3.getAuthStatus();
            writeToUiAppend(output, "authStatus: " + authStatus);

            // authenticate with read access key = 3
            int readAccessKeyNumber = 3;

            byte[] TDES_KEY_ZERO = new byte[16]; // 16 bytes even for single des key (double the key)
            KeyData objKEY_TDES_ZERO = new KeyData();
            SecretKeySpec secretKeySpecTDesZero = new SecretKeySpec(TDES_KEY_ZERO, "TDES");
            objKEY_TDES_ZERO.setKey(secretKeySpecTDesZero);

            IKeyData objKEY_2KTDES;
            //desFireEV3.authenticate(readAccessKeyNumber, IDESFireEV3.AuthType.Native, KeyType.THREEDES, objKEY_2KTDES);
            desFireEV3.authenticate(readAccessKeyNumber, IDESFireEV3.AuthType.Native, KeyType.THREEDES, objKEY_TDES_ZERO);
            authStatus = desFireEV3.getAuthStatus();
            writeToUiAppend(output, "authStatus: " + authStatus);
*/
/*
void changeKey(int cardkeyNumber,
               KeyType keyType,
               byte[] oldKey,
               byte[] newKey,
               byte newKeyVersion)
This method allows to change any key stored on the PICC. If the AID 0x00 is selected (PICC level ), the change applies to the PICC Master Key. As only one PICC Master key is stored on MIFARE DESFire EV1. In all other cases (if the selected AID is not 0x00 ) the change applies to the specified KeyNo within the currently selected application ( represented by it's AID ). On Application level ( the selected AID is not 0x00) it is not possible to change key after application creation. NOTE: oldkey and newKey is taken as byte array instead of IKeyData.This is because changing the the Key in the card require actual key bytes. IKeyData represents the secure key object which may be in the secure environment [ like HSM (Hardware secure module)] where we cant get the key contents always.

Parameters:
cardkeyNumber - key number to change.
keyType - Key type of the new Key
oldKey - old key of length 16/24 bytes depends on key type.
if type is AES128 then, key length should be 16 bytes.
if type is THREEDES then, [0 to 7 ] equal to [ 8 to 15 ] bytes of the 16 byte key.
if type is TWO_KEY_THREEDES then, [0 to 7 ] not equal to [ 8 to 15 ] bytes of the 16 byte key.
if type is THREE_KEY_THREEDES then, key data should be 24 bytes but key data not necessarily follow the pattern explained for THREEDES, TWO_KEY_THREEDES
newKey - new key of length 16/24 bytes depends on key type.
if type is AES128 then, key length should be 16 bytes.
if type is THREEDES then, [0 to 7 ] equal to [ 8 to 15 ] bytes of the 16 byte key.
if type is TWO_KEY_THREEDES then, [0 to 7 ] not equal to [ 8 to 15 ] bytes of the 16 byte key.
if type is THREE_KEY_THREEDES then, key data should be 24 bytes but key data not necessarily follow the pattern explained for THREEDES, TWO_KEY_THREEDES
newKeyVersion - new key version byte.
 */

/*
            int fileNumber = 12;
            int offset = 0;
            int readLength = 0; // if 0 the complete file is read
            byte[] dataRead = desFireEV3.readData(fileNumber, offset, readLength);
            writeToUiAppend(output, printData("dataRead", dataRead));
            writeToUiAppend(output, new String(dataRead, StandardCharsets.UTF_8));

            //desFireEV2.selectApplication(0);

            //desFireEV2.authenticate(0, IDESFireEV2.AuthType.Native, KeyType.THREEDES, objKEY_2KTDES);
*/
            // Set the custom path where logs will get stored, here we are setting the log folder DESFireLogs under
            // external storage.
            String spath = Environment.getExternalStorageDirectory().getPath() + File.separator + "DESFireLogs";
            Log.i("LogNXP", "path to logs :" + spath);

            NxpLogUtils.setLogFilePath(spath);
            // if you don't call save as below , logs will not be saved.
            NxpLogUtils.save();
        } catch (SecurityException e) {
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "SecurityException occurred\n" + e.getMessage(), COLOR_RED);
        } catch (PICCException e) {
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "PICCException occurred\n" + e.getMessage(), COLOR_RED);
        } catch (Exception e) {
            writeToUiAppend(output, "IOException occurred... check LogCat");
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
    }


    private void cardLogic(final Tag tag) {
        CardType type = CardType.UnknownCard;
        try {
            type = libInstance.getCardType(tag);
        } catch (NxpNfcLibException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        switch (type) {

            case DESFireEV1:
                mCardType = CardType.DESFireEV1;
                desFireEV1 = DESFireFactory.getInstance().getDESFire(libInstance.getCustomModules());
                try {

                    desFireEV1.getReader().connect();
                    desFireEV1.getReader().setTimeout(2000);
                    desfireEV1CardLogic();

                } catch (Throwable t) {
                    t.printStackTrace();
                    writeToUiAppend(output, "Unknown Error Tap Again!");
                }
                break;

            case DESFireEV2:
                mCardType = CardType.DESFireEV2;
                writeToUiAppend(output, "DESFireEV2 Card detected.");
                writeToUiAppend(output, "Card Detected : DESFireEV2");
                desFireEV2 = DESFireFactory.getInstance().getDESFireEV2(libInstance.getCustomModules());
                try {
                    desFireEV2.getReader().connect();
                    desFireEV2.getReader().setTimeout(2000);
                    desfireEV2CardLogic();
                    //desfireEV2CardLogicCustom(); // seems not to work

                } catch (Throwable t) {
                    t.printStackTrace();
                    writeToUiAppend(output, "Unknown Error Tap Again!");
                }
                break;

            case DESFireEV3: // ### todo added without changing classes
                mCardType = CardType.DESFireEV3;
                writeToUiAppend(output, "DESFireEV3 Card detected.");

                writeToUiAppend(output, "Card Detected : DESFireEV3");
                desFireEV3 = DESFireFactory.getInstance().getDESFireEV3(libInstance.getCustomModules());
                try {
                    desFireEV3.getReader().connect();
                    desFireEV3.getReader().setTimeout(2000);
                    //desfireEV3SetVcConfigurationKey();
                    desfireEV3CardLogic();
                    //desfireEV3CardLogicProximityCheck();
                    //desfireEV3CardLogicCustom();

                } catch (Throwable t) {
                    t.printStackTrace();
                    writeToUiAppend(output, "Unknown Error Tap Again!");
                }
                break;

        }
    }

    /**
     * Initialize the library and register to this activity.
     */
    @TargetApi(19)
    private void initializeLibrary() {
        libInstance = NxpNfcLib.getInstance();
        try {
            libInstance.registerActivity(this, packageKey);
        } catch (NxpNfcLibException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Initialize the Cipher and init vector of 16 bytes with 0xCD.
     */

    private void initializeCipherinitVector() {

        /* Initialize the Cipher */
        try {
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        /* set Application Master Key */
        bytesKey = KEY_APP_MASTER.getBytes();

        /* Initialize init vector of 16 bytes with 0xCD. It could be anything */
        byte[] ivSpec = new byte[16];
        Arrays.fill(ivSpec, (byte) 0xCD);
        iv = new IvParameterSpec(ivSpec);

    }

    /**
     * section for sample keys
     */

    private void initializeKeys() {
        KeyInfoProvider infoProvider = KeyInfoProvider.getInstance(getApplicationContext());

        SharedPreferences sharedPrefs = getPreferences(Context.MODE_PRIVATE);
        boolean keysStoredFlag = sharedPrefs.getBoolean(EXTRA_KEYS_STORED_FLAG, false);
        if (!keysStoredFlag) {
            //Set Key stores the key in persistent storage, this method can be called only once if key for a given alias does not change.
            byte[] ulc24Keys = new byte[24];
            System.arraycopy(SampleAppKeys.KEY_2KTDES_ULC, 0, ulc24Keys, 0, SampleAppKeys.KEY_2KTDES_ULC.length);
            System.arraycopy(SampleAppKeys.KEY_2KTDES_ULC, 0, ulc24Keys, SampleAppKeys.KEY_2KTDES_ULC.length, 8);
            infoProvider.setKey(ALIAS_KEY_2KTDES_ULC, SampleAppKeys.EnumKeyType.EnumDESKey, ulc24Keys);

            infoProvider.setKey(ALIAS_KEY_2KTDES, SampleAppKeys.EnumKeyType.EnumDESKey, SampleAppKeys.KEY_2KTDES);
            infoProvider.setKey(ALIAS_KEY_AES128, SampleAppKeys.EnumKeyType.EnumAESKey, SampleAppKeys.KEY_AES128);
            infoProvider.setKey(ALIAS_KEY_AES128_ZEROES, SampleAppKeys.EnumKeyType.EnumAESKey, SampleAppKeys.KEY_AES128_ZEROS);
            infoProvider.setKey(ALIAS_DEFAULT_FF, SampleAppKeys.EnumKeyType.EnumMifareKey, SampleAppKeys.KEY_DEFAULT_FF);

            sharedPrefs.edit().putBoolean(EXTRA_KEYS_STORED_FLAG, true).commit();
            //If you want to store a new key after key initialization above, kindly reset the flag EXTRA_KEYS_STORED_FLAG to false in shared preferences.
        }


        objKEY_2KTDES_ULC = infoProvider.getKey(ALIAS_KEY_2KTDES_ULC, SampleAppKeys.EnumKeyType.EnumDESKey);
        objKEY_2KTDES = infoProvider.getKey(ALIAS_KEY_2KTDES, SampleAppKeys.EnumKeyType.EnumDESKey);
        objKEY_AES128 = infoProvider.getKey(ALIAS_KEY_AES128, SampleAppKeys.EnumKeyType.EnumAESKey);
        default_zeroes_key = infoProvider.getKey(ALIAS_KEY_AES128_ZEROES, SampleAppKeys.EnumKeyType.EnumAESKey);
        default_ff_key = infoProvider.getMifareKey(ALIAS_DEFAULT_FF);
    }


    /**
     * section for NFC handling
     */

    // This method is run in another thread when a card is discovered
    // !!!! This method cannot cannot direct interact with the UI Thread
    // Use `runOnUiThread` method to change the UI from this method
    @Override
    public void onTagDiscovered(Tag tag) {

        writeToUiAppend(output, "NFC tag discovered");

        cardLogic(tag);

/*
        isoDep = null;
        try {
            isoDep = IsoDep.get(tag);
            if (isoDep != null) {

                // Make a Sound
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(150, 10));
                } else {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(200);
                }

                runOnUiThread(() -> {
                    output.setText("");
                    //output.setBackgroundColor(getResources().getColor(R.color.white));
                });
                isoDep.connect();
                // get tag ID
                tagIdByte = tag.getId();
                writeToUiAppend(output, "tag id: " + Utils.bytesToHex(tagIdByte));
                writeToUiAppend(output, "NFC tag connected");

            }

        } catch (IOException e) {
            writeToUiAppend(output, "ERROR: IOException " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNfcAdapter != null) {

            Bundle options = new Bundle();
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);

            // Enable ReaderMode for all types of card and disable platform sounds
            // the option NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK is NOT set
            // to get the data of the tag afer reading
            mNfcAdapter.enableReaderMode(this,
                    this,
                    NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_F |
                            NfcAdapter.FLAG_READER_NFC_V |
                            NfcAdapter.FLAG_READER_NFC_BARCODE |
                            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                    options);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableReaderMode(this);
    }

    /**
     * section for layout handling
     */
    private void allLayoutsInvisible() {
        // todo change this
        //llApplicationHandling.setVisibility(View.GONE);
        //llStandardFile.setVisibility(View.GONE);
    }

    /**
     * section for UI handling
     */

    private void writeToUiAppend(TextView textView, String message) {
        runOnUiThread(() -> {
            String oldString = textView.getText().toString();
            if (TextUtils.isEmpty(oldString)) {
                textView.setText(message);
            } else {
                String newString = message + "\n" + oldString;
                textView.setText(newString);
                System.out.println(message);
            }
        });
    }

    private void writeToUiAppendBorderColor(TextView textView, TextInputLayout textInputLayout, String message, int color) {
        runOnUiThread(() -> {

            // set the color to green
            //Color from rgb
            // int color = Color.rgb(255,0,0); // red
            //int color = Color.rgb(0,255,0); // green
            //Color from hex string
            //int color2 = Color.parseColor("#FF11AA"); light blue
            int[][] states = new int[][]{
                    new int[]{android.R.attr.state_focused}, // focused
                    new int[]{android.R.attr.state_hovered}, // hovered
                    new int[]{android.R.attr.state_enabled}, // enabled
                    new int[]{}  //
            };
            int[] colors = new int[]{
                    color,
                    color,
                    color,
                    //color2
                    color
            };
            ColorStateList myColorList = new ColorStateList(states, colors);
            textInputLayout.setBoxStrokeColorStateList(myColorList);

            String oldString = textView.getText().toString();
            if (TextUtils.isEmpty(oldString)) {
                textView.setText(message);
            } else {
                String newString = message + "\n" + oldString;
                textView.setText(newString);
                System.out.println(message);
            }
        });
    }

    public String printData(String dataName, byte[] data) {
        int dataLength;
        String dataString = "";
        if (data == null) {
            dataLength = 0;
            dataString = "IS NULL";
        } else {
            dataLength = data.length;
            dataString = Utils.bytesToHex(data);
        }
        StringBuilder sb = new StringBuilder();
        sb
                .append(dataName)
                .append(" length: ")
                .append(dataLength)
                .append(" data: ")
                .append(dataString);
        return sb.toString();
    }

    private void clearOutputFields() {
        output.setText("");
        errorCode.setText("");
        // reset the border color to primary for errorCode
        int color = R.color.colorPrimary;
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_focused}, // focused
                new int[]{android.R.attr.state_hovered}, // hovered
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{}  //
        };
        int[] colors = new int[]{
                color,
                color,
                color,
                color
        };
        ColorStateList myColorList = new ColorStateList(states, colors);
        errorCodeLayout.setBoxStrokeColorStateList(myColorList);
    }

    /**
     * section for options menu
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        MenuItem mApplications = menu.findItem(R.id.action_applications);
        mApplications.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                allLayoutsInvisible();
                llApplicationHandling.setVisibility(View.VISIBLE);
                return false;
            }
        });

        MenuItem mStandardFile = menu.findItem(R.id.action_standard_file);
        mStandardFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                allLayoutsInvisible();
                llStandardFile.setVisibility(View.VISIBLE);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}