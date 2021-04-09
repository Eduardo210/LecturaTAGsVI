package com.lectura.tags.peaje.Helpers;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lectura.tags.demopeaje.R;


import java.util.List;

public class AdapterTAGS extends ArrayAdapter<String> {
    Activity context;
    private final List<String> ListaTAGS;
    private final List<String> ListaTagIso;
    private final List<String> ListaTagSaldo;
//    private final List<String> ListaTagEstado;
//    private final List<String> ListaTagTipo;

    public AdapterTAGS(Activity context, List<String> ListaTAGS, List<String> ListaTagIso, List<String>ListaTagSaldo) {
        super(context, R.layout.mytags, ListaTAGS);
        this.context = context;
        this.ListaTAGS = ListaTAGS;
        this.ListaTagIso = ListaTagIso;
        this.ListaTagSaldo = ListaTagSaldo;
//        this.ListaTagEstado = ListaTagEstado;
//        this.ListaTagTipo = ListaTagTipo;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater= LayoutInflater.from(context);
        View rowView=inflater.inflate(R.layout.tags, null,true);

        TextView NumeroTAG = (TextView) rowView.findViewById(R.id.txtTagNumber);
        TextView IsoTag = (TextView)rowView.findViewById(R.id.txtProtocoloISO);
        TextView TagSaldo = (TextView)rowView.findViewById(R.id.txtSaldo);
        TextView TagEstado = (TextView) rowView.findViewById(R.id.txtEstado);
        TextView TagTipo = (TextView) rowView.findViewById(R.id.txtPrePosPago);


        NumeroTAG.setText(ListaTAGS.get(position));
        IsoTag.setText(ListaTagIso.get(position));
        TagSaldo.setText("");
//        TagEstado.setText(ListaTagEstado.get(position));
//        TagTipo.setText(ListaTagTipo.get(position));

        return rowView;

    };
}

