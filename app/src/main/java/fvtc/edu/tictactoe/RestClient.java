package fvtc.edu.tictactoe;

import static com.android.volley.Request.*;
import static com.android.volley.toolbox.Volley.*;

import android.content.Context;
import android.util.Log;



import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;


public class RestClient {
    public static final String TAG = "RestClient";
    public static void execGetOneRequest(String url,
                                         Context context,
                                         VolleyCallback volleyCallback)
    {
        Log.d(TAG, "execGetOneRequest: Start");
        RequestQueue requestQueue = newRequestQueue(context);

        ArrayList<Game> games = new ArrayList<Game>();
        Log.d(TAG, "execGetOneRequest: " + url);

        try {
            StringRequest stringRequest = new StringRequest(Method.GET,
                    url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "onResponse: " + response);

                            try {
                                JSONObject object = new JSONObject(response);
                                Game game = new Game();
                                game.setId(UUID.fromString(object.getString("id")));
                                game.setConnectionId(object.getString("connectionId"));
                                game.setPlayer1(object.getString("player1"));
                                game.setGameState(object.getString("gameState"));
                                game.setPlayer2(object.getString("player2"));
                                game.setWinner(object.getString("winner"));
                                game.setCompleted(object.getBoolean("completed"));

                                games.add(game);

                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            volleyCallback.onSuccess(games);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "onErrorResponse: " + error.getMessage());
                            Log.i(TAG, "onResponse: error1");
                        }
                    });

            // Important!!!
            requestQueue.add(stringRequest);

        } catch (Exception e) {
            Log.d(TAG, "execGetOneRequest: Error" + e.getMessage());
            Log.i(TAG, "onResponse: error2");
        }
    }

    public static void execGetRequest(String url,
                                      Context context,
                                      VolleyCallback volleyCallback)
    {
        Log.d(TAG, "execGetRequest: Start");
        RequestQueue requestQueue = newRequestQueue(context);
        ArrayList<Game> games = new ArrayList<Game>();
        Log.d(TAG, "execGetRequest: " + url);

        try {
            StringRequest stringRequest = new StringRequest(Method.GET,
                    url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "onResponse: " + response);

                            try {
                                JSONArray items = new JSONArray(response);
                                for(int i = 0; i < items.length(); i++)
                                {
                                    JSONObject object = items.getJSONObject(i);
                                    Game game = new Game();
                                    game.setId(UUID.fromString(object.getString("id")));
                                    game.setConnectionId(object.getString("connectionId"));
                                    game.setPlayer1(object.getString("player1"));
                                    game.setGameState(object.getString("gameState"));
                                    game.setPlayer2(object.getString("player2"));
                                    game.setWinner(object.getString("winner"));
                                    game.setCompleted(object.getBoolean("completed"));

                                    games.add(game);
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            volleyCallback.onSuccess(games);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "onErrorResponse: " + error.getMessage());
                        }
                    });

            // Important!!!
            requestQueue.add(stringRequest);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static void executeRequest(Game game,
                                       String url,
                                       Context context,
                                       VolleyCallback volleyCallback,
                                       int method)
    {
        Log.d(TAG, "executeRequest: " + method + ":" + url);

        try {
            RequestQueue requestQueue = newRequestQueue(context);
            JSONObject object = new JSONObject();

            object.put("id", game.getId());
            object.put("connectionid", game.getConnectionId());
            object.put("player1", game.getPlayer1());
            object.put("player2", game.getPlayer2());
            object.put("winner", game.getWinner());
            object.put("completed", game.getCompleted());
            object.put("gamestate", game.getGameState());
            object.put("lastupdatedate", game.getLastUpdateDate());

            final String requestBody = object.toString();
            Log.d(TAG, "executeRequest: " + requestBody);

            JsonObjectRequest request = new JsonObjectRequest(method, url, object,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "onResponse: " + response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "onErrorResponse: " + error.getMessage());
                    Log.d(TAG, "onResponse: error2");
                    Log.i(TAG, "onErrorResponse: url = " + url);
                }
            })
            {
                @Override
                public byte[] getBody(){
                    Log.i(TAG, "getBody: " + object.toString());
                    return object.toString().getBytes(StandardCharsets.UTF_8);
                }
            };

            requestQueue.add(request);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void execDeleteRequest(Game game,
                                         String url,
                                         Context context,
                                         VolleyCallback volleyCallback)
    {
        try {
            executeRequest(game, url, context, volleyCallback, Method.DELETE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void execPutRequest(Game game,
                                      String url,
                                      Context context,
                                      VolleyCallback volleyCallback)
    {
        try {
            executeRequest(game, url, context, volleyCallback, Method.PUT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void execPostRequest(Game game,
                                       String url,
                                       Context context,
                                       VolleyCallback volleyCallback)
    {
        try {
            executeRequest(game, url, context, volleyCallback, Method.POST);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
