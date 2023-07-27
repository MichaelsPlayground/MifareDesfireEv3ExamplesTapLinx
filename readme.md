# Mifare DESFire EV3 examples using TapLinx library

This application is using the **TapLinx library from NXP** to get access to a **Mifare DESFire EV3** tag.

### Important note: the app works with tags of type EV3 only (hardcoded). Trying to work with other tag types like EV1, EV2 or Light will fail for all operations !

### How to get the library:
The library is available on NXP's website for free after a registration (only email and password is necessary) and login:
https://www.mifare.net/en/login/.

You need a license string for app (you get it after log-in) - register the app with your individual package name.

### Do I need an additional setup?
Yes, as the library is bundled with **Google Analytics** you need to register the app with your Google account to get a google-services.json file 
for your individual package name. 

## application development status: UNFINISHED
This app is in development so most of the buttons in the main menu won't work or with internal defined values so please **use the app at this time with caution.**  




Sample project with TapLinx: https://github.com/dfpalomar/TapLinxSample 

Source: https://github.com/MichaelsPlayground/MifareDesfireEv3ExamplesDesTapLinx

Firebase Analytics: Michael Fehr fb.samples.2022@gmail.com

package de.androidcrypto.taplinxexample

signingReport: 
```plaintext
Alias: AndroidDebugKey
MD5: 4D:1E:D7:91:56:67:73:EA:54:73:BC:EC:D7:E5:9B:F5
SHA1: 19:22:A4:D7:01:A8:3D:09:8F:04:93:E9:8E:21:92:2D:5A:5F:B0:54
SHA-256: A7:A8:66:27:C7:76:6D:C3:3C:9E:3F:89:99:88:3E:A1:7B:ED:34:69:19:83:B6:EA:72:04:C9:13:8E:84:E0:90
```

https://www.mifare.net/support/forum/forum/taplinx-developers/

https://community.nxp.com

# Secure Dynamic Messaging (SDM)

Secure Dynamic Messaging offers a way to provide (internal) card data using the common know NDEF definitions, 
so that (nearly) every regular Smartphone (Android AND IOS) are been able to view the data without any proprietary 
app or software. The generated message is called "SUN" or "Secure Unique Number" as the card's UID is used for many 
systems (like access control). Please note: Secure Dynamic Messaging is available on DESFire EV3 only.

The data provided in the NDEF message can include the card's UID, a reader counter or other data on the card. All data 
is available in Plain or Encrypted encoding including a CMAC as digital signature.

Below there are some explanations to find out which parameter has be set to get the requested data. For explanations on 
the NDEF formats please refer to an internet search ("Google NDEF message").

The most use case for a SDM generated NDEF is an URL that links to a backend server so the server can act with the data on 
the tag. Fortunately a nice guy setup a server for NXP's NTAG424DNA tag but this works for DESFire EV3 tags as well. 

The server is (free) accessible using the link "https://sdm.nfcdeveloper.com/" - just click on the link to get more information. 
An URL directing to the server could look like: 
https://sdm.nfcdeveloper.com/tag?picc_data=00000000000000000000000000000000&cmac=0000000000000000

## How to get the template as an byte array ?

There some simple steps to follow to get the byte array encoded form of the link. Please try not to use the most simple form 
by running the "https://sdm..".getBytes(StandardCharsets.UTF_8) command, this will lead to a byte array but not to the NDEF 
format. Just run the following commands:

```plaintext
String ndefSampleBackendUrl = "https://sdm.nfcdeveloper.com/tag?picc_data=00000000000000000000000000000000&cmac=0000000000000000";
NdefRecord ndefRecord = NdefRecord.createUri(ndefSampleBackendUrl);
NdefMessage ndefMessage = new NdefMessage(ndefRecord);
byte[] ndefMessageBytesHeadless = ndefMessage.toByteArray();
// now we do have the NDEF message but  it needs to get wrapped by '0x00 || (byte) (length of NdefMessage)
byte[] ndefMessageBytes = new byte[ndefMessageBytesHeadless.length + 2];
System.arraycopy(new byte[]{(byte) 0x00, (byte) (ndefMessageBytesHeadless.length)}, 0, ndefMessageBytes, 0, 2);
System.arraycopy(ndefMessageBytesHeadless, 0, ndefMessageBytes, 2, ndefMessageBytesHeadless.length);
```

