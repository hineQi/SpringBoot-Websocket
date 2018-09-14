package com.online.spring.websocket.chat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 集群模式。使用redis作为消息中间件。
 * 
 * @author tongyufu
 *
 */
@Service
public class ChatClusterhandler extends TextWebSocketHandler {
    private static final AtomicInteger            userIds = new AtomicInteger(0);
    private final Map<WebSocketSession, UserInfo> users   = new ConcurrentHashMap<WebSocketSession, UserInfo>();
    @Autowired
    private ChatService                           chatService;

    private static final String MESSAGE_STR_SEND_TYPE = "sendType";
    private static final String MESSAGE_STR_SEND = "send";
    private static final String MESSAGE_STR_RECEIVE = "receive";
    private static final String MESSAGE_STR_MESSAGE = "message";

    private static final String SEND_TYPE_LOGIN = "subscribe";
    private static final String SEND_TYPE_LOGOUT = "logOut";
    private static final String SEND_TYPE_SEND_MESSAGE = "sendMessage";
    private static final String SEND_TYPE_SEND_SINGLE_MESSAGE = "sendSingleMessage";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UserInfo user = users.get(session);
        if (user == null) {
            Integer userId = userIds.getAndIncrement();
            user = new UserInfo(userId);
            users.put(session, user);
        }
        chatService.setUsers(users);
//        String message = chatService.buildMessage("message",
//            String.format("[%s]进入了聊天室", user.getName()));
//        chatService.publish(message);
//        chatService.publish(chatService.buildMessage("users", chatService.getNames()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage)
                                                                                       throws Exception {
        UserInfo user = users.get(session);
        JSONObject jsonObject = JSON.parseObject(textMessage.getPayload());
        if(jsonObject.containsKey(MESSAGE_STR_SEND_TYPE)){
            String sendType = jsonObject.getString(MESSAGE_STR_SEND_TYPE);
            String send = jsonObject.getString(MESSAGE_STR_SEND);
            String receive = jsonObject.getString(MESSAGE_STR_RECEIVE);
            if(sendType.equals(SEND_TYPE_LOGIN)){
                user.setName(send);
                user.setReceive(receive);
                users.put(session, user);
                String sendMessage = chatService.buildJsonMessage(user.getName(),user.getReceive(),SEND_TYPE_LOGIN,String.format("[%s]进入了聊天室", user.getName()));
                chatService.publish(sendMessage);
            }else if(sendType.equals(SEND_TYPE_SEND_MESSAGE)){
                String messageInfo = jsonObject.getString(MESSAGE_STR_MESSAGE);
                String message = String.format("[%s]对[%s]说：%s", user.getName(),receive, messageInfo);
                String sendMessage = chatService.buildJsonMessage(user.getName(),receive,SEND_TYPE_SEND_MESSAGE, message);
                chatService.publish(sendMessage);
            }else if(sendType.equals(SEND_TYPE_SEND_SINGLE_MESSAGE)){
                String messageInfo = jsonObject.getString(MESSAGE_STR_MESSAGE);
                String message = String.format("[%s]对[%s]说：%s", user.getName(),receive, messageInfo);
                String sendMessage = chatService.buildJsonMessage(user.getName(),receive,SEND_TYPE_SEND_SINGLE_MESSAGE, message);
                chatService.publish(sendMessage);
            }

        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
                                                                                   throws Exception {
        UserInfo user = users.remove(session);
        String message = chatService.buildJsonMessage(user.getName(),user.getReceive(),SEND_TYPE_LOGOUT,String.format("[%s]离开了聊天室", user.getName()));
        /*chatService.publish(message);*/
        chatService.setUsers(users);
        chatService.publish(message);
    }

}
