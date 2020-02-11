package io.netty.example.promischain;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.example.echo.EchoServerHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.nio.charset.StandardCharsets;

/**
 * @author ma.zhenjun
 * @since 09/02/2020
 */
public class Server {

    public static void main(String[] args) {
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();

                            p.addLast(new NioEventLoopGroup(), new ChannelOutboundHandlerAdapter() {
                                @Override
                                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                    ctx.writeAndFlush(Unpooled.copiedBuffer("aaa", StandardCharsets.UTF_8), promise).addListener(new GenericFutureListener<Future<? super Void>>() {
                                        @Override
                                        public void operationComplete(Future<? super Void> future) throws Exception {
                                            System.out.println("write2");
                                        }
                                    });
                                }
                            });
                            p.addLast(new NioEventLoopGroup(), new ChannelOutboundHandlerAdapter() {
                                @Override
                                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                    ctx.writeAndFlush(Unpooled.copiedBuffer("aaa", StandardCharsets.UTF_8), promise).addListener(new GenericFutureListener<Future<? super Void>>() {
                                        @Override
                                        public void operationComplete(Future<? super Void> future) throws Exception {
                                            System.out.println("write1");
                                        }
                                    });
                                }
                            });
                            p.addLast(new NioEventLoopGroup(), new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    ctx.writeAndFlush(Unpooled.copiedBuffer("aaa", StandardCharsets.UTF_8)).addListener(new GenericFutureListener<Future<? super Void>>() {
                                        @Override
                                        public void operationComplete(Future<? super Void> future) throws Exception {
                                            System.out.println("channelRead");
                                        }
                                    });
                                }
                            });
                        }
                    });

            // Start the server.
            ChannelFuture f = b.bind(8080).sync();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
