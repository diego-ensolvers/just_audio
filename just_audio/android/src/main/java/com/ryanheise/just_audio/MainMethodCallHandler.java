package com.ryanheise.just_audio;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.ryanheise.just_audio.dto.PlayerParamsDTO;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class MainMethodCallHandler implements MethodCallHandler {

    private final Context applicationContext;
    private final BinaryMessenger messenger;
    private ActivityPluginBinding activityPluginBinding;
    public static PlayerParamsDTO playerParamsDTO = null;

    private final Map<String, AudioPlayer> players = new HashMap<>();

    public MainMethodCallHandler(Context applicationContext,
                                 BinaryMessenger messenger) {
        this.applicationContext = applicationContext;
        this.messenger = messenger;
    }

    void setActivityPluginBinding(ActivityPluginBinding activityPluginBinding) {
        this.activityPluginBinding = activityPluginBinding;
        /* Old approach
        for (AudioPlayer player : players.values()) {
            player.setActivityPluginBinding(activityPluginBinding);
        }*/
    }

    @SuppressWarnings("deprecation")
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        try {
            ActivityManager manager = (ActivityManager) this.applicationContext.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public void onMethodCall(MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "init": {
                String id = call.argument("id");
                if (Boolean.TRUE.equals(call.argument("androidAudioPreview"))) {
                    // Old approach
                    if (players.containsKey(id)) {
                        result.error("Platform player " + id + " already exists", null, null);
                        break;
                    }
                    List<Object> rawAudioEffects = call.argument("androidAudioEffects");
                    final AudioPlayer player = new AudioPlayer(applicationContext, messenger, id, call.argument("audioLoadConfiguration"), rawAudioEffects, call.argument("androidOffloadSchedulingEnabled"));
                    players.put(id, player);
                    if (activityPluginBinding != null) {
                        player.setActivityPluginBinding(activityPluginBinding);
                    }
                } else {
                    if (isMyServiceRunning(AudioPlayer.class)) {
                        result.error("Platform player " + id + " already exists", null, null);
                        break;
                    }
                    playerParamsDTO = new PlayerParamsDTO(
                            applicationContext,
                            messenger,
                            id,
                            call.argument("audioLoadConfiguration"),
                            call.argument("androidAudioEffects"),
                            call.argument("androidOffloadSchedulingEnabled"),
                            activityPluginBinding
                    );

                    this.activityPluginBinding.getActivity().startForegroundService(new Intent(this.activityPluginBinding.getActivity(), AudioPlayer.class));
                }
                result.success(null);
                break;
            }
            case "disposePlayer": {
                String id = call.argument("id");
                AudioPlayer player = players.get(id);
                if (player != null) {
                    player.dispose();
                    players.remove(id);
                } else {
                    dispose();
                }
                result.success(new HashMap<String, Object>());
                break;
            }
            case "disposeAllPlayers": {
                for (AudioPlayer player : new ArrayList<AudioPlayer>(players.values())) {
                    player.dispose();
                }
                players.clear();
                dispose();
                result.success(new HashMap<String, Object>());
                break;
            }
            default:
                result.notImplemented();
                break;
        }
    }

    void dispose() {
        playerParamsDTO = null;
        Intent serviceIntent = new Intent(this.applicationContext, AudioPlayer.class);
        this.applicationContext.stopService(serviceIntent);
        for (AudioPlayer player : new ArrayList<AudioPlayer>(players.values())) {
            player.dispose();
        }
        players.clear();
    }
}
