package com.xinzhou.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xinzhou.entity.Message;
import com.xinzhou.service.impl.WebSocket;
import com.xinzhou.utils.CommonUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HttpAuthHandler extends TextWebSocketHandler {
    public String key;
    public static int onlineCount=0;
    public static ConcurrentHashMap<String, WebSocketSession> hashMap=new ConcurrentHashMap<>();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        key = CommonUtil.splitKey( (String) attributes.get("userId"), (String) attributes.get("identity"));
        if (StringUtils.isEmpty(key)){
            session.close();
        }
        hashMap.put(key, session);
        addOnlineCount();
        System.out.println(getOnlineCount());
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String mess = message.getPayload();
        if (StringUtils.isEmpty(mess)){
            return;
        }
        Message message2 = JSONUtil.toBean(mess, Message.class);
        if (message2.getCode()==0) {

        }

    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        super.handlePongMessage(session, message);

    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        removeOnlineCount();
        hashMap.remove(key);
        if (session.isOpen()) {
            session.close();
        }
    }

    //??????????????????
    public boolean isOnline(String key){
        return hashMap.containsKey(key);
    }

    //?????????????????????seeion
    public WebSocketSession getSession(String key){
        return hashMap.get(key);
    }

    //???????????????????????????
    public void sendToUser(String receiveKey,String mess) throws IOException {
        if (isOnline(receiveKey)) {
            try {
                getSession(receiveKey).sendMessage(new TextMessage(mess));
            } catch (IOException e) {
                //????????????????????????????????????
            }

        }

    }

    //??????????????????
    public static synchronized void addOnlineCount(){
        onlineCount++;
    }
    public static synchronized void removeOnlineCount(){
        onlineCount--;
    }
    public static synchronized int getOnlineCount(){
        return HttpAuthHandler.onlineCount;
    }
}
