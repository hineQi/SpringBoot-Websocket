package com.online.spring.websocket.chat;

import java.io.IOException;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.online.spring.websocket.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.online.spring.websocket.util.redis.JedisUtils;

/**
 * 订阅消息
 * 
 * @author tongyufu
 *
 */
@Service
public class Subscribe {
    private static final String SEND_TYPE_LOGIN = "subscribe";
    private static final String SEND_TYPE_LOGOUT = "logOut";
    private static final String SEND_TYPE_SEND_MESSAGE = "sendMessage";
    private static final String SEND_TYPE_SEND_SINGLE_MESSAGE = "sendSingleMessage";

    private static final String SUBSCRIBE_TYPE = "subscribe_type";
    private static final String SUBSCRIBE_VALUE = "subscribe_value";
    private static final String SUBSCRIBE_RECEIVE = "subscribe_receive";
    private static final String SUBSCRIBE_USER = "subscribe_user";

    /**
     * 使用注入通过构造函数注入，@Qualifier必须指定Bean名字。
     */
    @Autowired
    public Subscribe(@Qualifier("chatService") final ChatService chatService) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Jedis jedis = JedisUtils.getJedis();

                jedis.subscribe(new JedisPubSub() {

                    @Override
                    public void onUnsubscribe(String channel, int subscribedChannels) {
                        System.out.println("onUnsubscribe:" + channel);
                    }

                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        System.out.println("onSubscribe:" + channel);
                    }

                    @Override
                    public void onPUnsubscribe(String pattern, int subscribedChannels) {
                        System.out.println("onPUnsubscribe:" + pattern);
                    }

                    @Override
                    public void onPSubscribe(String pattern, int subscribedChannels) {
                        System.out.println("onPSubscribe:" + pattern);
                    }

                    @Override
                    public void onPMessage(String pattern, String channel, String message) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onMessage(String channel, String message) {
                        System.out.print("onMessage:" + channel);
                        System.out.println("\t" + message);
                        JSONObject object = JSONObject.parseObject(message);
                        String user = object.getString(SUBSCRIBE_USER);
                        String receive = object.getString(SUBSCRIBE_RECEIVE);
                        String type = object.getString(SUBSCRIBE_TYPE);
                        String value = object.getString(SUBSCRIBE_VALUE);

                        try {
                            /*chatService.broadcast(message);*/
                            /*if(object.containsKey("pushKey") && object.containsKey("value")){
                                chatService.broadcastToChannel(object.getString("pushKey"),message);
                            }*/
                            if(type.equals(SEND_TYPE_SEND_MESSAGE)){
                                chatService.broadcastToChannel(receive,message);
                            }else if(type.equals(SEND_TYPE_SEND_SINGLE_MESSAGE)){
                                chatService.broadcastToSingle(user,receive,message);
                            }else if (type.equals(SEND_TYPE_LOGIN) || type.equals(SEND_TYPE_LOGOUT)){
                                chatService.broadcast(message);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, ChatService.CHANNEL);
            }
        }).start();;
    }

}
