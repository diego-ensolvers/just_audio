package com.ryanheise.just_audio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
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
    public static Map<String, Object> asd = new HashMap<>();
    public static List<Object> asd2 = new ArrayList<>();
    public static AudioPlayer ap;

    private final Map<String, AudioPlayer> players = new HashMap<>();

    public MainMethodCallHandler(Context applicationContext,
            BinaryMessenger messenger) {
        this.applicationContext = applicationContext;
        this.messenger = messenger;
    }

    void setActivityPluginBinding(ActivityPluginBinding activityPluginBinding) {
        this.activityPluginBinding = activityPluginBinding;
        for (AudioPlayer player : players.values()) {
            player.setActivityPluginBinding(activityPluginBinding);
        }
    }

    @Override
    public void onMethodCall(MethodCall call, @NonNull Result result) {
        switch (call.method) {
        case "init": {
            String id = call.argument("id");
            if (players.containsKey(id)) {
                result.error("Platform player " + id + " already exists", null, null);
                break;
            }
            List<Object> rawAudioEffects = call.argument("androidAudioEffects");

            asd.put("appContext", applicationContext);
            asd.put("messenger", messenger);
            asd.put("id", id);
            asd.put("audioLoadConfiguration", call.argument("audioLoadConfiguration"));
            asd2 = rawAudioEffects;
            asd.put("scheduled", call.argument("androidOffloadSchedulingEnabled"));
            asd.put("activityPluginBinding", activityPluginBinding);

            this.activityPluginBinding.getActivity().startService(new Intent(this.activityPluginBinding.getActivity(), AudioPlayer.class));
            //final AudioPlayer player = new AudioPlayer(applicationContext, messenger, id, call.argument("audioLoadConfiguration"), rawAudioEffects, call.argument("androidOffloadSchedulingEnabled"));
            //players.put(id, player);
            //if (activityPluginBinding != null) {
            //    player.setActivityPluginBinding(activityPluginBinding);
            //}
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
                if (ap != null) {
                    ap.dispose();
                }
            }
            result.success(new HashMap<String, Object>());
            break;
        }
        case "disposeAllPlayers": {
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
        for (AudioPlayer player : new ArrayList<AudioPlayer>(players.values())) {
            player.dispose();
        }
        players.clear();
    }
}
