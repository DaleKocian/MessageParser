package io.github.dkocian.hipchatmessage.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.dkocian.hipchatmessage.network.VolleySingleton;
import io.github.dkocian.hipchatmessage.util.Constants;
import io.github.dkocian.hipchatmessage.util.Extras;
import io.github.dkocian.hipchatmessage.util.JsonKeys;

/**
 * Created by dkocian on 8/4/2015.
 */
public class ParseMessageIntentService extends IntentService {
    private static final String TAG = ParseMessageIntentService.class.getName();
    public static final int MENTIONS_PATTERN_INDEX = 1;
    public static final int EMOTICONS_PATTERN_INDEX = 2;
    public static final int LINKS_PATTERN_INDEX = 3;
    private AtomicInteger numRequests = new AtomicInteger(0);
    final JSONArray jsonArrayLinks = new JSONArray();

    public ParseMessageIntentService() {
        super(ParseMessageIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String message = intent.getStringExtra(Extras.MESSAGE);
        try {
            JSONObject parsedMessage = parseChatMessage(message);
            sendBroadCast(message, parsedMessage);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            sendErrorBroadCast(e);
        }
    }

    private void sendBroadCast(String message, JSONObject parsedMessage) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra(Extras.MESSAGE, message);
        broadcastIntent.putExtra(Extras.PARSED_MESSAGE, parsedMessage.toString());
        broadcastIntent.setAction(Constants.PARSE_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    private void sendErrorBroadCast(JSONException e) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra(Extras.INTENT_SERVICE_ERROR, e.getMessage());
        broadcastIntent.setAction(Constants.PARSE_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    public JSONObject parseChatMessage(CharSequence message) throws JSONException {
        Pattern pattern = getPattern();
        Matcher m = pattern.matcher(message);
        JSONArray jsonArrayMentions = new JSONArray();
        JSONArray jsonArrayEmoticons = new JSONArray();
        while (m.find()) {
            if (m.group(MENTIONS_PATTERN_INDEX) != null) {
                jsonArrayMentions.put(m.group(MENTIONS_PATTERN_INDEX));
            } else if (m.group(EMOTICONS_PATTERN_INDEX) != null) {
                jsonArrayEmoticons.put(m.group(EMOTICONS_PATTERN_INDEX));
            } else if (m.group(LINKS_PATTERN_INDEX) != null) {
                String url = m.group(LINKS_PATTERN_INDEX);
                if (m.group(Constants.PROTOCOL_INDENTIFIER_INDEX) == null) {
                    url = Constants.HTTP + url;
                }
                requestTitle(url);
            }
        }
        JSONObject jsonObject = new JSONObject();
        putArrayInJsonObjectIfNotEmpty(jsonObject, JsonKeys.JSON_KEY_MENTIONS, jsonArrayMentions);
        putArrayInJsonObjectIfNotEmpty(jsonObject, JsonKeys.JSON_KEY_EMOTICONS, jsonArrayEmoticons);
        while (numRequests.get() > 0) { //TODO timeout after a certain time.
            Log.d(TAG, "Num Requests = " + numRequests);
        }
        putArrayInJsonObjectIfNotEmpty(jsonObject, JsonKeys.JSON_KEY_LINKS, jsonArrayLinks);
        return jsonObject;
    }

    private JSONObject putArrayInJsonObjectIfNotEmpty(JSONObject jsonObject, String key, JSONArray jsonArray) throws JSONException {
        if (jsonArray != null && jsonArray.length() > 0) {
            jsonObject.put(key, jsonArray);
        }
        return jsonObject;
    }

    private void requestTitle(final String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Document doc = Jsoup.parse(response);
                            String title = doc.title();
                            synchronized (jsonArrayLinks) {
                                jsonArrayLinks.put(new JSONObject()
                                        .put(JsonKeys.JSON_KEY_URL, url)
                                        .put(JsonKeys.JSON_LEY_TITLE, title));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        numRequests.decrementAndGet();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                numRequests.decrementAndGet();
            }
        });
        numRequests.incrementAndGet();
        VolleySingleton.getInstance(ParseMessageIntentService.this).addToRequestQueue(stringRequest);
    }

    private Pattern getPattern() {
        return Pattern.compile(Constants.OPEN_PARENTHESIS + Constants.MENTIONS_REGEX + Constants.CLOSE_PAREN_OR +
                Constants.OPEN_PARENTHESIS + Constants.EMOTICONS_REGEX + Constants.CLOSE_PAREN_OR +
                Constants.OPEN_PARENTHESIS + Constants.LINK_REGEX + Constants.CLOSE_PARENTHESIS);
    }
}