The result is a byte array of length 96 that is (in hex encoding)
```plaintext
005ed1015a550473646d2e6e6663646576656c6f7065722e636f6d2f7461673f706963635f646174613d303030303030303030303030303030303030303030303030303030303030303026636d61633d30303030303030303030303030303030
```

You may have noticed that there are two placeholders in the URL, filled with zeroes. The first placeholder will take the 
(encrypted) PICC data and the second one the CMAC ("signature"). Explanations on the goth data fields are given later.

## What are the parameters to archive this data ?

The *change file settings* command needs these parameters:

```plaintext
// get the existing file settings for file 2 (this is the NDEF data file, fixed value)
DESFireEV3File.EV3FileSettings fileSettings = getFileSettings(for file 2);
// map the settings to a StandardFileSettings object
DESFireEV3File.StdEV3DataFileSettings stdFileSettings = (DESFireEV3File.StdEV3DataFileSettings) fileSettings;

stdFileSettings.setSDMEnabled(true);

stdFileSettings.setUIDMirroringEnabled(true);
stdFileSettings.setSDMReadCounterEnabled(true);
stdFileSettings.setSDMEncryptFileDataEnabled(false);
stdFileSettings.setSDMReadCounterLimitEnabled(false);

byte[] sdmPiccOffset = new byte[]{(byte) 0x2A, (byte) 0x00, (byte) 0x00}; // LSB encoding
byte[] sdmMacInputOffset = new byte[]{(byte) 0x50, (byte) 0x00, (byte) 0x00}; // LSB encoding
byte[] sdmMacOffset = new byte[]{(byte) 0x50, (byte) 0x00, (byte) 0x00}; // LSB encoding

stdFileSettings.setPiccDataOffset(sdmPiccOffset);
stdFileSettings.setSdmMacInputOffset(sdmMacInputOffset);
stdFileSettings.setSdmMacOffset(sdmMacOffset);

byte[] sdmAccessRights = Utils.hexStringToByteArray("F121"); // see explanation below
stdFileSettings.setSdmAccessRights(sdmAccessRights);
desFireEV3.changeDESFireEV3FileSettings(2, desFireEV3FileSettings);
```

The **SDM Access Rights** are defined as follows and refer to a key number defined during application creation. 'E' or 'F' values have a different meaning:

```plaintext
sdmAccessRights F121 are mapped to:
F = RFU, please just use F as value
1 = SDM Counter Ret Access Rights 0x00 to 0x0D: Targeted AppKey 0x0E : Free 0x0F : No Access
2 = SDM Meta Read Access Rights   0x00 to 0x0D: Encrypted PICC data mirroring using the targeted AppKey 0x0E : Plain PICC data mirroring 0x0F : No PICC data mirroring
1 = SDM File Read Access Rights   0x00 to 0x0D: Targeted AppKey 0x0F : No SDM for Reading
```

As we set `SDM Meta Read Access Rights` to`1` we are going to receive the **encrypted PICC data**, followed by a **CMAC**.

## What is the encrypted PICC data ?

When asking for encrypted PICC data the card generates these 4 data fields:

```plaintext
Name          Length Sample value   Description
PICC Data Tag    1   C7             encodes if there is an UID and/or Read Counter encoding and how low the UID is (7 bytes)
UID              7   04514032501490 The UID of our tag
ReadCounter      3   030000         The counter on the tag read command, encoded in LSB notation
RandomPadding    5*  33f77a0a9e     Note: the length depends on whether an UID or counter is included, fills the data up to 16 bytes length
```

