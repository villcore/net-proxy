package com.villcore;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

public class ByteBufTest {
    @Test
    public void testRef() {
        ByteBuf buf = Unpooled.wrappedBuffer(new byte[2]);
        ByteBuf buf2 = Unpooled.wrappedBuffer(new byte[2]).retain(2);
        //ByteBuf buf3 = Unpooled.wrappedBuffer(new byte[2]).retain();
        ByteBuf buf3 = buf2.copy();

        ByteBuf buf4 = Unpooled.wrappedBuffer(buf, buf2, buf3);

        System.out.printf("%d, %d, %d\n", buf.refCnt(), buf2.refCnt(), buf3.refCnt());
        //Unpooled.wrappedBuffer(buf, buf2, buf3).release();
        System.out.printf("%d, %d, %d\n", buf.refCnt(), buf2.refCnt(), buf3.refCnt());

        System.out.println(buf4.refCnt());
        //buf4.release();
        CompositeByteBuf cbf = (CompositeByteBuf) buf4;
        System.out.println(buf4.refCnt());
        cbf.iterator().forEachRemaining(byteBuf -> {
            if(byteBuf.refCnt() > 0) {
                byteBuf.release(byteBuf.refCnt());
            }
        });
        System.out.printf("%d, %d, %d\n", buf.refCnt(), buf2.refCnt(), buf3.refCnt());

    }
}
