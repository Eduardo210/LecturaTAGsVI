package com.lectura.tags.componentepeaje;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.atid.lib.dev.ATRfidManager;
import com.atid.lib.dev.ATRfidReader;
import com.atid.lib.dev.event.RfidReaderEventListener;
import com.atid.lib.dev.rfid.ATRfid900MAReader;
import com.atid.lib.dev.rfid.exception.ATRfidReaderException;
import com.atid.lib.dev.rfid.param.EpcMatchParam;
import com.atid.lib.dev.rfid.param.SelectionMask6b;
import com.atid.lib.dev.rfid.type.ActionState;
import com.atid.lib.dev.rfid.type.BankType;
import com.atid.lib.dev.rfid.type.ConnectionState;
import com.atid.lib.dev.rfid.type.MaskMatchingType;
import com.atid.lib.dev.rfid.type.ResultCode;
import com.atid.lib.diagnostics.ATLog;
import com.atid.lib.util.SysUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Componente Peaje at911 v1.2
 * 10/01/2020
 * Codigo modificado para que sea capaz de leer multitag 6b o 6c y
 * despues interpretar el inventario obtenido
 * con formato de Telepeaje
 */

@SuppressLint("Wakelock")
public class LecturaTags  implements RfidReaderEventListener {

    private String TAG = "Tags Peaje";

    private ATRfidReader mReader = null;
    private ATRfid900MAReader mMAReader;

    private Timer  rTimer;

    private volatile Boolean isBusy = false, isReaderOK =false;
    private volatile int TipoLectura = 0;

    private int ReadExtra = 0; // 0 -> 6C : 1 -> 6B
    private String sEPC = "";
    private InterpretaTags Interpreta;
    private PeajeListener listener;
    private Utilidades utilidades;
    private int iPower6B = 200;
    private int iPower6C = 200;
    private int iPowerTR = 200;

    private int iTime6B = 600;
    private int iTime6C = 400;
    private int iTimeTR = 600;

    private ResultCode res = ResultCode.NoError;
    private Handler mHandler = new Handler();
    private List<String> List6C = new ArrayList<>();
    private List<String> List6B = new ArrayList<>();
    private List<String> List6B_Interpretado = new ArrayList<>();
    private List<String> List6C_Interpretado = new ArrayList<>();
    private int posList6B=0;
    private int posList6C=0;
    private int CuentaVueltas=0;
    private int novueltas = 2;


    private static String Error="ER|",RES_OK="00|",RES_UNREAD="01|",RES_INVALID="02|";

    private boolean is_Inventory_ended=false, is6C_Complete=false, is6B_Complete=false;

    private boolean debug=true;

    private String dato_final="";
    public LecturaTags() {
    }

