package com.villcore.net.proxy.bio.handler;

import com.villcore.net.proxy.bio.pkg2.Package;
import com.villcore.net.proxy.bio.pkg2.UserPackage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

public class ToUserPackageHandler implements Handler {
    private int connectionId;
    private long userFlag;

    public ToUserPackageHandler(int connectionId, long userFlag) {
        this.connectionId = connectionId;
        this.userFlag = userFlag;
    }

    @Override
    public Package handle(Package pkg) throws IOException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] header = pkg.getHeader();
        byte[] body = pkg.getBody();

        UserPackage userPackage = new UserPackage();
        byte[] newHeader = userPackage.newHeader();
        byte[] newBody = body;

        userPackage.setHeader(newHeader);
        userPackage.setBody(newBody);

        userPackage.setConnectionId(connectionId);
        userPackage.setUserFlag(userFlag);

        return userPackage;
    }
}
