package com.lectura.tags.peaje;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ListView;
import android.widget.Toast;

import com.lectura.tags.demopeaje.R;
import com.lectura.tags.peaje.Helpers.AdapterTAGS;

import java.util.ArrayList;

public class Datos_TAG extends AppCompatActivity {
    private ListView list;
    ArrayList<String> TipoISO = new ArrayList();
    ArrayList<String> Errores = new ArrayList();
    ArrayList<String> CodeTAG = new ArrayList();
    ArrayList<String> ExaCodeTag = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos__t_a_g);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RecibeDatos();
    }

    private void RecibeDatos() {
        CodeTAG = (ArrayList<String>) getIntent().getStringArrayListExtra("codetag");
        TipoISO = (ArrayList<String>) getIntent().getStringArrayListExtra("tipoiso");
        ExaCodeTag = (ArrayList<String>) getIntent().getStringArrayListExtra("exacodetag");
        AdapterTAGS adapterTAGS = new AdapterTAGS(Datos_TAG.this, CodeTAG,TipoISO,ExaCodeTag);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapterTAGS);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == event.KEYCODE_BACK) {
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
