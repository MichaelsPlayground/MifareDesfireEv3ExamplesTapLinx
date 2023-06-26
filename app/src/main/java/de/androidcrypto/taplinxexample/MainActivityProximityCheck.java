/*
 * =============================================================================
 *
 *                       Copyright (c), NXP Semiconductors
 *
 *                        (C)NXP Electronics N.V.2013
 *         All rights are reserved. Reproduction in whole or in part is
 *        prohibited without the written consent of the copyright owner.
 *    NXP reserves the right to make changes without notice at any time.
 *   NXP makes no warranty, expressed, implied or statutory, including but
 *   not limited to any implied warranty of merchantability or fitness for any
 *  particular purpose, or that the use will not infringe any third party patent,
 *   copyright or trademark. NXP must not be liable for any loss or damage
 *                            arising from its use.
 *
 * =============================================================================
 */

package de.androidcrypto.taplinxexample;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.nxp.nfclib.CardType;
import com.nxp.nfclib.CustomModules;
import com.nxp.nfclib.KeyType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.classic.ClassicFactory;
import com.nxp.nfclib.classic.IMFClassic;
import com.nxp.nfclib.classic.IMFClassicEV1;
import com.nxp.nfclib.defaultimpl.KeyData;
import com.nxp.nfclib.desfire.DESFireFactory;
import com.nxp.nfclib.desfire.DESFireFile;
import com.nxp.nfclib.desfire.EV1ApplicationKeySettings;
import com.nxp.nfclib.desfire.EV2ApplicationKeySettings;
import com.nxp.nfclib.desfire.EV2PICCConfigurationSettings;
import com.nxp.nfclib.desfire.EV3ApplicationKeySettings;
import com.nxp.nfclib.desfire.IDESFireEV1;
import com.nxp.nfclib.desfire.IDESFireEV2;
import com.nxp.nfclib.desfire.IDESFireEV3;
import com.nxp.nfclib.exceptions.InvalidResponseLengthException;
import com.nxp.nfclib.exceptions.NxpNfcLibException;
import com.nxp.nfclib.exceptions.UsageException;
import com.nxp.nfclib.icode.ICode;
import com.nxp.nfclib.icode.ICodeFactory;
import com.nxp.nfclib.icode.IICodeDNA;
import com.nxp.nfclib.icode.IICodeSLI;
import com.nxp.nfclib.icode.IICodeSLIL;
import com.nxp.nfclib.icode.IICodeSLIS;
import com.nxp.nfclib.icode.IICodeSLIX;
import com.nxp.nfclib.icode.IICodeSLIX2;
import com.nxp.nfclib.icode.IICodeSLIXL;
import com.nxp.nfclib.icode.IICodeSLIXS;
import com.nxp.nfclib.interfaces.IKeyData;
import com.nxp.nfclib.ndef.NdefMessageWrapper;
import com.nxp.nfclib.ndef.NdefRecordWrapper;
import com.nxp.nfclib.ntag.INTAGI2Cplus;
import com.nxp.nfclib.ntag.INTag;
import com.nxp.nfclib.ntag.INTag203x;
import com.nxp.nfclib.ntag.INTag210;
import com.nxp.nfclib.ntag.INTag210u;
import com.nxp.nfclib.ntag.INTag213215216;
import com.nxp.nfclib.ntag.INTag213F216F;
import com.nxp.nfclib.ntag.INTagI2C;
import com.nxp.nfclib.ntag.NTagFactory;
import com.nxp.nfclib.plus.IPlus;
import com.nxp.nfclib.plus.IPlusEV1SL0;
import com.nxp.nfclib.plus.IPlusEV1SL1;
import com.nxp.nfclib.plus.IPlusEV1SL3;
import com.nxp.nfclib.plus.IPlusSL0;
import com.nxp.nfclib.plus.IPlusSL1;
import com.nxp.nfclib.plus.IPlusSL3;
import com.nxp.nfclib.plus.PlusFactory;
import com.nxp.nfclib.plus.PlusSL1Factory;
import com.nxp.nfclib.plus.ValueBlockInfo;
import com.nxp.nfclib.ultralight.IUltralight;
import com.nxp.nfclib.ultralight.IUltralightC;
import com.nxp.nfclib.ultralight.IUltralightEV1;
import com.nxp.nfclib.ultralight.IUltralightNano;
import com.nxp.nfclib.ultralight.UltralightFactory;
import com.nxp.nfclib.utils.NxpLogUtils;
import com.nxp.nfclib.utils.Utilities;
import java.io.File;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * @author nxp70496 Main start activity.
 */
public class MainActivityProximityCheck extends Activity {

    public static final String TAG = "SampleTapLinx";
    private IKeyData objKEY_2KTDES = null;
    private IKeyData objKEY_AES128 = null;


    /**
     * Package Key.
     */
    static String packageKey = "07e7a6e1091d445f60ce756883b42ef2";
    //static String packageKey = "a81f56f88e23e83d4617c23686cd9d22";
    /**
     * KEY_APP_MASTER key used for encrypt data.
     */
    private static final String KEY_APP_MASTER = "This is my key  ";

    /**
     * NxpNfclib instance.
     */
    private NxpNfcLib libInstance = null;
    /**
     * bytes key.
     */
    private byte[] bytesKey = null;
    /**
     * Cipher instance.
     */
    private Cipher cipher = null;
    /**
     * Iv.
     */
    private IvParameterSpec iv = null;
    /**
     * text view instance.
     */
    private TextView tv = null;
    /**
     * Image view inastance.
     */
    private ImageView mImageView = null;
    /**
     * byte array.
     */
    byte[] data;

    /**
     * Desfire card object.
     */
    private IDESFireEV1 desFireEV1;

    private IDESFireEV2 desFireEV2;

    private IDESFireEV3 desFireEV3; // todo manually added

    /**
     * Android Handler for handling messages from the threads.
     */
    private static Handler mHandler;

    private boolean mIsPerformingCardOperations = false;


    private CardType mCardType = CardType.UnknownCard;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Initialize the library and register to this activity */
        initializeLibrary();

