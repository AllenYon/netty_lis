/*
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
package cn.link.lis;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler implementation for the echo client.  It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class LIPClientHandler extends ChannelInboundHandlerAdapter {
    private Handler mHandler;

    public LIPClientHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Message hmsg = mHandler.obtainMessage(MainActivity.CLIENT_CONNECTED, "Connect ");
        mHandler.sendMessage(hmsg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf bb = (ByteBuf) msg;
        MessageProtocol mp = MessageProtocol.parse(bb);
        Log.w("", "*************************  mp action " + mp.action);
        switch (mp.action) {
            case MessageProtocol.K_SEND_LIP:
                //接收到 LIP 问题 列表
                Message hmsg = mHandler.obtainMessage(MainActivity.CLIENT_REV_LIS, mp);
                mHandler.sendMessage(hmsg);
                break;
            case MessageProtocol.K_SEND_CURRENT_INDEX:
                int[] currentIndex = mp.currentIndex;
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


//        char acton = bb.readChar();
//        if (acton == 'b') {
//            int sum = bb.readInt();
//            System.out.println("Sum =" + sum);
//
//            Message hmsg = mHandler.obtainMessage(1, "Sum = " + sum);
//            mHandler.sendMessage(hmsg);
////            notifyStateChanged(MSG_RECEIVE, "Sum = " + sum);
//
//        } else if (acton == 'r') {
//            byte[] re = new byte[bb.readableBytes()];
//            bb.readBytes(re);
//            String readLine = new String(re, "UTF-8");
//            System.out.println("ReadLine :" + re);
//        } else if (acton == 'x') {
//            System.out.println("Receive Notify ");
//            byte[] re = new byte[bb.readableBytes()];
//            bb.readBytes(re);
//            String readLine = new String(re, "UTF-8");
//            System.out.println("ReadLine :" + readLine);
//        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        ctx.close();
    }

}
