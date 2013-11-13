package cn.link.lis.test;

import cn.link.lis.MessageProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.io.InputStreamReader;
import java.util.Scanner;


public class LIPClient {
    private final String host;
    private final int port;
    private Channel channel;

    public LIPClient(String host, int port) {
        this.host = host;
        this.port = port;
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
                                    new LIPClientHandler()
                            );
                        }
                    });
            // Start the client.
            ChannelFuture f = b.connect(host, port).sync();
            channel = f.channel();
            String readLine = null;
            Scanner scanner = new Scanner(new InputStreamReader(System.in));
            while (true) {
                readLine = scanner.nextLine();
                if (readLine.equals("exit")) {
                    break;
                }

                if (readLine.equals("s")) {
                    for (int i = 0; i < 100; i++) {
                        ByteBuf bb = MessageProtocol.build(MessageProtocol.K_START_GAME);
                        channel.writeAndFlush(bb);
                        Thread.sleep(10);
                    }
                }
            }
            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
        }
    }

    public void close() {
        channel.closeFuture();
    }

    public static void main(String[] args) throws Exception {
        new LIPClient("192.168.10.135", 8082).connect();
    }
}