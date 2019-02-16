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
package io.netty.example.telnet;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * Simplistic telnet server.
 */
public final class TelnetServer {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8992" : "8023"));

    public static void main(String[] args) throws Exception {

        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        //Configure the server
        //创建两个EventLoopGroup对象
        //创建boss线程组 用于服务端接受客户端的连接
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);

        //创建woker线程组 用于进行SocketChannel的数据读写
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {

            //创建ServerBootstrap对象
            ServerBootstrap b = new ServerBootstrap();

            //设置使用的EventLoopGroup
            b.group(bossGroup, workerGroup)

             //设置要被实例化的为NioServerSocketChannel类
             .channel(NioServerSocketChannel.class)

             //设置NioServerSocketChannel的处理器
             .handler(new LoggingHandler(LogLevel.INFO))

             //设置连入服务端的Client的SocketChannel的处理器
             .childHandler(new TelnetServerInitializer(sslCtx));

            //绑定端口，并同步等待成功，即启动服务端 监听服务关闭，并阻塞等待
            b.bind(PORT).sync().channel().closeFuture().sync();
        } finally {

            //优雅关闭两个EventLoopGroup对象
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
