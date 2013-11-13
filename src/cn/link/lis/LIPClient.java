package cn.link.lis;

import android.os.Handler;
import android.os.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


public class LIPClient {
    private final String host;
    private final int port;
    private Channel channel;
    private Handler mHandler;

    public LIPClient(String host, int port, Handler handler) {
        this.host = host;
        this.port = port;
        this.mHandler = handler;
    }


    public void send(ByteBuf bb) {
        channel.writeAndFlush(bb);
    }

    public void connect() {
        // Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    //new LoggingHandler(LogLevel.INFO),
//                                    new DelimiterBasedFrameDecoder(1024 * 10, Delimiters.lineDelimiter()),
                                    new LengthFieldBasedFrameDecoder(1024 * 8, 0, 4, 0, 4),
                                    new LIPClientHandler(mHandler)
                            );
                        }
                    });
            // Start the client.
            ChannelFuture f = b.connect(host, port).sync();
            channel = f.channel();
            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Message msg = new Message();
            msg.what = MainActivity.ERROR;
            msg.obj = e;
            mHandler.sendMessage(msg);
        } finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
        }
    }

    public void close() {
        channel.closeFuture();
    }



}