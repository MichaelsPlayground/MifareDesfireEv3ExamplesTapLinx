https://www.mifare.net/?post_type=topic&p=52188

11,08,2023 10:40

PICC Exception error on running Proximity Check (MAC verification failed!)

I'm trying to run the Proximity Check on a Mifare DESFire EV3 tag using TapLinx version 3-1-0 on Android 13.

It ended with a PICCException occurred: MAC verification failed!

As a pre-set I changed the factory set Master Application Key to AES, the VC Configuration Key (0x20) and 
the VC Proximity Key (0x21) [all to a default zeroed key].

Then I ran the commands:

desFireEV3.selectApplication(0);
KeyData keyData = getAesKeyFromByteArray(VIRTUAL_CARD_PROXIMITY_KEY_AES_DEFAULT);
int numberOfRounds = 1;
desFireEV3.proximityCheckEV3(keyData, numberOfRounds);

which gave these APDU and responses:

// select Master Application
Command sent to card : 5A000000
Response received : 00
// prepare PC
Command sent to card : F0
Response received : 9001032000
// run PC
Command sent to card : F2081BE060120FED1296
Response received : CFE0A8546BCFFAA5
// verify PC
Command sent to card : FDAFCCDED31F1458D9
Response received : 9095ED9BB2A629E15F
// exception
runProximityCheck PICCException occurred
MAC verification failed!

with this error stack:

com.nxp.nfclib.exceptions.PICCException: MAC verification failed!
at com.nxp.nfclib.desfire.DESFireEV3.verifyPC(:1269)
at com.nxp.nfclib.desfire.DESFireEV3.proximityCheckEV3(:1134)
at de.xxx.taplinxexample.MainActivity$84.onClick(MainActivity.java:3457)
at android.view.View.performClick(View.java:6897)
at android.widget.TextView.performClick(TextView.java:12727)
at com.google.android.material.button.MaterialButton.performClick(MaterialButton.java:1131)
at android.view.View$PerformClick.run(View.java:26104)
at android.os.Handler.handleCallback(Handler.java:789)
at android.os.Handler.dispatchMessage(Handler.java:98)
at android.os.Looper.loop(Looper.java:164)
at android.app.ActivityThread.main(ActivityThread.java:6944)
at java.lang.reflect.Method.invoke(Native Method)
at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:327)
at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1374)

Did I miss any configuration on PICC level or missing authentication ?

Thanks for your help, in case you want to contact me directly please use edvmf@gmx.de, thanks.

Kind regards,
Michael
