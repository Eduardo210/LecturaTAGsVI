package com.lectura.tags.peaje;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lectura.tags.componentepeaje.LecturaTags;
import com.lectura.tags.componentepeaje.PeajeListener;
import com.lectura.tags.peaje.ConfiguracionActivity;
import com.lectura.tags.demopeaje.R;
import com.lectura.tags.peaje.Helpers.Helpers;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PeajeListener {

    Button btnRead;
    TextView txtTag, txtLog, txt6B, txt6C, txtTR;
    LecturaTags lectura;
    Context context;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtTag = (TextView)findViewById(R.id.lb_tagsPeaje);
        txtTag.setMovementMethod(new ScrollingMovementMethod());
        txtLog = (TextView)findViewById(R.id.lb_logPeaje);

        txt6B = (TextView)findViewById(R.id.lb_6B);
        txt6C = (TextView)findViewById(R.id.lb_6C);
        txtTR = (TextView)findViewById(R.id.lb_TR);

        context = getApplicationContext();
        lectura = new LecturaTags();

        if (!lectura.Initialize(this))
        {
            txtLog.setText("No se logró inicializar el Lector RFID");
        }


        lectura.ConfigurarLectorRF(VariablesGlobales.Power6B,
                                   VariablesGlobales.Power6C,
                                   VariablesGlobales.PowerTR,
                                   VariablesGlobales.Time6B,
                                   VariablesGlobales.Time6C,
                                   VariablesGlobales.TimeTR);

        ColoresDefault();

        btnRead = (Button)findViewById(R.id.buttonRead);
        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtTag.setText("");
                txtLog.setText("");
                lectura.LeerTagTelepeaje();
                btnRead.setEnabled(false);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_SOFT_RIGHT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT
                || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) && event.getRepeatCount() <= 0) {
            if (btnRead.isEnabled()) {
                txtTag.setText("");
                txtLog.setText("");
                lectura.LeerTagTelepeaje();
                btnRead.setEnabled(false);
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.context_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent i = new Intent(MainActivity.this, ConfiguracionActivity.class);
        startActivityForResult(i,1);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1)
        {
            lectura.ConfigurarLectorRF(VariablesGlobales.Power6B,
                    VariablesGlobales.Power6C,
                    VariablesGlobales.PowerTR,
                    VariablesGlobales.Time6B,
                    VariablesGlobales.Time6C,
                    VariablesGlobales.TimeTR);
            Toast.makeText(MainActivity.this, "Configuración Éxitosa", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lectura.onDestroy();
    }

    @Override
    protected  void onStart(){
        super.onStart();
        lectura.onStart();
    }

    @Override
    protected void onResume(){
        super.onResume();
        lectura.onResume();
    }

    @Override
    protected void onPause(){
        lectura.onPause();
        super.onPause();
    }

    @Override
    protected void onStop(){
        lectura.onStop();
        super.onStop();
    }

    @Override
    public void onReadTagPeaje(String Tag) {
        Log.e("FINAAAAL",Tag);
//            txtTag.setText(Tag.split("\\|")[0]+"  -  "+Tag.split("\\|")[1]);

//        txtTag.setText(Tag.replace("|","\r\n"));
        String[] elementos;
        elementos = Tag.split("@");
//        txtTag.setText(elementos.toString());

        ArrayList<String> TipoISO = new ArrayList();
        ArrayList<String> Errores = new ArrayList();
        ArrayList<String> CodeTAG = new ArrayList();
        ArrayList<String> ExaCodeTag = new ArrayList();

        for (int i = 0; i < elementos.length; i++) {
            String elem = elementos[i];
            String[]partes;
            partes = elem.split("\\|");
            if (partes[1].equals("01") || partes[1]=="01"){
                Toast.makeText(this, "Error en la lectura de algun TAG", Toast.LENGTH_SHORT).show();
            }else{
                if (partes.length<4){
                    Toast.makeText(this, "Error en la lectura de algun TAG", Toast.LENGTH_SHORT).show();
                }else{
                    TipoISO.add(partes[0]);
                    Errores.add(partes[1]);
                    CodeTAG.add(partes[2]);
                    ExaCodeTag.add(partes[3]);
                }
            }
        }
        if (TipoISO.isEmpty() || Errores.isEmpty() || CodeTAG.isEmpty() || ExaCodeTag.isEmpty()){
            Helpers helpers = new Helpers();
            helpers.Aviso(MainActivity.this, "Datos Obtenidos",Tag);
        }else{
            Intent intent = new Intent(MainActivity.this, Datos_TAG.class);
            intent.putStringArrayListExtra("codetag", CodeTAG);
            intent.putStringArrayListExtra("tipoiso", TipoISO);
            intent.putStringArrayListExtra("exacodetag", ExaCodeTag);
            startActivity(intent);
            txtTag.setText("");
            txtLog.setText("");
        }
    }

    @Override
    public void onPeajeLogInformation(String Loga) {

        final String Log1=Loga;
        //Log.e("log","Es: "+Loga);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                txtLog.setText(Log1);
            }
        });
        if (Loga.contains("6B|INICIA LECTURA|"))
        {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ColoresDefault();
                    txt6B.setBackgroundColor(Color.GREEN);
                }
            });
        }
        else if (Loga.contains("6C|INICIA LECTURA|"))
        {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ColoresDefault();
                    txt6C.setBackgroundColor(Color.GREEN);
                }
            });
        }
        else if (Loga.contains("TR|INICIA LECTURA|")){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ColoresDefault();
                    txtTR.setBackgroundColor(Color.GREEN);
                }
            });
        }
        else if (Loga.contains("RF|FINALIZA LECTURA|")){

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    btnRead.setEnabled(true);
                    ColoresDefault();
                }
            });
        }
      //  else
        //    txtLog.setText(Loga);
    }


    public void ColoresDefault(){
        txt6B.setBackgroundColor(Color.parseColor("#ff0099cc"));
        txt6C.setBackgroundColor(Color.parseColor("#ff0099cc"));
        txtTR.setBackgroundColor(Color.parseColor("#ff0099cc"));
    }

}