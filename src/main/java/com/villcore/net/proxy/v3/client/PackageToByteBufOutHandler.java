package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.v3.pkg.v1.Package;
import com.villcore.net.proxy.v3.pkg.v1.PackageUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageToByteBufOutHandler extends ChannelOutboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(PackageToByteBufOutHandler.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        LOG.debug(">>>>>>>>>>>>>>>>>>>>>>>{}", msg.getClass());
        if(msg instanceof Package) {
            Package pkg = (Package) msg;
//            ctx.writeAndFlush(pkg.toByteBuf());

            PackageUtils.printRef("before------------------"+getClass().getSimpleName(), pkg);
            ctx.write(pkg.toByteBuf());
            ctx.write(Unpooled.EMPTY_BUFFER);
//            try {
//                PackageUtils.release(Optional.of(pkg));
//            } catch (Exception e) {
//                LOG.error(e.getMessage(), e);
//            }
            PackageUtils.printRef("after------------------"+getClass().getSimpleName(), pkg);
            return;
        }

        LOG.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!! cur ref cnt = 0 for {}",  msg.getClass());
//        if(ReferenceCountUtil.refCnt(msg) > 0) {
////        ReferenceCountUtil.release(msg);
//            //System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxx  " + ReferenceCountUtil.refCnt(msg));
//            ctx.write(msg);
//            ReferenceCountUtil.release(msg);
//        }
        ctx.write(msg);
    }
}
