package com.villcore.net.proxy.bio.handler;

import com.villcore.net.proxy.bio.pkg2.Package;
import com.villcore.net.proxy.bio.pkg2.UserPackage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

public class FromUserPackageHandler implements Handler {
    @Override
    public Package handle(Package pkg) throws IOException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] header = pkg.getHeader();
        byte[] body = pkg.getBody();

        UserPackage userPackage = new UserPackage();
        userPackage.setHeader(header);
        userPackage.setBody(body);

        Package newPkg = new Package();
        newPkg.setHeader(newPkg.newHeader());
        newPkg.setBody(userPackage.getBody());

        return newPkg;
    }
}
