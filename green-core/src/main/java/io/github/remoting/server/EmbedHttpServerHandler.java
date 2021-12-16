package io.github.remoting.server;

import io.github.model.MetricConfig;
import io.github.model.Result;
import io.github.remoting.api.ClientApi;
import io.github.util.GsonUtil;
import io.github.util.ThrowableUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author maike
 * @date 2021年12月07日 10:38 下午
 */
@Slf4j
public class EmbedHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private ClientApi remoteService;
    private ThreadPoolExecutor bizThreadPool;

    public EmbedHttpServerHandler(ClientApi clientApi, ThreadPoolExecutor bizThreadPool) {
        this.remoteService = clientApi;
        this.bizThreadPool = bizThreadPool;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {

        // request parse
        String requestData = msg.content().toString(CharsetUtil.UTF_8);
        String uri = msg.uri();
        HttpMethod httpMethod = msg.method();
        boolean keepAlive = HttpUtil.isKeepAlive(msg);

        // invoke
        bizThreadPool.execute(() -> {
            // do invoke
            Object responseObj = process(httpMethod, uri, requestData);

            // to json
            String responseJson = GsonUtil.toJson(responseObj);

            // write response
            writeResponse(ctx, keepAlive, responseJson);
        });
    }

    private Object process(HttpMethod httpMethod, String uri, String requestData) {

        // valid
        if (HttpMethod.POST != httpMethod) {
            return Result.fail("invalid request, HttpMethod not support.");
        }
        if (uri == null || uri.trim().length() == 0) {
            return Result.fail("invalid request, uri-mapping empty.");
        }

        // services mapping
        try {
            if ("/beat".equals(uri)) {
                return remoteService.beat();
            } else if ("/pushConfig".equals(uri)) {
                MetricConfig config = GsonUtil.fromJson(requestData, MetricConfig.class);
                log.info("收到服务端推送的配置信息, config={}", requestData);
                return remoteService.pushConfig(config);
            } else {
                return Result.fail("invalid request, uri-mapping(" + uri + ") not found.");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.fail("request error:" + ThrowableUtil.toString(e));
        }
    }

    /**
     * write response
     */
    private void writeResponse(ChannelHandlerContext ctx, boolean keepAlive, String responseJson) {
        // write response
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(responseJson, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.writeAndFlush(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("netty_http server caught exception", cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
            log.debug("netty_http server close an idle channel.");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
