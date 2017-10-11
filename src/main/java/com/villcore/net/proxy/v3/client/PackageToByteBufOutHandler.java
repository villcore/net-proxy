package com.villcore.net.proxy.v3.client;

import com.villcore.net.proxy.v3.pkg.Package;
import com.villcore.net.proxy.v3.pkg.PackageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
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
//            PackageUtils.release(pkg);
            PackageUtils.printRef("after------------------"+getClass().getSimpleName(), pkg);
            return;
        }
//        if(msg instanceof ByteBuf) {
//            ByteBuf byteBuf = (ByteBuf) msg;
//            if(byteBuf.release()) {
//                LOG.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!! cur ref cnt = 0 for {}", byteBuf.getClass());
//            } else {
//                ctx.writeAndFlush(byteBuf);
//            }
//            return;
//        }
//        if(msg instanceof ByteBuf) {
//            ByteBuf byteBuf = (ByteBuf) msg;
//            byteBuf.release();
//        }
        LOG.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!! cur ref cnt = 0 for {}",  msg.getClass());
        if(ReferenceCountUtil.refCnt(msg) > 0) {
//        ReferenceCountUtil.release(msg);
            //System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxx  " + ReferenceCountUtil.refCnt(msg));
            ctx.write(msg);
            //ReferenceCountUtil.release(msg);
        }
    }
}
