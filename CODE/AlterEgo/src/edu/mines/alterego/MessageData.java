package edu.mines.alterego;

import android.util.Log;

import org.json.JSONObject;
import org.json.JSONException;

class MessageData {
    public static enum StringFormat {
        MESSAGE
    };

    int msgId;
    String json;
    long timestamp;
    int gameId;

    MessageData(int msgid, String jsonMsg, long timestamp, int gameid) {
        this.msgId = msgid;
        this.json = jsonMsg;
        this.timestamp = timestamp;
        this.gameId = gameid;
    }

    public String getJson() { return json; }
    public long getTimestamp() { return timestamp; }
    public int getMessageId() { return msgId; }
    public int getGameId() { return gameId; }

    @Override
    public String toString() {
        return "MsgId: " + msgId + " Timestamp: " + timestamp + " GameId " + gameId + " JSON: " + json;
    }

    public String toString(StringFormat format) {
        try {
            switch (format) {
                case MESSAGE:
                    JSONObject jo = new JSONObject(json);
                    if (!jo.has("body")) {
                        return "";
                    } else {
                        String msgBody = jo.getString("body");
                        int srcip = jo.getInt("senderIP");
                        int recvip = jo.getInt("receiverIP");
                        if (srcip == recvip) {
                            return "me: " + msgBody;
                        } else {
                            return "" + srcip + ": " + msgBody;
                        }
                    }
            }
        } catch (JSONException e) {
            Log.e("AlterEgo::MessageData", "An Error ocurred while processing JSON!");
            e.printStackTrace();
        }
        return "";
    }
}
