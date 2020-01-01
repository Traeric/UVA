package com.eric.uav.applications.voice.recog.event_handler;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.eric.uav.R;
import com.eric.uav.applications.voice.VoiceActivity;
import com.eric.uav.applications.voice.audioplay.listener.FinishStatus;
import com.eric.uav.applications.voice.wakeup.listener.SimpleWakeupListener;

/**
 * 处理用户的语音事件
 */
public abstract class EventHandler {
    protected Context context;

    public EventHandler(Context context) {
        this.context = context;
    }

    /**
     * 处理语音
     *
     * @param word 用户说的话
     */
    public void assignEvent(String word) {
        before(word);
        // 切换图片
        if (!FinishStatus.continueConversationMode) {
            Glide.with(context).load(R.drawable.voice_symble).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(((VoiceActivity) context).getVoiceAssistantLogo());
            // 开启唤醒
            SimpleWakeupListener.flag = true;
        }
    }

    protected abstract void before(String word);
}