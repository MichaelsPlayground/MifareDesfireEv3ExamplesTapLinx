package de.androidcrypto.taplinxexample;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
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
import com.nxp.nfclib.desfire.DESFireEV3FileSettingsHelper;
import com.nxp.nfclib.desfire.DESFireFactory;
import com.nxp.nfclib.desfire.DESFireFile;
import com.nxp.nfclib.desfire.EV3ApplicationKeySettings;
import com.nxp.nfclib.desfire.EV3CPICCConfigurationSettings;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
    //private Button changeMasterKeyToAes;

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
    private DESFireEV3File.EV3FileSettings selectedFileSettings;
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
    private Button fileRecordCreate, fileRecordWrite, fileRecordWriteTimestamp, fileRecordRead;
    private RadioButton rbLinearRecordFile, rbCyclicRecordFile;
    RadioButton rbFileRecordPlainCommunication, rbFileRecordMacedCommunication, rbFileRecordEncryptedCommunication;
    private com.shawnlin.numberpicker.NumberPicker npRecordFileId;
    private com.google.android.material.textfield.TextInputEditText fileRecordSize, fileRecordData, fileRecordNumberOfRecords;

    /**
     * section for authentication
     */

    /**
     * section for authentication
     */

    private Button authDM0D, authD0D, authD1D, authD2D, authD3D, authD4D; // auth with default DES keys
    private Button authDM0A, authD0A, authD1A, authD2A, authD3A, authD4A; // auth with default AES keys
    private Button authDM0DC, authD0DC, authD1DC, authD2DC, authD3DC, authD4DC; // auth with changed DES keys
    private Button authDM0AC, authD0AC, authD1AC, authD2AC, authD3AC, authD4AC; // auth with changed AES keys

    private Button authEv2D1A; // EV2 auth with default AES keys

    private Button authCheckAllKeysD, authCheckAllKeysA; // check all auth keys (default and changed) for DES and AES

    /**
     * section for key handling
     */

    private Button changeKeyDM0D, changeKeyD0D, changeKeyD1D, changeKeyD2D, changeKeyD3D, changeKeyD4D;
    private Button changeKeyDM0A, changeKeyD0A, changeKeyD1A, changeKeyD2A, changeKeyD3A, changeKeyD4A;
    private Button changeKeyDM0DC, changeKeyD0DC, changeKeyD1DC, changeKeyD2DC, changeKeyD3DC, changeKeyD4DC;
    private Button changeKeyDM0AC, changeKeyD0AC, changeKeyD1AC, changeKeyD2AC, changeKeyD3AC, changeKeyD4AC;

    // change all keys from DEFAULT to CHANGED
    private Button changeAllKeysWithDefaultMasterKeyD, changeAllKeysWithDefaultMasterKeyA;
    private Button changeAllKeysWithChangedMasterKeyD, changeAllKeysWithChangedMasterKeyA;

    // change all keys from CHANGED to DEFAULT
    private Button changeAllKeysWithDefaultMasterKeyDC, changeAllKeysWithDefaultMasterKeyAC;
    private Button changeAllKeysWithChangedMasterKeyDC, changeAllKeysWithChangedMasterKeyAC;

    // testing
    private Button createApplication1;
    private Button transactionTimer;

    // playing with secure data message
    private Button createNdef, createNdefSdm;
    private Button formatNdefT4T, sdmIsEnabled, sdmEnable;
    private Button changeFileSettingsToSdm;
    private Button sdmGetFileSettings, sdmCompleteFormat;

    // constants
    private String lineSeparator = "----------";

    private final byte[] MASTER_APPLICATION_IDENTIFIER = new byte[3]; // '00 00 00'
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

    private final byte[] VIRTUAL_CARD_CONFIG_KEY_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] VIRTUAL_CARD_KEY_CONFIG = Utils.hexStringToByteArray("20200000000000000000000000000000");
    private final int VIRTUAL_CARD_CONFIG_KEY_NUMBER_INT = 32; // 0x20
    private final byte VIRTUAL_CARD_CONFIG_KEY_VERSION = (byte) 0x20; // dec 32
    private final byte[] VIRTUAL_CARD_PROXIMITY_KEY_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] VIRTUAL_CARD_KEY_PROXIMITY = Utils.hexStringToByteArray("20200000000000000000000000000000");
    private final int VIRTUAL_CARD_PROXIMITY_KEY_NUMBER_INT = 33; // 0x21
    private final byte VIRTUAL_CARD_PROXIMITY_KEY_VERSION = (byte) 0x21; // dec 33


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
        //changeMasterKeyToAes = findViewById(R.id.btnChangeMasterKeyToAes);
        createNdef = findViewById(R.id.btnCreateNdef); // creates a ndef file/container
        createNdefSdm = findViewById(R.id.btnCreateNdefSdm); // creates a ndef file/container

        formatNdefT4T = findViewById(R.id.btnFormatT4T);
        sdmIsEnabled = findViewById(R.id.btnSdmIsEnabled);
        sdmEnable = findViewById(R.id.btnSdmEnable);
        changeFileSettingsToSdm = findViewById(R.id.btnChangeFileSettingsToSdm);

        sdmGetFileSettings = findViewById(R.id.btnSdmGetFileSettings);
        sdmCompleteFormat = findViewById(R.id.btnSdmCompleteFormat);


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
        fileRecordWriteTimestamp = findViewById(R.id.btnWriteRecordFileTimestamp);
        npRecordFileId = findViewById(R.id.npRecordFileId);
        rbFileRecordPlainCommunication = findViewById(R.id.rbFileRecordPlainCommunication);
        rbFileRecordMacedCommunication = findViewById(R.id.rbFileRecordMacedCommunication);
        rbFileRecordEncryptedCommunication = findViewById(R.id.rbFileRecordEncryptedCommunication);
        fileRecordSize = findViewById(R.id.etRecordFileSize);
        fileRecordNumberOfRecords = findViewById(R.id.etRecordFileNumberRecords);
        fileRecordData = findViewById(R.id.etRecordFileData);
        rbLinearRecordFile = findViewById(R.id.rbLinearRecordFile);
        rbCyclicRecordFile = findViewById(R.id.rbCyclicRecordFile);

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

        // authentication handlung AES default keys using EV2Auth
        authEv2D1A = findViewById(R.id.btnAuthEv2D1A);

        // check all auth keys
        authCheckAllKeysD = findViewById(R.id.btnCheckAllKeysD);
        authCheckAllKeysA = findViewById(R.id.btnCheckAllKeysA);

        // change keys handling DES from DEFAULT to CHANGED
        changeKeyDM0D = findViewById(R.id.btnChangeKeyDM0D);
        changeKeyD0D = findViewById(R.id.btnChangeKeyD0D);
        changeKeyD1D = findViewById(R.id.btnChangeKeyD1D);
        changeKeyD2D = findViewById(R.id.btnChangeKeyD2D);
        changeKeyD3D = findViewById(R.id.btnChangeKeyD3D);
        changeKeyD4D = findViewById(R.id.btnChangeKeyD4D);

        // change keys handling AES from CHANGED to DEFAULT
        changeKeyDM0A = findViewById(R.id.btnChangeKeyDM0A);
        changeKeyD0A = findViewById(R.id.btnChangeKeyD0A);
        changeKeyD1A = findViewById(R.id.btnChangeKeyD1A);
        changeKeyD2A = findViewById(R.id.btnChangeKeyD2A);
        changeKeyD3A = findViewById(R.id.btnChangeKeyD3A);
        changeKeyD4A = findViewById(R.id.btnChangeKeyD4A);

        // change keys handling DES from CHANGED to DEFAULT
        changeKeyDM0DC = findViewById(R.id.btnChangeKeyDM0DC);
        changeKeyD0DC = findViewById(R.id.btnChangeKeyD0DC);
        changeKeyD1DC = findViewById(R.id.btnChangeKeyD1DC);
        changeKeyD2DC = findViewById(R.id.btnChangeKeyD2DC);
        changeKeyD3DC = findViewById(R.id.btnChangeKeyD3DC);
        changeKeyD4DC = findViewById(R.id.btnChangeKeyD4DC);

        // change keys handling AES from CHANGED to DEFAULT
        changeKeyDM0AC = findViewById(R.id.btnChangeKeyDM0AC);
        changeKeyD0AC = findViewById(R.id.btnChangeKeyD0AC);
        changeKeyD1AC = findViewById(R.id.btnChangeKeyD1AC);
        changeKeyD2AC = findViewById(R.id.btnChangeKeyD2AC);
        changeKeyD3AC = findViewById(R.id.btnChangeKeyD3AC);
        changeKeyD4AC = findViewById(R.id.btnChangeKeyD4AC);

        // change all application keys DES from Default to Changed with Default Master Key
        changeAllKeysWithDefaultMasterKeyD = findViewById(R.id.btnChangeKeysAllMasterDefaultD);
        // change all application keys AES from Default to Changed with Default Master Key
        changeAllKeysWithDefaultMasterKeyA = findViewById(R.id.btnChangeKeysAllMasterDefaultA);
        // change all application keys DES from Changed to Default with Default Master Key
        changeAllKeysWithDefaultMasterKeyDC = findViewById(R.id.btnChangeKeysAllMasterDefaultDC);
        // change all application keys AES from Changed to Default with Default Master Key
        changeAllKeysWithDefaultMasterKeyAC = findViewById(R.id.btnChangeKeysAllMasterDefaultAC);

        // change all application keys DES from Default to Changed with Changed Master Key
        changeAllKeysWithChangedMasterKeyD = findViewById(R.id.btnChangeKeysAllMasterChangedD);
        // change all application keys AES from Default to Changed with Changed Master Key
        changeAllKeysWithChangedMasterKeyA = findViewById(R.id.btnChangeKeysAllMasterChangedA);
        // change all application keys DES from Changed to Default with Changed Master Key
        changeAllKeysWithChangedMasterKeyDC = findViewById(R.id.btnChangeKeysAllMasterChangedDC);
        // change all application keys AES from Changed to Default with Changed Master Key
        changeAllKeysWithChangedMasterKeyAC = findViewById(R.id.btnChangeKeysAllMasterChangedAC);


        // testing
        transactionTimer = findViewById(R.id.btnTransactionTimer);


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
                    vibrateShort();
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
                String logString = "select an application";
                writeToUiAppend(output, logString);
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
                            vibrateShort();
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
         * section for files
         */

        fileSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get all files within a selected applications and show them in a listview for selection
                clearOutputFields();
                String logString = "select a file";
                writeToUiAppend(output, logString);
                // check that an application is already selected
                if (selectedApplicationId == null) {
                    writeToUiAppend(output, "you need to select an application first, aborted");
                    return;
                }
                byte[] fileIds = getFileIds(logString);
                if (fileIds == null) {
                    writeToUiAppend(output, "the getFileIds returned null");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "getFileIds FAILURE, aborted", COLOR_RED);
                    return;
                }
                if (fileIds.length == 0) {
                    writeToUiAppend(output, "The getFileIds returned no files");
                    return;
                }
                List<Byte> fileIdList = new ArrayList<>();
                List<DESFireEV3File.EV3FileSettings> fileSettingsList = new ArrayList<>();
                for (int i = 0; i < fileIds.length; i++) {
                    fileIdList.add(fileIds[i]);
                    fileSettingsList.add(getFileSettings(logString, (int) fileIds[i]));
                }
                for (int i = 0; i < fileIdList.size(); i++) {
                    writeToUiAppend(output, "entry " + i + " file id : " + fileIdList.get(i) + (" (") + Utils.byteToHex(fileIdList.get(i)) + ")");
                }
                String[] fileList = new String[fileIdList.size()];
                for (int i = 0; i < fileIdList.size(); i++) {
                    //fileList[i] = Utils.byteToHex(fileIdList.get(i));
                    fileList[i] = String.valueOf((int) fileIdList.get(i))
                            + " (" + fileSettingsList.get(i).getType().toString() + ")";
                }
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Choose a file");
                builder.setItems(fileList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        writeToUiAppend(output, "you  selected nr " + which + " = " + fileList[which]);
                        selectedFileId = String.valueOf((int) fileIdList.get(which));
                        selectedFileIdInt = Integer.parseInt(selectedFileId);
                        selectedFileSettings = fileSettingsList.get(which);
                        String type = selectedFileSettings.getType().toString();
                        String comm = selectedFileSettings.getComSettings().toString();
                        int accessRW = (int) selectedFileSettings.getReadWriteAccess();
                        int accessCar = (int) selectedFileSettings.getChangeAccess();
                        int accessR = (int) selectedFileSettings.getReadAccess();
                        int accessW = (int) selectedFileSettings.getWriteAccess();
                        StringBuilder sb = new StringBuilder();
                        sb.append("selectedFileId: ").append(selectedFileId).append("\n");
                        sb.append(lineSeparator).append("\n");
                        sb.append("file type: ").append(type).append("\n");
                        sb.append("comm type: ").append(comm).append("\n");
                        sb.append("key for RW access:  ").append(accessRW).append("\n");
                        sb.append("key for CAR access: ").append(accessCar).append("\n");
                        sb.append("key for R access:   ").append(accessR).append("\n");
                        sb.append("key for W access:   ").append(accessW).append("\n");
                        writeToUiAppend(output, sb.toString());
                        fileSelected.setText(selectedFileId + " (" + type + ")");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "file selected", COLOR_GREEN);
                        vibrateShort();
                    }
                });
                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        /**
         * section for standard & backup files
         */

        fileStandardCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "create a standard or backup file";
                writeToUiAppend(output, logString);

                byte fileIdByte = (byte) (npStandardFileId.getValue() & 0xFF);
                int fileIdInt = npStandardFileId.getValue();

                // the number of files on an EV1 tag is limited to 32 (00..31), but we are using the limit for the old D40 tag with a maximum of 15 files (00..14)
                // this limit is hardcoded in the XML file for the fileId numberPicker

                //byte fileIdByte = Byte.parseByte(fileId.getText().toString());
                int fileSizeInt = Integer.parseInt(fileSize.getText().toString());
                if (fileIdByte > (byte) 0x0f) {
                    // this should not happen as the limit is hardcoded in npFileId
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file ID", COLOR_RED);
                    return;
                }
                // communication setting choice
                IDESFireEV1.CommunicationType comSettings;
                if (rbFileStandardPlainCommunication.isChecked()) {
                    comSettings = IDESFireEV1.CommunicationType.Plain;
                } else if (rbFileStandardMacedCommunication.isChecked()) {
                    comSettings = IDESFireEV1.CommunicationType.MACed;
                } else {
                    comSettings = IDESFireEV1.CommunicationType.Enciphered;
                }
                boolean isStandardFile = rbStandardFile.isChecked(); // as there are 2 options only we just just check rbStandardFile
                boolean success = createAStandardBackupFile(logString, isStandardFile, fileIdInt, comSettings, 1, 2, 3, 4, fileSizeInt);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                    return;
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                    return;
                }
            }
        });

        fileStandardRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "read from a standard or backup file";
                writeToUiAppend(output, logString);
                // check that the selected file is a standard or backup file
                DESFireEV3File.EV3FileType fileType = selectedFileSettings.getType();
                if ((fileType == DESFireEV3File.EV3FileType.DataStandard) || (fileType == DESFireEV3File.EV3FileType.DataBackup)) {
                    // everything is ok, do nothing
                } else {
                    writeToUiAppend(output, "the selected fileId is not of type Standard or Backup file, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                    return;
                }
                byte[] data = readFromStandardBackupFile(logString);
                if (data == null) {
                    writeToUiAppend(output, logString + " FAILURE");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                    return;
                }
                writeToUiAppend(output, "data read: " + Utilities.byteToHexString(data));
                writeToUiAppend(output, lineSeparator);
                writeToUiAppend(output, "data read: " + new String(data, StandardCharsets.UTF_8));
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                vibrateShort();
            }
        });

        fileStandardWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "write to a standard or backup file";
                writeToUiAppend(output, logString);
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                String dataToWriteString = fileData.getText().toString();
                if (TextUtils.isEmpty(dataToWriteString)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "please enter some data to write", COLOR_RED);
                    return;
                }
                // check that the selected file is a standard or backup file
                DESFireEV3File.StdEV3DataFileSettings stdEV3DataFileSettings;
                DESFireEV3File.BackupEV3DataFileSettings backupEV3DataFileSettings;
                int fileSize = 0;
                boolean isBackupFile = false; // backup files require a commit after writing
                DESFireEV3File.EV3FileType fileType = selectedFileSettings.getType();
                if ((fileType == DESFireEV3File.EV3FileType.DataStandard) || (fileType == DESFireEV3File.EV3FileType.DataBackup)) {
                    // everything is ok, get the file size
                    if (fileType == DESFireEV3File.EV3FileType.DataStandard) {
                        stdEV3DataFileSettings = (DESFireEV3File.StdEV3DataFileSettings) selectedFileSettings;
                        fileSize = stdEV3DataFileSettings.getFileSize();
                    } else {
                        backupEV3DataFileSettings = (DESFireEV3File.BackupEV3DataFileSettings) selectedFileSettings;
                        fileSize = backupEV3DataFileSettings.getFileSize();
                        isBackupFile = true;
                    }
                } else {
                    writeToUiAppend(output, "the selected fileId is not of type Standard or Backup file, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                    return;
                }

                // get a random payload with 32 bytes
                UUID uuid = UUID.randomUUID(); // this is 36 characters long
                //byte[] dataToWrite = Arrays.copyOf(uuid.toString().getBytes(StandardCharsets.UTF_8), 32); // this 32 bytes long

                // create an empty array and copy the dataToWrite to clear the complete standard file
                byte[] fullDataToWrite = new byte[fileSize];
                // limit the string
                if (dataToWriteString.length() > fileSize)
                    dataToWriteString = dataToWriteString.substring(0, fileSize);
                byte[] dataToWrite = dataToWriteString.getBytes(StandardCharsets.UTF_8);
                System.arraycopy(dataToWrite, 0, fullDataToWrite, 0, dataToWrite.length);
                Log.d(TAG, logString + " fullDataToWrite: " + Utilities.byteToHexString(fullDataToWrite));
                boolean success = writeToStandardBackupFile(logString, fullDataToWrite);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    if (!isBackupFile) {
                        // finish the operation
                        vibrateShort();
                        return;
                    }
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                    return;
                }
                // as it is a Backup file type we need to submit a COMMIT
                boolean successCommit = commitATransaction(logString);
                if (successCommit) {
                    writeToUiAppend(output, "COMMIT " + logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "COMMIT " + logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                    return;
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "COMMIT " + logString + " NO SUCCESS", COLOR_RED);
                    return;
                }
            }
        });

        /**
         * section for value files
         */

        fileValueCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "create a value file";
                writeToUiAppend(output, logString);

                byte fileIdByte = (byte) (npValueFileId.getValue() & 0xFF);
                int fileIdInt = npValueFileId.getValue();

                // the number of files on an EV1 tag is limited to 32 (00..31), but we are using the limit for the old D40 tag with a maximum of 15 files (00..14)
                // this limit is hardcoded in the XML file for the fileId numberPicker

                //byte fileIdByte = Byte.parseByte(fileId.getText().toString());
                int lowerLimitInt = Integer.parseInt(lowerLimitValue.getText().toString());
                int upperLimitInt = Integer.parseInt(upperLimitValue.getText().toString());
                int initialValueInt = Integer.parseInt(initialValueValue.getText().toString());

                if (fileIdByte > (byte) 0x0f) {
                    // this should not happen as the limit is hardcoded in npStandardFileId
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file ID", COLOR_RED);
                    return;
                }
                // communication setting choice
                IDESFireEV1.CommunicationType comSettings;
                if (rbFileStandardPlainCommunication.isChecked()) {
                    comSettings = IDESFireEV1.CommunicationType.Plain;
                } else if (rbFileStandardMacedCommunication.isChecked()) {
                    comSettings = IDESFireEV1.CommunicationType.MACed;
                } else {
                    comSettings = IDESFireEV1.CommunicationType.Enciphered;
                }
                PayloadBuilder pb = new PayloadBuilder();
                if ((lowerLimitInt < pb.getMINIMUM_VALUE_LOWER_LIMIT()) || (lowerLimitInt > pb.getMAXIMUM_VALUE_LOWER_LIMIT())) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong lower limit, maximum 1000 allowed only", COLOR_RED);
                    return;
                }
                if ((upperLimitInt < pb.getMINIMUM_VALUE_UPPER_LIMIT()) || (upperLimitInt > pb.getMAXIMUM_VALUE_UPPER_LIMIT())) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong upper limit, maximum 1000 allowed only", COLOR_RED);
                    return;
                }
                if (upperLimitInt <= lowerLimitInt) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong upper limit, should be higher than lower limit", COLOR_RED);
                    return;
                }
                if ((initialValueInt < pb.getMINIMUM_VALUE_LOWER_LIMIT()) || (initialValueInt > pb.getMAXIMUM_VALUE_UPPER_LIMIT())) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong initial value, should be between lower and higher limit", COLOR_RED);
                    return;
                }
                boolean success = createAValueFile(logString, fileIdInt, comSettings, 1, 2, 3, 4, lowerLimitInt, upperLimitInt, initialValueInt);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                    return;
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                    return;
                }
            }
        });

        fileValueRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "read from a value file";
                writeToUiAppend(output, logString);
                if (TextUtils.isEmpty(selectedFileId)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                // check that the selected file is a value file
                DESFireEV3File.EV3FileType fileType = selectedFileSettings.getType();
                if (fileType == DESFireEV3File.EV3FileType.Value) {
                    // everything is ok, do nothing
                } else {
                    writeToUiAppend(output, "the selected fileId is not of type Value file, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                    return;
                }
                int data = readFromAValueFile((logString));
                if (data < 0) {
                    writeToUiAppend(output, logString + " FAILURE");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                    return;
                }
                writeToUiAppend(output, "data read: " + data);
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                vibrateShort();
            }
        });

        fileValueCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "credit a value file";
                writeToUiAppend(output, logString);
                if (TextUtils.isEmpty(selectedFileId)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                // check that the selected file is a value file
                DESFireEV3File.EV3FileType fileType = selectedFileSettings.getType();
                if (fileType == DESFireEV3File.EV3FileType.Value) {
                    // everything is ok, do nothing
                } else {
                    writeToUiAppend(output, "the selected fileId is not of type Value file, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                    return;
                }
                int changeValueInt = Integer.parseInt(creditDebitValue.getText().toString());
                PayloadBuilder pb = new PayloadBuilder();
                if ((changeValueInt < 1) || (changeValueInt > pb.getMAXIMUM_VALUE_UPPER_LIMIT())) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong change value, should be between lower and higher limit", COLOR_RED);
                    return;
                }
                writeToUiAppend(output, "credit amount: " + changeValueInt);
                boolean success = creditAValueFile(logString, changeValueInt);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                    return;
                }
                boolean successCommit = commitATransaction(logString);
                if (successCommit) {
                    writeToUiAppend(output, "COMMIT " + logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                    return;
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "COMMIT " + logString + " NO SUCCESS", COLOR_RED);
                    return;
                }
            }
        });

        fileValueDebit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "debit a value file";
                writeToUiAppend(output, logString);
                if (TextUtils.isEmpty(selectedFileId)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                // check that the selected file is a value file
                DESFireEV3File.EV3FileType fileType = selectedFileSettings.getType();
                if (fileType == DESFireEV3File.EV3FileType.Value) {
                    // everything is ok, do nothing
                } else {
                    writeToUiAppend(output, "the selected fileId is not of type Value file, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                    return;
                }
                int changeValueInt = Integer.parseInt(creditDebitValue.getText().toString());
                PayloadBuilder pb = new PayloadBuilder();
                if ((changeValueInt < 1) || (changeValueInt > pb.getMAXIMUM_VALUE_UPPER_LIMIT())) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong change value, should be between lower and higher limit", COLOR_RED);
                    return;
                }
                writeToUiAppend(output, "debit amount: " + changeValueInt);
                boolean success = debitAValueFile(logString, changeValueInt);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                    return;
                }
                boolean successCommit = commitATransaction(logString);
                if (successCommit) {
                    writeToUiAppend(output, "COMMIT " + logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                    return;
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "COMMIT " + logString + " NO SUCCESS", COLOR_RED);
                    return;
                }
            }
        });

        /**
         * section for linear & cyclic record files
         */

        fileRecordCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "create a linear or cyclic record file";
                writeToUiAppend(output, logString);

                byte fileIdByte = (byte) (npRecordFileId.getValue() & 0xFF);
                int fileIdInt = npRecordFileId.getValue();

                // the number of files on an EV1 tag is limited to 32 (00..31), but we are using the limit for the old D40 tag with a maximum of 15 files (00..14)
                // this limit is hardcoded in the XML file for the fileId numberPicker

                int fileSizeInt = Integer.parseInt(fileRecordSize.getText().toString());
                if (fileSizeInt == 0) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a 0 size (minimum 1)", COLOR_RED);
                    return;
                }
                if (fileIdByte > (byte) 0x0f) {
                    // this should not happen as the limit is hardcoded in npFileId
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file ID", COLOR_RED);
                    return;
                }
                int fileNumberOfRecordsInt = Integer.parseInt(fileRecordNumberOfRecords.getText().toString());
                if (fileNumberOfRecordsInt < 2) {
                    // this should not happen as the limit is hardcoded in npFNumberOfRecords
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a 0 record number (minimum 2)", COLOR_RED);
                    return;
                }
                // get the type of file - linear or cyclic
                boolean isLinearRecordFile = rbLinearRecordFile.isChecked();

                String fileTypeString = "";
                if (isLinearRecordFile) {
                    fileTypeString = "Linear Record File";
                } else {
                    fileTypeString = "Cyclic Record File";
                }

                // communication setting choice
                IDESFireEV1.CommunicationType comSettings;
                if (rbFileStandardPlainCommunication.isChecked()) {
                    comSettings = IDESFireEV1.CommunicationType.Plain;
                } else if (rbFileStandardMacedCommunication.isChecked()) {
                    comSettings = IDESFireEV1.CommunicationType.MACed;
                } else {
                    comSettings = IDESFireEV1.CommunicationType.Enciphered;
                }
                writeToUiAppend(output, "trying to create a " + fileTypeString);
                boolean success = createARecordFile(logString, isLinearRecordFile, fileIdInt, comSettings, 1, 2, 3, 4, fileSizeInt, fileNumberOfRecordsInt, 0);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                    return;
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                    return;
                }
            }
        });

        fileRecordRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "read from a linear or cyclic record file";
                writeToUiAppend(output, logString);
                // check that the selected file is a linear or cyclic record file
                DESFireEV3File.EV3FileType fileType = selectedFileSettings.getType();
                int recordSize = 0;
                int maxRecords = 0;
                int currentRecords = 0;
                String fileTypeName;
                if ((fileType == DESFireEV3File.EV3FileType.RecordLinear) || (fileType == DESFireEV3File.EV3FileType.RecordCyclic)) {
                    // everything is ok, do nothing
                    if (fileType == DESFireEV3File.EV3FileType.RecordLinear) {
                        recordSize = ((DESFireEV3File.LinearRecordFileSettings) selectedFileSettings).getRecordSize();
                        maxRecords = ((DESFireEV3File.LinearRecordFileSettings) selectedFileSettings).getMaxNumberOfRecords();
                        currentRecords = ((DESFireEV3File.LinearRecordFileSettings) selectedFileSettings).getCurrentNumberOfRecords();
                        fileTypeName = selectedFileSettings.getType().toString();

                    } else {
                        recordSize = ((DESFireEV3File.CyclicRecordFileSettings) selectedFileSettings).getRecordSize();
                        maxRecords = ((DESFireEV3File.CyclicRecordFileSettings) selectedFileSettings).getMaxNumberOfRecords();
                        currentRecords = ((DESFireEV3File.CyclicRecordFileSettings) selectedFileSettings).getCurrentNumberOfRecords();
                        fileTypeName = selectedFileSettings.getType().toString();
                    }
                } else {
                    writeToUiAppend(output, "the selected fileId is not of type Linear or Cyclic Record file, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                    return;
                }
                writeToUiAppend(output, "fileType: " + fileTypeName + " recordSize: " + recordSize + " currentRecords: " + currentRecords + " maxRecords: " + maxRecords);
                if (currentRecords == 0) {
                    writeToUiAppend(output, "the are no records to write, consider writing some records");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "nothing to do", COLOR_GREEN);
                    return;
                }
                byte[] data = readFromARecordFile(logString, 0, currentRecords);
                if (data == null) {
                    writeToUiAppend(output, logString + " FAILURE");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                    return;
                }
                // need to separate the data into single records based on recordSize
                List<byte[]> readRecordList = Ev3.divideArray(data, recordSize);
                int listSize = readRecordList.size();
                writeToUiAppend(output, lineSeparator);
                for (int i = 0; i < listSize; i++) {
                    byte[] record = readRecordList.get(i);
                    writeToUiAppend(output, "record " + i + printData(" data", record));
                    if (record != null) {
                        writeToUiAppend(output, new String(record, StandardCharsets.UTF_8));
                    }
                    writeToUiAppend(output, lineSeparator);
                }
                writeToUiAppend(output, "finished");
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                vibrateShort();
            }
        });

        fileRecordWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "write to a linear or record file";
                writeToUiAppend(output, logString);
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                String dataToWriteString = fileData.getText().toString();
                if (TextUtils.isEmpty(dataToWriteString)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "please enter some data to write", COLOR_RED);
                    return;
                }
                // check that the selected file is a linear or cyclic records file
                // check that the selected file is a linear or cyclic record file
                DESFireEV3File.EV3FileType fileType = selectedFileSettings.getType();
                int recordSize = 0;
                int maxRecords = 0;
                int currentRecords = 0;
                String fileTypeName;
                if ((fileType == DESFireEV3File.EV3FileType.RecordLinear) || (fileType == DESFireEV3File.EV3FileType.RecordCyclic)) {
                    // everything is ok, do nothing
                    if (fileType == DESFireEV3File.EV3FileType.RecordLinear) {
                        recordSize = ((DESFireEV3File.LinearRecordFileSettings) selectedFileSettings).getRecordSize();
                        maxRecords = ((DESFireEV3File.LinearRecordFileSettings) selectedFileSettings).getMaxNumberOfRecords();
                        currentRecords = ((DESFireEV3File.LinearRecordFileSettings) selectedFileSettings).getCurrentNumberOfRecords();
                        fileTypeName = selectedFileSettings.getType().toString();
                    } else {
                        recordSize = ((DESFireEV3File.CyclicRecordFileSettings) selectedFileSettings).getRecordSize();
                        maxRecords = ((DESFireEV3File.CyclicRecordFileSettings) selectedFileSettings).getMaxNumberOfRecords();
                        currentRecords = ((DESFireEV3File.CyclicRecordFileSettings) selectedFileSettings).getCurrentNumberOfRecords();
                        fileTypeName = selectedFileSettings.getType().toString();
                    }
                } else {
                    writeToUiAppend(output, "the selected fileId is not of type Linear or Cyclic Record file, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                    return;
                }
                writeToUiAppend(output, "fileType: " + fileTypeName + " recordSize: " + recordSize + " currentRecords: " + currentRecords + " maxRecords: " + maxRecords);
                // create an empty array and copy the dataToWrite to clear the complete standard file
                byte[] fullDataToWrite = new byte[recordSize];
                fullDataToWrite = Utils.generateTestData(recordSize);
                //System.arraycopy(dataToWrite, 0, fullDataToWrite, 0, dataToWrite.length);
                writeToUiAppend(output, printData("fullDataToWrite", fullDataToWrite));
                boolean success = writeToARecordFile(logString, fullDataToWrite);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                    return;
                }
                // as it is a Record file type we need to submit a COMMIT
                boolean successCommit = commitATransaction(logString);
                if (successCommit) {
                    writeToUiAppend(output, "COMMIT " + logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "COMMIT " + logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                    return;
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "COMMIT " + logString + " NO SUCCESS", COLOR_RED);
                    return;
                }
            }
        });

        fileRecordWriteTimestamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "write a timestamp to a linear or record file";
                writeToUiAppend(output, logString);
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                String dataToWriteString = fileData.getText().toString();
                if (TextUtils.isEmpty(dataToWriteString)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "please enter some data to write", COLOR_RED);
                    return;
                }
                // check that the selected file is a linear or cyclic records file
                // check that the selected file is a linear or cyclic record file
                DESFireEV3File.EV3FileType fileType = selectedFileSettings.getType();
                int recordSize = 0;
                int maxRecords = 0;
                int currentRecords = 0;
                String fileTypeName;
                if ((fileType == DESFireEV3File.EV3FileType.RecordLinear) || (fileType == DESFireEV3File.EV3FileType.RecordCyclic)) {
                    // everything is ok, do nothing
                    if (fileType == DESFireEV3File.EV3FileType.RecordLinear) {
                        recordSize = ((DESFireEV3File.LinearRecordFileSettings) selectedFileSettings).getRecordSize();
                        maxRecords = ((DESFireEV3File.LinearRecordFileSettings) selectedFileSettings).getMaxNumberOfRecords();
                        currentRecords = ((DESFireEV3File.LinearRecordFileSettings) selectedFileSettings).getCurrentNumberOfRecords();
                        fileTypeName = selectedFileSettings.getType().toString();
                    } else {
                        recordSize = ((DESFireEV3File.CyclicRecordFileSettings) selectedFileSettings).getRecordSize();
                        maxRecords = ((DESFireEV3File.CyclicRecordFileSettings) selectedFileSettings).getMaxNumberOfRecords();
                        currentRecords = ((DESFireEV3File.CyclicRecordFileSettings) selectedFileSettings).getCurrentNumberOfRecords();
                        fileTypeName = selectedFileSettings.getType().toString();
                    }
                } else {
                    writeToUiAppend(output, "the selected fileId is not of type Linear or Cyclic Record file, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                    return;
                }
                writeToUiAppend(output, "fileType: " + fileTypeName + " recordSize: " + recordSize + " currentRecords: " + currentRecords + " maxRecords: " + maxRecords);
                /// limit the string
                dataToWriteString = Utils.getTimestamp() + " " + dataToWriteString;
                if (dataToWriteString.length() > recordSize)
                    dataToWriteString = dataToWriteString.substring(0, recordSize);
                byte[] dataToWrite = dataToWriteString.getBytes(StandardCharsets.UTF_8);
                byte[] fullDataToWrite = new byte[recordSize];
                System.arraycopy(dataToWrite, 0, fullDataToWrite, 0, dataToWrite.length);
                writeToUiAppend(output, printData("fullDataToWrite", fullDataToWrite));
                boolean success = writeToARecordFile(logString, fullDataToWrite);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                    return;
                }
                // as it is a Record file type we need to submit a COMMIT
                boolean successCommit = commitATransaction(logString);
                if (successCommit) {
                    writeToUiAppend(output, "COMMIT " + logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "COMMIT " + logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                    return;
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "COMMIT " + logString + " NO SUCCESS", COLOR_RED);
                    return;
                }
            }
        });

        /**
         * section for authentication
         */

        authDM0D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with DES Master Application Key
                clearOutputFields();
                String logString = "legacy authentication with DES DEFAULT Master Application Key";
                writeToUiAppend(output, logString);
                // check that the Master Application is selected
                if (!Arrays.equals(selectedApplicationId, MASTER_APPLICATION_IDENTIFIER)) {
                    writeToUiAppend(output, "you need to select the Master Application first, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Nothing to do", COLOR_GREEN);
                    return;
                }
                boolean success = legacyDesAuth(logString, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD0D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with DES Application Master Key
                clearOutputFields();
                String logString = "legacy authentication with DES DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = legacyDesAuth(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD1D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with DES Application Key 1 = read&write access key
                clearOutputFields();
                String logString = "legacy authentication with DES DEFAULT Application Key 1 = read&write access key";
                writeToUiAppend(output, logString);
                boolean success = legacyDesAuth(logString, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_DES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD2D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with DES Application Key 2 = change access rights key
                clearOutputFields();
                String logString = "legacy authentication with DES DEFAULT Application Key 2 = change access rights key";
                writeToUiAppend(output, logString);
                boolean success = legacyDesAuth(logString, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_DES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD3D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with DES Application Key 3 = read access key
                clearOutputFields();
                String logString = "legacy authentication with DES DEFAULT Application Key 3 = read access key";
                writeToUiAppend(output, logString);
                boolean success = legacyDesAuth(logString, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_DES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD4D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with DES Application Key 4 = write access key
                clearOutputFields();
                String logString = "legacy authentication with DES DEFAULT Application Key 4 = write access key";
                writeToUiAppend(output, logString);
                boolean success = legacyDesAuth(logString, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_DES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });


        authDM0A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Master Application Key
                clearOutputFields();
                String logString = "legacy authentication with AES DEFAULT Master Application Key";
                writeToUiAppend(output, logString);
                // check that the Master Application is selected
                if (!Arrays.equals(selectedApplicationId, MASTER_APPLICATION_IDENTIFIER)) {
                    writeToUiAppend(output, "you need to select the Master Application first, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Nothing to do", COLOR_GREEN);
                    return;
                }
                boolean success = legacyAesAuth(logString, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD0A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Key 0 = Application Master key
                clearOutputFields();
                String logString = "legacy authentication with AES DEFAULT Key 0 = Application Master key";
                writeToUiAppend(output, logString);
                boolean success = legacyAesAuth(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD1A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Key 1 = Read & Write Access key
                clearOutputFields();
                String logString = "legacy authentication with AES DEFAULT Key 1 = Read & Write Access key";
                writeToUiAppend(output, logString);
                boolean success = legacyAesAuth(logString, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD2A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Application Key 2 = change access rights key
                clearOutputFields();
                String logString = "legacy authentication with AES DEFAULT Application Key 2 = change access rights key";
                writeToUiAppend(output, logString);
                boolean success = legacyAesAuth(logString, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD3A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Application Key 3 = read access key
                clearOutputFields();
                String logString = "legacy authentication with AES DEFAULT Application Key 3 = read access key";
                writeToUiAppend(output, logString);
                boolean success = legacyAesAuth(logString, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD4A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Application Key 4 = write access key
                clearOutputFields();
                String logString = "legacy authentication with AES DEFAULT Application Key 4 = write access key";
                writeToUiAppend(output, logString);
                boolean success = legacyAesAuth(logString, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        /**
         * section for authentication with changed keys
         */

        authDM0DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with DES Master Application Key
                clearOutputFields();
                String logString = "legacy authentication with DES CHANGED Master Application Key";
                writeToUiAppend(output, logString);
                // check that the Master Application is selected
                if (!Arrays.equals(selectedApplicationId, MASTER_APPLICATION_IDENTIFIER)) {
                    writeToUiAppend(output, "you need to select the Master Application first, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Nothing to do", COLOR_GREEN);
                    return;
                }
                boolean success = legacyDesAuth(logString, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD0DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with DES Application Master Key
                clearOutputFields();
                String logString = "legacy authentication with DES CHANGED Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = legacyDesAuth(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD1DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with DES Application Key 1 = read&write access key
                clearOutputFields();
                String logString = "legacy authentication with DES CHANGED Application Key 1 = read&write access key";
                writeToUiAppend(output, logString);
                boolean success = legacyDesAuth(logString, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD2DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with DES Application Key 2 = change access rights key
                clearOutputFields();
                String logString = "legacy authentication with DES CHANGED Application Key 2 = change access rights key";
                writeToUiAppend(output, logString);
                boolean success = legacyDesAuth(logString, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD3DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with DES Application Key 3 = read access key
                clearOutputFields();
                String logString = "legacy authentication with DES CHANGED Application Key 3 = read access key";
                writeToUiAppend(output, logString);
                boolean success = legacyDesAuth(logString, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD4DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with DES Application Key 4 = write access key
                clearOutputFields();
                String logString = "legacy authentication with DES CHANGED Application Key 4 = write access key";
                writeToUiAppend(output, logString);
                boolean success = legacyDesAuth(logString, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });


        authDM0AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Master Application Key
                clearOutputFields();
                String logString = "legacy authentication with AES CHANGED Master Application Key";
                writeToUiAppend(output, logString);
                // check that the Master Application is selected
                if (!Arrays.equals(selectedApplicationId, MASTER_APPLICATION_IDENTIFIER)) {
                    writeToUiAppend(output, "you need to select the Master Application first, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Nothing to do", COLOR_GREEN);
                    return;
                }
                boolean success = legacyAesAuth(logString, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD0AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Application Master Key
                clearOutputFields();
                String logString = "legacy authentication with AES CHANGED Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = legacyAesAuth(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD1AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Key 1 = Read & Write Access key
                clearOutputFields();
                String logString = "legacy authentication with AES CHANGED Key 1 = Read & Write Access key";
                writeToUiAppend(output, logString);
                boolean success = legacyAesAuth(logString, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD2AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Application Key 2 = change access rights key
                clearOutputFields();
                String logString = "legacy authentication with AES CHANGED Application Key 2 = change access rights key";
                writeToUiAppend(output, logString);
                boolean success = legacyAesAuth(logString, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD3AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Application Key 3 = read access key
                clearOutputFields();
                String logString = "legacy authentication with AES CHANGED Application Key 3 = read access key";
                writeToUiAppend(output, logString);
                boolean success = legacyAesAuth(logString, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD4AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Application Key 4 = write access key
                clearOutputFields();
                String logString = "legacy authentication with AES CHANGED Application Key 4 = write access key";
                writeToUiAppend(output, logString);
                boolean success = legacyAesAuth(logString, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        /**
         * section for authentication with AES default keys using EV2Auth
         */

        authEv2D1A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // native authentication with AES Key 1 = Read & Write Access key
                clearOutputFields();
                String logString = "EV2 authentication with AES DEFAULT Key 1 = Read & Write Access key";
                writeToUiAppend(output, logString);
                //boolean success = legacyAesAuth(logString, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_AES_DEFAULT);
                boolean success = ev2AesAuth(logString, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });


        /**
         * section for checking all auth keys
         */

        authCheckAllKeysD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check the authentication with all access keys
                clearOutputFields();
                String logString = "check all DES authentication keys";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                boolean success0 = legacyDesAuth(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT);
                boolean success0C = false;
                if (!success0) {
                    success0C = legacyDesAuth(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES);
                }
                boolean success1 = legacyDesAuth(logString, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_DES_DEFAULT);
                boolean success1C = false;
                if (!success1) {
                    success1C = legacyDesAuth(logString, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_DES);
                }
                boolean success2 = legacyDesAuth(logString, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_DES_DEFAULT);
                boolean success2C = false;
                if (!success2) {
                    success2C = legacyDesAuth(logString, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_DES);
                }
                boolean success3 = legacyDesAuth(logString, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_DES_DEFAULT);
                boolean success3C = false;
                if (!success3) {
                    success3C = legacyDesAuth(logString, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_DES);
                }
                boolean success4 = legacyDesAuth(logString, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_DES_DEFAULT);
                boolean success4C = false;
                if (!success4) {
                    success4C = legacyDesAuth(logString, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_DES);
                }
                StringBuilder sb = new StringBuilder();
                sb.append("check of all DES auth keys:").append("\n");
                sb.append("key 0 master default: ").append(success0).append("\n");
                sb.append("key 0 master changed: ").append(success0C).append("\n");
                sb.append("key 1 read&write default: ").append(success1).append("\n");
                sb.append("key 1 read&write changed: ").append(success1C).append("\n");
                sb.append("key 2 CAR default: ").append(success2).append("\n");
                sb.append("key 2 CAR changed: ").append(success2C).append("\n");
                sb.append("key 3 read default: ").append(success3).append("\n");
                sb.append("key 3 read changed: ").append(success3C).append("\n");
                sb.append("key 4 write default: ").append(success4).append("\n");
                sb.append("key 4 write changed: ").append(success4C).append("\n");
                writeToUiAppend(output, sb.toString());
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " see above results", COLOR_GREEN);
                vibrateShort();
            }
        });

        authCheckAllKeysA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check the authentication with all access keys
                clearOutputFields();
                String logString = "check all AES authentication keys";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                boolean success0 = legacyAesAuth(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT);
                boolean success0C = false;
                if (!success0) {
                    success0C = legacyAesAuth(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES);
                }
                boolean success1 = legacyAesAuth(logString, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_AES_DEFAULT);
                boolean success1C = false;
                if (!success1) {
                    success1C = legacyAesAuth(logString, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_AES);
                }
                boolean success2 = legacyAesAuth(logString, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_AES_DEFAULT);
                boolean success2C = false;
                if (!success2) {
                    success2C = legacyAesAuth(logString, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_AES);
                }
                boolean success3 = legacyAesAuth(logString, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_AES_DEFAULT);
                boolean success3C = false;
                if (!success3) {
                    success3C = legacyAesAuth(logString, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_AES);
                }
                boolean success4 = legacyAesAuth(logString, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_AES_DEFAULT);
                boolean success4C = false;
                if (!success4) {
                    success4C = legacyAesAuth(logString, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_AES);
                }
                StringBuilder sb = new StringBuilder();
                sb.append("check of all AES auth keys:").append("\n");
                sb.append("key 0 master default: ").append(success0).append("\n");
                sb.append("key 0 master changed: ").append(success0C).append("\n");
                sb.append("key 1 read&write default: ").append(success1).append("\n");
                sb.append("key 1 read&write changed: ").append(success1C).append("\n");
                sb.append("key 2 CAR default: ").append(success2).append("\n");
                sb.append("key 2 CAR changed: ").append(success2C).append("\n");
                sb.append("key 3 read default: ").append(success3).append("\n");
                sb.append("key 3 read changed: ").append(success3C).append("\n");
                sb.append("key 4 write default: ").append(success4).append("\n");
                sb.append("key 4 write changed: ").append(success4C).append("\n");
                writeToUiAppend(output, sb.toString());
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " see above results", COLOR_GREEN);
                vibrateShort();
            }
        });

        /**
         * section for change key from DEFAULT to CHANGED
         */

        changeKeyDM0D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 0 = change DES Master Application Key from DEFAULT to CHANGED with DEFAULT Master Application Key";
                writeToUiAppend(output, logString);
                // check that the Master Application is selected
                if (!Arrays.equals(selectedApplicationId, MASTER_APPLICATION_IDENTIFIER)) {
                    writeToUiAppend(output, "you need to select the Master Application first, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Nothing to do", COLOR_GREEN);
                    return;
                }
                boolean success = changeKeyDes(logString, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES_DEFAULT, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES, MASTER_APPLICATION_KEY_DES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD0D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 0 = change DES Application Master Key from DEFAULT to CHANGED with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES, APPLICATION_KEY_MASTER_DES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD1D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 1 = change DES Read & Write Access Key from DEFAULT to CHANGED with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_DES, APPLICATION_KEY_RW_DES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD2D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 2 = change DES Change Access Rights Key from DEFAULT to CHANGED with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_DES, APPLICATION_KEY_CAR_DES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD3D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 3 = change DES Read Access Key from DEFAULT to CHANGED with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_DES, APPLICATION_KEY_R_DES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD4D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 4 = change DES Write Access Key from DEFAULT to CHANGED with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_DES, APPLICATION_KEY_W_DES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyDM0A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 0 = change AES Master Application Key from DEFAULT to CHANGED with DEFAULT Master Application Key";
                writeToUiAppend(output, logString);
                // check that the Master Application is selected
                if (!Arrays.equals(selectedApplicationId, MASTER_APPLICATION_IDENTIFIER)) {
                    writeToUiAppend(output, "you need to select the Master Application first, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Nothing to do", COLOR_GREEN);
                    return;
                }
                boolean success = changeKeyAes(logString, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_AES_DEFAULT, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_AES, MASTER_APPLICATION_KEY_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD0A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 0 = change AES Application Master Key from DEFAULT to CHANGED with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES, APPLICATION_KEY_MASTER_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD1A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 1 = change AES Read & Write Access Key from DEFAULT to CHANGED with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_AES, APPLICATION_KEY_RW_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD2A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 2 = change AES Change Access Rights Key from DEFAULT to CHANGED with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_AES, APPLICATION_KEY_CAR_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD3A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 3 = change AES Read Access Key from DEFAULT to CHANGED with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_AES, APPLICATION_KEY_R_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD4A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 4 = change AES Write Access Key from DEFAULT to CHANGED with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_AES, APPLICATION_KEY_W_AES_DEFAULT);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        /**
         * section for change key from CHANGED to DEFAULT
         */

        changeKeyDM0DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 0 = change DES Master Application Key from CHANGED to DEFAULT with DEFAULT Master Application Key";
                writeToUiAppend(output, logString);
                // check that the Master Application is selected
                if (!Arrays.equals(selectedApplicationId, MASTER_APPLICATION_IDENTIFIER)) {
                    writeToUiAppend(output, "you need to select the Master Application first, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Nothing to do", COLOR_GREEN);
                    return;
                }
                boolean success = changeKeyDes(logString, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES_DEFAULT, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES_DEFAULT, MASTER_APPLICATION_KEY_DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD0DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 0 = change DES Application Master Key from CHANGED to DEFAULT with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_MASTER_DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD1DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 1 = change DES Read & Write Access Key from CHANGED to DEFAULT with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_DES_DEFAULT, APPLICATION_KEY_RW_DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD2DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 2 = change DES Access Rights Key from CHANGED to DEFAULT with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_DES_DEFAULT, APPLICATION_KEY_CAR_DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD3DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 3 = change DES Read Access Key from CHANGED to DEFAULT with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_DES_DEFAULT, APPLICATION_KEY_R_DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD4DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 4 = change DES Write Access Key from CHANGED to DEFAULT with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_DES_DEFAULT, APPLICATION_KEY_W_DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyDM0AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 0 = change AES Master Application Key from CHANGED to DEFAULT with DEFAULT Master Application Key";
                writeToUiAppend(output, logString);
                // check that the Master Application is selected
                if (!Arrays.equals(selectedApplicationId, MASTER_APPLICATION_IDENTIFIER)) {
                    writeToUiAppend(output, "you need to select the Master Application first, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Nothing to do", COLOR_GREEN);
                    return;
                }
                boolean success = changeKeyAes(logString, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_AES_DEFAULT, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_AES_DEFAULT, MASTER_APPLICATION_KEY_AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD0AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 0 = change AES Application Master Key from CHANGED to DEFAULT with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_MASTER_AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD1AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 1 = change AES Read & Write Access Key from CHANGED to DEFAULT with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_AES_DEFAULT, APPLICATION_KEY_RW_AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD2AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 2 = change AES Access Rights Key from CHANGED to DEFAULT with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_AES_DEFAULT, APPLICATION_KEY_CAR_AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD3AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 3 = change AES Read Access Key from CHANGED to DEFAULT with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_AES_DEFAULT, APPLICATION_KEY_R_AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        changeKeyD4AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "change key 4 = change AES Write Access Key from CHANGED to DEFAULT with DEFAULT Application Master Key";
                writeToUiAppend(output, logString);
                boolean success = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_AES_DEFAULT, APPLICATION_KEY_W_AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        /**
         * section for changing all application keys from Default To Changed (personalization)
         * there are each 2 methods for DES and AES using the Default and Changed Application Master Key
         */

        changeAllKeysWithDefaultMasterKeyD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change all application keys to changed with default master application key
                clearOutputFields();
                String logString = "DES change all application keys to CHANGED with DEFAULT Master Key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                // change keys 1 to 4 first to CHANGED, authenticate with DEFAULT Application Master Key
                boolean success1 = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_DES, APPLICATION_KEY_RW_DES_DEFAULT);
                boolean success2 = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_DES, APPLICATION_KEY_CAR_DES_DEFAULT);
                boolean success3 = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_DES, APPLICATION_KEY_R_DES_DEFAULT);
                boolean success4 = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_DES, APPLICATION_KEY_W_DES_DEFAULT);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_RW_NUMBER + " result: " + success1);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_CAR_NUMBER + " result: " + success2);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_R_NUMBER + " result: " + success3);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_W_NUMBER + " result: " + success4);
                // proceed only when all changes are successfully
                if ((!success1) || (!success2) || (!success3) || (!success4)) {
                    writeToUiAppend(output, "not all key changes were successfully, change of Application Master Key aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "change of all application keys FAILURE", COLOR_RED);
                    return;
                }
                // now change the Application Master Key from DEFAULT to CHANGED
                boolean success0 = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES, APPLICATION_KEY_MASTER_DES_DEFAULT);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_MASTER_NUMBER + " result: " + success0);
                if (!success0) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "change of application master key FAILURE", COLOR_RED);
                    return;
                }
                writeToUiAppend(output, logString + " SUCCESS");
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                vibrateShort();
            }
        });

        changeAllKeysWithDefaultMasterKeyA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change all application keys to changed with default master application key
                clearOutputFields();
                String logString = "AES change all application keys to CHANGED with DEFAULT Master Key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                // change keys 1 to 4 first to CHANGED, authenticate with DEFAULT Application Master Key
                boolean success1 = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_AES, APPLICATION_KEY_RW_AES_DEFAULT);
                boolean success2 = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_AES, APPLICATION_KEY_CAR_AES_DEFAULT);
                boolean success3 = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_AES, APPLICATION_KEY_R_AES_DEFAULT);
                boolean success4 = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_AES, APPLICATION_KEY_W_AES_DEFAULT);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_RW_NUMBER + " result: " + success1);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_CAR_NUMBER + " result: " + success2);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_R_NUMBER + " result: " + success3);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_W_NUMBER + " result: " + success4);
                // proceed only when all changes are successfully
                if ((!success1) || (!success2) || (!success3) || (!success4)) {
                    writeToUiAppend(output, "not all key changes were successfully, change of Application Master Key aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "change of all application keys FAILURE", COLOR_RED);
                    return;
                }
                // now change the Application Master Key from DEFAULT to CHANGED
                boolean success0 = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES, APPLICATION_KEY_MASTER_AES_DEFAULT);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_MASTER_NUMBER + " result: " + success0);
                if (!success0) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "change of application master key FAILURE", COLOR_RED);
                    return;
                }
                writeToUiAppend(output, logString + " SUCCESS");
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                vibrateShort();
            }
        });

        changeAllKeysWithChangedMasterKeyDC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change all application keys to changed with default master application key
                clearOutputFields();
                String logString = "DES change all application keys to DEFAULT with CHANGED Master Key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                // change keys 1 to 4 first to DEFAULT, authenticate with CHANGED Application Master Key
                boolean success1 = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_DES_DEFAULT, APPLICATION_KEY_RW_DES);
                boolean success2 = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_DES_DEFAULT, APPLICATION_KEY_CAR_DES);
                boolean success3 = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_DES_DEFAULT, APPLICATION_KEY_R_DES);
                boolean success4 = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_DES_DEFAULT, APPLICATION_KEY_W_DES);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_RW_NUMBER + " result: " + success1);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_CAR_NUMBER + " result: " + success2);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_R_NUMBER + " result: " + success3);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_W_NUMBER + " result: " + success4);
                // proceed only when all changes are successfully
                if ((!success1) || (!success2) || (!success3) || (!success4)) {
                    writeToUiAppend(output, "not all key changes were successfully, change of Application Master Key aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "change of all application keys FAILURE", COLOR_RED);
                    return;
                }
                // now change the Application Master Key from CHANGED to DEFAULT
                boolean success0 = changeKeyDes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_MASTER_DES);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_MASTER_NUMBER + " result: " + success0);
                if (!success0) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "change of application master key FAILURE", COLOR_RED);
                    return;
                }
                writeToUiAppend(output, logString + " SUCCESS");
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                vibrateShort();
            }
        });

        changeAllKeysWithChangedMasterKeyAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change all application keys to changed with default master application key
                clearOutputFields();
                String logString = "AES change all application keys to DEFAULT with CHANGED Master Key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                // change keys 1 to 4 first to DEFAULT, authenticate with CHANGED Application Master Key
                boolean success1 = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_AES_DEFAULT, APPLICATION_KEY_RW_AES);
                boolean success2 = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_AES_DEFAULT, APPLICATION_KEY_CAR_AES);
                boolean success3 = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_AES_DEFAULT, APPLICATION_KEY_R_AES);
                boolean success4 = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_AES_DEFAULT, APPLICATION_KEY_W_AES);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_RW_NUMBER + " result: " + success1);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_CAR_NUMBER + " result: " + success2);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_R_NUMBER + " result: " + success3);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_W_NUMBER + " result: " + success4);
                // proceed only when all changes are successfully
                if ((!success1) || (!success2) || (!success3) || (!success4)) {
                    writeToUiAppend(output, "not all key changes were successfully, change of Application Master Key aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "change of all application keys FAILURE", COLOR_RED);
                    return;
                }
                // now change the Application Master Key from CHANGED to DEFAULT
                boolean success0 = changeKeyAes(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, APPLICATION_KEY_MASTER_AES);
                writeToUiAppend(output, "change key " + APPLICATION_KEY_MASTER_NUMBER + " result: " + success0);
                if (!success0) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "change of application master key FAILURE", COLOR_RED);
                    return;
                }
                writeToUiAppend(output, logString + " SUCCESS");
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                vibrateShort();
            }
        });
        
