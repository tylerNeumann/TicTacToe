package fvtc.edu.tictactoe;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

public class Board {
    public static final int BOARDSIZE = 3;
    String[][] cellvalues = new String[BOARDSIZE][BOARDSIZE];
    Rect[][] cells = new Rect[BOARDSIZE][BOARDSIZE];
    String[][] comptercellvalues = new String[BOARDSIZE][BOARDSIZE];

    int SIZE = 135;
    int OFFSET = 5;
    private static final String TAG = "Board";
    int viewWidth;
    int viewHeight;

    public Board()
    {

    }

    public Board(int width)
    {
        viewWidth = width / BOARDSIZE;
        viewHeight = viewWidth;
        SIZE = viewWidth - 3;
    }

    public Board(int width, int height)
    {
        viewWidth = width / BOARDSIZE;
        viewHeight = height / BOARDSIZE;
        SIZE = viewWidth - 3;
    }

    public String hitTest(Point pt, String turn) {
        String results = "-1";
        for(int row = 0; row < cells[0].length; row++)
        {
            for(int col = 0; col < cells[1].length; col++)
            {
                if(cellvalues[row][col].equals("E"))
                {
                    if(cells[row][col].contains(pt.x, pt.y))
                    {
                        Log.d(TAG, "hitTest: ");
                        cellvalues[row][col] = turn;
                        results = turn + ":" + String.valueOf(row) + ":" + String.valueOf(col);
                        return results;
                    }
                }
            }
        }
        return results;
    }

    public void Draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        Paint paintGrid = new Paint();
        paintGrid.setColor(Color.BLACK);
        paintGrid.setStrokeWidth(20);
        paintGrid.setStyle(Paint.Style.FILL_AND_STROKE);

        //Log.d(TAG, "Draw: " + Arrays.deepToString(cellvalues));

        for(int row = 0; row < cells[0].length; row++)
        {
            for(int col = 0; col < cells[1].length; col++)
            {
                cells[row][col] = new Rect();
                cells[row][col].left = col * SIZE + OFFSET;
                cells[row][col].top = row * SIZE + OFFSET;
                cells[row][col].right = col * SIZE + OFFSET + SIZE;
                cells[row][col].bottom = row * SIZE + OFFSET + SIZE;

                canvas.drawRect(cells[row][col], paint);
                canvas.drawLine(cells[row][col].right, cells[row][col].top, cells[row][col].right, cells[row][col].bottom, paintGrid); // up and down line
                canvas.drawLine(cells[row][col].left, cells[row][col].bottom, cells[row][col].right, cells[row][col].bottom, paintGrid); // left and right line

                if(cellvalues[row][col].equals("O"))
                    drawTurn(canvas, cells[row][col], row, col, Color.BLUE);
                else if (cellvalues[row][col].equals("X"))
                    drawTurn(canvas, cells[row][col], row, col, Color.RED);
            }
        }
    }

    private void drawTurn(Canvas canvas, Rect rect, int row, int col, int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);

        int x = rect.centerX();
        int y = rect.centerY();

        if (cellvalues[row][col].equals("X"))
        {
            canvas.drawLine(rect.left + 50, rect.top + 50, rect.right - 50, rect.bottom - 50, paint);
            canvas.drawLine(rect.right - 50, rect.top + 50, rect.left + 50, rect.bottom - 50, paint);
        }
        else canvas.drawCircle(x, y, SIZE * .35f, paint);
    }

    public String checkVictory()
    {
        String[] values = {"O", "X"};
        for(String value : values) //check if either player won
        {
            if(cellvalues[0][0].equals(value) && cellvalues[0][1].equals(value) && cellvalues[0][2].equals(value)
                    ||cellvalues[1][0].equals(value) && cellvalues[1][1].equals(value) && cellvalues[1][2].equals(value)
                    ||cellvalues[2][0].equals(value) && cellvalues[2][1].equals(value) && cellvalues[2][2].equals(value)
                    ||cellvalues[0][0].equals(value) && cellvalues[1][0].equals(value) && cellvalues[2][0].equals(value)
                    ||cellvalues[0][1].equals(value) && cellvalues[1][1].equals(value) && cellvalues[2][1].equals(value)
                    ||cellvalues[0][2].equals(value) && cellvalues[1][2].equals(value) && cellvalues[2][2].equals(value)
                    ||cellvalues[0][0].equals(value) && cellvalues[1][1].equals(value) && cellvalues[2][2].equals(value)
                    ||cellvalues[0][2].equals(value) && cellvalues[1][1].equals(value) && cellvalues[2][0].equals(value))
            {
                Log.d(TAG, "checkVictory: " + value);
                return value; //return the value of the player who won (X or O)
            }
        }

        //Log.d(TAG, "checkVictory: " + Arrays.deepToString(cellvalues));

        for(int row = 0; row < cellvalues[0].length; row++)
        {
            for (int col = 0; col < cellvalues[1].length; col++)
            {
                if(cellvalues[row][col].equals("E"))
                {
                    return "0"; //if there are any empty squares left (value of 0), return 0 (game still active)
                }
            }
        }
        Log.d(TAG, "checkVictory: 3" );
        return "3"; // if no one won and there are no empty squares left, return 3 (tie)
    }

    public Rect getRect(int row, int col)
    {
        return cells[row][col];
    }

    public void clearBoard()
    {
        cellvalues = new String[BOARDSIZE][BOARDSIZE];
        cells = new Rect[BOARDSIZE][BOARDSIZE];
    }
}
