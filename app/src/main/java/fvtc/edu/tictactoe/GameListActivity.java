package fvtc.edu.tictactoe;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

import java.util.ArrayList;
import java.util.UUID;

public class GameListActivity extends AppCompatActivity {

    public static final String TAG = "myDebug";
    RecyclerView gameList;
    GameAdapter gameAdapter;
    ArrayList<Game> games;
    private String hubConnectionId;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        // Retrieve the InternetCheck boolean.
        /*new InternetCheck(i -> {
            Log.d(TAG, "onCreate: InternetCheck: " + String.valueOf(i));
        });*/

        Log.d(TAG, "onCreate: 1");

        initListButton();
        Log.d(TAG, "onCreate: 2");
        initAddGameButton();
        Log.d(TAG, "onCreate: 3");
        initDeleteSwitch();
        Log.d(TAG, "onCreate: 4");
        iniSettingsButton();
        Log.d(TAG, "onCreate: 5");

        this.setTitle("Game List");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void iniSettingsButton() {
        ImageButton ibSettings = findViewById(R.id.imageButtonSettings);

        ibSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show the List Activity
                Intent intent = new Intent(GameListActivity.this, SetUser.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void initSignalR(String from, String msg) {

        HubConnection hubConnection = HubConnectionBuilder
                .create("https://fvtcdp.azurewebsites.net/GameHub")
                .build();

        Log.d(TAG, "initSignalR: Starting the hub connection...");

        hubConnection.start().blockingAwait();
        hubConnection.invoke(Void.class, "GetConnectionId");

        hubConnectionId = hubConnection.getConnectionId();

        Log.d(TAG, "initSignalR: Started the hub connection..." + hubConnectionId);

        // Great a callback method to receive messages.
        hubConnection.on("ReceiveMessage", (user, message) -> {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: New Message : " + user + " : " + message);
                    gameAdapter.notifyDataSetChanged();
                    RecyclerView rv = findViewById(R.id.rvGames);
                    rv.scrollToPosition(games.size() -1);

                }
            });
        }, String.class, String.class);

        // Send a message
        //hubConnection.send("SendMessage", from, msg);

        Log.d(TAG, "initSignalR: Finish");
    }

    private void initDeleteSwitch() {
        Switch s = findViewById(R.id.switchDelete);

        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Boolean status = compoundButton.isChecked();
                // Communicate the status to the adapter
                Log.d(TAG, "onCheckedChanged: Checked change event Switch: " + b);
                gameAdapter.setDelete(status);

                // rebind the recyclerview in the adapter
                gameAdapter.notifyDataSetChanged();
            }
        });

    }

    private void initAddGameButton() {
        Button btnAddGame = findViewById(R.id.buttonAddGame);
        btnAddGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show Dialog
                addItemDialog();
            }
        });
    };

    public void saveToAPI(Game game, boolean post) {
        try {
            if(post) {
                RestClient.execPostRequest(game,
                        MainActivity.GAMEAPI,
                        this, new VolleyCallback() {
                            @Override
                            public void onSuccess(ArrayList<Game> result) {
                                Log.d(TAG, "onSuccess: Post: " + result);
                                games.add(result.get(0));
                                initSignalR("bfoote", "New Game Created...");
                            }
                        });
            }
            else
            {
                RestClient.execPutRequest(game,
                        MainActivity.GAMEAPI + game.getId(),
                        this, new VolleyCallback() {
                            @Override
                            public void onSuccess(ArrayList<Game> result) {
                                Log.d(TAG, "onSuccess: Put: " + result);
                            }
                        });
            }
        }
        catch(Exception e)
        {
            Log.d(TAG, "saveToAPI: " + e.getMessage());
        }
    }

    private void addNewGame(String owner, String name, Boolean playComputer)
    {
        Game game = new Game(owner, name, playComputer ? "Computer" : null);
        Log.d(TAG, "addNewGame: " + owner + ":" + name+ ":" + (playComputer ? "Computer" : null));
        saveToAPI(game, true);
    }
    private void initListButton() {
        ImageButton ibList = findViewById(R.id.imageButtonList);

        ibList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show the List Activity
                Intent intent = new Intent(GameListActivity.this, GameListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            UUID gameId = games.get(position).getId();
            Log.d(TAG, "onClick: " + games.get(position).getPlayer1());

            Intent intent = new Intent(GameListActivity.this, MainActivity.class);
            intent.putExtra("gameId", gameId.toString());
            startActivity(intent);
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();

        String sortField = getSharedPreferences("GamesPreferences",
                Context.MODE_PRIVATE).getString("sortfield", "name");
        String sortOrder = getSharedPreferences("GamesPreferences",
                Context.MODE_PRIVATE).getString("sortorder", "ASC");

        username = getSharedPreferences("GamesPreferences",
                Context.MODE_PRIVATE).getString("username", "human");

        if(username.equals("human"))
        {
            Intent intent = new Intent(GameListActivity.this, SetUser.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        games = new ArrayList<>();

        try{
            RestClient.execGetRequest(MainActivity.GAMEAPI, this,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(ArrayList<Game> result) {
                            for(Game t : result)
                            {
                                Log.d(TAG, "onSuccess: " + t.getPlayer1());
                            }
                            games = result;
                            Log.d(TAG, "onSuccess: ");
                            RebindGames();
                        }
                    });
        }
        catch(Exception e)
        {
            Log.d(TAG, "onResume: " + e.getMessage());
        }

    }

    private void RebindGames()
    {
        Log.d(TAG, "RebindGames: 1" );
        gameList = findViewById(R.id.rvGames);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        gameList.setLayoutManager(layoutManager);
        Log.d(TAG, "RebindGames: 2" );

        gameAdapter = new GameAdapter(games, this);
        Log.d(TAG, "RebindGames: 3" );
        gameAdapter.setOnClickListener(onItemClickListener);
        Log.d(TAG, "RebindGames: 4" );
        gameList.setAdapter(gameAdapter);
        Log.d(TAG, "RebindGames: 5" );
        initSignalR(username, "Connected...");
    }

    private void addItemDialog(){
        Log.d(TAG, "addItemDialog: ");

        LayoutInflater inflater = LayoutInflater.from(this);
        View addView = inflater.inflate(R.layout.add_item, null);

        new AlertDialog.Builder(this)
                .setTitle("Add Item")
                .setView(addView)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                EditText etNewItem = addView.findViewById(R.id.etNewItem);
                                String name = etNewItem.getText().toString();
                                Switch switchPlayerComputer = addView.findViewById(R.id.switchPlayComputer);
                                //Log.d(TAG, "onClick: " + username + ":" +  name + ":" + switchPlayerComputer.isChecked());
                                addNewGame(username, name, switchPlayerComputer.isChecked());
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Cancel was clicked - do nothing
                            }
                        })
                .show();
    }
}