package io.github.biezhi.wechat;


import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import io.github.biezhi.wechat.api.annotation.Bind;
import io.github.biezhi.wechat.api.constant.Config;
import io.github.biezhi.wechat.api.enums.MsgType;
import io.github.biezhi.wechat.api.model.WeChatMessage;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JuBoWeChatBot extends WeChatBot {

    Logger log = LoggerFactory.getLogger(JuBoWeChatBot.class);

    private static AutoMsg autoMsg = null;

    static {
        String jsonPath = System.getProperty("user.dir") + "/assets/config.json";
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(jsonPath), "UTF-8");
            autoMsg = (AutoMsg) new Gson().fromJson(reader, AutoMsg.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JuBoWeChatBot(Config config) {
        super(config);
    }

    public static void main(String[] args) {
        Config conf = new Config();
        conf.autoLogin(false);
        conf.showTerminal(false);
        conf.autoAddFriend(true);
        JuBoWeChatBot bot = new JuBoWeChatBot(conf);
        bot.start();
    }

    @Bind(msgType = {MsgType.TEXT})
    public void handleText(WeChatMessage message) {
        if (StringUtils.isNotEmpty(message.getName())) {
            this.log.info("接受到 [{}] 的文本消息: {}", message.getName(), message.getText());
            sendMsg(message);
        }
    }

    @Bind(msgType = {MsgType.IMAGE})
    public void handleImage(WeChatMessage message) {
        if (StringUtils.isNotEmpty(message.getName())) {
            this.log.info("接受到 [{}] 的图片消息: {}", message.getName(), message.getImagePath());
            sendMsg(message);
        }
    }

    @Bind(msgType = {MsgType.VOICE})
    public void handleVoice(WeChatMessage message) {
        if (StringUtils.isNotEmpty(message.getName())) {
            this.log.info("接受到 [{}] 的音频消息: {}", message.getName(), message.getVoicePath());
            sendMsg(message);
        }
    }

    @Bind(msgType = {MsgType.VIDEO})
    public void handleVideo(WeChatMessage message) {
        if (StringUtils.isNotEmpty(message.getName())) {
            this.log.info("接受到 [{}] 的音频消息: {}", message.getName(), message.getVideoPath());
            sendMsg(message);
        }
    }

    @Bind(msgType = {MsgType.PERSON_CARD})
    public void handlePersonCard(WeChatMessage message) {
        if (StringUtils.isNotEmpty(message.getName())) {
            this.log.info("接受到 [{}] 的名片消息: {}", message.getName(), message.getRecommend());
            api().verify(message.getRecommend());
            sendMsg(message);
        }
    }

    @Bind(msgType = {MsgType.EMOTICONS})
    public void handleEMOTICONS(WeChatMessage message) {
        if (StringUtils.isNotEmpty(message.getName())) {
            this.log.info("接受到 [{}] 的动画表情消息: {}", message.getName(), JSON.toJSONString(message.getRaw()));
            sendMsg(message);
        }
    }

    @Bind(msgType = {MsgType.SHARE})
    public void handleShare(WeChatMessage message) {
        if (StringUtils.isNotEmpty(message.getName())) {
            this.log.info("接受到 [{}] 的动画表情消息: {}", message.getName(), JSON.toJSONString(message.getRaw()));
            sendMsg(message);
        }
    }

    public void sendMsg(WeChatMessage message) {
        String text = message.getText();
        boolean sendFlag = false;
        if ((StringUtils.isNotEmpty(text)) && (null != autoMsg) && (autoMsg.getList().size() > 0)) {
            List<Msg> msgList = autoMsg.getList();
            for (Msg msg : msgList) {
                if (text.contains(msg.getKey())) {
                    sendMsg(message.getFromUserName(), msg.getValue());
                    sendFlag = true;
                    break;
                }
            }
        }
        if (!sendFlag) {
            if (!message.isGroup()) {
                sendRobotMsg(message);
            } else {
                // sendRobotMsg(message);
                System.out.println("-----群消息------");
            }
        }
    }

    public void sendRobotMsg(WeChatMessage message) {
        if (message.getMsgType().equals(MsgType.TEXT)) {
            String url = autoMsg.getAuto().getCustomRobotUrl();
            String remoteMst = requestRemoteService(message.getText(), url);
            if (StringUtils.isNotBlank(remoteMst)) {
                sendMsg(message.getFromUserName(), remoteMst);
            }
        } else {
            String automsg = autoMsg.getAuto().getAutoReplyMsg();
            sendMsg(message.getFromUserName(), automsg);
        }
    }

    public String requestRemoteService(String text, String url) {
        String msg = null;
        url = url + "?text=" + text;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        try {
            Response response = client.newCall(request).execute();
            if ((response.isSuccessful()) && (null != response.body())) {
                msg = response.body().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }
}
