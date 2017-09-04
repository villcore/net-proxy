package com.villcore.net.proxy.v2.pkg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

import java.io.Serializable;

/**
 * make sure construct Package with exist ByteBuf read/writer index correct
 */
public class Package implements Serializable {
    public static final int FIXED_LEN = 4 + 4 + 4 + 2;
    //totalLen
    //headerLen
    //bodyLen
    //pkgType
    protected ByteBuf fixed = Unpooled.buffer(4 + 4 + 4 + 2);

    //header
    protected ByteBuf header = Unpooled.EMPTY_BUFFER;

    //body
    protected ByteBuf body = Unpooled.EMPTY_BUFFER;

    public int getTotalLen() {
        return this.fixed.getInt(0);
    }

    public int getHeaderLen() {
        return this.fixed.getInt(4);
    }

    public int getBodyLen() {
        return this.fixed.getInt(4 + 4);
    }

    public short getPkgType() {
        return this.fixed.getShort(4 + 4 + 4);
    }

    public ByteBuf getHeader() {
        return this.header;
    }

    public ByteBuf getBody() {
        return this.body;
    }

    public void setHeader(ByteBuf header) {
        this.header = header;
        this.fixed.setInt(4, header.writerIndex() - header.readerIndex());
        setTotolLen();
    }

    public void setTotolLen() {
        int total = header.writerIndex() - header.readerIndex() + body.writerIndex() - body.readerIndex() + 4 + 4 + 2;
        fixed.setInt(0, total);
    }

    public void setBody(ByteBuf body) {
        this.body = body;
        this.fixed.setInt(4 + 4, body.writerIndex() - body.readerIndex());
        setTotolLen();
    }

    public void setPkgType(short type) {
        this.fixed.setShort(4 + 4 + 4, type);
    }

    public ByteBuf toByteBuf() {
        return Unpooled.wrappedBuffer(fixed, header, body);
    }
}
