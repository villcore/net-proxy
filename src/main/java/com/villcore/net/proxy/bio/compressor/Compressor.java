package com.villcore.net.proxy.bio.compressor;

import java.io.IOException;

public interface Compressor {
    byte[] compress(byte[] bytes) throws IOException;
    byte[] decompress(byte[] bytes) throws IOException;
}
