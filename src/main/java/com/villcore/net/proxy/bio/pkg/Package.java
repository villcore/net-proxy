package com.villcore.net.proxy.bio.pkg;

import com.villcore.net.proxy.bio.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2017/7/18.
 */
public abstract class Package {
    private static final Logger LOG = LoggerFactory.getLogger(Package.class);

    private byte[] header;
    private byte[] body;

    /**
     * 从header读取body size
     * @return
     */
    public abstract int getSize(byte[] header);

    /**
     * 在header写入body size
     * @param header
     * @param size
     * @return
     */
    public abstract int setSize(byte[] header, int size);

    /**
     * 获取header长度
     * @return
     */
    public abstract int getHeaderLen();

    public abstract long getUserFlag(byte[] header);
    public abstract void setUserFlag(byte[] header, long userFlag);

    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void readPackageWithHeader(InputStream inputStream) throws IOException {
        header = new byte[getHeaderLen()];
        readFully(inputStream, header);
        //read body
        int size = getSize(header);
        if(size < 0 || size > 10 * 1024 * 1024){
            throw new IOException("illegal byte size...");
        }
        body = new byte[size];
        readFully(inputStream, body);
        //LOG.debug("read page without header, header size = {}, body size = {} ", getHeader().length, getBody().length);
    }

    public void readPackageWithoutHeader(InputStream inputStream) throws IOException {
        byte[] bytes = readFully(inputStream);
        if(bytes.length == 0) {
            return;
        }
//        System.out.println("body = " + bytes.length);
//        System.out.println("body = " + new String(bytes));
        setBody(bytes);
        header = new byte[getHeaderLen()];
        setHeader(header);
        setSize(header, getBody().length);
        //LOG.debug("read page without header, header size = {}, body size = {} ", getHeader().length, getBody().length);

    }

    public void writePackageWithHeader(OutputStream outputStream) throws IOException {
        //LOG.debug("writePackageWithHeader  header = {}, body = {}", getHeader() == null, getBody() == null);

        if(getHeader().length != 0) {
            writeFully(outputStream, getHeader());
        }

        if(getBody().length != 0) {
            writeFully(outputStream, getBody());
        }
    }

    public void writePackageWithoutHeader(OutputStream outputStream) throws IOException {
        if(getBody().length != 0) {
            writeFully(outputStream, getBody());
        }
    }

    public byte[] readFully(InputStream inputStream) throws IOException {
        //LOG.debug("read fully ");

        int bytesArraySize = 1 * 1024 * 1024; //1M
        int availableSize = inputStream.available();
        if(availableSize > 0) {
            bytesArraySize = availableSize;
        }
        byte[] bytes = new byte[bytesArraySize];
        int pos = inputStream.read(bytes);
        //LOG.debug("read bytes in once {}", pos);
        if(pos == -1) {
            throw new IOException("socket closed");
        }
        if(pos == bytes.length) {
            return bytes;
        } else {
            return ByteArrayUtils.trimByteArray(bytes, 0, pos);
        }
    }

    public void readFully(InputStream inputStream, byte[] bytes) throws IOException {
        int pos = -1;
        int readSize = 0;

        while(true) {
            //LOG.debug("read fully 2 {}, {}, {}", bytes.length, pos, readSize);
            if((pos = inputStream.read(bytes, readSize, bytes.length - readSize)) > 0) {
                //LOG.debug("pso = {}", pos);
                readSize += pos;
                if(readSize >= bytes.length) {
                    break;
                }
            }
            else if(pos == -1) {
                throw new IOException("socket closed");
            } else {
                return;
            }
        }
        //LOG.debug("read full size = {}", readSize);
    }

    private void writeFully(OutputStream outputStream, byte[] bytes) throws IOException {
        outputStream.write(bytes);
        outputStream.flush();
    }

    public void buildPackage(byte[] header, byte[] body) {
        setHeader(header);
        setSize(header, body.length);
        setBody(body);
    }
}
