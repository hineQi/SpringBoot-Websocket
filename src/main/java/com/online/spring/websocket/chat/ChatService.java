package com.online.spring.websocket.chat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import redis.clients.jedis.Jedis;

import com.online.spring.websocket.util.JsonUtil;
import com.online.spring.websocket.util.redis.JedisUtils;

/**
 * 聊天服务
 * 
 * @author tongyufu
 *
 */
@Service
public class ChatService {

    public static final String              CHANNEL = "chat.message";

    private Map<WebSocketSession, UserInfo> users;

    private Map<String, Map<WebSocketSession, UserInfo>> pushKeysAndUsers;

    private static final String SUBSCRIBE_TYPE = "subscribe_type";
    private static final String SUBSCRIBE_VALUE = "subscribe_value";
    private static final String SUBSCRIBE_RECEIVE = "subscribe_receive";
    private static final String SUBSCRIBE_USER = "subscribe_user";

    /**推送消息*/
    public void broadcast(String message) throws IOException {
        for (WebSocketSession session : users.keySet()) {
            session.sendMessage(new TextMessage(message));
        }
    }
    /**根据channel推送消息 By QiHaiYang*/
    public void broadcastToChannel(String pushKey,String message) throws IOException {
        for (WebSocketSession session : users.keySet()) {
            UserInfo userInfo = users.get(session);
            if(userInfo.getReceive().equals(pushKey)){
                session.sendMessage(new TextMessage(message));
            }
        }
    }

    public void broadcastToSingle(String user,String receive,String message) throws IOException {
        for (WebSocketSession session : users.keySet()) {
            UserInfo userInfo = users.get(session);
            if(userInfo.getName().equals(receive) || userInfo.getName().equals(user)){
                session.sendMessage(new TextMessage(message));
            }
        }
    }

    /**发布到redis*/
    public void publish(String message) {
        Jedis jedis = null;
        try {
            jedis = JedisUtils.getJedis();
            jedis.publish(CHANNEL, message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public String buildMessage(String type, String message) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("type", type);
        map.put("value", message);
        return JsonUtil.toJson(map);
    }

    public String buildJsonMessage(String user,String receive,String type, String message) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(SUBSCRIBE_TYPE, type);
        map.put(SUBSCRIBE_VALUE, message);
        map.put(SUBSCRIBE_RECEIVE, receive);
        map.put(SUBSCRIBE_USER, user);
        return JsonUtil.toJson(map);
    }

    public String getNames() {
        StringBuilder names = new StringBuilder("<b>在线用户</b><br>");
        for (UserInfo userInfo : users.values()) {
            names.append(userInfo.getName()).append("<br>");
        }
        return names.toString();
    }

    public void setUsers(Map<WebSocketSession, UserInfo> users) {
        this.users = users;
    }

    public void setPushKeysAndUsers(Map<String, Map<WebSocketSession, UserInfo>> pushKeysAndUsers) {
        this.pushKeysAndUsers = pushKeysAndUsers;
    }

    public Map<String, Map<WebSocketSession, UserInfo>> getPushKeysAndUsers(){
        return pushKeysAndUsers;
    }

}