		/* Initialize the Cipher and init vector of 16 bytes with 0xCD */
        initializeCipherinitVector();

		/* Get text view handle to be used further */
        initializeView();

		/* Set the UI handler */
        initializeUIhandler();

        /* Setup the About buttton handler */
        readMeHelpAbout();

		/* Show About dialog */
//            showDisclaimer();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Initializing the UI thread.
     */
    private void initializeUIhandler() {

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mImageView.getLayoutParams().width = (size.x * 2) / 3;
        mImageView.getLayoutParams().height = size.y / 3;

        mHandler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                Log.i(TAG, "inside handleMessage, msg.what is " + msg.what);
                String messageResponse = msg.getData().getString("message");
                char where = msg.getData().getChar("where");
                if (messageResponse != null) {
                    showMessage(messageResponse, where);
                }
            }
        };

        mImageView.setImageResource(R.drawable.product_overview);
    }


    /**
     * Initializing the widget, and Get text view handle to be used further.
     */
    private void initializeView() {

		/* Get text view handle to be used further */
        tv = (TextView) findViewById(R.id.tvLog);
        tv.setMovementMethod(new ScrollingMovementMethod());
        tv.setText(R.string.info_string);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/AvenirNextLTPro-MediumCn.otf");
        tv.setTypeface(face);
        tv.setTextColor(Color.BLACK);

		/* Get image view handle to be used further */
        mImageView = (ImageView) findViewById(R.id.cardSnap);
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
     * (non-Javadoc).
     *
     * @param intent NFC intent from the android framework.
     * @see Activity#onNewIntent(Intent)
     */
    @Override
    public void onNewIntent(final Intent intent) {
        cardLogic(intent);
        super.onNewIntent(intent);
    }


    private void cardLogic(final Intent intent) {
        CardType type = CardType.UnknownCard;
        try {
            type = libInstance.getCardType(intent);
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
                    showMessage("Unknown Error Tap Again!", 't');
                }
                break;
            case DESFireEV2:
                mCardType = CardType.DESFireEV2;
                showMessage("DESFireEV2 Card detected.", 't');
                tv.setText(" ");
                showImageSnap(R.drawable.desfire_ev2);
                showMessage("Card Detected : DESFireEV2", 'n');
                desFireEV2 = DESFireFactory.getInstance().getDESFireEV2(libInstance.getCustomModules());
                try {
                    desFireEV2.getReader().connect();
                    desfireEV2CardLogic();
                    //desfireEV2CardLogicCustom(); // seems not to work

                } catch (Throwable t) {
                    t.printStackTrace();
                    showMessage("Unknown Error Tap Again!", 't');
                }
                break;
            case DESFireEV3: // ### todo added without changing classes
                mCardType = CardType.DESFireEV3;
                showMessage("DESFireEV3 Card detected.", 't');
                tv.setText(" ");
                showImageSnap(R.drawable.desfire_ev2);
                showMessage("Card Detected : DESFireEV3", 'a');
                desFireEV3 = DESFireFactory.getInstance().getDESFireEV3(libInstance.getCustomModules());
                try {
                    desFireEV3.getReader().connect();
                    desFireEV3.getReader().setTimeout(2000);
                    desfireEV3SetVcConfigurationKey();
                    //desfireEV3CardLogic();
                    //desfireEV3CardLogicProximityCheck();
                    //desfireEV3CardLogicCustom();

                } catch (Throwable t) {
                    t.printStackTrace();
                    showMessage("Unknown Error Tap Again!", 't');
                }
                break;

        }
    }


    private void onCardNotSupported(Tag tag) {

    }

    /**
     * DESFire Pre Conditions.
     * <p/>
     * PICC Master key should be factory default settings, (ie 16 byte All zero
     * Key ).
     * <p/>
     */
    private void desfireEV1CardLogic() {

        byte[] appId = new byte[]{0x12, 0x00, 0x00};
        int fileSize = 100;
        byte[] data = new byte[]{0x11, 0x11, 0x11, 0x11,
                0x11};
        int timeOut = 2000;
        int fileNo = 0;

        tv.setText(" ");
        showImageSnap(R.drawable.desfire_ev1);
        showMessage("Card Detected : " + desFireEV1.getType().getTagName(), 'n');

        try {
            desFireEV1.getReader().setTimeout(timeOut);
            showMessage(
                    "Version of the Card : "
                            + Utilities.dumpBytes(desFireEV1.getVersion()),
                    'd');
            desFireEV1.selectApplication(0); // todo do this before getApplicationIds
            showMessage(
                    "Existing Applications Ids : " + Arrays.toString(desFireEV1.getApplicationIDs()),
                    'd');

            //desFireEV1.selectApplication(0);

            desFireEV1.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.THREEDES, objKEY_2KTDES);

            desFireEV1.getReader().close();

            // Set the custom path where logs will get stored, here we are setting the log folder DESFireLogs under
            // external storage.
            String spath = Environment.getExternalStorageDirectory().getPath() + File.separator + "DESFireLogs";
            NxpLogUtils.setLogFilePath(spath);
            // if you don't call save as below , logs will not be saved.
            NxpLogUtils.save();

        } catch (Exception e) {
            showMessage("IOException occurred... check LogCat", 't');
            e.printStackTrace();
        }

    }

    private void desfireEV2CardLogic() {
        byte[] appId = new byte[]{0x12, 0x00, 0x00};
        int fileSize = 100;
        byte[] data = new byte[]{0x11, 0x11, 0x11, 0x11, 0x11};
        int timeOut = 2000;
        int fileNo = 0;

        tv.setText(" ");
        showImageSnap(R.drawable.desfire_ev2);
        showMessage("Card Detected : " + desFireEV2.getType().getTagName(), 'n');

        try {
            desFireEV2.getReader().setTimeout(timeOut);
            showMessage(
                    "Version of the Card : "
                            + Utilities.dumpBytes(desFireEV2.getVersion()),
                    'd');
            desFireEV2.selectApplication(0); // todo run this before get applications

            showMessage(
                    "Existing Applications Ids : " + Arrays.toString(desFireEV2.getApplicationIDs()),
                    'd');


            //desFireEV2.selectApplication(0);

            desFireEV2.authenticate(0, IDESFireEV2.AuthType.Native, KeyType.THREEDES, objKEY_2KTDES);

            // Set the custom path where logs will get stored, here we are setting the log folder DESFireLogs under
            // external storage.
            String spath = Environment.getExternalStorageDirectory().getPath() + File.separator + "DESFireLogs";
            Log.i("LogNXP", "path to logs :" + spath);

            NxpLogUtils.setLogFilePath(spath);
            // if you don't call save as below , logs will not be saved.
            NxpLogUtils.save();

        } catch (Exception e) {
            showMessage("IOException occurred... check LogCat", 't');
            e.printStackTrace();
        }
    }

    private void desfireEV3SetVcConfigurationKey() {
        // AES
        byte[] AES_KEY_ZERO = hexStringToByteArray("00000000000000000000000000000000"); // 16 byte
        KeyData objKEY_AES128ZERO = new KeyData();
        SecretKeySpec secretKeySpecAesZero = new SecretKeySpec(AES_KEY_ZERO, "AES");
        objKEY_AES128ZERO.setKey(secretKeySpecAesZero);

        tv.setText(" ");
        showImageSnap(R.drawable.desfire_ev2);
        showMessage("Card Detected : " + desFireEV3.getType().getTagName(), 'n');
        showMessage("Set VC Configuration key (0x20)", 'n');
        try {
            int timeOut = 2000;
            desFireEV3.getReader().setTimeout(timeOut);
            // select master application
            desFireEV3.selectApplication(0);
            // authenticate the master application with the PICC Master Key
            // DES default key
            byte[] TDES_KEY_ZERO = new byte[16]; // 16 bytes
            KeyData objKEY_TDES128ZERO = new KeyData();
            SecretKeySpec secretKeySpecTDesZero = new SecretKeySpec(TDES_KEY_ZERO, "TDES");
            objKEY_TDES128ZERO.setKey(secretKeySpecTDesZero);
            desFireEV3.authenticate(0, IDESFireEV3.AuthType.Native, KeyType.THREEDES, objKEY_TDES128ZERO);
            showMessage("authStatus 32: " + desFireEV3.getAuthStatus(), 'n');

            showMessage("change VC Configuration key (0x20)", 'n');
            try {
                byte NEW_KEY_VERSION = (byte) 0x00;
                desFireEV3.changeVCKey(32, new byte[16], new byte[16], NEW_KEY_VERSION);
                showMessage("change VC Configuration key (0x20) done", 'n');
            } catch (UsageException e) {
                showMessage("UsageException: " + e.getMessage(), 'n');
            } catch (InvalidResponseLengthException e) {
                showMessage("InvalidResponseLengthException: " + e.getMessage(), 'n');
            } catch (Exception e) {
                showMessage("Exception: " + e.getMessage(), 'n');
            }



            //showMessage("authentication on MasterApplication done", 'd');
            //showMessage("authStatus: " + desFireEV3.getAuthStatus(), 'n');

            // authenticate with the default VC Configuration key
            //desFireEV3.authenticate(32, IDESFireEV1.AuthType.AES, KeyType.AES128, objKEY_AES128ZERO);
            //desFireEV3.authenticateEV2First(32, objKEY_AES128ZERO, new byte[]{0, 0, 0, 0, 0, 0});
            //desFireEV3.changeKey(32, KeyType.AES128, new byte[16], new byte[16], NEW_KEY_VERSION);

        } catch (Exception e) {
            showMessage("IOException occurred... check LogCat", 'n');
            showMessage("Exception: " + e.getMessage(), 'n');
            e.printStackTrace();
        }

    }

    // todo manually added
    private void desfireEV3CardLogicProximityCheck() {
        int timeOut = 2000;

        tv.setText(" ");
        showImageSnap(R.drawable.desfire_ev2);
        showMessage("Card Detected : " + desFireEV3.getType().getTagName(), 'n');

        try {
            desFireEV3.getReader().setTimeout(timeOut);

            /*
            showMessage(
                    "Version of the Card : "
                            + Utilities.dumpBytes(desFireEV3.getVersion()),
                    'd');
            desFireEV3.selectApplication(0); // todo run this before get applications

            showMessage(
                    "Existing Applications Ids : " + Arrays.toString(desFireEV3.getApplicationIDs()),
                    'd');


            //desFireEV2.selectApplication(0);
*/
            //desFireEV3.authenticate(0, IDESFireEV3.AuthType.Native, KeyType.THREEDES, objKEY_2KTDES);
            desFireEV3.authenticate(0, IDESFireEV3.AuthType.Native, KeyType.THREEDES, objKEY_2KTDES);
            showMessage("authentication on MasterApplication done", 'd');
            showMessage("authStatus: " + desFireEV3.getAuthStatus(), 'n');

            showMessage("Start Proximity Check preparations", 'n');

            // old/unused key
            byte[] AES_KEY_ZERO = hexStringToByteArray("00000000000000000000000000000000"); // 16 byte
            KeyData objKEY_AES128ZERO = new KeyData();
            SecretKeySpec secretKeySpecZero = new SecretKeySpec(AES_KEY_ZERO, "AES");
            objKEY_AES128ZERO.setKey(secretKeySpecZero);
            byte newKeyVersion = (byte) 0x00;

            showMessage("AES_KEY_ZERO: " + bytesToHexNpe(objKEY_AES128ZERO.getKey().getEncoded()), 'n');

            showMessage("step 01 authenticate Master Application done", 'n');
/*
            showMessage("step 01b getKeyVersion VC key 0x20", 'n');
            int keyVersion = desFireEV3.getKeyVersionFor(32);
            showMessage("step 01b getKeyVersion VC key 0x20 is " + keyVersion, 'n');
*/
            showMessage("step 02 change VC configuration key (0x20)", 'n');
            try {
                desFireEV3.changeKey(32, KeyType.AES128, new byte[16], new byte[16], newKeyVersion);
                //desFireEV3.changeVCKey(32, new byte[16], new byte[16], newKeyVersion);
                showMessage("step 02 change VC configuration key (0x20) done", 'n');
            } catch (UsageException e) {
                showMessage("UsageException: " + e.getMessage(), 'n');
            } catch (InvalidResponseLengthException e) {
                showMessage("InvalidResponseLengthException: " + e.getMessage(), 'n');
            } catch (Exception e) {
                showMessage("Exception: " + e.getMessage(), 'n');
            }

            showMessage("step 03 authenticate VC configuration key (0x20)", 'n');
            try {
            desFireEV3.authenticate(32, IDESFireEV3.AuthType.AES, KeyType.AES128, objKEY_AES128ZERO);
            showMessage("step 03 authenticate VC configuration key (0x20) done", 'n');
            } catch (UsageException e) {
                showMessage("UsageException: " + e.getMessage(), 'n');
            } catch (InvalidResponseLengthException e) {
                showMessage("InvalidResponseLengthException: " + e.getMessage(), 'n');
            } catch (Exception e) {
                showMessage("Exception: " + e.getMessage(), 'n');
            }



/*
            byte[] AES_KEY_VC22 = hexStringToByteArray("AA000000000000000000000000000000"); // 16 byte
            KeyData objKEY_AES128New = new KeyData();
            SecretKeySpec secretKeySpecNew = new SecretKeySpec(AES_KEY_VC22, "AES");
            objKEY_AES128New.setKey(secretKeySpecNew);

            // authenticate on old key
            desFireEV3.authenticate(34, IDESFireEV3.AuthType.Native, KeyType.AES128, objKEY_AES128ZERO);
            // gives com.nxp.nfclib.exceptions.InvalidResponseLengthException: Permission Denied

            // changing the key
            desFireEV3.changeVCKey(34, AES_KEY_ZERO, AES_KEY_VC22, newKeyVersion);
            // gives error: com.nxp.nfclib.exceptions.PICCException: Authentication Error SW2 = -82



            //desFireEV3.authenticate(34, IDESFireEV3.AuthType.Native, KeyType.AES128, objKEY_AES128ZERO);
            //desFireEV3.authenticate(34, IDESFireEV3.AuthType.Native, KeyType.THREEDES, objKEY_2KTDES);
            byte[] pcdData = new byte[0];
            desFireEV3.authenticateEV2First(34, objKEY_AES128ZERO, pcdData);
            showMessage("authentication on VC key  done", 'd');
            // com.nxp.nfclib.exceptions.InvalidResponseLengthException: Permission Denied

            // now we are authenticated

            //byte[] AES_KEY_VC22 = hexStringToByteArray("AA000000000000000000000000000000"); // 16 byte
            //byte newKeyVersion = (byte) 0x00;
            //IKeyData aesKeyVc20a = new IKeyData();
            //KeyData aesKeyVc20 = new KeyData();
            //IKeyData objKEY_AES128AA = null;
            SecretKeySpec secretKeySpec = new SecretKeySpec(AES_KEY_VC22, "AES");
            showMessage("changeVCKey before", 'd');
            desFireEV3.changeVCKey(34, AES_KEY_ZERO, AES_KEY_VC22, newKeyVersion);
            // key number 0 + 1 + 20 gives com.nxp.nfclib.exceptions.UsageException: Provided key number is not a VC key.
            // 34 gives com.nxp.nfclib.exceptions.PICCException: Authentication Error SW2 = -82 (-82 = AE)

            showMessage("changeVCKey done", 'd');
            //desFireEV3.changeVCKey(int cardkeyNumber, byte[] oldKey, byte[] newKey, byte newKeyVersion)

            KeyData objKEY_AES128AA = new KeyData();
            objKEY_AES128AA.setKey(secretKeySpec);
            showMessage("authenticate objKEY_AES128AA before", 'd');
            desFireEV3.authenticate(0, IDESFireEV3.AuthType.Native, KeyType.AES128, objKEY_AES128AA);
            showMessage("authenticate objKEY_AES128AA done", 'd');

            // let see what result we are getting
            showMessage("proximityCheckEV3 objKEY_AES128AA before", 'd');
            desFireEV3.proximityCheckEV3(objKEY_AES128AA, 1);
            showMessage("proximityCheckEV3 objKEY_AES128AA done", 'd');
*/
/*
Prepare DESFire EV3 for Proximity Check

I'm working with TapLinx SDK v3.0 on Android and trying to prepare the card for the Proximity Check. The card has factory settings so there are not keys set on PICC Master Application.

Up to now I run these steps:

`// select master file application
desFireEV3.selectApplication(0);

// authenticate with zeroed TDES key (using the keys in the sample app)
desFireEV3.authenticate(0, IDESFireEV3.AuthType.Native, KeyType.THREEDES, objKEY_2KTDES);

// old/unused key
byte[] AES_KEY_ZERO = hexStringToByteArray("00000000000000000000000000000000"); // 16 byte
KeyData objKEY_AES128ZERO = new KeyData();
SecretKeySpec secretKeySpecZero = new SecretKeySpec(AES_KEY_ZERO, "AES");
objKEY_AES128ZERO.setKey(secretKeySpecZero);

// new key
byte newKeyVersion = (byte) 0x00;
byte[] AES_KEY_VC22 = hexStringToByteArray("AA000000000000000000000000000000"); // 16 byte
KeyData objKEY_AES128New = new KeyData();
SecretKeySpec secretKeySpecNew = new SecretKeySpec(AES_KEY_VC22, "AES");
objKEY_AES128New.setKey(secretKeySpecNew);

// changing the key
desFireEV3.changeVCKey(34, AES_KEY_ZERO, AES_KEY_VC22, newKeyVersion);
// gives error: com.nxp.nfclib.exceptions.PICCException: Authentication Error SW2 = -82`

As I received an authentication error I tried to authenticate the old key number 34 with the zeroes AES key before:

`...
// authenticate on old key
desFireEV3.authenticate(34, IDESFireEV3.AuthType.Native, KeyType.AES128, objKEY_AES128ZERO);
// gives com.nxp.nfclib.exceptions.InvalidResponseLengthException: Permission Denied

// changing the key
desFireEV3.changeVCKey(34, AES_KEY_ZERO, AES_KEY_VC22, newKeyVersion);
// gives error: com.nxp.nfclib.exceptions.PICCException: Authentication Error SW2 = -82`

So how to authenticate the "old" VCKey 34 to change the VCKey to a new value ?

As far as I checked the JavaDoc the next step could be the proximity check - I would use this command (1 iterations) - is this the right way ?:

`desFireEV3.proximityCheckEV3(objKEY_AES128New, 1);`

Kind regards
Michael
 */

            /*
from API:
authenticateEV2First
void authenticateEV2First(int cardKeyNumber,
                          IKeyData key,
                          byte[] pCDcap2)
Authenticate EV2 to the PICC using single AES. After this authentication EV2 secure messaging is used. This authentication is intended to be the first in a transaction.

Parameters:
cardKeyNumber - defines the card key no to authenticate with.
key - Authentication key.
pCDcap2 - PCD capabilities which has to communicated with PD(PICC).
Throws:
UsageException - Usage Exception
InvalidResponseLengthException - Invalid Response Length Exception
SecurityException - Security Exception
authenticateEV2NonFirst
@Deprecated
void authenticateEV2NonFirst(int cardKeyNumber,
                                          IKeyData key,
                                          byte[] pCDcap2)
Deprecated. PCDCap2 is not needed in authenticateNonFirst cmd this is deprecated since version 1.4 this method will be removed from future releases use authenticateEV2NonFirst(int, IKeyData) instead this:
Non first is intended for any subsequent authentication after First authentication in a transaction

Parameters:
cardKeyNumber - defines the card key no to authenticate with.
key - Authentication key.
pCDcap2 - PCD capabilities which has to communicated with PD(PICC).
Throws:
UsageException - Usage Exception
InvalidResponseLengthException - Invalid Response Length Exception
SecurityException - Security Exception
Since:
1.4
authenticateEV2NonFirst
void authenticateEV2NonFirst(int cardKeyNumber,
                             IKeyData key)
Non first is intended for any subsequent authentication after First authentication in a transaction

Parameters:
cardKeyNumber - defines the card key no to authenticate with.
key - Authentication key.
Throws:
UsageException - Usage Exception
InvalidResponseLengthException - Invalid Response Length Exception
SecurityException - Security Exception
format
void format()
Format card - all applications and files will be deleted. Requires preceding authentication with PICC master key and selected application has to be 0.
Specified by:
format in interface IDESFireEV1
Throws:
UsageException - Usage Exception
InvalidResponseLengthException - Invalid Response Length Exception
SecurityException - Security Exception
 */


            //desFireEV3.changeVCKey();
/*
from API:
void	changeVCKey(int cardkeyNumber, byte[] oldKey, byte[] newKey, byte newKeyVersion)
This method allows to change VC key stored on the PICC.

changeVCKey
void changeVCKey(int cardkeyNumber,
                 byte[] oldKey,
                 byte[] newKey,
                 byte newKeyVersion)
This method allows to change VC key stored on the PICC. All VC keys are of AES type keys

Parameters:
cardkeyNumber - key number to change.
oldKey - old key of length 16/24 bytes depends on key type.
if type is AES128 then, key length should be 16 bytes.
newKey - new key of length 16/24 bytes depends on key type.
if type is AES128 then, key length should be 16 bytes.
newKeyVersion - new key version byte.
Throws:
UsageException - Usage Exception
InvalidResponseLengthException - Invalid Response Length Exception
 */


            //desFireEV3.changeVCKey();
            //desFireEV3.proximityCheck();
/*
from API:
Interface IDESFireEV3

proximityCheckEV3
void proximityCheckEV3(IKeyData vcProximityKey,
                       int numOfIterations)
The API performs the 3 operations:

Prepare Proximity Check: The PD is prepared to perform the Proximity Check by drawing a 7-byte random number RndR.
Proximity Check: The PD answers with a prepared random number at the published response time in Cmd.PreparePC This command may be repeated up to 8 times splitting the random number for different time measurements.
Verify Proximity Check: The random numbers are verified using cryptographic methods.
Note:
vcProximityKey can be NULL when authentication type is AuthType.AES/ authenticateEV2First or authenticateEV2NonFirst.
vcProximityKey passed is not considered if in authenticated state in AuthType.AES/authenticateEV2First or authenticateEV2NonFirst.
Parameters:
vcProximityKey - VCProximityKey configured onto card.
numOfIterations - Number of iterations of challenge-response pair for complete 8byte random challenge is exchanged, where number of iterations range from 1 to 8 (inclusive)
Throws:
UsageException - If not in authenticated state and vcProximityKey passed is NULL or if invalid numOfIterations value is passed
PICCException - PICC Exception
 */

/*
// https://www.mifare.net/support/forum/topic/desfire-ev2-authenticateev2nonfirst-exception/
EV2ApplicationKeySettings appSettings01 = appsetbuilderEV2

                    .setAppKeySettingsChangeable(true)

                    .setAppMasterKeyChangeable(true)

                    .setAuthenticationRequiredForFileManagement(true)

                    .setAuthenticationRequiredForDirectoryConfigurationData(true)

                    .setMaxNumberOfApplicationKeys(3)

                    .setKeyTypeOfApplicationKeys(KeyType.AES128)

                    .build();

...

desFireEV2.authenticateEV2First(0, objKEY_AES128_PICC, new byte[]{0, 0, 0, 0, 0, 0});



desFireEV2.createApplication(appId01, appSettings01);

desFireEV2.selectApplication(appId01);

desFireEV2.authenticateEV2First(0, default_zeroes_key, new byte[]{0, 0, 0, 0, 0, 0});



desFireEV2.createFile(fileNo, new DESFireFile.StdDataFileSettings(IDESFireEV1.CommunicationType.Enciphered, (byte) 0x2, (byte) 0x1, (byte) 0xf, (byte) 0xf, fileSize));



desFireEV2.authenticateEV2First(0, default_zeroes_key, new byte[]{0, 0, 0, 0, 0, 0});

desFireEV2.authenticateEV2NonFirst(1, objKEY_AES128_01, new byte[]{0, 0, 0, 0, 0, 0});

...
 */

/*
Class EV3PICCConfigurationSettings:
void	setPCDCap(byte pdCap1_2, byte pdCap2_5, byte pdCap2_6, byte pdCap2_2)
Set Proximity device capability data in the PICCConfiguration settings.

setPCDCap
public void setPCDCap(byte pdCap1_2,
                      byte pdCap2_5,
                      byte pdCap2_6,
                      byte pdCap2_2)
Set Proximity device capability data in the PICCConfiguration settings.
Parameters:
pdCap1_2 - Proximity device capability data 1.2
pdCap2_5 - Proximity device capability data 2.5
pdCap2_6 - Proximity device capability data 2.6
pdCap2_2 - Proximity device capability data 2.2


 */

        } catch (Exception e) {
            showMessage("IOException occurred... check LogCat", 'n');
            showMessage("Exception: " + e.getMessage(), 'n');
            e.printStackTrace();
        }


    }

    private void desfireEV3CardLogicOriginal() {
        byte[] appId = new byte[]{0x12, 0x00, 0x00};
        int fileSize = 100;
        byte[] data = new byte[]{0x11, 0x11, 0x11, 0x11, 0x11};
        int timeOut = 2000;
        int fileNo = 0;

        tv.setText(" ");
        showImageSnap(R.drawable.desfire_ev2);
        showMessage("Card Detected : " + desFireEV3.getType().getTagName(), 'n');

        try {
            desFireEV3.getReader().setTimeout(timeOut);
            showMessage(
                    "Version of the Card : "
                            + Utilities.dumpBytes(desFireEV3.getVersion()),
                    'd');
            desFireEV3.selectApplication(0); // todo run this before get applications

            showMessage(
                    "Existing Applications Ids : " + Arrays.toString(desFireEV3.getApplicationIDs()),
                    'd');


            //desFireEV2.selectApplication(0);

            desFireEV3.authenticate(0, IDESFireEV3.AuthType.Native, KeyType.THREEDES, objKEY_2KTDES);
/*
from API:
authenticateEV2First
void authenticateEV2First(int cardKeyNumber,
                          IKeyData key,
                          byte[] pCDcap2)
Authenticate EV2 to the PICC using single AES. After this authentication EV2 secure messaging is used. This authentication is intended to be the first in a transaction.

Parameters:
cardKeyNumber - defines the card key no to authenticate with.
key - Authentication key.
pCDcap2 - PCD capabilities which has to communicated with PD(PICC).
Throws:
UsageException - Usage Exception
InvalidResponseLengthException - Invalid Response Length Exception
SecurityException - Security Exception
authenticateEV2NonFirst
@Deprecated
void authenticateEV2NonFirst(int cardKeyNumber,
                                          IKeyData key,
                                          byte[] pCDcap2)
Deprecated. PCDCap2 is not needed in authenticateNonFirst cmd this is deprecated since version 1.4 this method will be removed from future releases use authenticateEV2NonFirst(int, IKeyData) instead this:
Non first is intended for any subsequent authentication after First authentication in a transaction

Parameters:
cardKeyNumber - defines the card key no to authenticate with.
key - Authentication key.
pCDcap2 - PCD capabilities which has to communicated with PD(PICC).
Throws:
UsageException - Usage Exception
InvalidResponseLengthException - Invalid Response Length Exception
SecurityException - Security Exception
Since:
1.4
authenticateEV2NonFirst
void authenticateEV2NonFirst(int cardKeyNumber,
                             IKeyData key)
Non first is intended for any subsequent authentication after First authentication in a transaction

Parameters:
cardKeyNumber - defines the card key no to authenticate with.
key - Authentication key.
Throws:
UsageException - Usage Exception
InvalidResponseLengthException - Invalid Response Length Exception
SecurityException - Security Exception
format
void format()
Format card - all applications and files will be deleted. Requires preceding authentication with PICC master key and selected application has to be 0.
Specified by:
format in interface IDESFireEV1
Throws:
UsageException - Usage Exception
InvalidResponseLengthException - Invalid Response Length Exception
SecurityException - Security Exception
 */


            //desFireEV3.changeVCKey();
/*
from API:
void	changeVCKey(int cardkeyNumber, byte[] oldKey, byte[] newKey, byte newKeyVersion)
This method allows to change VC key stored on the PICC.

changeVCKey
void changeVCKey(int cardkeyNumber,
                 byte[] oldKey,
                 byte[] newKey,
                 byte newKeyVersion)
This method allows to change VC key stored on the PICC. All VC keys are of AES type keys

Parameters:
cardkeyNumber - key number to change.
oldKey - old key of length 16/24 bytes depends on key type.
if type is AES128 then, key length should be 16 bytes.
newKey - new key of length 16/24 bytes depends on key type.
if type is AES128 then, key length should be 16 bytes.
newKeyVersion - new key version byte.
Throws:
UsageException - Usage Exception
InvalidResponseLengthException - Invalid Response Length Exception
 */

            desFireEV3.proximityCheckEV3(objKEY_AES128, 1);
            //desFireEV3.changeVCKey();
            //desFireEV3.proximityCheck();
/*
from API:
proximityCheckEV3
void proximityCheckEV3(IKeyData vcProximityKey,
                       int numOfIterations)
The API performs the 3 operations:

Prepare Proximity Check: The PD is prepared to perform the Proximity Check by drawing a 7-byte random number RndR.
Proximity Check: The PD answers with a prepared random number at the published response time in Cmd.PreparePC This command may be repeated up to 8 times splitting the random number for different time measurements.
Verify Proximity Check: The random numbers are verified using cryptographic methods.
Note:
vcProximityKey can be NULL when authentication type is AuthType.AES/ authenticateEV2First or authenticateEV2NonFirst.
vcProximityKey passed is not considered if in authenticated state in AuthType.AES/authenticateEV2First or authenticateEV2NonFirst.
Parameters:
vcProximityKey - VCProximityKey configured onto card.
numOfIterations - Number of iterations of challenge-response pair for complete 8byte random challenge is exchanged, where number of iterations range from 1 to 8 (inclusive)
Throws:
UsageException - If not in authenticated state and vcProximityKey passed is NULL or if invalid numOfIterations value is passed
PICCException - PICC Exception
 */

/*
// https://www.mifare.net/support/forum/topic/desfire-ev2-authenticateev2nonfirst-exception/
EV2ApplicationKeySettings appSettings01 = appsetbuilderEV2

                    .setAppKeySettingsChangeable(true)

                    .setAppMasterKeyChangeable(true)

                    .setAuthenticationRequiredForFileManagement(true)

                    .setAuthenticationRequiredForDirectoryConfigurationData(true)

                    .setMaxNumberOfApplicationKeys(3)

                    .setKeyTypeOfApplicationKeys(KeyType.AES128)

                    .build();

...

desFireEV2.authenticateEV2First(0, objKEY_AES128_PICC, new byte[]{0, 0, 0, 0, 0, 0});



desFireEV2.createApplication(appId01, appSettings01);

desFireEV2.selectApplication(appId01);

desFireEV2.authenticateEV2First(0, default_zeroes_key, new byte[]{0, 0, 0, 0, 0, 0});



desFireEV2.createFile(fileNo, new DESFireFile.StdDataFileSettings(IDESFireEV1.CommunicationType.Enciphered, (byte) 0x2, (byte) 0x1, (byte) 0xf, (byte) 0xf, fileSize));



desFireEV2.authenticateEV2First(0, default_zeroes_key, new byte[]{0, 0, 0, 0, 0, 0});

desFireEV2.authenticateEV2NonFirst(1, objKEY_AES128_01, new byte[]{0, 0, 0, 0, 0, 0});

...
 */

/*
Class EV3PICCConfigurationSettings:
void	setPCDCap(byte pdCap1_2, byte pdCap2_5, byte pdCap2_6, byte pdCap2_2)
Set Proximity device capability data in the PICCConfiguration settings.

setPCDCap
public void setPCDCap(byte pdCap1_2,
                      byte pdCap2_5,
                      byte pdCap2_6,
                      byte pdCap2_2)
Set Proximity device capability data in the PICCConfiguration settings.
Parameters:
pdCap1_2 - Proximity device capability data 1.2
pdCap2_5 - Proximity device capability data 2.5
pdCap2_6 - Proximity device capability data 2.6
pdCap2_2 - Proximity device capability data 2.2


 */


            // Set the custom path where logs will get stored, here we are setting the log folder DESFireLogs under
            // external storage.

            // todo work on saving data to external directory

            String spath = Environment.getExternalStorageDirectory().getPath() + File.separator + "DESFireLogs";
            Log.i("LogNXP", "path to logs :" + spath);

            NxpLogUtils.setLogFilePath(spath);
            // if you don't call save as below , logs will not be saved.
            NxpLogUtils.save();

        } catch (Exception e) {
            showMessage("IOException occurred... check LogCat", 't');
            e.printStackTrace();
        }
    }

    private void desfireEV2CardLogicCustom(){
        //format(true, SampleAppKeys.KEY_AES128_PICC_MASTER);
        initPiccAndChangeMasterKey(SampleAppKeys.KEY_2KTDES, SampleAppKeys.KEY_AES128_PICC_MASTER, true);
        authenticateMasterKeyAndecureMessaging(SampleAppKeys.KEY_AES128_PICC_MASTER);
    }

    // todo manually added
    private void desfireEV3CardLogicCustom(){
        //format(true, SampleAppKeys.KEY_AES128_PICC_MASTER);
        initPiccAndChangeMasterKey(SampleAppKeys.KEY_2KTDES, SampleAppKeys.KEY_AES128_PICC_MASTER, true);
        authenticateMasterKeyAndecureMessaging(SampleAppKeys.KEY_AES128_PICC_MASTER);
    }

    private EV2ApplicationKeySettings getApplicationSettings(byte[] appId) {
        Log.i(TAG, "Creating application Settings");

        EV2ApplicationKeySettings.Builder appsetbuilder = new EV2ApplicationKeySettings.Builder();
        appsetbuilder
                .setAppKeySettingsChangeable(true)
                .setAppMasterKeyChangeable(true)
                .setAuthenticationRequiredForFileManagement(true)
                .setAuthenticationRequiredForDirectoryConfigurationData(true)
                .setKeyTypeOfApplicationKeys(KeyType.AES128);

        EV2ApplicationKeySettings appsettings = appsetbuilder.build();

        return appsettings;
    }

    private void authenticateMasterKeyAndecureMessaging(byte[] keyToAuthenticate) {
        Log.i(TAG, "Authenticating with AES master key and starting EV2Auth");

        int timeOut = 2000;
        desFireEV2.getReader().setTimeout(timeOut);
        desFireEV2.selectApplication(0);

        newAESAuthEV2Auth(SampleAppKeys.KEY_AES128);
    }

    private void newAESAuthEV2Auth(byte[] keyToAuthenticate){
        Log.i(TAG, "New AES auth and secure messaging");

        SecretKey originalKey = new SecretKeySpec(keyToAuthenticate, 0, keyToAuthenticate.length, "AES");
        KeyData keyData = new KeyData();
        keyData.setKey(originalKey);
        desFireEV2.authenticateEV2First(0, keyData, new byte[]{0, 0, 0, 0, 0, 0});
        desFireEV2.getFreeMemory();
    }

    private void oldThreeDesAuth(byte[] keyToAuthenticate){
        Log.i(TAG, "Old 3DES auth");

        SecretKey originalKey = new SecretKeySpec(keyToAuthenticate, 0, keyToAuthenticate.length, "DESede");
        KeyData keyData = new KeyData();
        keyData.setKey(originalKey);

        desFireEV2.authenticate(0, IDESFireEV2.AuthType.Native, KeyType.THREEDES, keyData);
    }

    private void initPiccAndChangeMasterKey(byte[] oldKey, byte[] newKey, boolean useAES) {
        Log.i(TAG, "Changing master keys");

        desFireEV2.selectApplication(0);

        EV2PICCConfigurationSettings piccKeySettings = new EV2PICCConfigurationSettings();
        piccKeySettings.setAppDefaultKey(SampleAppKeys.KEY_AES128_PICC_APP_DEFAULT_KEY, (byte)1);
        //piccKeySettings.setVCAndPICConfigurations(/*auth for virtual card selection*/false, /*proximity check*/true);

        if(useAES){
            newAESAuthEV2Auth(oldKey);
        }else {
            oldThreeDesAuth(oldKey);
        }

        desFireEV2.setConfigurationByte(piccKeySettings);
        desFireEV2.changeKey(0, KeyType.AES128, oldKey, newKey, (byte) 1);
    }

    private void changePiccKeys(){

    }


    private void authenticateOldApplicationKey() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        libInstance.stopForeGroundDispatch();
    }


    @Override
    protected void onResume() {
        super.onResume();
        libInstance.startForeGroundDispatch();
    }

    /**
     * Encrypt the supplied data with key provided.
     *
     * @param data data bytes to be encrypted
     * @param key  Key to encrypt the buffer
     * @return encrypted data bytes
     * @throws NoSuchAlgorithmException           NoSuchAlgorithmException
     * @throws NoSuchPaddingException             NoSuchPaddingException
     * @throws InvalidKeyException                InvalidKeyException
     * @throws IllegalBlockSizeException          IllegalBlockSizeException
     * @throws BadPaddingException                eption BadPaddingException
     * @throws InvalidAlgorithmParameterException InvalidAlgorithmParameterException
     */
    protected byte[] encryptAESData(final byte[] data, final byte[] key)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {
        final SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
        byte[] encdata = cipher.doFinal(data);
        return encdata;
    }

    /**
     * @param encdata Encrypted input buffer.
     * @param key     Key to decrypt the buffer.
     * @return byte array.
     * @throws NoSuchAlgorithmException           No such algorithm exce.
     * @throws NoSuchPaddingException             NoSuchPaddingException.
     * @throws InvalidKeyException                if key is invalid.
     * @throws IllegalBlockSizeException          if block size is illegal.
     * @throws BadPaddingException                if padding is bad.
     * @throws InvalidAlgorithmParameterException if algo. is not avaliable or not present.
     */
    protected byte[] decryptAESData(final byte[] encdata, final byte[] key)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {
        final SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
        byte[] decdata = cipher.doFinal(encdata);
        return decdata;
    }

    /**
     * Update the card image on the screen.
     *
     * @param cardTypeId resource image id of the card image
     */

    private void showImageSnap(final int cardTypeId) {

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mImageView.getLayoutParams().width = (size.x * 2) / 3;
        mImageView.getLayoutParams().height = size.y / 3;
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                mImageView.setImageResource(cardTypeId);
                mImageView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomnrotate));
            }
        }, 1250);
        mImageView.setImageResource(R.drawable.product_overview);
        mImageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));
    }

    /**
     * This will display message in toast or logcat or on screen or all three.
     *
     * @param str   String to be logged or displayed
     * @param where 't' for Toast; 'l' for Logcat; 'd' for Display in UI; 'n' for
     *              logcat and textview 'a' for All
     */
    protected void showMessage(final String str, final char where) {

        switch (where) {

            case 't':
                Toast.makeText(MainActivityProximityCheck.this, "\n" + str, Toast.LENGTH_SHORT)
                        .show();
                break;
            case 'l':
                NxpLogUtils.i(TAG, "\n" + str);
                break;
            case 'd':
                tv.setText(tv.getText() + "\n-----------------------------------\n"
                        + str);
                break;
            case 'a':
                Toast.makeText(MainActivityProximityCheck.this, "\n" + str, Toast.LENGTH_SHORT)
                        .show();
                NxpLogUtils.i(TAG, "\n" + str);
                tv.setText(tv.getText() + "\n-----------------------------------\n"
                        + str);
                break;
            case 'n':
                NxpLogUtils.i(TAG, "Dump Data: " + str);
                tv.setText(tv.getText() + "\n-----------------------------------\n"
                        + str);
                break;
            default:
                break;
        }

        Log.i("NXPLog", str);
        return;
    }


    /**
     * This will send the message to the handler with required String and
     * character.
     *
     * @param stringMessage message to be send
     * @param codeLetter    't' for Toast; 'l' for Logcat; 'd' for Display in UI; 'a' for
     *                      All
     */
    protected void sendMessageToHandler(final String stringMessage,
                                        final char codeLetter) {
        Bundle b = new Bundle();
        b.putString("message", stringMessage);
        b.putChar("where", codeLetter);
        Message msg = mHandler.obtainMessage();
        msg.setData(b);
        mHandler.sendMessage(msg);
    }

    /**
     * Read Me section contain Help and About product.
     */
    private void readMeHelpAbout() {

        ((TextView) findViewById(R.id.tvAbout))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        showDisclaimer();
                    }
                });
    }


    private String getApplicationVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Disclaimer Section contain Details About product.
     */
    private void showDisclaimer() {

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivityProximityCheck.this);
        alert.setTitle("About");
        alert.setCancelable(false);
        String[] cards = libInstance.getSupportedCards();
        showMessage("Supported Cards", 'l');
        String message = getString(R.string.about_text);

        String alertMessage = message + "\n";

        alertMessage += "\n";
        String appVer = getApplicationVersion();
        if (appVer != null)
            alertMessage += "Application version is: " + appVer + "\n";

        alertMessage += "\n\n" + "Supported Cards: "
                + Arrays.toString(cards) + "\n";

        alert.setMessage(alertMessage);

        alert.setIcon(R.drawable.product_overview);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog,
                                final int whichButton) {

            }
        });

        alert.show();
    }

    public String bytesToHexNpe(byte[] bytes) {
        if (bytes == null) return "";
        StringBuffer result = new StringBuffer();
        for (byte b : bytes)
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
