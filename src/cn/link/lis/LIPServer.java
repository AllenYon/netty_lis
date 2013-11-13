package cn.link.lis;

import android.os.Handler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.InputStreamReader;
import java.util.Scanner;

public class LIPServer {

    public final String ipaddress;
    private final int port;
    private Channel mChannel;
    private Handler mHandler;
    LIPServerHandler mServerHandler;

    public LIPServer(String ip, int port, Handler h) {
        this.ipaddress = ip;
        this.port = port;
        this.mHandler = h;
    }

    public LIPServer(int port, Handler h) {
        this("localhost", port, h);
    }

    public void bind() throws Exception {
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    //new LoggingHandler(LogLevel.INFO),
//                                    new DelimiterBasedFrameDecoder(1024 * 10, Delimiters.lineDelimiter()),
                                    new LengthFieldBasedFrameDecoder(1024 * 8, 0, 4, 0, 4),
                                    mServerHandler = new LIPServerHandler(mHandler));

                        }
                    });


            // Start the server.
            ChannelFuture f = b.bind(ipaddress, port).sync();
            mChannel = f.channel();
            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void send(ByteBuf bb) {
//        LIPServerHandler handler = (LIPServerHandler) mChannel.pipeline().get("serverhandler");
        mServerHandler.notifyMessage(bb);
    }

    public void close() {
        mChannel.close();
    }
}
