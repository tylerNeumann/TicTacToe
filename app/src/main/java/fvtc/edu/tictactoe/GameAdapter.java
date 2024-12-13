package fvtc.edu.tictactoe;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.UUID;

public class GameAdapter extends RecyclerView.Adapter {
    private static final String TAG = "myDebug";
    private ArrayList<Game> gameData;
    private View.OnClickListener onClickListener;
    private Context parentContext;
    private boolean isDeleting;

    public void setDelete(boolean status) {
        isDeleting = status;
    }

    public class TeamViewHolder extends RecyclerView.ViewHolder{

        private TextView textPlayer1;
        private TextView textPlayer2;
        private TextView textName;
        private CheckBox chkCompleted;
        private Button btnDelete;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            textPlayer1 = itemView.findViewById(R.id.txtPlayer1);
            textPlayer2 = itemView.findViewById(R.id.txtPlayer2);
            textName = itemView.findViewById(R.id.txtName);
            chkCompleted = itemView.findViewById(R.id.chkCompleted);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            itemView.setTag(this);
            itemView.setOnClickListener(onClickListener);
        }

        public TextView getTextPlayer1() { return textPlayer1; }
        public TextView getTextPlayer2() {return textPlayer2;}
        public TextView getTextName() {return textName;}
        public CheckBox getChkCompleted() {return chkCompleted;}
        public Button getBtnDelete() {return btnDelete;}
    }

    public GameAdapter(ArrayList<Game> arrayList, Context context){
        gameData = arrayList;
        parentContext = context;
        //Log.d(TAG, "TeamAdapter: " + arrayList.size());
    }

    public void setOnClickListener(View.OnClickListener itemClickListener)
    {
        //Log.d(TAG, "setOnClickListener: ");
        onClickListener = itemClickListener;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // retrieve and inflate the list_item.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_item, parent, false );
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TeamViewHolder teamViewHolder = (TeamViewHolder) holder;

        Game game = gameData.get(position);

        // Bind to the screen
        teamViewHolder.getTextPlayer1().setText(game.getPlayer1());
        teamViewHolder.getTextPlayer2().setText(game.getPlayer2());
        teamViewHolder.getTextName().setText(game.getConnectionId());
        //teamViewHolder.getImageButtonPhoto().setImageResource(team.getImgId());
        teamViewHolder.getChkCompleted().setChecked(game.getCompleted());

        //Log.d(TAG, "onBindViewHolder: Completed: " + game.getId() + ":" +  game.getCompleted());

        if(isDeleting) {
            //Log.d(TAG, "onBindViewHolder: Deleting: " + isDeleting);
            teamViewHolder.getBtnDelete().setVisibility(View.VISIBLE);
            teamViewHolder.getBtnDelete().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    deleteItem(position, game.getId());
                }
            });
        }
        else
        {
            //Log.d(TAG, "onBindViewHolder: Not Deleting: " + isDeleting);
            teamViewHolder.getBtnDelete().setVisibility(View.INVISIBLE);
        }

    }

    private void deleteItem(int position, UUID id) {
        try{

            RestClient.execDeleteRequest(gameData.get(position),
                    MainActivity.GAMEAPI + id,
                    this.parentContext,
                    new VolleyCallback(){
                        @Override
                        public void onSuccess(ArrayList<Game> result){
                            notifyDataSetChanged();
                            Log.d(TAG, "onSuccess: Delete");
                        }
                    });


            Log.d(TAG, "deleteItem: Delete Team: " + id);

            // Remove it from the teamData
            gameData.remove(position);


        }
        catch(Exception ex)
        {
            Log.d(TAG, "deleteItem: " + ex.getMessage());
        }
        // Rebind
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return gameData.size();
    }
}
