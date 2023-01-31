package com.ryanheise.just_audio.dto;

import android.content.Context;

import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;

public class PlayerParamsDTO {
    public Context appContext;
    public BinaryMessenger binaryMessenger;
    public String playerId;
    public Map<?, ?> audioLoadConfiguration;
    public List<Object> rawAudioEffects;
    public Boolean offloadSchedulingEnabled;
    public ActivityPluginBinding activityPluginBinding;

    public PlayerParamsDTO(Context appContext, BinaryMessenger binaryMessenger, String playerId, Map<?, ?> audioLoadConfiguration, List<Object> rawAudioEffects, Boolean offloadSchedulingEnabled, ActivityPluginBinding activityPluginBinding) {
        this.appContext = appContext;
        this.binaryMessenger = binaryMessenger;
        this.playerId = playerId;
        this.audioLoadConfiguration = audioLoadConfiguration;
        this.rawAudioEffects = rawAudioEffects;
        this.offloadSchedulingEnabled = offloadSchedulingEnabled;
        this.activityPluginBinding = activityPluginBinding;
    }
}
