package com.starking.bebpopstkng.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.starking.bebpopstkng.Common.Constantes;
import com.starking.bebpopstkng.R;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {

    TextView tvCounterDucks, tvTimer, tvNick;
    ImageView ivDuck;
    ImageButton btnPause;
    int counter = 0;
    int screenWidth;
    int screenHeight;
    Random aleatorio;
    boolean gameOver = false;
    String id, nick;
    FirebaseFirestore db;
    long timing = 60000;
    long timeRemaining;
    long timeLeft;

    private CountDownTimer mCountDownTimer;

    private float duckX;

    private Handler handler = new Handler();
    private Timer timer = new Timer();

    //Declare a variable to hold count down timer's paused status
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        db = FirebaseFirestore.getInstance();

        initViewComponents();
        eventos();
        initScreen();
        ivDuck.setX(screenWidth + 80.0f);
        moveDuck();
        initCountDown();

    }

    private void initCountDown() {
        mCountDownTimer = new CountDownTimer(timing, 1000) {

            public void onTick(long millisUntilFinished){
                if(!isPaused){
                    //Display the remaining seconds to app interface
                    //1 second = 1000 milliseconds
                    timeLeft = millisUntilFinished / 1000;
                    tvTimer.setText( timeLeft + "s" );
                    //Put count down timer remaining time in a variable
                    timeRemaining = millisUntilFinished;
                }
            }

            public void onFinish(){

                tvTimer.setText("0s");
                gameOver = true;
                timer.cancel();
                timer = null;
                showGameOver();
                saveResultFirestore();
            }

        }.start();

        if (timer == null){
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        changePos();
                    }
                });
            }
        }, 0, 20);

    }

    private void saveResultFirestore() {

        db.collection("users")
                .document(id)
                .update(
                        "ducks", counter
                );

    }

    private void showGameOver() {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Haz conseguido cazar " + counter + " patos.")
            .setTitle("Game Over");


        builder.setPositiveButton("Reiniciar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                counter = 0;
                tvCounterDucks.setText("0");
                gameOver = false;
                initCountDown();
                moveDuck();
            }
        });
        builder.setNegativeButton("Ver ranking", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();
                Intent i = new Intent(GameActivity.this, RankingActivity.class);
                startActivity(i);
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        // 4. Show dialog
        dialog.show();

    }

    private void initScreen() {
        // 1. Obtener el tamaño de la pantalla del dispositivo
        // en el que estamos ejecutando la app
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        // 2. Inicializamos el objeto para generar nùmeros aleatorios
        aleatorio = new Random();
    }

    private void initViewComponents() {

        tvCounterDucks = findViewById(R.id.textViewCounter);
        tvNick = findViewById(R.id.textViewNick);
        tvTimer = findViewById(R.id.textViewTimer);
        ivDuck = findViewById(R.id.imageViewDuck);

        // Cambiar fuentes
        Typeface typeface = Typeface.createFromAsset(getAssets(), "Jellee-Roman.ttf");
        tvCounterDucks.setTypeface(typeface);
        tvTimer.setTypeface(typeface);
        tvNick.setTypeface(typeface);

        // Extras: obtener nick de usuario y setear textview
        Bundle extras = getIntent().getExtras();

        nick = extras.getString(Constantes.EXTRA_NICK);
        id = extras.getString(Constantes.EXTRA_ID);
        tvNick.setText(nick);

        // Pausas

        btnPause = findViewById(R.id.imageButtonPause);

    }

    private void eventos() {
        ivDuck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameOver && !isPaused){
                counter++;
                tvCounterDucks.setText(String.valueOf(counter));

                ivDuck.setImageResource(R.drawable.duck_clicked);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ivDuck.setImageResource(R.drawable.duck);
                        ivDuck.setX(screenWidth + 80.0f);
                        moveDuck();
                    }
                }, 500);
            }
            }
        });


        btnPause.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //When user request to pause the CountDownTimer
                if (!isPaused) {

                    isPaused = true;
                    gameOver = true;

                    // Stop the timer.
                    timer.cancel();
                    timer = null;

                    timing = timeRemaining;

                    mCountDownTimer.cancel();

                }
                else {

                    gameOver = false;
                    isPaused = false;

                    initCountDown();

                }

            }
        });

    }

    private void moveDuck() {
        int min = 0;
                //int maxScreenX = 0;
                //((screenWidth - ivDuck.getWidth()) / 2);

        int maxScreenY = screenHeight - ivDuck.getHeight();

        // Generamos 2 nùmeros aleatorios, uno para la coordenada
        // X y otro para la coordenada Y.
                //int randomX = aleatorio.nextInt( ((maxScreenX - min) + 1) + min );
        final int randomY = aleatorio.nextInt( ((maxScreenY - min) + 1) + min );

        // Utilizamos los nùmeros aleatorios para mover el pato
        // a esa posicion
                //ivDuck.setX(randomX);
        ivDuck.setY(randomY);

    }

    private void changePos() {
        // Hacer que el pato avance

        // Right
        duckX += 10;
        if (ivDuck.getX() > screenWidth){
            moveDuck();
            duckX = -100.0f;
        }
        ivDuck.setX(duckX);

    }

}
