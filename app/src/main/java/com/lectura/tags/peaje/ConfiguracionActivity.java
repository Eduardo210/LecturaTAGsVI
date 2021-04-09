package com.lectura.tags.peaje;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lectura.tags.demopeaje.R;

public class ConfiguracionActivity extends AppCompatActivity {

    private TextView txtPower6B,txtPower6C,txtPowerTR,txtTime6B,txtTime6C,txtTimeTR;

    private Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);


        txtPower6B = (TextView)findViewById(R.id.txtPower6B);
        txtPower6C = (TextView)findViewById(R.id.txtPower6C);
        txtPowerTR = (TextView)findViewById(R.id.txtPowerTR);
        txtTime6B = (TextView)findViewById(R.id.txtTime6B);
        txtTime6C = (TextView)findViewById(R.id.txtTime6C);
        txtTimeTR = (TextView)findViewById(R.id.txtTimeTR);


        btnGuardar = (Button)findViewById(R.id.btnGuardar);

        txtPower6B.setText(String.valueOf(VariablesGlobales.Power6B));
        txtPower6C.setText(String.valueOf(VariablesGlobales.Power6C));
        txtPowerTR.setText(String.valueOf(VariablesGlobales.PowerTR));
        txtTime6B.setText(String.valueOf(VariablesGlobales.Time6B));
        txtTime6C.setText(String.valueOf(VariablesGlobales.Time6C));
        txtTimeTR.setText(String.valueOf(VariablesGlobales.TimeTR));



        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int iPower6B = Integer.parseInt(txtPower6B.getText().toString());
                int iPower6C = Integer.parseInt(txtPower6C.getText().toString());
                int iPowerTR = Integer.parseInt(txtPowerTR.getText().toString());
                int iTime6B = Integer.parseInt(txtTime6B.getText().toString());
                int iTime6C = Integer.parseInt(txtTime6C.getText().toString());
                int iTimeTR = Integer.parseInt(txtTimeTR.getText().toString());

                if (iPower6B < 100 || iPower6B > 300){
                    Toast.makeText(ConfiguracionActivity.this, "POTENCIA 6B NO VALIDA", Toast.LENGTH_LONG).show();
                    return;
                }
                if (iPower6C < 100 || iPower6C > 300){
                    Toast.makeText(ConfiguracionActivity.this, "POTENCIA 6C NO VALIDA", Toast.LENGTH_LONG).show();
                    return;
                }
                if (iPowerTR < 100 || iPowerTR > 300){
                    Toast.makeText(ConfiguracionActivity.this, "POTENCIA TR NO VALIDA", Toast.LENGTH_LONG).show();
                    return;
                }

                if (iTime6B < 600 || iTime6B > 1000){
                    Toast.makeText(ConfiguracionActivity.this, "TIEMPO DE LECTURA 6B NO VALIDO", Toast.LENGTH_LONG).show();
                    return;
                }
                if (iTime6C < 300 || iTime6C > 1000){
                    Toast.makeText(ConfiguracionActivity.this, "TIEMPO DE LECTURA 6C NO VALIDO", Toast.LENGTH_LONG).show();
                    return;
                }
                if (iTimeTR < 600 || iTimeTR > 1000){
                    Toast.makeText(ConfiguracionActivity.this, "TIEMPO DE LECTURA TR NO VALIDO", Toast.LENGTH_LONG).show();
                    return;
                }

                VariablesGlobales.Power6B = iPower6B;
                VariablesGlobales.Power6C = iPower6C;
                VariablesGlobales.PowerTR = iPowerTR;
                VariablesGlobales.Time6B = iTime6B;
                VariablesGlobales.Time6C = iTime6C;
                VariablesGlobales.TimeTR = iTimeTR;

                ConfiguracionActivity.this.finish();
            }
        });
    }
}
