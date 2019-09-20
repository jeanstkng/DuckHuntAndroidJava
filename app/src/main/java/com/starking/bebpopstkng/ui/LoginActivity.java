package com.starking.bebpopstkng.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.starking.bebpopstkng.Common.Constantes;
import com.starking.bebpopstkng.Models.User;
import com.starking.bebpopstkng.R;

public class LoginActivity extends AppCompatActivity {

    EditText etNick;
    Button btnStart;
    String nick;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Instanciar conexion a base de datos
        db = FirebaseFirestore.getInstance();

        etNick = findViewById(R.id.editTextNick);
        btnStart = findViewById(R.id.buttonStart);

        // Cambiar fuentes
        Typeface typeface = Typeface.createFromAsset(getAssets(), "Jellee-Roman.ttf");
        etNick.setTypeface(typeface);
        btnStart.setTypeface(typeface);

        //Eventos: evento click
        btnStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                nick = etNick.getText().toString();

                if(nick.isEmpty()){
                    etNick.setError("Nombre de usuario necesario.");
                }
                else{

                    addNickAndStart();

                }
            }
        });
    }

    private void addNickAndStart() {

        db.collection("users").whereEqualTo("nick",nick)
            .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size() > 0){
                            etNick.setError("Nickname no disponible");
                        }
                        else {
                            addNickToFirestore();
                        }
                    }
                });

    }

    private void addNickToFirestore() {

        User newUser = new User(nick, 0);

        db.collection("users")
                .add(newUser)
        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

                etNick.setText("");
                Intent i = new Intent( LoginActivity.this, GameActivity.class);

                i.putExtra(Constantes.EXTRA_NICK, nick);
                i.putExtra(Constantes.EXTRA_ID, documentReference.getId());

                startActivity(i);

            }
        });


    }
}
