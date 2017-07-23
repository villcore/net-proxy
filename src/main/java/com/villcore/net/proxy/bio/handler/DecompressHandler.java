package com.villcore.net.proxy.bio.handler;

import com.villcore.net.proxy.bio.compressor.Compressor;
import com.villcore.net.proxy.bio.pkg2.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DecompressHandler implements Handler {
    private static final Logger LOG = LoggerFactory.getLogger(DecompressHandler.class);

    private Compressor compressor;

    public DecompressHandler(Compressor compressor) {
        this.compressor = compressor;
    }

    @Override
    public Package handle(Package pkg) throws IOException {
        byte[] header = pkg.getHeader();
        byte[] body = pkg.getBody();

        byte[] decompressHeader = compressor.decompress(header);
        byte[] decompressBody = compressor.decompress(body);
        //LOG.debug("ori body size = {}, decompress body size = {}", body.length, decompressBody.length);

        Package newPkg = new Package();
        newPkg.setHeader(decompressHeader);
        newPkg.setBody(decompressBody);
        return newPkg;
    }
}
