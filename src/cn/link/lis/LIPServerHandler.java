package cn.link.lis;/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler implementation for the echo server.
 */
@Sharable
public class LIPServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(
            LIPServerHandler.class.getName());
    private Handler mHandler;
    public static ChannelGroup recipients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public LIPServerHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        recipients.add(ctx.channel());
        Message msg = mHandler.obtainMessage(MainActivity.SERVER_CLIENT_CONNECTED,
                ctx.channel().remoteAddress().toString());
        mHandler.sendMessage(msg);


    }


    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        try {
            recipients.remove(ctx.channel());
            System.out.println("删除channel成功" + recipients.size());
        } catch (Exception ex) {
            System.out.println("删除channel失败" + ex.getMessage());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf bb = (ByteBuf) msg;
        MessageProtocol mp = MessageProtocol.parse(bb);
        System.out.println("Action " + mp.action);
        switch (mp.action) {
            case MessageProtocol.K_START_GAME:
                System.out.println("K Start Game");
                int[] random = LISProblem.randomIntArray(6, 9);
                LISProblem lisProblem = LISProblem.solve(random);
                System.out.println(lisProblem.toString());
                ByteBuf bbsend = MessageProtocol.sendLIP(lisProblem);
                ctx.writeAndFlush(bbsend);
                //Handler the message to MainActivity
                Message hmsg = mHandler.obtainMessage(MainActivity.SERVER_LIP, lisProblem);
                mHandler.sendMessage(hmsg);
                break;
            case MessageProtocol.K_SEND_CURRENT_INDEX:
                System.out.println("K Current Index");
                int[] currentIndex = mp.currentIndex;
                System.out.println("Current Index :");
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < currentIndex.length; i++) {
                    sb.append(currentIndex[i] + " ");
                }
                System.out.println(sb.toString());

                Message msg1 = mHandler.obtainMessage(MainActivity.SERVER_REV_CURRENT_INDEX, mp);
                mHandler.sendMessage(msg1);

                break;
            case MessageProtocol.K_CLIENT_WIN:
                Log.w("LIPPP", "Client  Win");
                Message msgcw = mHandler.obtainMessage(MainActivity.CLIENT_WIN, mp);
                mHandler.sendMessage(msgcw);
                break;
            case MessageProtocol.K_SERVER_WIN:
                Log.w("LIPPP", "Server  Win");
                Message msgsw = mHandler.obtainMessage(MainActivity.SERVER_WIN, mp);
                mHandler.sendMessage(msgsw);
                break;
            case MessageProtocol.K_SERVER_CONFIRM_WIN:
            case MessageProtocol.K_CLIENT_CONFIRM_LOST:
                mHandler.sendEmptyMessage(MainActivity.SERVER_CONFIRM_WIN);
                break;

            case MessageProtocol.K_CLIENT_CONFIRM_WIN:
            case MessageProtocol.K_SERVER_CONFIRM_LOST:
                mHandler.sendEmptyMessage(MainActivity.CLIENT_CONFIRM_WIN);
                break;
        }


//        byte[] readB = new byte[];
//        bb.readBytes(bb);
//        char action = bb.readChar();
//
//        if (action == 'a') {
//            int f = bb.readInt();
//            char ex = bb.readChar();
//            int s = bb.readInt();
//            System.out.println("Server sum=" + (f + s));
//
//            ByteBuf writeBuf = Unpooled.buffer();
//            writeBuf.writeChar('b').writeInt(f + s);
//            ctx.writeAndFlush(writeBuf);
//        }

//        ByteBuf wb = Unpooled.buffer();
//        wb.writeChar('x');
//        recipients.flushAndWrite(wb);

//        String readLine = new String(readB, "UTF-8");
//        String[] sp = readLine.split(" ");
//        int sum = Integer.valueOf(sp[0]) + Integer.valueOf(sp[1]);

//        for (int i = 0; i < bb.readableBytes(); i++) {
//            System.out.print(bb.readByte() + " ");
//        }


    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        try {
            recipients.remove(ctx.channel());
            System.out.println("删除channel成功" + recipients.size());
        } catch (Exception ex) {
            System.out.println("删除channel失败" + ex.getMessage());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        logger.log(Level.WARNING, "Unexpected exception from downstream.", cause);
        ctx.close();
    }

    public void notifyMessage(ByteBuf bb) {
        recipients.flushAndWrite(bb);
    }
}
