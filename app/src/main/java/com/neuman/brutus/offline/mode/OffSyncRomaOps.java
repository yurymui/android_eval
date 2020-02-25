package com.neuman.brutus.offline.mode;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import com.neuman.brutus.retrofit.Client;
import com.neuman.brutus.retrofit.models.RomaFilters;
import com.neuman.brutus.retrofit.models.RomaResponse;
import com.neuman.brutus.storage.OffSyncDbHandler;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OffSyncRomaOps {
    private JsonObject fetch_params = null;
    private OffSyncDbHandler offSyncDbHandler;
    Gson gson = new Gson();

    public void offsync_fetch_roma(ArrayList<RomaFilters> filters, String roma_module_id, String offset, String limit, Context context, Callback<RomaResponse> callback) {

        fetch_params = new JsonObject();
        fetch_params.addProperty("account", "1");
        fetch_params.addProperty("roma_module_id", roma_module_id);
        fetch_params.add("filters", new JsonParser().parse(filters.toString()));
        fetch_params.addProperty("offset", offset);
        fetch_params.addProperty("limit", limit);

        if (isNetworkAvailable(context)) {
            Client.getService(context).roma_fetch(fetch_params).enqueue(callback);
        } else {
            readfrom_offsync(fetch_params, context);
        }
    }

    public void writeto_offsync(RomaResponse romaResponse, Context context, Integer max_stored) {
        if (fetch_params != null) {
            offSyncDbHandler = new OffSyncDbHandler(context, 1);
            offSyncDbHandler.pushOffsyncRequest(fetch_params.toString(), gson.toJson(romaResponse), "", "roma_fetch_req", 0, 0);
            readfrom_offsync(fetch_params, context);
        }
    }

    public void readfrom_offsync(JsonObject fetch_params, Context context) {
        offSyncDbHandler = new OffSyncDbHandler(context, 1);
        offSyncDbHandler.readOffsyncRequest(fetch_params.toString());
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