/*
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

 */
/*
new AES EV2First auth and secure messaging
Authenticate EV2 First Command : 710006000000000000
Authenticate EV2 First Command Response : 1E4ED24D113209311EF9DE00977D354A
Command sent to card : 6E4B66EC7DFD37438E
Response received : 00000A002525C5AF6CE39963
 */
        /*
            }
        });

         */


/*
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

 */
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
    /*
    }
        });
*/

        /**
         * this method will format the card and change the Master Application Key to an AES default key
         */

        /*
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
            }
        });
        */

        /*
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
*/


/*
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

 */


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
                    vibrateShort();
                } else {
                    writeToUiAppend(output, logString + ": get an ERROR");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " FAILURE", COLOR_RED);
                }
            }
        });

        selectMasterApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "select the Master Application";
                writeToUiAppend(output, logString);
                boolean success = selectMasterApplication(logString);
                writeToUiAppend(output, logString + ": " + success);
                if (!success) {
                    writeToUiAppend(output, logString + " NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NOT Success, aborted", COLOR_RED);
                    return;
                } else {
                    applicationSelected.setText("000000");
                    selectedApplicationId = MASTER_APPLICATION_IDENTIFIER.clone(); // 00 00 00
                    selectedFileId = "";
                    selectedFileIdInt = -1;
                    fileSelected.setText("");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + ": " + success, COLOR_GREEN);
                    vibrateShort();
                }
            }
        });

        // testing

        transactionTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // work with transaction timer

                // https://www.youtube.com/watch?v=FCEWF-EmrV8
                // individual enablement for each application
                // different transaction timer for different applications
                // the transaction timer is enabled by using the SetConfiguration command with option 0x55
                // the value of the transaction timer can be set for each application individually (1, 10 or 100 can be chosen)
                // once enabled timer settings will be reflected in the response to the AuthenticateEv2First
                // command in the parameter PDCap 2.2
                // the timer starts counting for every application selection

                //
                // get the default application settings
                String logString = "Transaction Timer";
                try {
                    int numberOfKeysInt = 5;
                    KeyType keyType = KeyType.AES128;
                    EV3ApplicationKeySettings applicationKeySettings = getApplicationSettingsDefault(numberOfKeysInt, keyType);
                    if (applicationKeySettings == null) {
                        Log.e(TAG, logString + " the applicationKeysSettings are NULL, aborted");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " could not get the application settings, aborted", COLOR_RED);
                    }
                    // important: first select the Master Application ('0')
                    desFireEV3.selectApplication(0);
                    // depending on MasterKey settings an authentication is necessary, skipped here
                    byte[] applicationIdentifier = Utils.hexStringToByteArray("998877");
                    desFireEV3.createApplication(applicationIdentifier, applicationKeySettings);
                    EV3CPICCConfigurationSettings ev3CPICCConfigurationSettings = new EV3CPICCConfigurationSettings();
                    ev3CPICCConfigurationSettings.setPCDCap((byte) 0, (byte) 0, (byte) 0, (byte) 0);

                    //writeToUiAppend(output, "create a new application done," + printData("new appID", applicationIdentifier));
                    //writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);

                } catch (InvalidResponseLengthException e) {
                    Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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

            }
        });


        /*
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
*/


        /**
         * section for general workflow
         */

        getFileSettingsDesfire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "getFileSettingsDesfire";
                writeToUiAppend(output, logString);
                writeToUiAppend(output, logString + " for selectedFileIdInt: " + selectedFileIdInt);
                DESFireEV3File.EV3FileSettings selectedFileSettings = getFileSettings(logString, selectedFileIdInt);
                DESFireEV3File.StdEV3DataFileSettings selectedStandardFileSettings = null;
                if (selectedFileSettings.getType() == DESFireEV3File.EV3FileType.DataStandard) {
                    writeToUiAppend(output, logString + " it is a StandardDataFile, mapping fileSettings");
                    selectedStandardFileSettings = (DESFireEV3File.StdEV3DataFileSettings) selectedFileSettings;
                }

                // see Mifare DESFire Light Features and Hints AN12343.pdf pages 24-26 getFileSettings
                // see NTAG 424 DNA and NTAG 424 DNA TagTamper features and hints AN12196.pdf pages 26-27 showing SDM options

                if  (selectedFileSettings != null) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppend(output, logString + " communicationSettings:\n" + selectedFileSettings.getComSettings());
                    if (selectedStandardFileSettings != null) {
                        // get more data
                        writeToUiAppend(output, "standardFileSize: " + selectedStandardFileSettings.getFileSize());
                        boolean isSdmEnabled = selectedStandardFileSettings.isSDMEnabled();
                        boolean isSdmEncryptedEnabled = selectedStandardFileSettings.isSDMEncryptFileDataEnabled();
                        boolean isSdmReadCounterEnabled = selectedStandardFileSettings.isSDMReadCounterEnabled();
                        boolean isSdmUidMirroringEnabled = selectedStandardFileSettings.isUIDMirroringEnabled();
                        writeToUiAppend(output, "isSdmEnabled: " + isSdmEnabled +
                                " isSdmEncryptedEnabled: " + isSdmEncryptedEnabled +
                                " isSdmReadCounterEnabled: " + isSdmReadCounterEnabled +
                                " isSdmUidMirroringEnabled: " + isSdmUidMirroringEnabled);
                    }
                } else {
                    writeToUiAppend(output, logString + " FAILURE");
                }
            }
        });



        formatPicc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "format PICC";
                writeToUiAppend(output, logString);
                boolean success = selectMasterApplication(logString);
                if (!success) {
                    writeToUiAppend(output, "select MasterApplication NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout,"select MasterApplication NOT Success, aborted", COLOR_RED);
                }
                success = legacyDesAuth(logString, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES_DEFAULT);
                if (!success) {
                    writeToUiAppend(output, "auth with DES MasterApplication Key NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "auth with DES MasterApplication Key  NOT Success, aborted", COLOR_RED);
                }
                success = formatPiccCommand(logString);
                writeToUiAppend(output, logString + ": " + success);
                if (!success) {
                    writeToUiAppend(output, logString + " NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NOT Success, aborted", COLOR_RED);
                    return;
                } else {
                    //applicationSelected.setText("000000");
                    //selectedApplicationId = MASTER_APPLICATION_IDENTIFIER.clone(); // 00 00 00
                    //selectedFileId = "";
                    //selectedFileIdInt = -1;
                    //fileSelected.setText("");
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + ": " + success, COLOR_GREEN);
                    vibrateShort();
                }
            }
        });

        createNdef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "createNdef";
                writeToUiAppend(output, logString);
                boolean success = createANdefFile(logString);
                writeToUiAppend(output, logString + ": " + success);
                if (!success) {
                    writeToUiAppend(output, logString + " NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NOT Success, aborted", COLOR_RED);
                    return;
                } else {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + ": " + success, COLOR_GREEN);
                    vibrateShort();
                }
            }
        });

        createNdefSdm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "createNdefSdm->AES";
                writeToUiAppend(output, logString);
                //boolean success = createANdefFileSdmEnabled(logString);
                boolean success = createANdefFileAes(logString);
                writeToUiAppend(output, logString + ": " + success);
                if (!success) {
                    writeToUiAppend(output, logString + " NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NOT Success, aborted", COLOR_RED);
                    return;
                } else {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + ": " + success, COLOR_GREEN);
                    vibrateShort();
                }
            }
        });

        formatNdefT4T.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "formatNdefT4T";
                writeToUiAppend(output, logString);
                boolean success = formatT4TCommand(logString, 256);
                /*
                if (!success) {
                    writeToUiAppend(output, "select MasterApplication NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout,"select MasterApplication NOT Success, aborted", COLOR_RED);
                }
                success = legacyDesAuth(logString, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES_DEFAULT);
                if (!success) {
                    writeToUiAppend(output, "auth with DES MasterApplication Key NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "auth with DES MasterApplication Key  NOT Success, aborted", COLOR_RED);
                }
                success = formatPiccCommand(logString);

                 */
                writeToUiAppend(output, logString + ": " + success);
                if (!success) {
                    writeToUiAppend(output, logString + " NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NOT Success, aborted", COLOR_RED);
                    return;
                } else {
                    //applicationSelected.setText("000000");
                    //selectedApplicationId = MASTER_APPLICATION_IDENTIFIER.clone(); // 00 00 00
                    //selectedFileId = "";
                    //selectedFileIdInt = -1;
                    //fileSelected.setText("");
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + ": " + success, COLOR_GREEN);
                    vibrateShort();
                }
            }
        });

        sdmIsEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "sdmIsEnabled";
                writeToUiAppend(output, logString);
                boolean success = sdmIsEnabledCommand(logString);
                /*
                if (!success) {
                    writeToUiAppend(output, "select MasterApplication NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout,"select MasterApplication NOT Success, aborted", COLOR_RED);
                }
                success = legacyDesAuth(logString, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES_DEFAULT);
                if (!success) {
                    writeToUiAppend(output, "auth with DES MasterApplication Key NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "auth with DES MasterApplication Key  NOT Success, aborted", COLOR_RED);
                }
                success = formatPiccCommand(logString);

                 */
                writeToUiAppend(output, logString + ": " + success);
                if (!success) {
                    writeToUiAppend(output, logString + " NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NOT Success, aborted", COLOR_RED);
                    return;
                } else {
                    //applicationSelected.setText("000000");
                    //selectedApplicationId = MASTER_APPLICATION_IDENTIFIER.clone(); // 00 00 00
                    //selectedFileId = "";
                    //selectedFileIdInt = -1;
                    //fileSelected.setText("");
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + ": " + success, COLOR_GREEN);
                    vibrateShort();
                }
            }
        });

        sdmEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "sdmEnable";
                writeToUiAppend(output, logString);
                boolean success = sdmEnableCommand(logString);
                /*
                if (!success) {
                    writeToUiAppend(output, "select MasterApplication NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout,"select MasterApplication NOT Success, aborted", COLOR_RED);
                }
                success = legacyDesAuth(logString, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES_DEFAULT);
                if (!success) {
                    writeToUiAppend(output, "auth with DES MasterApplication Key NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "auth with DES MasterApplication Key  NOT Success, aborted", COLOR_RED);
                }
                success = formatPiccCommand(logString);

                 */
                writeToUiAppend(output, logString + ": " + success);
                if (!success) {
                    writeToUiAppend(output, logString + " NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NOT Success, aborted", COLOR_RED);
                    return;
                } else {
                    //applicationSelected.setText("000000");
                    //selectedApplicationId = MASTER_APPLICATION_IDENTIFIER.clone(); // 00 00 00
                    //selectedFileId = "";
                    //selectedFileIdInt = -1;
                    //fileSelected.setText("");
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + ": " + success, COLOR_GREEN);
                    vibrateShort();
                }
            }
        });



        changeFileSettingsToSdm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "changeFileSettingsToSdm";
                writeToUiAppend(output, logString);
                boolean success = changeFileSettingsToSdmCommand(logString);
                /*
                if (!success) {
                    writeToUiAppend(output, "select MasterApplication NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout,"select MasterApplication NOT Success, aborted", COLOR_RED);
                }
                success = legacyDesAuth(logString, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES_DEFAULT);
                if (!success) {
                    writeToUiAppend(output, "auth with DES MasterApplication Key NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "auth with DES MasterApplication Key  NOT Success, aborted", COLOR_RED);
                }
                success = formatPiccCommand(logString);

                 */
                writeToUiAppend(output, logString + ": " + success);
                if (!success) {
                    writeToUiAppend(output, logString + " NOT Success, aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NOT Success, aborted", COLOR_RED);
                    return;
                } else {
                    //applicationSelected.setText("000000");
                    //selectedApplicationId = MASTER_APPLICATION_IDENTIFIER.clone(); // 00 00 00
                    //selectedFileId = "";
                    //selectedFileIdInt = -1;
                    //fileSelected.setText("");
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + ": " + success, COLOR_GREEN);
                    vibrateShort();
                }
            }
        });

        sdmGetFileSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "SDM getFileSettings";
                writeToUiAppend(output, logString);

                // I'm trying to read fileSettings for some file and hope they are SDM enabled

                int selectedFileIdInt = 2; // run cr ndef sdm first !
                writeToUiAppend(output, logString + " for selectedFileIdInt: " + selectedFileIdInt);
                DESFireEV3File.EV3FileSettings selectedFileSettings = getFileSettingsSdmCommand(logString, selectedFileIdInt);
                if (selectedFileSettings == null) {
                    writeToUiAppend(output, "Error on Authentication, aborted");
                    return;
                }
                DESFireEV3File.StdEV3DataFileSettings selectedStandardFileSettings = null;
                if (selectedFileSettings.getType() == DESFireEV3File.EV3FileType.DataStandard) {
                    writeToUiAppend(output, logString + " it is a StandardDataFile, mapping fileSettings");
                    selectedStandardFileSettings = (DESFireEV3File.StdEV3DataFileSettings) selectedFileSettings;
                }

                // see Mifare DESFire Light Features and Hints AN12343.pdf pages 24-26 getFileSettings
                // see NTAG 424 DNA and NTAG 424 DNA TagTamper features and hints AN12196.pdf pages 26-27 showing SDM options

                if  (selectedFileSettings != null) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppend(output, logString + " communicationSettings:\n" + selectedFileSettings.getComSettings());
                    if (selectedStandardFileSettings != null) {
                        // get more data
                        writeToUiAppend(output, "standardFileSize: " + selectedStandardFileSettings.getFileSize());
                        boolean isSdmEnabled = selectedStandardFileSettings.isSDMEnabled();
                        boolean isSdmEncryptedEnabled = selectedStandardFileSettings.isSDMEncryptFileDataEnabled();
                        boolean isSdmReadCounterEnabled = selectedStandardFileSettings.isSDMReadCounterEnabled();
                        boolean isSdmUidMirroringEnabled = selectedStandardFileSettings.isUIDMirroringEnabled();
                        writeToUiAppend(output, "isSdmEnabled: " + isSdmEnabled +
                                " isSdmEncryptedEnabled: " + isSdmEncryptedEnabled +
                                " isSdmReadCounterEnabled: " + isSdmReadCounterEnabled +
                                " isSdmUidMirroringEnabled: " + isSdmUidMirroringEnabled);
                    }
                } else {
                    writeToUiAppend(output, logString + " FAILURE");
                }
            }
        });

    }

    /**
     * empty one
     */

    private boolean emptyTask(String logString) {
        Log.d(TAG, logString);
        try {
            desFireEV3.selectApplication(0);
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
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
     * section for applications
     */

    private boolean selectMasterApplication(String logString) {
        Log.d(TAG, logString);
        try {
            desFireEV3.selectApplication(0);
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
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
     * @param logString:             provide a string for error log
     * @param applicationIdentifier: 3 bytes long array with the application identifier
     * @param numberOfKeysInt:       minimum is 1 BUT you should give a minimum of 5 keys as on file creation we will need them. Maximum is 14
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
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
     * @param logString:                provide a string for error log
     * @param applicationIdentifierInt: an integer for the application number
     * @param numberOfKeysInt:          minimum is 1 BUT you should give a minimum of 5 keys as on file creation we will need them. Maximum is 14
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
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
     * section for auth handling
     */
    private boolean authenticate(String logString, byte  keyNumber, byte[] keyData) {
        Log.d(TAG, logString);
        try {
            desFireEV3.selectApplication(0);
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
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
     * section for general handling
     */

    private boolean formatPiccCommand(String logString) {
        Log.d(TAG, logString);
        try {
            desFireEV3.format();
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    private boolean formatT4TCommand(String logString, int fileSize) {
        Log.d(TAG, logString);
        try {
            desFireEV3.formatT4T(fileSize);
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    private boolean sdmIsEnabledCommand(String logString) {
        Log.d(TAG, logString);
        try {
            DESFireEV3File.EV3FileSettings fileSettings = getFileSettings(logString, 2);
            DESFireEV3File.StdEV3DataFileSettings stdFileSettings = (DESFireEV3File.StdEV3DataFileSettings) fileSettings;
            int fileSize = stdFileSettings.getFileSize();
            boolean isSDMEnabled = stdFileSettings.isSDMEnabled();
            writeToUiAppend(output, "file 2 size: " + fileSize + " isSDMEnabled: " + isSDMEnabled);
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    private boolean sdmEnableCommand(String logString) {
        Log.d(TAG, logString);
        try {

            // first select master app
            // important: first select the Master Application ('0')
            writeToUiAppend(output, logString + " step 1 select master application");
            desFireEV3.selectApplication(0);

            writeToUiAppend(output, logString + " step 2 select ndef application 00 00 01");
            byte[] APPLICATION_ID = Utils.hexStringToByteArray("010000");
            desFireEV3.selectApplication(APPLICATION_ID);

            writeToUiAppend(output, logString + " step 3 auth with application master key");
            boolean suc = newAesEv2Auth(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT);
            if (!suc) {
                Log.e(TAG, "Authentication failure");
                return false;
            }

            writeToUiAppend(output, logString + " step 4 reading existing fileSettings");
            DESFireEV3File.EV3FileSettings fileSettings = getFileSettings(logString, 2);
            DESFireEV3File.StdEV3DataFileSettings stdFileSettings = (DESFireEV3File.StdEV3DataFileSettings) fileSettings;
            int fileSize = stdFileSettings.getFileSize();
            boolean isSDMEnabled = stdFileSettings.isSDMEnabled();
            writeToUiAppend(output, "file 2 size: " + fileSize + " isSDMEnabled: " + isSDMEnabled);
            //if (!isSDMEnabled) {
            writeToUiAppend(output, "trying to enable SDM feature");
            stdFileSettings.setSDMEnabled(true);

            // without this I'm receiving a sdmEnable UsageResponseLength occurred UsageException
            // Invalid Parameters! {Invalid Value for SdmAccessRights}
            //byte[] sdmAccessRights = Utils.hexStringToByteArray("EEEF");
            //byte[] sdmAccessRights = Utils.hexStringToByteArray("0000");
            byte[] sdmAccessRights = Utils.hexStringToByteArray("F121"); // WORKING !! see NTAG 424 DNA and NTAG 424 DNA TagTamper features and hints AN12196.pdf page 34
            //byte[] sdmAccessRights = Utils.hexStringToByteArray("E121");
            stdFileSettings.setSdmAccessRights(sdmAccessRights);

            // without the next line I'm receiving Response received : 9E
            stdFileSettings.setUIDMirroringEnabled(true);
            // not published
            byte[] sdmUidOffset = new byte[]{(byte) 0x10, (byte) 0x00, (byte) 0x00};
            //stdFileSettings.setUidOffset(sdmUidOffset);

            // not given out, it is encrypted PICC data
            stdFileSettings.setSDMReadCounterEnabled(true);
            byte[] sdmReadCounterOffset = new byte[]{(byte) 0x10, (byte) 0x00, (byte) 0x00};
            //stdFileSettings.setSdmReadCounterOffset(sdmReadCounterOffset);

            // this is published as encrypted PICC data
            byte[] sdmPiccOffset = new byte[]{(byte) 0x10, (byte) 0x00, (byte) 0x00};
            stdFileSettings.setPiccDataOffset(sdmPiccOffset);

            // this is not published
            byte[] sdmMacInputOffset = new byte[]{(byte) 0x32, (byte) 0x00, (byte) 0x00};
            //stdFileSettings.setSdmMacInputOffset(sdmMacInputOffset);

            // latest position
            byte[] sdmMacOffset = new byte[]{(byte) 0x40, (byte) 0x00, (byte) 0x00};
            stdFileSettings.setSdmMacOffset(sdmMacOffset);


            // now  sending this data back to tag
            writeToUiAppend(output, logString + " step 4 change file settings command to PICC");
            desFireEV3.changeDESFireEV3FileSettings(2, stdFileSettings);

            // using                byte[] sdmAccessRights = Utils.hexStringToByteArray("F121");
            // old getFileSettings: 000040EEEE00010041F1212000003000004000000322D0E81A3D45A1
            // command to change:   5F0240EEEE41F121200000300000400000
            // Response received :  00


/*
data: BEB9D162EFB003EA562015516B3B38F1
MAC:
# NDEF message: 09E6BCD892412A7080
[00] D1 01 52 55 04 63 68 6F 6F 73 65 2E 75 72 6C 2E |..RU.choose.url.|
[10] 63 6F 6D 2F 6E 74 61 67 34 32 34 3F 65 3D 42 45 |com/ntag424?e=BE|
[20] 42 39 44 31 36 32 45 46 42 30 30 33 45 41 35 36 |B9D162EFB003EA56|
[30] 32 30 31 35 35 31 36 42 33 42 33 38 46 31 30 30 |2015516B3B38F100|
[40] 30 30 30 39 45 36 42 43 44 38 39 32 34 31 32 41 |0009E6BCD892412A|
[50] 37 30 38 30 39 39                               |708099          |
 */

            writeToUiAppend(output, "SDM feature should be enabled now");

            // get some key data
            byte[] sdmAccessRightsRead = stdFileSettings.getSdmAccessRights();
            Log.d(TAG, printData("sdmAccessRightsRead", sdmAccessRightsRead));


            //}
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            //writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    // as this configuration is working and providing encrypted PICC data this is a frozen config
    private boolean sdmEnableCommandFrozen(String logString) {
        Log.d(TAG, logString);
        try {

            // first select master app
            // important: first select the Master Application ('0')
            writeToUiAppend(output, logString + " step 1 select master application");
            desFireEV3.selectApplication(0);

            writeToUiAppend(output, logString + " step 2 select ndef application 00 00 01");
            byte[] APPLICATION_ID = Utils.hexStringToByteArray("010000");
            desFireEV3.selectApplication(APPLICATION_ID);

            writeToUiAppend(output, logString + " step 3 auth with application master key");
            boolean suc = newAesEv2Auth(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT);
            if (!suc) {
                Log.e(TAG, "Authentication failure");
                return false;
            }

            writeToUiAppend(output, logString + " step 4 reading existing fileSettings");
            DESFireEV3File.EV3FileSettings fileSettings = getFileSettings(logString, 2);
            DESFireEV3File.StdEV3DataFileSettings stdFileSettings = (DESFireEV3File.StdEV3DataFileSettings) fileSettings;
            int fileSize = stdFileSettings.getFileSize();
            boolean isSDMEnabled = stdFileSettings.isSDMEnabled();
            writeToUiAppend(output, "file 2 size: " + fileSize + " isSDMEnabled: " + isSDMEnabled);
            //if (!isSDMEnabled) {
                writeToUiAppend(output, "trying to enable SDM feature");
                stdFileSettings.setSDMEnabled(true);

                // without this I'm receiving a sdmEnable UsageResponseLength occurred UsageException
                // Invalid Parameters! {Invalid Value for SdmAccessRights}
                //byte[] sdmAccessRights = Utils.hexStringToByteArray("EEEF");
                //byte[] sdmAccessRights = Utils.hexStringToByteArray("0000");
                byte[] sdmAccessRights = Utils.hexStringToByteArray("F121"); // WORKING !! see NTAG 424 DNA and NTAG 424 DNA TagTamper features and hints AN12196.pdf page 34
                //byte[] sdmAccessRights = Utils.hexStringToByteArray("E121");
                stdFileSettings.setSdmAccessRights(sdmAccessRights);

            /**
             * WARNING - DO NOT CHANGE ANY DATA HERE
             */

            // without the next line I'm receiving Response received : 9E
                stdFileSettings.setUIDMirroringEnabled(false);
                // not published
                byte[] sdmUidOffset = new byte[]{(byte) 0x10, (byte) 0x00, (byte) 0x00};
                stdFileSettings.setUidOffset(sdmUidOffset);

                // not given out, it is encrypted PICC data
                stdFileSettings.setSDMReadCounterEnabled(true);
                byte[] sdmReadCounterOffset = new byte[]{(byte) 0x10, (byte) 0x00, (byte) 0x00};
                stdFileSettings.setSdmReadCounterOffset(sdmReadCounterOffset);

                // this is published as encrypted PICC data
                byte[] sdmPiccOffset = new byte[]{(byte) 0x20, (byte) 0x00, (byte) 0x00};
                stdFileSettings.setPiccDataOffset(sdmPiccOffset);

                // this is not published
                byte[] sdmMacInputOffset = new byte[]{(byte) 0x32, (byte) 0x00, (byte) 0x00};
                stdFileSettings.setSdmMacInputOffset(sdmMacInputOffset);

                // latest position
                byte[] sdmMacOffset = new byte[]{(byte) 0x45, (byte) 0x00, (byte) 0x00};
                stdFileSettings.setSdmMacOffset(sdmMacOffset);


                // now  sending this data back to tag
                writeToUiAppend(output, logString + " step 4 change file settings command to PICC");
                desFireEV3.changeDESFireEV3FileSettings(2, stdFileSettings);

                // using                byte[] sdmAccessRights = Utils.hexStringToByteArray("F121");
                // old getFileSettings: 000040EEEE00010041F1212000003000004000000322D0E81A3D45A1
                // command to change:   5F0240EEEE41F121200000300000400000
                // Response received :  00
/*
protocol field: https://
URI field: choose.url.com/ntag424?e=A0BBE8C95F311498FB35F6C9FAAAAF896F64CFBBAB067D6A00000099
Payload length: 82 bytes
Payload data:
[00] 04 63 68 6F 6F 73 65 2E 75 72 6C 2E 63 6F 6D 2F |.choose.url.com/|
[10] 6E 74 61 67 34 32 34 3F 65 3D 41 30 42 42 45 38 |ntag424?e=A0BBE8|
[20] 43 39 35 46 33 31 31 34 39 38 46 42 33 35 46 36 |C95F311498FB35F6|
[30] 43 39 46 41 41 41 41 46 38 39 36 46 36 34 43 46 |C9FAAAAF896F64CF|
[40] 42 42 41 42 30 36 37 44 36 41 30 30 30 30 30 30 |BBAB067D6A000000|
[50] 39 39                                           |99              |

# NDEF message:
[00] D1 01 52 55 04 63 68 6F 6F 73 65 2E 75 72 6C 2E |..RU.choose.url.|
[10] 63 6F 6D 2F 6E 74 61 67 34 32 34 3F 65 3D 41 30 |com/ntag424?e=A0|
[20] 42 42 45 38 43 39 35 46 33 31 31 34 39 38 46 42 |BBE8C95F311498FB|
[30] 33 35 46 36 43 39 46 41 41 41 41 46 38 39 36 46 |35F6C9FAAAAF896F|
[40] 36 34 43 46 42 42 41 42 30 36 37 44 36 41 30 30 |64CFBBAB067D6A00|
[50] 30 30 30 30 39 39                               |000099          |

pos begin NDEF message ab pos 31
ins 48 chars = 24 bytes
data seems to be sdmPicOffset (16 bytes)  A0BBE8C95F311498FB35F6C9FAAAAF89 confirmed
sdmMacOffset (8 bytes)                    6F64CFBBAB067D6A                 confirmed
 */

/*
data: 38444244393741343139334544324541413933323134384438414235333537
mac:

# NDEF message:
[00] D1 01 52 55 04 63 68 6F 6F 73 65 2E 75 72 6C 2E |..RU.choose.url.|
[10] 63 6F 6D 2F 6E 74 61 67 34 32 34 3F 65 3D 42 45 |com/ntag424?e=BE|
[20] 42 39 44 31 36 32 45 46 42 30 30 33 45 41 35 36 |B9D162EFB003EA56|
[30] 32 30 31 35 35 31 36 42 33 42 33 38 46 31 30 30 |2015516B3B38F100|
[40] 30 30 30 39 45 36 42 43 44 38 39 32 34 31 32 41 |0009E6BCD892412A|
[50] 37 30 38 30 39 39                               |708099          |
 */

/*
for decryption of PICC data see:
NTAG 424 DNA NT4H2421Gx.pdf page 37
SDM Session Key Generation: page 41
for general lengths see: page 43

NTAG 424 DNA and NTAG 424 DNA TagTamper features and hints AN12196.pdf
page 8 SDM
page 10 SDM Session Key Generation

Decryption of PICCData
Verification side (e.g. backend, RF reader, NFC Mobile application, etc.) needs to know following parameters:
Prerequisites: Offset name: Length [bytes]: Algorithm:
SDMMetaReadKey set to App.KeyX (0x0 - 0x4)
PICCENCDataOffset
32*n; n=1,2,..., n
PICCENCData = E(KSDMMetaRead; PICCDataTag [ || UID ][ || SDMReadCtr ] || RandomPadding(1))
Prerequisites: Offset name: Length [bytes]: Algorithm:
SDMMetaReadKey used PICCENCDataOffset in URL PICCENCDataLength
PICCData = D(KSDMMetaRead; PICCENCData)

Encrypted PICC Data is 16 bytes long: EF963FF7828658A599F3041510671E88
SDMMetaReadKey = App.Key0             00000000000000000000000000000000
D(KSDMMetaReadKey, PICCENCData)       C704DE5F1EACC0403D0000DA5CF60941
Content:
Lc  Name             Sample
01  PICC Data Tag    C7
07  UID              04DE5F1EACC040
03  SdmReadCtr       3D0000
05  Random Padding   DA5CF60941
16  total

PICCDataTag [bit]:                         1100 0111
PICCDataTag - UID mirroring [bit7]:        1 (UID mirroring enabled)        leftmost bit
PICCDataTag - SDMReadCtr mirroring [bit6]: 1 (SDMReadCtr mirroring enabled) 2. bit from left
PICCDataTag - UID Length [bit3-0]:         111b = 7d (7 byte UID)           last 4 bits

 */


                writeToUiAppend(output, "SDM feature should be enabled now");
            //}
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            //writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    //changeFileSettingsToSdmCommand
    public boolean changeFileSettingsToSdmCommand(String logString) {
        Log.d(TAG, logString);
        try {
            // status WORKING
            /*
            https://www.mifare.net/support/forum/search/ntag424/
            writeNDEF API is using ISOUpdateBinary command. This command requires selection of NDEF file upfront with ISOSelectFile. Please check below lines in correct order:
            byte[] NTAG424DNA_NDEF_FILE = {(byte) 0x04, (byte) 0xE1};
            tag.isoSelectApplicationByFileID(NTAG424DNA_NDEF_FILE);
            tag.writeNDEF(myndefMessageWrapper);

            byte[] NTAG424DNA_NDEF_APP_NAME =
            {(byte) 0xD2, (byte) 0x76, 0x00, 0x00, (byte) 0x85, 0x01, 0x01};
            keyData.setKey(keyDefault);
            // this is a MUST //
            ntag424DNA.isoSelectApplicationByDFName(NTAG424DNA_NDEF_APP_NAME);
            ntag424DNA.authenticateEV2First(1, keyData, null);

            In order that SDM is working, also UID mirroring needs to be enabled, you need to set Offset positions for each as well. Something like:

            fileSettings.setUIDMirroringEnabled(true);
            fileSettings.setUidOffset(new byte[] { (byte) 0x1A, (byte) 0x00, (byte) 0x00 });
            fileSettings.setSdmMacOffset(new byte[] {(byte) 0x42, (byte) 0x00, (byte) 0x00 });
            fileSettings.setSdmMacInputOffset(new byte[] {(byte) 0x42, (byte) 0x00, (byte) 0x00 });
            fileSettings.setSdmReadCounterOffset(new byte[]{(byte) 0x29, (byte) 0x00, (byte) 0x00});
            fileSettings.setSdmAccessRights(new byte[]{(byte) 0x12, (byte) 0xFE}); //FileAR.SDMMetaRead (ENCPICCData) key = 0xE, FileAR.SDMFileRead (CMAC) key = 0x2, RFU = 0xF, FileAR.SDMCtrRet key = 0xE
            ntag424DNATT.changeFileSettings( (byte) 0x02, settings);`

             */

            // first select master app
            // important: first select the Master Application ('0')
            writeToUiAppend(output, logString + " step 1 select master application");
            desFireEV3.selectApplication(0);

            writeToUiAppend(output, logString + " step 2 select ndef application 00 00 01");
            byte[] APPLICATION_ID = Utils.hexStringToByteArray("010000");
            desFireEV3.selectApplication(APPLICATION_ID);

            writeToUiAppend(output, logString + " step 3 auth with application master key");
            boolean suc = newAesEv2Auth(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT);
            if (!suc) {
                Log.e(TAG, "Authentication failure");
                return false;
            }

            writeToUiAppend(output, logString + " step 4 change file settings");
            DESFireEV3File.EV3FileSettings fileSettings = getFileSettings(logString, 2);
            DESFireEV3File.StdEV3DataFileSettings stdFileSettings = (DESFireEV3File.StdEV3DataFileSettings) fileSettings;
            int fileSize = stdFileSettings.getFileSize();
            boolean isSDMEnabled = stdFileSettings.isSDMEnabled();
            writeToUiAppend(output, "file 2 size: " + fileSize + " isSDMEnabled: " + isSDMEnabled);
            if (!isSDMEnabled) {
                writeToUiAppend(output, "trying to enable SDM feature");

                byte keyWriteAccess = stdFileSettings.getWriteAccess();
                writeToUiAppend(output, "key with writeAccess: " + keyWriteAccess);

                // don't forget - all offset length data are LSB hex values
                // based on sample data in NTAG 424 DNA and NTAG 424 DNA TagTamper features and hints AN12196.pdf pages 30 ff
                // https://choose.url.com/ntag424?e=00000000000000000000000000000000&c=0000000000000000
                // for offset samples see page 34 ff

                // use Android's NDEF classes to construct the NDEF message
                // this is the data from Feature and Hints with offsets:
                // byte[] sdmPiccOffset = new byte[]{(byte) 0x20, (byte) 0x00, (byte) 0x00}; // 20h = 32d, counting from s in https
                // byte[] sdmMacOffset = new byte[]{(byte) 0x43, (byte) 0x00, (byte) 0x00};  // 43h = 67d, counting from s in https
                //String ndefSampleUrlWrong = "https://choose.url.com/ntag424?e=00000000000000000000000000000000&c=0000000000000000";
                //String ndefSampleUrl = "https://choose.aburl.com/ntag424?e=00000000000000000000000000000000&c=0000000000000000";
                //String ndefSampleBackendUrl = "https://sdm.nfcdeveloper.com/tag?picc_data=00000000000000000000000000000000&cmac=0000000000000000";
                // BackendUrl offset: PICC data: 40d = 28h, cmac: 78d = 4Eh

                stdFileSettings.setSDMEnabled(true);

                stdFileSettings.setUIDMirroringEnabled(true);
                //byte[] sdmUidOffset = new byte[]{(byte) 0x03, (byte) 0x00, (byte) 0x00};
                //stdFileSettings.setUidOffset(sdmUidOffset);

                stdFileSettings.setSDMReadCounterEnabled(true);
                stdFileSettings.setSDMEncryptFileDataEnabled(false);
                stdFileSettings.setSDMReadCounterLimitEnabled(false);

                //byte[] sdmCounterOffset = new byte[]{(byte) 0x18, (byte) 0x00, (byte) 0x00};
                //stdFileSettings.setSdmReadCounterOffset(sdmCounterOffset);
                /*
                byte[] sdmPiccOffset = new byte[]{(byte) 0x20, (byte) 0x00, (byte) 0x00};
                byte[] sdmMacInputOffset = new byte[]{(byte) 0x43, (byte) 0x00, (byte) 0x00};
                byte[] sdmMacOffset = new byte[]{(byte) 0x43, (byte) 0x00, (byte) 0x00};
                */

                // settings for https://sdm.nfcdeveloper.com/ Secure Dynamic Messaging Backend Server Demo
                byte[] sdmPiccOffset = new byte[]{(byte) 0x2A, (byte) 0x00, (byte) 0x00};
                byte[] sdmMacInputOffset = new byte[]{(byte) 0x50, (byte) 0x00, (byte) 0x00};
                byte[] sdmMacOffset = new byte[]{(byte) 0x50, (byte) 0x00, (byte) 0x00};

                stdFileSettings.setPiccDataOffset(sdmPiccOffset);
                stdFileSettings.setSdmMacInputOffset(sdmMacInputOffset);
                //byte[] sdmMacOffset = new byte[]{(byte) 0x43, (byte) 0x00, (byte) 0x00};
                stdFileSettings.setSdmMacOffset(sdmMacOffset);

                // ASCII Encoding mode: 1 - no setting in API

                //F121h = SDMAccessRights (RFU: 0xF, FileAR.SDMCtrRet = 0x1, FileAR.SDMMetaRead: 0x2, FileAR.SDMFileRead: 0x1)
                //byte[] sdmAccessRights = Utils.hexStringToByteArray("F121");

                // SDM Access Rights.
                // Bit 15-12: SDM Meta Read Access Rights. 0x00 to 0x04 : Encrypted PICC data mirroring using the targeted AppKey 0x0E : Plain PICC data mirroring 0x0F : No PICC data mirroring
                // Bit 11-8: SDM File Read Access Rights 0x00 to 0x04: Targeted AppKey 0x0F : No SDM for Reading
                // Bit 3-0: SDM Counter Ret Access Rights 0x00 to 0x04: Targeted AppKey 0x0E : Free 0x0F : No Access
                //byte[] sdmAccessRights = Utils.hexStringToByteArray("E1F1");

                //byte[] sdmAccessRights = Utils.hexStringToByteArray("F1E1");
                byte[] sdmAccessRights = Utils.hexStringToByteArray("F121");
                stdFileSettings.setSdmAccessRights(sdmAccessRights);

                DESFireEV3File.EV3FileSettings desFireEV3FileSettings = (DESFireEV3File.EV3FileSettings) stdFileSettings;
                writeToUiAppend(output, logString + " step 4 change file settings command to PICC");
                desFireEV3.changeDESFireEV3FileSettings(2, desFireEV3FileSettings);
                writeToUiAppend(output, "SDM feature should be enabled now");
/*
for dencryption of PICC data see:
NTAG 424 DNA NT4H2421Gx.pdf page 37
SDM Session Key Generation: page 41
for general lengths see: page 43

For PICC data tag see page 37:
Bit 7 UID mirroring 1 = enabled
Bit 6 ReadCounter mirroring 1 = enabled
Bit 4+5 RFU 00
Bit 3-0 UID length, 7h if UID is mirrored and 0 if not mirrored
The key applied for encryption of PICCData is the SDMMetaReadKey as defined by the SDMMetaRead access right.

NTAG 424 DNA and NTAG 424 DNA TagTamper features and hints AN12196.pdf
page 8 SDM
page 10 SDM Session Key Generation

Decryption of PICCData
Verification side (e.g. backend, RF reader, NFC Mobile application, etc.) needs to know following parameters:
Prerequisites: Offset name: Length [bytes]: Algorithm:
SDMMetaReadKey set to App.KeyX (0x0 - 0x4)
PICCENCDataOffset
32*n; n=1,2,..., n
PICCENCData = E(KSDMMetaRead; PICCDataTag [ || UID ][ || SDMReadCtr ] || RandomPadding(1))
Prerequisites: Offset name: Length [bytes]: Algorithm:
SDMMetaReadKey used PICCENCDataOffset in URL PICCENCDataLength
PICCData = D(KSDMMetaRead; PICCENCData)

Encrypted PICC Data is 16 bytes long: EF963FF7828658A599F3041510671E88
SDMMetaReadKey = App.Key0             00000000000000000000000000000000
D(KSDMMetaReadKey, PICCENCData)       C704DE5F1EACC0403D0000DA5CF60941
Content:
Lc  Name             Sample
01  PICC Data Tag    C7
07  UID              04DE5F1EACC040
03  SdmReadCtr       3D0000
05  Random Padding   DA5CF60941
16  total

PICCDataTag [bit]:                         1100 0111
PICCDataTag - UID mirroring [bit7]:        1 (UID mirroring enabled)        leftmost bit
PICCDataTag - SDMReadCtr mirroring [bit6]: 1 (SDMReadCtr mirroring enabled) 2. bit from left
PICCDataTag - UID Length [bit3-0]:         111b = 7d (7 byte UID)           last 4 bits


for Offsets see NTAG 424 DNA NT4H2421Gx.pdf pages 36 + 37
 */


/*
result:
https://sdm.nfcdeveloper.com/tag?picc_data=FBCBE6602D4FF482C1B961242300394D&cmac=112E0AE968CF6DE7
 */

                return true;
            }
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            //writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    public DESFireEV3File.EV3FileSettings getFileSettingsSdmCommand(String logString, int fileNumber) {
        Log.d(TAG, logString);
        try {

            /*
            https://www.mifare.net/support/forum/search/ntag424/
            writeNDEF API is using ISOUpdateBinary command. This command requires selection of NDEF file upfront with ISOSelectFile. Please check below lines in correct order:
            byte[] NTAG424DNA_NDEF_FILE = {(byte) 0x04, (byte) 0xE1};
            tag.isoSelectApplicationByFileID(NTAG424DNA_NDEF_FILE);
            tag.writeNDEF(myndefMessageWrapper);

            byte[] NTAG424DNA_NDEF_APP_NAME =
            {(byte) 0xD2, (byte) 0x76, 0x00, 0x00, (byte) 0x85, 0x01, 0x01};
            keyData.setKey(keyDefault);
            // this is a MUST //
            ntag424DNA.isoSelectApplicationByDFName(NTAG424DNA_NDEF_APP_NAME);
            ntag424DNA.authenticateEV2First(1, keyData, null);

            In order that SDM is working, also UID mirroring needs to be enabled, you need to set Offset positions for each as well. Something like:

            fileSettings.setUIDMirroringEnabled(true);
            fileSettings.setUidOffset(new byte[] { (byte) 0x1A, (byte) 0x00, (byte) 0x00 });
            fileSettings.setSdmMacOffset(new byte[] {(byte) 0x42, (byte) 0x00, (byte) 0x00 });
            fileSettings.setSdmMacInputOffset(new byte[] {(byte) 0x42, (byte) 0x00, (byte) 0x00 });
            fileSettings.setSdmReadCounterOffset(new byte[]{(byte) 0x29, (byte) 0x00, (byte) 0x00});
            fileSettings.setSdmAccessRights(new byte[]{(byte) 0x12, (byte) 0xFE}); //FileAR.SDMMetaRead (ENCPICCData) key = 0xE, FileAR.SDMFileRead (CMAC) key = 0x2, RFU = 0xF, FileAR.SDMCtrRet key = 0xE
            ntag424DNATT.changeFileSettings( (byte) 0x02, settings);`

             */

            // first select master app
            // important: first select the Master Application ('0')
            writeToUiAppend(output, logString + " step 1 select master application");
            desFireEV3.selectApplication(0);

            writeToUiAppend(output, logString + " step 2 select ndef application 00 00 01");
            byte[] APPLICATION_ID = Utils.hexStringToByteArray("010000");
            desFireEV3.selectApplication(APPLICATION_ID);

            writeToUiAppend(output, logString + " step 3 auth with application master key");
            boolean suc = newAesEv2Auth(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT);
            if (!suc) {
                Log.e(TAG, "Authentication failure");
                return null;
            }

            writeToUiAppend(output, logString + " step 4 get file settings");
            DESFireEV3File.EV3FileSettings fileSettings = getFileSettings(logString, fileNumber);
            return fileSettings;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * section for service methods
     */

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
     * section for files
     */

    private boolean createAStandardBackupFile(String logString, boolean isStandardFile, int fileNumber, IDESFireEV1.CommunicationType communicationType, int rwKeyInt, int carKeyInt, int rKeyInt, int wKeyInt, int fileSize) {
        Log.d(TAG, logString + " isStandardFile: " + isStandardFile);
        try {
            byte readAccess = (byte) (rKeyInt & 0xff);
            byte writeAccess = (byte) (wKeyInt & 0xff);
            byte readWriteAccess = (byte) (rwKeyInt & 0xff);
            byte changeAccess = (byte) (carKeyInt & 0xff);
            DESFireFile.FileSettings fileSettings;
            if (isStandardFile) {
                fileSettings = new DESFireFile.StdDataFileSettings(communicationType, readAccess, writeAccess, readWriteAccess, changeAccess, fileSize);
            } else {
                fileSettings = new DESFireFile.BackupDataFileSettings(communicationType, readAccess, writeAccess, readWriteAccess, changeAccess, fileSize);
            }
            desFireEV3.createFile(fileNumber, fileSettings);
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }


/*
void createFile(int fileNumber,
                byte[] isoFileID,
                DESFireEV3File.EV3FileSettings fileSettings)
Creates a file within a DESFire EV3 application. This API allows flexibility to use ISO file ID.
This method also accepts the ISO file ID that is used in ISO 7816-4 operations.
Parameters:
fileNumber - File to be created with this file number.
isoFileID - ISO file identifier of file.
fileSettings - File to be created with these file settings which are present in FileSettings Object.use
builders of different derived class of FileSettings to create file settings object,

see DESFireEV3File.StdEV3DataFileSettings file:///Users/michaelfehr/Downloads/TapLinx%20SDK%203-0-0%20JavaDoc/index.html createFile()

 */

    // todo missing: write NDEF container, see DesfirePlayground NDEF

    private boolean createAStandardNdefFile(String logString, int fileNumber, byte[] isoFileId, IDESFireEV1.CommunicationType communicationType, int rwKeyInt, int carKeyInt, int rKeyInt, int wKeyInt, int fileSize) {
        Log.d(TAG, logString);
        try {
            byte readAccess = (byte) (rKeyInt & 0xff);
            byte writeAccess = (byte) (wKeyInt & 0xff);
            byte readWriteAccess = (byte) (rwKeyInt & 0xff);
            byte changeAccess = (byte) (carKeyInt & 0xff);
            DESFireEV3File.StdEV3DataFileSettings fileSettings;
            fileSettings = new DESFireEV3File.StdEV3DataFileSettings(communicationType, readAccess, writeAccess, readWriteAccess, changeAccess, fileSize);
            fileSettings.setSDMEnabled(true);
            fileSettings.setUIDMirroringEnabled(true);
            byte[] sdmUidOffset = new byte[]{(byte) 0x10, (byte) 0x00, (byte) 0x00};
            fileSettings.setUidOffset(sdmUidOffset);
            byte[] sdmPiccOffset = new byte[]{(byte) 0x20, (byte) 0x00, (byte) 0x00};
            fileSettings.setUidOffset(sdmPiccOffset);
            desFireEV3.createFile(fileNumber, isoFileId, fileSettings);
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    private boolean createANdefFileAes(String logString) {
        Log.d(TAG, logString);

        // status
        // will create an empty NDEF message, maximum size 256 byte
        // new: using 5 AES keys

        // check enabled by reading the file settings
        // see NTAG 424 DNA and NTAG 424 DNA TagTamper features and hints AN12196.pdf pages 26 + 27

        try {
            // this script will run several steps
            Log.d(TAG, "this script will generate a NDEF file on the DESFire EV3 tag with 5 AES keys");

            // step 1: select Master Application
            Log.d(TAG, "step 1: select Master Application 00 00 00");
            desFireEV3.selectApplication(0);

            // step 2: CreateApplication using the default AID 000001h
/*
void createApplication(byte[] applicationID,
                       EV3ApplicationKeySettings applicationSettings,
                       byte[] isoFileID,
                       byte[] dfName)
Creates new applications on the PICC. The application is initialized according to the given settings.
The application keys of the active key set are initialized with the Default Application Key.
Note: PICC Application (AID 0) has to be selected in advance.
Takes the extra parameters ISO file ID and directory file name to facilitate ISO 7816-4 operations.
Parameters:
applicationID - Desired application ID (3 bytes)
applicationSettings - use EV3ApplicationKeySettings.Builder to build/Set the application settings.
isoFileID - 2 byte ISO/IEC 7816-4 File Identifier.
dfName - 0 to 16 bytes ISO/IEC 7816-4 DF Name for this application.
 */
            byte[] APPLICATION_ID = Utils.hexStringToByteArray("010000");
            byte[] ISO_APPLICATION_ID = Utils.hexStringToByteArray("10E1"); // the AID is E110 but written in low endian
            byte[] ISO_APPLICATION_DF = Utils.hexStringToByteArray("D2760000850101"); // this is the AID for NDEF
            //byte APPLICATION_KEY_SETTINGS = (byte) 0x0F;
            byte APPLICATION_NUMBER_OF_KEYS = (byte) 0x5; // number of keys: 5, AES keys
            //byte COMMUNICATION_SETTINGS = (byte) 0x0f;
            int FILE_ID_01 = 1;
            byte[] ISO_FILE_ID_01 = Utils.hexStringToByteArray("03E1"); // the file ID is E103 but written as low endian
            int FILE_01_SIZE = 15;

            EV3ApplicationKeySettings applicationSettings = new EV3ApplicationKeySettings.Builder()
                    .setMaxNumberOfApplicationKeys(APPLICATION_NUMBER_OF_KEYS)
                    // trying to use AES
                    //.setKeyTypeOfApplicationKeys(KeyType.THREEDES)
                    .setKeyTypeOfApplicationKeys(KeyType.AES128)
                    .setIsoFileIdentifierPresent(true)
                    // application key settings
                    .setAppKeySettingsChangeable(true)
                    .setAuthenticationRequiredForDirectoryConfigurationData(false)
                    .setAppDeletionWithAppMasterKey(false)
                    .setAuthenticationRequiredForFileManagement(false)
                    .build();
            Log.d(TAG, "step 2: create new application 00 00 01");
            desFireEV3.createApplication(APPLICATION_ID, applicationSettings, ISO_APPLICATION_ID, ISO_APPLICATION_DF);

            // step 3: select the new application '000001'
            Log.d(TAG, "step 3: select new application 00 00 01");
            desFireEV3.selectApplication(APPLICATION_ID);

            // step 4: create a standard data file 01
/*
void createFile(int fileNumber,
                byte[] isoFileID,
                DESFireEV3File.EV3FileSettings fileSettings)
Creates a file within a DESFire EV3 application. This API allows flexibility to use ISO file ID.
This method also accepts the ISO file ID that is used in ISO 7816-4 operations.
Parameters:
fileNumber - File to be created with this file number.
isoFileID - ISO file identifier of file.
fileSettings - File to be created with these file settings which are present in FileSettings Object.use builders of different derived class of FileSettings to create file settings object,
 */
            byte readAccess01 = (byte) (0x0E);
            byte writeAccess01 = (byte) (0x0E);
            byte readWriteAccess01 = (byte) (0x0E);
            byte changeAccess01 = (byte) (0x0E);
            DESFireEV3File.StdEV3DataFileSettings fileSettings01;
            fileSettings01 = new DESFireEV3File.StdEV3DataFileSettings(IDESFireEV1.CommunicationType.Plain, readAccess01, writeAccess01, readWriteAccess01, changeAccess01, FILE_01_SIZE);
            Log.d(TAG, "step 4: create a standard data file 01");
            desFireEV3.createFile(FILE_ID_01, ISO_FILE_ID_01, fileSettings01);

            // step 05: write to standard file 01
            // byte[] NDEF_CONTAINER = Utils.hexStringToByteArray("000F20003A00340406E10408000000"); // 2048 bytes
            byte[] NDEF_CONTAINER = Utils.hexStringToByteArray("000F20003A00340406E10401000000"); // 256 byte

/*
void writeData(int fileNumber,
               int offset,
               byte[] data)
The writeData command allows to write data to a Standard Data File or a Standard Backup File.
Parameters:
fileNumber - File Number to which data is written.
offset - Offset within the data file
data - Data to be written to the file
 */
            Log.d(TAG, "step 5: write to the standard data file 01 (NDEF container)");
            desFireEV3.writeData(FILE_ID_01, 0, NDEF_CONTAINER);


            // step 06a: authenticate with app master key
            //Log.d(TAG, "step 06a: authenticate with DES app master key");
            Log.d(TAG, "step 06a: authenticate with AES app master key");
            boolean success = newAesEv2Auth(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT);
            //boolean success = newAesEv2Auth(APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT);
            //boolean success = legacyDesAuth(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT);
            if (success) {
                writeToUiAppend(output, logString + " SUCCESS");
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                vibrateShort();
            } else {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
            }

            // step 06 create a standard file 02
            byte FILE_ID_02 = (byte) 0x02;
            byte[] ISO_FILE_ID_02 = Utils.hexStringToByteArray("04E1");// the file ID is E104 but written as low endian
            int FILE_02_SIZE = 256; // NDEF FileSize equal to 000100h (256 Bytes)
            byte[] ISO_DF = Utils.hexStringToByteArray("D2760000850101"); // this is the AID for NDEF

/*
public StdEV3DataFileSettings(IDESFireEV1.CommunicationType communicationType,
                              byte readAccess,
                              byte writeAccess,
                              byte readWriteAccess,
                              byte changeAccess,
                              int fileSize)
Base class Constructor.
Parameters:
communicationType - Communication settings used while accessing the file.
readAccess - Take values from 0x00 to 0xF.
0xE : free access.
0xF : read access denied.
0x00 to 0x0d -- authentication required with the key number for read access.
writeAccess - Take values from 0x00 to 0xF.
0xE : free access.
0xF : write access denied.
0x00 to 0x0d -- authentication required with the key number for write access.
readWriteAccess - Take values from 0x00 to 0xF.
0xE : free access.
0xF : read and write access denied.
0x00 to 0x0d -- authentication required with the key number for read and write access.
changeAccess - Take values from 0x00 to 0xF.
0xE : free access.
0xF : read access denied.
0x00 to 0x0d -- authentication required with the key number for changing the access rights of the file.
fileSize - Size of the Standard Data File
 */

            DESFireEV3File.StdEV3DataFileSettings fileSettings02;
            // this file is using nearly the same access settings as file 01
            //byte changeAccess02 = (byte) (0x00);
            //byte writeAccess02 = (byte) 0x00;
            fileSettings02 = new DESFireEV3File.StdEV3DataFileSettings(IDESFireEV1.CommunicationType.Plain, readAccess01, writeAccess01, readWriteAccess01, changeAccess01, FILE_02_SIZE);
            //fileSettings02 = new DESFireEV3File.StdEV3DataFileSettings(IDESFireEV1.CommunicationType.Plain, readAccess01, writeAccess01, readWriteAccess01, changeAccess01, FILE_02_SIZE);
            //fileSettings02 = new DESFireEV3File.StdEV3DataFileSettings(IDESFireEV1.CommunicationType.Plain, readAccess01, writeAccess01, readWriteAccess01, changeAccess02, FILE_02_SIZE);

            // this part is to add SDM enabling
            fileSettings02.setSDMEnabled(true);
            fileSettings02.setUIDMirroringEnabled(false);
            byte[] sdmUidOffset = new byte[]{(byte) 0x10, (byte) 0x00, (byte) 0x00};
            fileSettings02.setUidOffset(sdmUidOffset);
            //byte[] sdmPiccOffset = new byte[]{(byte) 0x20, (byte) 0x00, (byte) 0x00};
            //fileSettings02.setPiccDataOffset(sdmPiccOffset);
            fileSettings02.setSDMReadCounterEnabled(true);
            byte[] sdmReadCounterOffset = new byte[]{(byte) 0x20, (byte) 0x00, (byte) 0x00};
            fileSettings02.setSdmReadCounterOffset(sdmReadCounterOffset);

            Log.d(TAG, "step 6: create a standard data file 02");
            desFireEV3.createFile(FILE_ID_02, ISO_FILE_ID_02, fileSettings02);

            // step 7: write to standard file 02
/*
NTAG 424 DNA and NTAG 424 DNA TagTamper features and hints AN12196.pdf pages 30 ff
Note: offset won't fit using this content template - use the one I'm providing
6.7 Prepare NDEF message
01 NDEF File Content format https://choose.url.com/ntag424?e=00000000000000000000000000000000&c=0000000000000000
02 NDEF File Content in Hex: 63686F6F73652E75726C2E636F6D2F6E7461673432343F653D303030303030303030303030303030303030303030303030303030303030303026633D30303030303030303030303030303030000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
03 NDEF Length + NDEF header: 0051 + D1014D5504
04 Size of data – useful for Lc in APDUs: 80 (128d)
08 UID Offset (in Bytes): 20 (49d) (NDEF Length + NDEF header Length + NDEF File Content Length, including “=” sign in “? e=”)
10 CMAC Input Offset (in Bytes): 43 (67d): Fully configurable. Verification side needs to know this value to check validity of received CMAC
11: CMAC Offset (in Bytes):      43 (67d): including "=" sign in "&c=
 */

            // use Android's NDEF classes to construct the NDEF message
            // this is the data from Feature and Hints with offsets:
            // byte[] sdmPiccOffset = new byte[]{(byte) 0x20, (byte) 0x00, (byte) 0x00}; // 20h = 32d, counting from s in https
            // byte[] sdmMacOffset = new byte[]{(byte) 0x43, (byte) 0x00, (byte) 0x00};  // 43h = 67d, counting from s in https
            String ndefSampleUrlWrong = "https://choose.url.com/ntag424?e=00000000000000000000000000000000&c=0000000000000000";
            //String ndefSampleUrl = "https://choose.aburl.com/ntag424?e=00000000000000000000000000000000&c=0000000000000000";
            //NdefRecord ndefRecord = NdefRecord.createUri(ndefSampleUrl);

            String ndefSampleBackendUrl = "https://sdm.nfcdeveloper.com/tag?picc_data=00000000000000000000000000000000&cmac=0000000000000000";
            // BackendUrl offset: PICC data: 40d = 28h, cmac: 78d = 4Eh
            NdefRecord ndefRecord = NdefRecord.createUri(ndefSampleBackendUrl);

            NdefMessage ndefMessage = new NdefMessage(ndefRecord);
            byte[] ndefMessageBytesHeadless = ndefMessage.toByteArray();
            // 2 bytes are missing: 0x0062
            writeToUiAppend(output, printData("ndefMessageBytesHeadless", ndefMessageBytesHeadless));
            byte[] ndefMessageBytes = new byte[ndefMessageBytesHeadless.length + 2];
            System.arraycopy(new byte[]{(byte) 0x00, (byte) (ndefMessageBytesHeadless.length)}, 0, ndefMessageBytes, 0, 2);
            System.arraycopy(ndefMessageBytesHeadless, 0, ndefMessageBytes, 2, ndefMessageBytesHeadless.length);
            writeToUiAppend(output, "ndefSampleUrl: " + ndefSampleBackendUrl);
            writeToUiAppend(output, printData("ndefMessageBytes", ndefMessageBytes));

            /*
            // data from NTAG424DNA Feature & Hints
            byte[] ndefHeaderUrl = Utils.hexStringToByteArray("0022D1011E5504"); // 7 bytes // append: 0022
            byte[] ndefSampleData = Utils.hexStringToByteArray("63686F6F73652E75726C2E636F6D2F6E7461673432343F653D303030303030303030303030303030303030303030303030303030303030303026633D30303030303030303030303030303030000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
            // https://choose.url.com/ntag424?e=00000000000000000000000000000000&c=0000000000000000
            writeToUiAppend(output, "ndefSampleData: " + new String(ndefSampleData, StandardCharsets.UTF_8));
            writeToUiAppend(output, printData("ndefSampleData",ndefSampleData));

            byte[] ndefMessage = new byte[(ndefHeaderUrl.length) + ndefSampleData.length];
            System.arraycopy(ndefHeaderUrl, 0, ndefMessage, 0, ndefHeaderUrl.length);
            System.arraycopy(ndefSampleData, 0, ndefMessage, ndefHeaderUrl.length, ndefSampleData.length);

            //ndefMessage =  Utils.hexStringToByteArray("0056D10152550463686F6F73652E75726C2E636F6D2F6E7461673432343F653D3030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303939");
            ndefMessage =  Utils.hexStringToByteArray("0058D10154550463686F6F73652E75726C2E636F6D2F6E7461673432343F653D30303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303039393939");
            //ndefMessage = Utils.hexStringToByteArray("0076D10152750463686F6F73652E75726C2E636F6D2F6E7461673432343F653D30303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303939");
            // ndefMessage = Utils.hexStringToByteArray("0056D101525504 63686F6F73652E75726C2E636F6D2F6E7461673432343F653D3030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303939");
            writeToUiAppend(output, printData("ndefMessage",ndefMessage));
             */
/*
# NDEF file contents:
[000] 00 56 D1 01 52 55 04 63 68 6F 6F 73 65 2E 75 72 |.V..RU.choose.ur|
[010] 6C 2E 63 6F 6D 2F 6E 74 61 67 34 32 34 3F 65 3D |l.com/ntag424?e=|
[020] 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
[030] 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
[040] 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
[050] 30 30 30 30 30 30 39 39 00 00 00 00 00 00 00 00 |00000099........|
 */


            // working ndefMessage after writing by NfcNdefExample
            //byte[] ndefMessageExample = Utils.hexStringToByteArray("0022D1011E5504736F6D652E6578616D706C652E636F6D26613D31323334353637383930");
            //                                                                                some.example.com&a=1234567890  29 chars = 0x13
            //                                                                                choose.url.com/ntag424?e=0000  29 chars = 0x13
            //writeToUiAppend(output, printData("ndefMessageExample",ndefMessageExample));

            Log.d(TAG, "step 7: write to the standard data file 02 (NDEF container)");
            //desFireEV3.writeData(FILE_ID_02, 0, ndefMessage);
            desFireEV3.writeData(FILE_ID_02, 0, ndefMessageBytes);

            Log.d(TAG, "generation of a NDEF file on the DESFire EV3 tag finished");
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    private boolean createANdefFileSdmEnabled(String logString) {
        Log.d(TAG, logString);

        // status WORKING
        // will create an empty NDEF message, maximum size 256 byte

        // check enabled by reading the file settings
        // see NTAG 424 DNA and NTAG 424 DNA TagTamper features and hints AN12196.pdf pages 26 + 27

        try {
            // this script will run several steps
            Log.d(TAG, "this script will generate a NDEF file on the DESFire EV3 tag");

            // step 1: select Master Application
            Log.d(TAG, "step 1: select Master Application 00 00 00");
            desFireEV3.selectApplication(0);

            // step 2: CreateApplication using the default AID 000001h
/*
void createApplication(byte[] applicationID,
                       EV3ApplicationKeySettings applicationSettings,
                       byte[] isoFileID,
                       byte[] dfName)
Creates new applications on the PICC. The application is initialized according to the given settings.
The application keys of the active key set are initialized with the Default Application Key.
Note: PICC Application (AID 0) has to be selected in advance.
Takes the extra parameters ISO file ID and directory file name to facilitate ISO 7816-4 operations.
Parameters:
applicationID - Desired application ID (3 bytes)
applicationSettings - use EV3ApplicationKeySettings.Builder to build/Set the application settings.
isoFileID - 2 byte ISO/IEC 7816-4 File Identifier.
dfName - 0 to 16 bytes ISO/IEC 7816-4 DF Name for this application.
 */
            byte[] APPLICATION_ID = Utils.hexStringToByteArray("010000");
            byte[] ISO_APPLICATION_ID = Utils.hexStringToByteArray("10E1"); // the AID is E110 but written in low endian
            byte[] ISO_APPLICATION_DF = Utils.hexStringToByteArray("D2760000850101"); // this is the AID for NDEF
            //byte APPLICATION_KEY_SETTINGS = (byte) 0x0F;
            byte APPLICATION_NUMBER_OF_KEYS = (byte) 0x1; // number of keys: 1, TDES keys
            //byte COMMUNICATION_SETTINGS = (byte) 0x0f;
            int FILE_ID_01 = 1;
            byte[] ISO_FILE_ID_01 = Utils.hexStringToByteArray("03E1"); // the file ID is E103 but written as low endian
            int FILE_01_SIZE = 15;

            EV3ApplicationKeySettings applicationSettings = new EV3ApplicationKeySettings.Builder()
                    .setMaxNumberOfApplicationKeys(APPLICATION_NUMBER_OF_KEYS)
                    // trying to use AES
                    //.setKeyTypeOfApplicationKeys(KeyType.THREEDES)
                    .setKeyTypeOfApplicationKeys(KeyType.AES128)
                    .setIsoFileIdentifierPresent(true)
                    // application key settings
                    .setAppKeySettingsChangeable(true)
                    .setAuthenticationRequiredForDirectoryConfigurationData(false)
                    .setAppDeletionWithAppMasterKey(false)
                    .setAuthenticationRequiredForFileManagement(false)
                    .build();
            Log.d(TAG, "step 2: create new application 00 00 01");
            desFireEV3.createApplication(APPLICATION_ID, applicationSettings, ISO_APPLICATION_ID, ISO_APPLICATION_DF);

            // step 3: select the new application '000001'
            Log.d(TAG, "step 3: select new application 00 00 01");
            desFireEV3.selectApplication(APPLICATION_ID);

            // step 4: create a standard data file 01
/*
void createFile(int fileNumber,
                byte[] isoFileID,
                DESFireEV3File.EV3FileSettings fileSettings)
Creates a file within a DESFire EV3 application. This API allows flexibility to use ISO file ID.
This method also accepts the ISO file ID that is used in ISO 7816-4 operations.
Parameters:
fileNumber - File to be created with this file number.
isoFileID - ISO file identifier of file.
fileSettings - File to be created with these file settings which are present in FileSettings Object.use builders of different derived class of FileSettings to create file settings object,
 */
            byte readAccess01 = (byte) (0x0E);
            byte writeAccess01 = (byte) (0x0E);
            byte readWriteAccess01 = (byte) (0x0E);
            byte changeAccess01 = (byte) (0x0E);
            DESFireEV3File.StdEV3DataFileSettings fileSettings01;
            fileSettings01 = new DESFireEV3File.StdEV3DataFileSettings(IDESFireEV1.CommunicationType.Plain, readAccess01, writeAccess01, readWriteAccess01, changeAccess01, FILE_01_SIZE);
            Log.d(TAG, "step 4: create a standard data file 01");
            desFireEV3.createFile(FILE_ID_01, ISO_FILE_ID_01, fileSettings01);

            // step 05: write to standard file 01
            // byte[] NDEF_CONTAINER = Utils.hexStringToByteArray("000F20003A00340406E10408000000"); // 2048 bytes
            byte[] NDEF_CONTAINER = Utils.hexStringToByteArray("000F20003A00340406E10401000000"); // 256 byte

/*
void writeData(int fileNumber,
               int offset,
               byte[] data)
The writeData command allows to write data to a Standard Data File or a Standard Backup File.
Parameters:
fileNumber - File Number to which data is written.
offset - Offset within the data file
data - Data to be written to the file
 */
            Log.d(TAG, "step 5: write to the standard data file 01 (NDEF container)");
            desFireEV3.writeData(FILE_ID_01, 0, NDEF_CONTAINER);


            // step 06a: authenticate with app master key
            //Log.d(TAG, "step 06a: authenticate with DES app master key");
            Log.d(TAG, "step 06a: authenticate with AES app master key");
            boolean success = newAesEv2Auth(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT);
            //boolean success = legacyDesAuth(logString, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT);
            if (success) {
                writeToUiAppend(output, logString + " SUCCESS");
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                vibrateShort();
            } else {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
            }

            // step 06 create a standard file 02
            byte FILE_ID_02 = (byte) 0x02;
            byte[] ISO_FILE_ID_02 = Utils.hexStringToByteArray("04E1");// the file ID is E104 but written as low endian
            int FILE_02_SIZE = 256; // NDEF FileSize equal to 000100h (256 Bytes)
            byte[] ISO_DF = Utils.hexStringToByteArray("D2760000850101"); // this is the AID for NDEF

/*
public StdEV3DataFileSettings(IDESFireEV1.CommunicationType communicationType,
                              byte readAccess,
                              byte writeAccess,
                              byte readWriteAccess,
                              byte changeAccess,
                              int fileSize)
Base class Constructor.
Parameters:
communicationType - Communication settings used while accessing the file.
readAccess - Take values from 0x00 to 0xF.
0xE : free access.
0xF : read access denied.
0x00 to 0x0d -- authentication required with the key number for read access.
writeAccess - Take values from 0x00 to 0xF.
0xE : free access.
0xF : write access denied.
0x00 to 0x0d -- authentication required with the key number for write access.
readWriteAccess - Take values from 0x00 to 0xF.
0xE : free access.
0xF : read and write access denied.
0x00 to 0x0d -- authentication required with the key number for read and write access.
changeAccess - Take values from 0x00 to 0xF.
0xE : free access.
0xF : read access denied.
0x00 to 0x0d -- authentication required with the key number for changing the access rights of the file.
fileSize - Size of the Standard Data File
 */

            DESFireEV3File.StdEV3DataFileSettings fileSettings02;
            // this file is using nearly the same access settings as file 01
            byte changeAccess02 = (byte) (0x00);
            fileSettings02 = new DESFireEV3File.StdEV3DataFileSettings(IDESFireEV1.CommunicationType.Plain, readAccess01, writeAccess01, readWriteAccess01, changeAccess02, FILE_02_SIZE);

            // this part is to add SDM enabling
            fileSettings02.setSDMEnabled(true);
            fileSettings02.setUIDMirroringEnabled(true);
            byte[] sdmUidOffset = new byte[]{(byte) 0x10, (byte) 0x00, (byte) 0x00};
            fileSettings02.setUidOffset(sdmUidOffset);
            byte[] sdmPiccOffset = new byte[]{(byte) 0x20, (byte) 0x00, (byte) 0x00};
            fileSettings02.setUidOffset(sdmPiccOffset);

            Log.d(TAG, "step 6: create a standard data file 02");
            desFireEV3.createFile(FILE_ID_02, ISO_FILE_ID_02, fileSettings02);

            // step 7: write to standard file 02
            Log.d(TAG, "step 7: write to the standard data file 02 (NDEF container)");
            byte[] NDEF_FILE_02 = Utils.hexStringToByteArray("0000"); // empty NDEF container
            desFireEV3.writeData(FILE_ID_02, 0, NDEF_FILE_02);

            Log.d(TAG, "generation of a NDEF file on the DESFire EV3 tag finished");
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    private boolean createANdefFile(String logString) {
        Log.d(TAG, logString);

        // status WORKING
        // will create an empty NDEF message, maximum size 256 byte

        try {
            // this script will run several steps
            Log.d(TAG, "this script will generate a NDEF file on the DESFire EV3 tag");

            // step 1: select Master Application
            Log.d(TAG, "step 1: select Master Application 00 00 00");
            desFireEV3.selectApplication(0);

            // step 2: CreateApplication using the default AID 000001h
/*
void createApplication(byte[] applicationID,
                       EV3ApplicationKeySettings applicationSettings,
                       byte[] isoFileID,
                       byte[] dfName)
Creates new applications on the PICC. The application is initialized according to the given settings.
The application keys of the active key set are initialized with the Default Application Key.
Note: PICC Application (AID 0) has to be selected in advance.
Takes the extra parameters ISO file ID and directory file name to facilitate ISO 7816-4 operations.
Parameters:
applicationID - Desired application ID (3 bytes)
applicationSettings - use EV3ApplicationKeySettings.Builder to build/Set the application settings.
isoFileID - 2 byte ISO/IEC 7816-4 File Identifier.
dfName - 0 to 16 bytes ISO/IEC 7816-4 DF Name for this application.
 */
            byte[] APPLICATION_ID = Utils.hexStringToByteArray("010000");
            byte[] ISO_APPLICATION_ID = Utils.hexStringToByteArray("10E1"); // the AID is E110 but written in low endian
            byte[] ISO_APPLICATION_DF = Utils.hexStringToByteArray("D2760000850101"); // this is the AID for NDEF
            //byte APPLICATION_KEY_SETTINGS = (byte) 0x0F;
            byte APPLICATION_NUMBER_OF_KEYS = (byte) 0x1; // number of keys: 1, TDES keys
            //byte COMMUNICATION_SETTINGS = (byte) 0x0f;
            int FILE_ID_01 = 1;
            byte[] ISO_FILE_ID_01 = Utils.hexStringToByteArray("03E1"); // the file ID is E103 but written as low endian
            int FILE_01_SIZE = 15;

            EV3ApplicationKeySettings applicationSettings = new EV3ApplicationKeySettings.Builder()
                    .setMaxNumberOfApplicationKeys(APPLICATION_NUMBER_OF_KEYS)
                    .setKeyTypeOfApplicationKeys(KeyType.THREEDES)
                    .setIsoFileIdentifierPresent(true)
                    // application key settings
                    .setAppKeySettingsChangeable(true)
                    .setAuthenticationRequiredForDirectoryConfigurationData(false)
                    .setAppDeletionWithAppMasterKey(false)
                    .setAuthenticationRequiredForFileManagement(false)
                    .build();
            Log.d(TAG, "step 2: create new application 00 00 01");
            desFireEV3.createApplication(APPLICATION_ID, applicationSettings, ISO_APPLICATION_ID, ISO_APPLICATION_DF);

            // step 3: select the new application '000001'
            Log.d(TAG, "step 3: select new application 00 00 01");
            desFireEV3.selectApplication(APPLICATION_ID);

            // step 4: create a standard data file 01
/*
void createFile(int fileNumber,
                byte[] isoFileID,
                DESFireEV3File.EV3FileSettings fileSettings)
Creates a file within a DESFire EV3 application. This API allows flexibility to use ISO file ID.
This method also accepts the ISO file ID that is used in ISO 7816-4 operations.
Parameters:
fileNumber - File to be created with this file number.
isoFileID - ISO file identifier of file.
fileSettings - File to be created with these file settings which are present in FileSettings Object.use builders of different derived class of FileSettings to create file settings object,
 */
            byte readAccess01 = (byte) (0x0E);
            byte writeAccess01 = (byte) (0x0E);
            byte readWriteAccess01 = (byte) (0x0E);
            byte changeAccess01 = (byte) (0x0E);
            DESFireEV3File.StdEV3DataFileSettings fileSettings01;
            fileSettings01 = new DESFireEV3File.StdEV3DataFileSettings(IDESFireEV1.CommunicationType.Plain, readAccess01, writeAccess01, readWriteAccess01, changeAccess01, FILE_01_SIZE);
            Log.d(TAG, "step 4: create a standard data file 01");
            desFireEV3.createFile(FILE_ID_01, ISO_FILE_ID_01, fileSettings01);

            // step 05: write to standard file 01
            // byte[] NDEF_CONTAINER = Utils.hexStringToByteArray("000F20003A00340406E10408000000"); // 2048 bytes
            byte[] NDEF_CONTAINER = Utils.hexStringToByteArray("000F20003A00340406E10401000000"); // 256 byte

/*
void writeData(int fileNumber,
               int offset,
               byte[] data)
The writeData command allows to write data to a Standard Data File or a Standard Backup File.
Parameters:
fileNumber - File Number to which data is written.
offset - Offset within the data file
data - Data to be written to the file
 */
            Log.d(TAG, "step 5: write to the standard data file 01 (NDEF container)");
            desFireEV3.writeData(FILE_ID_01, 0, NDEF_CONTAINER);

            // step 06 create a standard file 02

            byte FILE_ID_02 = (byte) 0x02;
            byte[] ISO_FILE_ID_02 = Utils.hexStringToByteArray("04E1");// the file ID is E104 but written as low endian
            int FILE_02_SIZE = 256; // NDEF FileSize equal to 000100h (256 Bytes)
            byte[] ISO_DF = Utils.hexStringToByteArray("D2760000850101"); // this is the AID for NDEF

/*
public StdEV3DataFileSettings(IDESFireEV1.CommunicationType communicationType,
                              byte readAccess,
                              byte writeAccess,
                              byte readWriteAccess,
                              byte changeAccess,
                              int fileSize)
Base class Constructor.
Parameters:
communicationType - Communication settings used while accessing the file.
readAccess - Take values from 0x00 to 0xF.
0xE : free access.
0xF : read access denied.
0x00 to 0x0d -- authentication required with the key number for read access.
writeAccess - Take values from 0x00 to 0xF.
0xE : free access.
0xF : write access denied.
0x00 to 0x0d -- authentication required with the key number for write access.
readWriteAccess - Take values from 0x00 to 0xF.
0xE : free access.
0xF : read and write access denied.
0x00 to 0x0d -- authentication required with the key number for read and write access.
changeAccess - Take values from 0x00 to 0xF.
0xE : free access.
0xF : read access denied.
0x00 to 0x0d -- authentication required with the key number for changing the access rights of the file.
fileSize - Size of the Standard Data File
 */

            DESFireEV3File.StdEV3DataFileSettings fileSettings02;
            // this file is using nearly the same access settings as file 01
            byte changeAccess02 = (byte) (0x00);
            fileSettings02 = new DESFireEV3File.StdEV3DataFileSettings(IDESFireEV1.CommunicationType.Plain, readAccess01, writeAccess01, readWriteAccess01, changeAccess02, FILE_02_SIZE);
            Log.d(TAG, "step 6: create a standard data file 02");
            desFireEV3.createFile(FILE_ID_02, ISO_FILE_ID_02, fileSettings02);

            // step 7: write to standard file 02
            Log.d(TAG, "step 7: write to the standard data file 02 (NDEF container)");
            byte[] NDEF_FILE_02 = Utils.hexStringToByteArray("0000"); // empty NDEF container
            desFireEV3.writeData(FILE_ID_02, 0, NDEF_FILE_02);

            Log.d(TAG, "generation of a NDEF file on the DESFire EV3 tag finished");
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    private byte[] readFromStandardBackupFile(String logString) {
        Log.d(TAG, logString);
        byte[] data;
        try {
            int offset = 0; // read from the beginning
            int length = 0; // read complete file
            data = desFireEV3.readData(selectedFileIdInt, offset, length);
            return data;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a read access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return null;
    }

    private boolean writeToStandardBackupFile(String logString, byte[] dataToWrite) {
        Log.d(TAG, logString + " data: " + Utilities.byteToHexString(dataToWrite));
        try {
            int offset = 0; // write from the beginning
            int length = 0; // write complete file
            desFireEV3.writeData(selectedFileIdInt, offset, dataToWrite);
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
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
     * section for value files
     */

    private boolean createAValueFile(String logString, int fileNumber, IDESFireEV1.CommunicationType communicationType, int rwKeyInt, int carKeyInt, int rKeyInt, int wKeyInt, int lowerLimit, int upperLimit, int initialValue) {
        Log.d(TAG, logString);
        try {
            byte readAccess = (byte) (rKeyInt & 0xff);
            byte writeAccess = (byte) (wKeyInt & 0xff);
            byte readWriteAccess = (byte) (rwKeyInt & 0xff);
            byte changeAccess = (byte) (carKeyInt & 0xff);
            DESFireFile.FileSettings fileSettings;
            boolean limitedCreditValueEnabled = false;
            boolean getFreeValueEnabled = false;
            fileSettings = new DESFireFile.ValueFileSettings(communicationType, readAccess, writeAccess, readWriteAccess, changeAccess, lowerLimit, upperLimit, initialValue, limitedCreditValueEnabled, getFreeValueEnabled);
            desFireEV3.createFile(fileNumber, fileSettings);
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;

    }

    private int readFromAValueFile(String logString) {
        Log.d(TAG, logString);
        int data;
        try {
            data = desFireEV3.getValue(selectedFileIdInt);
            return data;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a read access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return -1;
    }

    private boolean creditAValueFile(String logString, int changeValue) {
        Log.d(TAG, logString + " creditValue " + changeValue);
        try {
            desFireEV3.credit(selectedFileIdInt, changeValue);
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a read access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    private boolean debitAValueFile(String logString, int changeValue) {
        Log.d(TAG, logString + " debitValue " + changeValue);
        try {
            desFireEV3.debit(selectedFileIdInt, changeValue);
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a read access key ?");
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
     * section for linear & cyclic record files
     */

    private boolean createARecordFile(String logString, boolean isLinearRecordFile, int fileNumber, IDESFireEV1.CommunicationType communicationType, int rwKeyInt, int carKeyInt, int rKeyInt, int wKeyInt, int recordSize, int maxNrOfRecords, int currNoOfRecords) {
        Log.d(TAG, logString);
        try {
            byte readAccess = (byte) (rKeyInt & 0xff);
            byte writeAccess = (byte) (wKeyInt & 0xff);
            byte readWriteAccess = (byte) (rwKeyInt & 0xff);
            byte changeAccess = (byte) (carKeyInt & 0xff);
            DESFireFile.FileSettings fileSettings;
            if (isLinearRecordFile) {
                Log.d(TAG, "create a Linear Record File, recordSize: " + recordSize + " maxNrOfRecords: " + maxNrOfRecords + " currNoOfRecords: " + currNoOfRecords);
                fileSettings = new DESFireFile.LinearRecordFileSettings(communicationType, readAccess, writeAccess, readWriteAccess, changeAccess, recordSize, maxNrOfRecords, currNoOfRecords);
            } else {
                Log.d(TAG, "create a Cyclic Record File, recordSize: " + recordSize + " maxNrOfRecords: " + maxNrOfRecords + " currNoOfRecords: " + currNoOfRecords);
                fileSettings = new DESFireFile.CyclicRecordFileSettings(communicationType, readAccess, writeAccess, readWriteAccess, changeAccess, recordSize, maxNrOfRecords, currNoOfRecords);
            }
            desFireEV3.createFile(fileNumber, fileSettings);
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    private byte[] readFromARecordFile(String logString, int offsetRecords, int noOfRecords) {
        Log.d(TAG, logString);
        byte[] data;
        try {
            data = desFireEV3.readRecords(selectedFileIdInt, offsetRecords, noOfRecords);
            return data;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a read access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppend(output, logString + " Exception occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return null;
    }

    private boolean writeToARecordFile(String logString, byte[] dataToWrite) {
        Log.d(TAG, logString + " data: " + Utilities.byteToHexString(dataToWrite));
        try {
            int offsetInRecord = 0; // write from the beginning
            desFireEV3.writeRecord(selectedFileIdInt, offsetInRecord, dataToWrite);
            return true;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppend(errorCode, "Did you forget to authenticate with a write access key ?");
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
     * section for write commit
     */

    private boolean commitATransaction(String logString) {
        try {
            Log.d(TAG, "COMMIT " + logString);
            desFireEV3.commitTransaction();
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
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * section for authentication
     */

    private boolean legacyDesAuth(String logString, int keyNumber, byte[] keyToAuthenticate) {
        try {
            Log.d(TAG, logString + " keyNumber " + keyNumber + " keyToAuthenticate " + Utilities.dumpBytes(keyToAuthenticate));
            // build a TDES key from DES
            byte[] tdesKey = new byte[16];
            System.arraycopy(keyToAuthenticate, 0, tdesKey, 0, 8);
            System.arraycopy(keyToAuthenticate, 0, tdesKey, 8, 8);
            SecretKey originalKey = new SecretKeySpec(tdesKey, 0, tdesKey.length, "TDES");
            KeyData keyData = new KeyData();
            keyData.setKey(originalKey);
            desFireEV3.authenticate(keyNumber, IDESFireEV1.AuthType.Native, KeyType.THREEDES, keyData);
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
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    private boolean legacyAesAuth(String logString, int keyNumber, byte[] keyToAuthenticate) {
        try {
            Log.d(TAG, logString + " keyNumber " + keyNumber + " keyToAuthenticate " + Utilities.dumpBytes(keyToAuthenticate));
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
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    private boolean ev2AesAuth(String logString, int keyNumber, byte[] keyToAuthenticate) {
        try {
            Log.d(TAG, logString + " keyNumber " + keyNumber + " keyToAuthenticate " + Utilities.dumpBytes(keyToAuthenticate));
            /*
            SecretKey originalKey = new SecretKeySpec(keyToAuthenticate, 0, keyToAuthenticate.length, "AES");
            KeyData keyData = new KeyData();
            keyData.setKey(originalKey);
            desFireEV3.authenticate(keyNumber, IDESFireEV1.AuthType.AES, KeyType.AES128, keyData);
             */
            KeyData keyData = getAesKeyFromByteArray(keyToAuthenticate);
            byte[] pCDcap2 = new byte[]{0, 0, 0, 0, 0, 0};
            desFireEV3.authenticateEV2First(keyNumber, keyData, pCDcap2);
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
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
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
     * section for change key
     */

    private boolean changeKeyDes(String logString, int keyNumberToAuthenticate, byte[] keyToAuthenticate, int keyNumberToChange, byte[] newKey, byte[] oldKey) {
        try {
            Log.d(TAG, logString + " keyNumberToAuthenticate " + keyNumberToAuthenticate + " keyToAuthenticate " + Utilities.dumpBytes(keyToAuthenticate));
            Log.d(TAG, "keyNumberToChange " + keyNumberToChange + printData(" oldKey", oldKey) + " | " + printData("newKey", newKey));
            Log.d(TAG, "authenticate the change");
            desFireEV3.authenticate(keyNumberToAuthenticate, IDESFireEV1.AuthType.Native, KeyType.THREEDES, getDesKeyFromByteArray(keyToAuthenticate));
            Log.d(TAG, "change the key");
            //EV3ApplicationKeySettings.Builder ev3ApplicationKeySettings = new EV3ApplicationKeySettings.Builder();
            byte newKeyVersion = (byte) 0x00;
            desFireEV3.changeKey(keyNumberToChange, KeyType.THREEDES, getTdesKeyFromDesKeyByteArray(oldKey), getTdesKeyFromDesKeyByteArray(newKey), newKeyVersion);
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
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
            e.printStackTrace();
        }
        return false;
    }

    private boolean changeKeyAes(String logString, int keyNumberToAuthenticate, byte[] keyToAuthenticate, int keyNumberToChange, byte[] newKey, byte[] oldKey) {
        try {
            Log.d(TAG, logString + " keyNumberToAuthenticate " + keyNumberToAuthenticate + " keyToAuthenticate " + Utilities.dumpBytes(keyToAuthenticate));
            Log.d(TAG, "keyNumberToChange " + keyNumberToChange + printData(" oldKey", oldKey) + " | " + printData("newKey", newKey));
            Log.d(TAG, "authenticate the change");
            desFireEV3.authenticate(keyNumberToAuthenticate, IDESFireEV1.AuthType.AES, KeyType.AES128, getAesKeyFromByteArray(keyToAuthenticate));
            Log.d(TAG, "change the key");
            byte newKeyVersion = (byte) 0x00;
            desFireEV3.changeKey(keyNumberToChange, KeyType.AES128, oldKey, newKey, newKeyVersion);
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
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception occurred\n" + e.getMessage(), COLOR_RED);
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
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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

    private byte[] getFileIds(String logString) {
        byte[] fileIdsByteArray;
        try {
            fileIdsByteArray = desFireEV3.getFileIDs();
            Log.d(TAG, "fileSelect fileIds: " + Utilities.dumpBytes(fileIdsByteArray));
            return fileIdsByteArray;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
        return null;
    }

    private DESFireEV3File.EV3FileSettings getFileSettings(String logString, int fileNumber) {
        // see Mifare DESFire Light Features and Hints AN12343.pdf pages 24-26 getFileSettings
        // see NTAG 424 DNA and NTAG 424 DNA TagTamper features and hints AN12196.pdf pages 26-27 showing SDM options

        DESFireEV3File.EV3FileSettings fileSettings;
        try {
            fileSettings = desFireEV3.getDESFireEV3FileSettings(fileNumber);
            Log.d(TAG, "fileInformation for file " + fileSettings + " settings:\n" + fileSettings.toString());
            return fileSettings;
        } catch (InvalidResponseLengthException e) {
            Log.e(TAG, logString + " InvalidResponseLength occurred\n" + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " InvalidResponseLength occurred\n" + e.getMessage(), COLOR_RED);
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
        return null;
    }

    /**
     * section for key conversion
     */

    private byte[] getTdesKeyFromDesKeyByteArray(byte[] desKey) {
        if (desKey == null) {
            Log.e(TAG, "getTdesKeyFromDesKeyByteArray desKey is NULL, aborted");
            return null;
        }
        if (desKey.length != 8) {
            Log.e(TAG, "getTdesKeyFromDesKeyByteArray desKey length is not 8, found " + desKey.length + ", aborted");
            return null;
        }
        byte[] tdesKey = new byte[16];
        System.arraycopy(desKey, 0, tdesKey, 0, 8);
        System.arraycopy(desKey, 0, tdesKey, 8, 8);
        return tdesKey;
    }

    private KeyData getDesKeyFromByteArray(byte[] desKeyBytes) {
        // check if the key is 8 or 16 bytes long
        SecretKey originalKey;
        if ((desKeyBytes != null) && (desKeyBytes.length == 8)) {
            // build a 3KTDES key from DES
            //byte[] tdesKey = new byte[16];
            byte[] tdesKey = new byte[24];
            System.arraycopy(desKeyBytes, 0, tdesKey, 0, 8);
            System.arraycopy(desKeyBytes, 0, tdesKey, 8, 8);
            System.arraycopy(desKeyBytes, 0, tdesKey, 16, 8);
            originalKey = new SecretKeySpec(tdesKey, 0, tdesKey.length, "DESede");
        } else if ((desKeyBytes != null) && (desKeyBytes.length == 16)) {
            originalKey = new SecretKeySpec(desKeyBytes, 0, desKeyBytes.length, "DESede");
        } else {
            Log.e(TAG, "getDesKeyFromByteArray desKeyBytes length is not 8 or 16, aborted");
            return null;
        }
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
     * section for helper methods
     */

    private void vibrateShort() {
        // Make a Sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(50, 10));
        } else {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(50);
        }
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
        // Make a Sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(150, 10));
        } else {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(200);
        }
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