    //Inicializa
    public boolean Initialize(Context context) {

        Interpreta = new InterpretaTags();
        utilidades = new Utilidades(context);

        try {
            listener = null;
            if (mReader != null) {
                mReader.destroy();
                ATRfidManager.onDestroy();
            }

            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (context instanceof PeajeListener)
            listener = (PeajeListener) context;

        if ((mReader = ATRfidManager.getInstance()) == null) {
            if (listener != null) listener.onPeajeLogInformation("ER|LECTOR NO INICIALIZADO|");
            return false;
        } else {
            mReader.setEventListener(this);
            try {
                ATRfidManager.wakeUp();
                mReader.disconnect();
                Thread.sleep(150);
                mReader.connect();
                Thread.sleep(150);
                ATLog.e(TAG, "Firmware: " +  mReader.getFirmwareVersion() );
            } catch (ATRfidReaderException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
        isReaderOK = true;
        return true;
    }


    public void ConfigurarLectorRF(int Power6B, int Power6C, int PowerTR, int Time6B, int Time6C, int TimeTR){

        iPower6B = Power6B;
        iPower6C = Power6C;
        iPowerTR = PowerTR;

        iTime6B = Time6B;
        iTime6C = Time6C;
        iTimeTR = TimeTR;

        if (iPower6B < 100 || iPower6B > 300){
            if (listener != null){
                listener.onPeajeLogInformation("ER|POTENCIA 6B NO VALIDA|");
                listener.onPeajeLogInformation("RF|FINALIZA LECTURA|");}
            return;
        }
        if (iPower6C < 100 || iPower6C > 300){
            if (listener != null) {
                listener.onPeajeLogInformation("ER|POTENCIA 6C NO VALIDA|");
                listener.onPeajeLogInformation("RF|FINALIZA LECTURA|");
            }
            return;
        }
        if (iPowerTR < 100 || iPowerTR > 300){
            if (listener != null) {
                listener.onPeajeLogInformation("ER|POTENCIA TR NO VALIDA|");
                listener.onPeajeLogInformation("RF|FINALIZA LECTURA|");
            }
            return;
        }

        if (iTime6B < 600 || iTime6B > 1000){
            if (listener != null) {
                listener.onPeajeLogInformation("ER|TIEMPO DE LECTURA 6B NO VALIDO|");
                listener.onPeajeLogInformation("RF|FINALIZA LECTURA|");}
            return;
        }
        if (iTime6C < 300 || iTime6C > 1000){
            if (listener != null){
                listener.onPeajeLogInformation("ER|TIEMPO DE LECTURA 6C NO VALIDO|");
                listener.onPeajeLogInformation("RF|FINALIZA LECTURA|");
            }
            return;
        }
        if (iTimeTR < 600 || iTimeTR > 1000){
            if (listener != null){
                listener.onPeajeLogInformation("ER|TIEMPO DE LECTURA TR NO VALIDO|");
                listener.onPeajeLogInformation("RF|FINALIZA LECTURA|");}
            return;
        }

    }

    public void LeerTagTelepeaje() {


        if (!isReaderOK)
            listener.onPeajeLogInformation("ER|LECTOR NO INICIALIZADO|");
        if (!isBusy ) {
            Inicializar_lectura();
            listener.onPeajeLogInformation("6B|INICIA LECTURA|");
            IniciarLectura6B();
        } else if (listener != null){
            listener.onPeajeLogInformation("ER|LECTOR OCUPADO|");
            listener.onPeajeLogInformation("RF|FINALIZA LECTURA|");
        }
    }

    private void Inicializar_lectura(){
        limpiarlistas();
        TipoLectura = 0;
        CuentaVueltas=0;
        dato_final="";
        is_Inventory_ended=is6B_Complete=is6C_Complete=false;
    }

    private void limpiarlistas(){

        List6B_Interpretado.clear();
        List6C_Interpretado.clear();
        List6B.clear();
        List6C.clear();
        posList6C=0;
        posList6C=0;
    }

    private void IniciarLectura6B() {
        try {
            isBusy = true;
            sEPC = "";
            List6B.clear();
            List6B_Interpretado.clear();
            is_Inventory_ended=false;
            posList6B=0;
            CuentaVueltas++;

            mReader.setPower(iPower6B);

            mMAReader = (ATRfid900MAReader) mReader;
            is_Inventory_ended=false;
            //cambio readepb6btag por inventory6btag para leer mas de uno alv 06-01-2020
          // if ((res = mMAReader.readEpc6bTag()) != ResultCode.NoError) {
            if ((res = mMAReader.inventory6bTag()) != ResultCode.NoError) {
                ATLog.e(TAG, "ERROR. startAction() - Failed to start read Any tag [%s]", res);
                if (listener != null) listener.onPeajeLogInformation("ER|ERROR LECTURA 6B|");

                if (res == ResultCode.NotSupported)
                    if (listener != null) {
                        listener.onReadTagPeaje("ER|LECTOR NO SOPORTADO|");
                        listener.onPeajeLogInformation("RF|FINALIZA LECTURA|");
                    }
                DetenerRF();
                utilidades.playError();
            } else {
                ATLog.d(TAG, "Inicia Inventario 6B Any Tag " + res);
                StartTimer(iTime6B);
            }
        } catch (Exception ex) {
            Log.d(TAG, "onReaderReadTag error: " + ex.getMessage());
            isBusy = false;
        }
    }

    private void IniciarLecturaTR() {
        try {
            isBusy = true;
            sEPC = "";

            mReader.setPower(iPowerTR);

            mMAReader = (ATRfid900MAReader) mReader;

            if ((res = mMAReader.readEpcRailTag()) != ResultCode.NoError) {
                ATLog.e(TAG, "ERROR. startAction() - Failed to start read Any tag [%s]", res);
                if (listener != null){
                    listener.onPeajeLogInformation("ER|ERROR LECTURA TR|");
                    listener.onPeajeLogInformation("RF|FINALIZA LECTURA|");
                }

                if (res == ResultCode.NotSupported)
                    if (listener != null) {
                        listener.onReadTagPeaje("ER|LECTOR NO SOPORTADO|");
                        listener.onPeajeLogInformation("RF|FINALIZA LECTURA|");
                    }
                DetenerRF();
            } else {
                ATLog.d(TAG, "Inicia Inventario TR Any Tag " + res);
                StartTimer(iTimeTR);
            }
        } catch (Exception ex) {
            Log.d(TAG, "onReaderReadTag error: " + ex.getMessage());
            isBusy = false;
        }
    }

    private void IniciarLectura6C() {
        try {
            isBusy = true;
            sEPC = "";
            mReader.setPower(iPower6C);
            List6C.clear();
            List6C_Interpretado.clear();
            posList6C=0;

            //if ((res = mReader.readEpc6cTag()) != ResultCode.NoError) {
            if ((res = mReader.inventory6cTag()) != ResultCode.NoError) {
                ATLog.e(TAG, "ERROR. startAction() - Failed to start read Any tag [%s]", res);
                if (listener != null){
                    listener.onPeajeLogInformation("RF|FINALIZA LECTURA|");
                    listener.onPeajeLogInformation("ER|ERROR LECTURA 6C|");
                }
                if (res == ResultCode.NotSupported)
                    if (listener != null){
                        listener.onReadTagPeaje("ER|LECTOR NO SOPORTADO|");
                        listener.onPeajeLogInformation("RF|FINALIZA LECTURA|");
                    }
                DetenerRF();

            } else {
                ATLog.d(TAG, "Inicia Inventario 6C Any Tag " + res);
                StartTimer(iTime6C);
            }
        } catch (Exception ex) {
            Log.d(TAG, "onReaderReadTag error: " + ex.getMessage());
            isBusy = false;
        }
    }

    private boolean Proceso6B(){
        is_Inventory_ended=true;
        if(List6B.size()>0){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // DetenerRF();
                    ATLog.d(TAG, "EVENT. 6B Tag  [%s]", List6B.get(0));
                    DetenerRF();
                    leeUID_6B(0);
                }
            });

            return true;
        }
        return false;
    }

