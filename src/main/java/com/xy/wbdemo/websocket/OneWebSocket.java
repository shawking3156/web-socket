package com.xy.wbdemo.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
@Slf4j
@ServerEndpoint(value = "/websocket")
@Component
public class OneWebSocket {
    /** 记录当前在线连接数 */
    private static AtomicInteger onlineCount = new AtomicInteger(0);
    /**
     * 定义会话对应开关的Map，用于服务端事实推送判断关闭
     */
    private  Map<String,Boolean> sessionOpenMap =new HashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        onlineCount.incrementAndGet(); // 在线数加1
        log.info("有新连接加入：{}，当前在线人数为：{}", session.getId(), onlineCount.get());
        sessionOpenMap.put(session.getId(),true);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        onlineCount.decrementAndGet(); // 在线数减1
        log.info("有一连接关闭：{}，当前在线人数为：{}", session.getId(), onlineCount.get());
        sessionOpenMap.put(session.getId(),false);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message
     *            客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) throws InterruptedException {
        log.info("服务端收到客户端[{}]的消息:{}", session.getId(), message);
        while(Boolean.TRUE.equals(sessionOpenMap.get(session.getId()))){
            log.info("查询数据库最新5s中的数据");
            this.sendMessage("最新5s中的数据, " + message, session);
            Thread.sleep(3000);
        }

    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
        sessionOpenMap.put(session.getId(),false);
    }

    /**
     * 服务端发送消息给客户端
     */
    private void sendMessage(String message, Session toSession) {
        try {
            log.info("服务端给客户端[{}]发送消息{}", toSession.getId(), message);
            toSession.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("服务端发送消息给客户端失败：{}", e);
        }
    }
}
