package com.lectura.tags.peaje.Helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

public class Helpers {
    public void Aviso(Context context, String titulo, String Mensaje){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titulo);
        builder.setMessage(Mensaje);
        builder.setCancelable(false);
        builder.setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