    public void leeUID_6B(int pos){
        ATLog.e(TAG,"LEYENDO UID: "+(pos+1)+" de: "+List6B.size());
        listener.onPeajeLogInformation("6B|LEYENDO UID: "+(pos+1)+" de: "+List6B.size());
        DetenerRF();
        mMAReader = (ATRfid900MAReader) mReader;

        ReadExtra = 1;
        sEPC = List6B.get(pos);
        SelectionMask6b mask6b =
                new SelectionMask6b(0, sEPC, MaskMatchingType.Match);
        StartTimer(iTime6B);
        mMAReader.readMemory6b(112, 16, mask6b);
    }

    private boolean Proceso6C(){
        is_Inventory_ended=true;
        DetenerRF();
        if(List6C.size()>0){//Si sí hay 6C´s hacer el cagadero
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    leeTID_6C(0);
                }
            });

            ATLog.d(TAG, "EVENT. 6C Tag 1st TID---true [%s]", List6C.get(0));
            return true;
        }
        return false;
    }

    private void leeTID_6C(int pos){
        DetenerRF();
        ATLog.e(TAG,"LEYENDO TID: "+(pos+1)+" de: "+List6C.size());
        listener.onPeajeLogInformation("6C|LEYENDO TID: "+(pos+1)+" de: "+List6C.size());
       /* try {
            mReader.setPower(230);
        } catch (ATRfidReaderException e) {
            e.printStackTrace();
        }
        mMAReader = (ATRfid900MAReader) mReader;*/
        int PWR =0;
        try {
            PWR=mMAReader.getPower();
        } catch (ATRfidReaderException e) {
            e.printStackTrace();
        }
        if(debug){

            ATLog.e(TAG,"Lectura con Power:     "+PWR);
        }
        ReadExtra = 0;
        sEPC = List6C.get(pos).substring(4);
        int lenght=sEPC.length()*4;
        EpcMatchParam epcMask = new EpcMatchParam(MaskMatchingType.Match, 0, lenght, sEPC);


        ResultCode codea=mReader.readMemory6c(BankType.TID,0,6,"0000",epcMask);
        if(debug){
            ATLog.e(TAG,"Resulcode de EPCMASK 6c>>>>>>>>>>>>>>>>>>>>>>"+codea);
        }
        StartTimer(iTime6C);

    }

    private void StartTimer(final int iTime) {
        if(debug){
            ATLog.e(TAG,"Se inicia Timer-------"+iTime);
        }
        if(CuentaVueltas>novueltas){
            DetenerRF();
            Fin_proceso();
            return;
        }
        rTimer = new Timer();
        rTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                if(debug){

                    ATLog.e(TAG, "-------------SE ACABO EL TIEMPO PERRO!!   tiempo: "+iTime +
                            "\r\n           #### TipoLectura: "+TipoLectura+"         ####isInventoryEnded?: "+is_Inventory_ended+ "        ####Vueltas: "+CuentaVueltas+
                            "\r\n           #### Inventario6B.size: "+List6B.size()+
                            "\r\n           #### inventario6C.size:::"+List6C.size());
                }
                DetenerRF();
                /**
                 * Tipo lectura --->0=lectura 6B
                 *              --->1=Lectura 6C
                 */
                switch (TipoLectura){
                    case 0:

                        if(!is_Inventory_ended){//Acaba de terminar inventario 6B
                            if(!Proceso6B()){
                                TipoLectura = 1;
                                listener.onPeajeLogInformation("6C|INICIA LECTURA|");
                                is_Inventory_ended=false;
                                limpiarlistas();
                                IniciarLectura6C();
                            }
                        }else{//Regreso de intentar leer uid
//                            Comentario del ISO B
                            if(List6B.size()>0){
                                dato_final=dato_final+"6B|"+List6B.get(posList6B)+"|@";
                                posList6B++;
                                if(posList6B<List6B.size()){
                                    leeUID_6B(posList6B);
                                }else{//ya se obtuvieron todos los datos 6B
                                    DetenerRF();
                                    is6B_Complete=true;
                                    if(CuentaVueltas<novueltas&&!is6C_Complete){
                                        CuentaVueltas=novueltas;
                                        TipoLectura = 1;
                                        listener.onPeajeLogInformation("6C|INICIA LECTURA|");
                                        is_Inventory_ended=false;
                                        limpiarlistas();
                                        IniciarLectura6C();
                                    }else{
                                        Fin_proceso();
                                    }
                                }
                            }else{
                                TipoLectura = 1;
                                listener.onPeajeLogInformation("6C|INICIA LECTURA|");
                                is_Inventory_ended=false;
                                 limpiarlistas();
                                IniciarLectura6C();
                            }
                        }
                        break;
                    case 1:
                        boolean bTag6C ;
                        if(CuentaVueltas==novueltas){
                            TipoLectura=3;
                        }
                        if(!is_Inventory_ended){//acaba de terminar inventario 6C
                            bTag6C=Proceso6C();
                            if (!bTag6C)  {//si regresa falso es porque no hubo inventario alv
                                TipoLectura = 0;//antes era 3
                                listener.onPeajeLogInformation("6B|INICIA LECTURA|");
                                is_Inventory_ended=false;
                                limpiarlistas();
                                IniciarLectura6B();
                            }
                        }else{//rse acabo el timer cuando leia un TID
                           if(List6C.size()>0){
                                DetenerRF();
                               dato_final=dato_final+"6C|"+RES_UNREAD+Interpreta.Tag6CPeajewithouTID(List6C.get(posList6C).substring(4));
                               posList6C++;
                                if(posList6C<List6C.size()){
                                   if(debug){
                                       ATLog.e(TAG,"ENTRE A LEER EL TID DEL "+(posList6C+1));
                                   }
                                   leeTID_6C(posList6C);
                                }else{//ya se capturaron todos los datos del inventario
                                   DetenerRF();
                                   is6C_Complete=true;
                                   if(CuentaVueltas<novueltas&&!is6B_Complete){
                                       CuentaVueltas=novueltas;
                                       TipoLectura = 0;
                                       listener.onPeajeLogInformation("6B|INICIA LECTURA|");
                                       is_Inventory_ended=false;
                                       limpiarlistas();
                                       IniciarLectura6B();
                                   }else{
                                       Fin_proceso();
                                   }
                               }
                            }else{
                                TipoLectura = 0;//antes era 3
                                listener.onPeajeLogInformation("6B|INICIA LECTURA|");
                                is_Inventory_ended=false;
                                limpiarlistas();
                                IniciarLectura6B();
                            }
                        }
                        break;
                        //TODO: agregar caso 2 que inicia lectura TR
                    case 2:
                        //caso del TR
                        break;
                    case 3:
                        if(!is_Inventory_ended){//Acaba de terminar inventario 6B
                           if(!Proceso6B()){
                               CuentaVueltas=0;
                               Fin_proceso();
                           }
                        }else{
                            if(List6C.size()>0){
                                DetenerRF();
                                dato_final=dato_final+"6C|"+RES_UNREAD+Interpreta.Tag6CPeajewithouTID(List6C.get(posList6C).substring(4));
                                posList6C++;
                                if(posList6C<List6C.size()){
                                    if(debug){
                                        ATLog.e(TAG,"ENTRE A LEER EL TID DEL "+(posList6C+1));
                                    }
                                    leeTID_6C(posList6C);
                                }else{//ya se capturaron todos los datos del inventario
                                    DetenerRF();
                                    is6C_Complete=true;
                                    if(CuentaVueltas<novueltas&&!is6B_Complete){
                                        CuentaVueltas=novueltas;
                                        TipoLectura = 0;
                                        listener.onPeajeLogInformation("6B|INICIA LECTURA|");
                                        is_Inventory_ended=false;
                                        limpiarlistas();
                                        IniciarLectura6B();
                                    }else{
                                        Fin_proceso();
                                    }
                                }
                            }else{
                                Fin_proceso();
                            }
                        }
                        break;
                    default:
                        DetenerRF();
                        Fin_proceso();
                        break;
                }

            }
        }, iTime, iTime);
    }

    private void Fin_proceso(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(dato_final.length()>0){
                    utilidades.playSuccess();
                    listener.onReadTagPeaje(dato_final);
                    listener.onPeajeLogInformation("RF|FINALIZA LECTURA|");
                }else{
                    utilidades.playError();
                    listener.onReadTagPeaje("ER|NO TAG|");
                    listener.onPeajeLogInformation("RF|FINALIZA LECTURA|");
                }
            }
        });
    }



    private void StopTimer() {
        ATLog.d(TAG, "Se Cancela Timer");
        if (rTimer != null) {
            rTimer.cancel();
            rTimer.purge();
            rTimer = null;
        }
    }


    @Override
    public void onReaderStateChanged(ATRfidReader atRfidReader, ConnectionState connectionState) {

        switch (connectionState) {
            case Connected:
                String version = "";
                try {
                    version = mReader.getFirmwareVersion();
                } catch (ATRfidReaderException e) {
                    Log.d(TAG, "ERROR. onReaderStateChanged(%s) - Failed to get firmware version" + connectionState);
                    version = "";
                    mReader.disconnect();
                }
                Log.d(TAG, version);

                break;
            case Disconnected:
                Log.d(TAG, "onReaderStateChanged: " + connectionState);
                break;
            case Connecting:
                Log.d(TAG, "onReaderStateChanged: " + connectionState);
                break;
            default:
                Log.d(TAG, "onReaderStateChanged: " + connectionState);
                break;
        }

    }

    @Override
    public void onReaderActionChanged(ATRfidReader atRfidReader, ActionState actionState) {
        ATLog.d(TAG, "EVENT. onReaderActionchanged--> " + actionState);

    }

    @Override
    public void onReaderReadTag(ATRfidReader atRfidReader, String s, float rssi, float phase)  {
        ATLog.d(TAG, "EVENT. onReaderReadTag([%s], %.2f, %.2f)", s, rssi, phase);

        if(s.length()==16){//----> LECTURA DE 6B
            if (!List6B.contains(s))
                List6B.add(s);
        }else if (s.length() == 28) {
            ATLog.d(TAG, "EVENT. 6C Tag  [%s]", s);
            if (s.substring(6,8).equals("F7")){
                if (!List6C.contains(s))
                    List6C.add(s);
            }
        }

    }

    @Override
    public void onReaderResult(ATRfidReader atRfidReader, ResultCode resultCode, ActionState actionState, String epc, String tid, float rssi, float phase) {
        ATLog.e(TAG, "EVENT. onReaderResult(%s, %s, [%s], [%s], %.2f, %.2f", resultCode, actionState, epc, tid, rssi, phase);
        String dato;

        switch (ReadExtra){
            /**
             * Caso 0 <----->Lectura 6C
             */
            case 0:
                dato=Interpreta.Tag6CPeaje(sEPC,tid);
                if(debug){
                    ATLog.e(TAG,dato);
                }
                //List6C_Interpretado.add(dato.split("\\|")[1]);
                List6C_Interpretado.add("6C|"+RES_OK+dato);
//                Datos lista c
                dato_final=dato_final+"6C|"+RES_OK+dato;

                posList6C++;
                if(posList6C<List6C.size()){
                    if(debug){
                        ATLog.e(TAG,"ENTRE A LEER EL TID DEL "+(posList6C+1));
                    }
                    leeTID_6C(posList6C);
                }else{//ya se capturaron todos los datos del inventario
                    DetenerRF();
                    //dato_final="";
                    is6C_Complete=true;
                   /* for(int i=0;i<List6C_Interpretado.size();i++){
                        dato_final=dato_final + List6C_Interpretado.get(i);
                    }*/
                    if(CuentaVueltas<novueltas&&!is6B_Complete){
                        CuentaVueltas=novueltas;
                        TipoLectura = 0;//antes era 3
                        listener.onPeajeLogInformation("6B|INICIA LECTURA|");
                        is_Inventory_ended=false;
                        limpiarlistas();
                        IniciarLectura6B();
                    }else{
                       /* utilidades.playSuccess();
                        listener.onReadTagPeaje(dato_final);
                        listener.onPeajeLogInformation("RF|FINALIZA LECTURA|");*/
                       Fin_proceso();
                    }
                }
                break;

            /**
             * Caso 1 <----->Lectura 6B
             */
            case 1:
                dato=Interpreta.Tag6BPeaje(sEPC,tid);
//                dato = sEPC;
                Log.d("sEPC", sEPC);
                Log.d("tid",tid);

                if(debug){
                    ATLog.e(TAG,dato);
                }
                if(dato.split("\\|")[0].replace(" ","").length()==0){
                    List6B_Interpretado.add("6B|"+RES_INVALID+dato.replace(" ",""));
                    dato_final=dato_final+"6B|"+RES_INVALID+dato.replace(" ","");
                }else{
                    List6B_Interpretado.add("6B|"+RES_OK+dato);
                    dato_final=dato_final+"6B|"+RES_OK+dato;
                }
                posList6B++;
                if(posList6B<List6B.size()){
                    leeUID_6B(posList6B);
                }else{//ya se obtuvieron todos los datos 6B
                    DetenerRF();
                    is6B_Complete=true;
                   /* for(int i=0;i<List6B_Interpretado.size();i++){
                        dato_final=dato_final + List6B_Interpretado.get(i);
                    }*/
                    if(CuentaVueltas<novueltas&&!is6C_Complete){
                        CuentaVueltas=novueltas;
                        TipoLectura = 1;
                        listener.onPeajeLogInformation("6C|INICIA LECTURA|");
                        is_Inventory_ended=false;
                        limpiarlistas();
                        IniciarLectura6C();
                    }else{
                        utilidades.playSuccess();
                        listener.onReadTagPeaje(dato_final);
                        listener.onPeajeLogInformation("RF|FINALIZA LECTURA|");
                    }
                }
                break;
            default:
                break;
        }
    }


    protected void stopAction() {

        if (mReader.getAction() == ActionState.Stop) {
            ATLog.e(TAG, "ActionState is not busy.");
            return;
        }

        if ((res = mReader.stop()) != ResultCode.NoError) {
            ATLog.e(TAG, "ERROR. stopAction() - Failed to stop operation [%s]", res);
            return;
        }
        ATLog.i(TAG, "INFO. stopAction()");
        isBusy = false;
    }

    public void onDestroy() {
        if (mReader != null) {
            try {
                mReader.disconnect();
            } catch (Exception ex) {
                Log.e(TAG, "Destroy: " + ex.getMessage());
            }
        }
        mReader = null;
        ATRfidManager.onDestroy();
        SysUtil.wakeUnlock();
        ATLog.d(TAG, "INFO. onDestroy");
        ATLog.shutdown();
    }

    public void onStop() {
        ATRfidManager.sleep();
        ATLog.i(TAG, "INFO. onStop()");
    }

    public void onResume() {
        if (mReader != null)
            mReader.setEventListener(this);
        ATLog.d(TAG, "INFO. onResume()");
    }

    public void onPause() {
        if (mReader != null)
            mReader.removeEventListener(this);

        ATLog.i(TAG, "INFO. onPause()");
    }

    public void onStart() {
        if (mReader != null) {
            ATRfidManager.wakeUp();
        }
    }

    private void DetenerRF() {
        StopTimer();
        stopAction();
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

