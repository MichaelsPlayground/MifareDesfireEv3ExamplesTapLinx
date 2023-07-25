package de.androidcrypto.taplinxexample;

import com.nxp.nfclib.desfire.DESFireEV3File;
import com.nxp.nfclib.desfire.IDESFireEV1;

public class StdEV3DataFileSettingsExtended extends DESFireEV3File.StdEV3DataFileSettings {


    public StdEV3DataFileSettingsExtended(IDESFireEV1.CommunicationType communicationType, byte b, byte b1, byte b2, byte b3, int i) {
        super(communicationType, b, b1, b2, b3, i);
    }

    public StdEV3DataFileSettingsExtended(IDESFireEV1.CommunicationType communicationType, byte b, byte b1, byte b2, byte b3, int i, byte b4, byte[] bytes) {
        super(communicationType, b, b1, b2, b3, i, b4, bytes);
    }
}
