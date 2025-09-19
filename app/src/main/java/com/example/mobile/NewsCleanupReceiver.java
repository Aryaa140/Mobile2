package com.example.mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NewsCleanupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("news_prefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();

        String json = sharedPreferences.getString("news_items", null);
        if (json != null) {
            Type type = new TypeToken<List<NewsItem>>(){}.getType();
            List<NewsItem> newsItems = gson.fromJson(json, type);

            if (newsItems != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                Date oneWeekAgo = calendar.getTime();

                List<NewsItem> itemsToKeep = new ArrayList<>();

                for (NewsItem item : newsItems) {
                    if (item.getTimestamp().after(oneWeekAgo)) {
                        itemsToKeep.add(item);
                    }
                }

                String updatedJson = gson.toJson(itemsToKeep);
                sharedPreferences.edit().putString("news_items", updatedJson).apply();
            }
        }
    }
}