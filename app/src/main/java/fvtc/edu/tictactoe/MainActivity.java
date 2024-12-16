package fvtc.edu.tictactoe;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    public static final String GAMEAPI = "https://fvtcdp.azurewebsites.net/api/Game/";
    private static final String TAG = "MainActivity";
    Point pt = new Point();
    Board board = new Board();
    String[][] cellvalues = new String[Board.BOARDSIZE][Board.BOARDSIZE];
    int width;
    int height;
    Game game;
    String username;
    private String hubConnectionId;
    HubConnection hubConnection;
    String turn = "O"; // Starting player
    String playerPiece = "E";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        GetScreenDims();
        board = new Board(width);
        initialSetup();
        board.cellvalues = cellvalues;

        Bundle extras = getIntent().getExtras();

        username = getSharedPreferences("GamesPreferences",
                Context.MODE_PRIVATE).getString("username", "human");

        if(username.equals("human"))
        {
            Intent intent = new Intent(MainActivity.this, SetUser.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        if(extras != null) {
            // Edit an existing team
            Log.d(TAG, "onCreate: " + extras.getString("gameId"));
            initGame(extras.getString("gameId"));

        }
        else {
            // Make a new one.
            //game = new Game();
            //initSignalR("Human", "Connected...");
            //Log.d(TAG, "onCreate: New Team");
        }
        //setContentView(new DrawView(getMainActivity()));
        setTitle(R.string.app_name);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        username = getSharedPreferences("GamesPreferences",
                Context.MODE_PRIVATE).getString("username", "human");

        if(username.equals("human"))
        {
            Intent intent = new Intent(MainActivity.this, SetUser.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }


    private MainActivity getMainActivity() {
        return this;
    }

    private void initialSetup() {
        for (int row = 0; row < cellvalues[0].length; row++)
        {
            for (int col = 0; col < cellvalues[0].length; col++)
            {
                cellvalues[row][col] = "E";
            }
        }
    }

    private void RebindGame(Game game) {
        String[] values = game.getGameState().split("\\|");
        Log.d(TAG, "RebindGame: " + Arrays.toString(values));
        int counter = 0;
        if(values.length == 9) {
            for (int row = 0; row < cellvalues[0].length; row++) {
                for (int col = 0; col < cellvalues[0].length; col++) {
                    cellvalues[row][col] = values[counter++];
                }
            }
        }


        if(username.equals(game.getPlayer1()))
        {
            Log.d(TAG, "RebindGame: Assigning " + username + " to Player 1 and O");
            playerPiece = "O";
        }
        else if(username.equals(game.getPlayer2()))
        {
            Log.d(TAG, "RebindGame: Assigning " + username + " to Player 2 and X");
            playerPiece = "X";
        }

        setTitle(getString(R.string.app_name)
                + " : " + game.getConnectionId()
                + " : " + username
                + " (" + playerPiece + ")");

        board.cellvalues = cellvalues;
        setContentView(new DrawView(getMainActivity()));
    }

    private void initGame(String gameId) {

        try{
            Log.d(TAG, "initTeam: " + GAMEAPI + gameId);
            RestClient.execGetOneRequest(GAMEAPI + gameId, this,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(ArrayList<Game> result) {
                            Log.d(TAG, "onSuccess: " + result.get(0).getId());
                            game = result.get(0);
                            initSignalRGroup(username, "Connected...", game.getConnectionId());
                            RebindGame(game);
                        }
                    });
        }
        catch(Exception e)
        {
            Log.d(TAG, "initTeam: " + e.getMessage());
        }
    }

    private void initSignalRGroup(String from, String msg, String groupName) {

        hubConnection = HubConnectionBuilder
                .create("https://fvtcdp.azurewebsites.net/GameHub")
                .build();

        Log.d(TAG, "initSignalRGroup: Starting the hub connection...");

        hubConnection.start().blockingAwait();
        hubConnection.invoke(Void.class, "GetConnectionId");
        hubConnectionId = hubConnection.getConnectionId();

        Log.d(TAG, "initSignalR: Started the hub connection..." + hubConnection.getConnectionId());

        // Great a callback method to receive messages.
        hubConnection.on("ReceiveMessage", (user, message) -> {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //X:1:2
                    //Log.d(TAG, "run: length : " + message.length() + ":" + message);
                    if(message.length() == 23) {

                        turn = message.substring(0, 1);
                        int row = Integer.parseInt(message.substring(2, 3));
                        int col = Integer.parseInt(message.substring(4, 5));
                        Log.d(TAG, "GameState: " +  message.substring(6));

                        game.setGameState(message.substring(6));

                        String info = turn + ":" + row + ":" + col;
                        cellvalues[row][col] = turn;
                        board.cellvalues = cellvalues;

                        turn = turn.equals("X") ? "O" : "X";
                        game.setTurn(turn);
                        Log.d(TAG, "run: Turn changed to " + turn);
                        Log.d(TAG, "run: New Message : " + user + " : " + info);
                        setContentView(new DrawView(getMainActivity()));
                    }
                    else
                    {
                        Log.d(TAG, "Other Message : " + user + " : " + message + " : " + message.length());
                    }
                }
            });
        }, String.class, String.class);

        // Send a message
        Log.d(TAG, "initSignalRGroup: Joining " + groupName);
        hubConnection.send("JoinGame", groupName, from);
        Log.d(TAG, "initSignalRGroup: " + from + " joined " + groupName);

    }

    private void GetScreenDims()
    {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        Log.d(TAG, "GetScreenDims: " + width + ":" + height);
    }

    public void saveToAPI(Game game, boolean post) {
        try {
            if(post) {
                RestClient.execPostRequest(game,
                        MainActivity.GAMEAPI,
                        this, new VolleyCallback() {
                            @Override
                            public void onSuccess(ArrayList<Game> result) {
                                Log.d(TAG, "onSuccess: Post: " + result);
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

    private class DrawView extends View implements View.OnTouchListener {


        public DrawView(MainActivity mainActivity) {
            super(mainActivity);
            this.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent){

            Log.d(TAG, "onTouch: playerPiece: " + playerPiece + " : Turn: " + turn);


            if(playerPiece != turn)
                return false;

            if(board.checkVictory() != "0")
                return false;

            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            {

                // Decide where the user clicked.
                pt.x = (int)motionEvent.getX();
                pt.y = (int)motionEvent.getY();
                Log.d(TAG, "onTouch: ");
                // Perform a hit test.
                String result= board.hitTest(pt, playerPiece);

                if(result != "-1") {
                    hubConnection.send("SendTurnMessage", username, result + ":" + game.getGameState(), game.getConnectionId());
                    //Log.d(TAG, "onTouch: " + Arrays.deepToString(cellvalues));
                    if(game.getPlayer2().equals("Computer"))
                        playerPiece = playerPiece.equals("X") ? "O" : "X"; // Change the turn

                    Log.d(TAG, "onTouch: Turn: " + turn);
                    invalidate();
                }
                else {
                    return false; // Cancel the detection
                }

                if(board.checkVictory() != "0") {
                    displayVictory();
                }

                // if player2 is the computer, than player the computer.
                if(game.getPlayer2().equals("Computer")) {
                    for (int row = 0; row < cellvalues[0].length; row++) {
                        for (int col = 0; col < cellvalues[1].length; col++) {
                            if (cellvalues[row][col].equals("E")) {
                                Rect computerSelect = board.getRect(row, col);
                                pt.x = computerSelect.centerX();
                                pt.y = computerSelect.centerY();
                                if (board.hitTest(pt, playerPiece) != "-1") {
                                    Log.d(TAG, "onTouch: GameState: " + game.getGameState() );
                                    hubConnection.send("SendTurnMessage", "Computer", playerPiece
                                                    + ":" + row + ":" + col + ":" + game.getGameState(),
                                            game.getConnectionId());
                                    //turn = turn == "X" ? "O" : "X";
                                    playerPiece = playerPiece.equals("X") ? "O" : "X"; // Change the turn
                                    game.setTurn(playerPiece);
                                    //Log.d(TAG, "onTouch: " + Arrays.deepToString(cellvalues));
                                    if (board.checkVictory() != "0") {
                                        displayVictory();
                                    }
                                    invalidate();
                                    return true;
                                }

                                return true;
                            }
                        }
                    }
                    invalidate(); // redraw
                }
            }
            return true;
        }



        private void displayVictory() {
            // inflate the layout of the popup window
            LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.popup, null);


            // create the popup window
            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            // show the popup window
            popupWindow.showAtLocation(this, Gravity.CENTER, 0, 0);
            TextView tvVictory = (TextView) popupWindow.getContentView().findViewById(R.id.tvVictory);
            String message;
            String result = board.checkVictory();

            Log.d(TAG, "displayVictory: " + result);
            if(result == "3") {
                //tvVictory.setText("It's a tie!");
                message = "It's a tie!";
            }
            else {
                message = "Player " + result + " wins!";
                //tvVictory.setText(message);
            }
            game.setWinner(result);
            game.setCompleted(true);
            saveToAPI(game, false);

            //Log.d(TAG, "displayVictory: Computer" + ":" +  message + ":" + game.getConnectionId());
            hubConnection.send("SendMessageToGroup", "Computer", message, game.getConnectionId());

            // dismiss the popup window when touched
            popupView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popupWindow.dismiss();
                    board.clearBoard();
                    initialSetup();
                    board.cellvalues = cellvalues;
                    turn = "O";
                    invalidate();
                    return true;
                }
            });
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            //Log.d(TAG, "onDraw: " + Arrays.deepToString(cellvalues));
            canvas.drawColor(Color.DKGRAY);
            board.Draw(canvas);
        }
    }
}