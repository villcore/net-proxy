package com.villcore;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

public class ByteBufTest {
    @Test
    public void testRef() {
        ByteBuf buf = Unpooled.wrappedBuffer(new byte[2]);
        ByteBuf buf2 = Unpooled.wrappedBuffer(new byte[2]).retain(2);
        //ByteBuf buf3 = Unpooled.wrappedBuffer(new byte[2]).retain();
        ByteBuf buf3 = buf2.copy();

        System.out.printf("%d, %d, %d\n", buf.refCnt(), buf2.refCnt(), buf3.refCnt());
        Unpooled.wrappedBuffer(buf, buf2, buf3).release();
        System.out.printf("%d, %d, %d\n", buf.refCnt(), buf2.refCnt(), buf3.refCnt());
    }
}
