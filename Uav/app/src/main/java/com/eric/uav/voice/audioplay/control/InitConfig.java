package com.eric.uav.voice.audioplay.control;

import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.eric.uav.voice.audioplay.utils.IOfflineResourceConst;

import java.util.Map;

/**
 * 合成引擎的初始化参数
 * <p>
 * Created by fujiayi on 2017/9/13.
 */

public class InitConfig {
    /**
     * appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
     * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
     */
    private String appId;

    private String appKey;

    private String secretKey;

    private String sn;

    /**
     * 纯在线或者离在线融合
     */
    private TtsMode ttsMode;


    /**
     * 初始化的其它参数，用于setParam
     */
    private Map<String, String> params;

    private InitConfig() {

    }

    // 离在线SDK用
    public InitConfig(String appId, String appKey, String secretKey, TtsMode ttsMode,
                      Map<String, String> params) {
        this.appId = appId;
        this.appKey = appKey;
        this.secretKey = secretKey;
        this.ttsMode = ttsMode;
        this.params = params;
    }


    // 纯离线SDK用
    public InitConfig(String appId, String appKey, String secretKey, String sn, TtsMode ttsMode,
                      Map<String, String> params) {
        this(appId, appKey, secretKey, ttsMode, params);
        this.sn = sn;
        if (sn != null) {
            // 纯离线sdk 才有的参数；离在线版本没有
            params.put(IOfflineResourceConst.PARAM_SN_NAME, sn);
        }
    }

    public Map<String, String> getParams() {
        return params;
    }


    public String getAppId() {
        return appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public TtsMode getTtsMode() {
        return ttsMode;
    }

    public String getSn() {
        return sn;
    }
}