This data is encrypted by the key defined in `SDM Meta Read Access Key` without any further key derivation 
(the reason for "no derivation" is easy: as the UID is part of the key derivation this UID is not knows at this time to the backend).

For decryption a simple AES decryption is run with IV = 16 bytes "zero" key.

The encrypted PICC data has a (fixed) **length of 16 bytes**.

## What is the CMAC value ?

The `CMAC` is calculated over the UID and ReadCounter values using a key derivation and has a (fixed) **length of 8 bytes** 
(don't forget, this is the truncated value of the 16 bytes CMAC value). 

## How are the offsets defined ?

We defined 3 offset values for 3 positions but our output has only two values - this may disturbing you a little but we clear 
this. As there is an option to include extra data in the mirrored message we need to provide the position (= start) of all data 
values for CMAC calculation. For our example we do not include extra data both fields for `sdmMacInputOffset` and `sdmMacOffset` equals.

All offset values are defined by a byte array of length 3 but the data are in LSB encoding (meaning the lowest value part is at the 
beginning):

```plaintext
sdmPiccOffset =     0x2A 0x00 0x00  = 00002A = 42 (decimal)  
sdmMacInputOffset = 0x50 0x00 0x00  = 000050 = 80 (decimal)
sdmMacOffset =      0x50 0x00 0x00  = 000050 = 80 (decimal)
```

Let us see where we do find these positions within our URL template:

```plaintext
      0 - 9     10 - 19   20 - 29   30 - 39   40 - 49   50 - 59   60 - 69   70 - 79   80 - 89   
      01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890
URL: https://sdm.nfcdeveloper.com/tag?picc_data=00000000000000000000000000000000&cmac=0000000000000000
                                                | offset = start of enc PICC data
                                                                                      | start of CMAC data
      01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890
      0 - 9     10 - 19   20 - 29   30 - 39   40 - 49   50 - 59   60 - 69   70 - 79   80 - 89       
```

It is little bit tricky to identify the positions, for that reason I used the above notation. 

The following data are taken from the document `NTAG 424 DNA and NTAG 424 DNA TagTamper features and hints AN12196.pdf` 
on pages 31 (step 1) and 34 (step 7):

```plaintext
sdmPiccOffset =     0x20 0x00 0x00  = 000020 = 32 (decimal)  
sdmMacInputOffset = 0x43 0x00 0x00  = 000043 = 67 (decimal)
sdmMacOffset =      0x43 0x00 0x00  = 000050 = 67 (decimal)
```

```plaintext
      0 - 9     10 - 19   20 - 29   30 - 39   40 - 49   50 - 59   60 - 69   70 - 79   80 - 89   
      01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890
URL: https://choose.url.com/ntag424?e=00000000000000000000000000000000&c=0000000000000000
                                      | offset = start of enc PICC data
                                                                         | start of CMAC data
      01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890
      0 - 9     10 - 19   20 - 29   30 - 39   40 - 49   50 - 59   60 - 69   70 - 79   80 - 89       
```

When tapping the tag to the reader the link will direct to:

```plaintext
disabled SDM: https://sdm.nfcdeveloper.com/tag?picc_data=00000000000000000000000000000000&cmac=0000000000000000
enabled SDM:  https://sdm.nfcdeveloper.com/tag?picc_data=FBCBE6602D4FF482C1B961242300394D&cmac=112E0AE968CF6DE7
```

Just try this link and receive a positive confirmation (CMAC is valid) and the decrypted data:

https://sdm.nfcdeveloper.com/tag?picc_data=FBCBE6602D4FF482C1B961242300394D&cmac=112E0AE968CF6DE7

```plaintext
Secure Dynamic Messaging Backend Server Demo
Cryptographic signature validated.

Encryption mode: AES
PICC Data Tag: c7
NFC TAG UID: 04514032501490
Read counter: 9
```





