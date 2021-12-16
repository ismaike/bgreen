package io.github.remoting.server;

import io.github.remoting.api.ClientApi;
import io.github.remoting.api.ClientApiImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author maike
 * @date 2021年12月07日 10:03 下午
 */
@Slf4j(topic = "EmbedServer")
public class EmbedHttpServer {

    private static final ThreadPoolExecutor BIZ_THREAD_POOL = new ThreadPoolExecutor(
            0,
            200,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(2000),
            r -> new Thread(r, "BGreen, EmbedServer bizThreadPool-" + r.hashCode()),
            (r, executor) -> {
                throw new RuntimeException("BGreen, EmbedServer bizThreadPool is EXHAUSTED!");
            });
    private Thread thread;
    private ClientApi remoteService = new ClientApiImpl();

    public void start(final int port) {
        thread = new Thread(() -> {
            // param
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline()
                                        .addLast(new IdleStateHandler(0, 0, 30 * 3, TimeUnit.SECONDS))
                                        .addLast(new HttpServerCodec())
                                        .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                        .addLast(new EmbedHttpServerHandler(remoteService, BIZ_THREAD_POOL));
                            }
                        })
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
                ChannelFuture future = bootstrap.bind(port).sync();
                log.info("server start success, port = {}", port);

                future.channel().closeFuture().sync();
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    log.info("server stop.");
                } else {
                    log.error("server error.", e);
                }
            } finally {
                try {
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() throws Exception {
        // destroy server thread
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
        log.info("BGreen, EmbedServer destroy success.");
    }
}
