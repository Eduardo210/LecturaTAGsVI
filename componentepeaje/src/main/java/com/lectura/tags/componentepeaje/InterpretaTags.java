package com.lectura.tags.componentepeaje;

import android.support.annotation.NonNull;
import java.math.BigInteger;

public class InterpretaTags {


    public InterpretaTags() {

    }

    public String Tag6CPeaje(String sEPC, String sTID) {
        try {
            if (!sEPC.substring(16, 24).equals(sTID.substring(16, 24)))
                return "6C|Error Formato|";

            String sBits = getBinaryFromHex(sEPC, 96);
            if (!sBits.substring(0, 2).equals("00"))
                return "6C|Error Formato|";

            if (!sBits.substring(6, 16).equals("0011110111"))
                return "6C|Error Formato|";

            String Operador = Integer.parseInt(sBits.substring(16, 26), 2) + "";
            String sFolio = Integer.parseInt(sBits.substring(26, 58), 2) + "";

            return  padLeftZeros(Operador, 3) + padLeftZeros(sFolio, 8) + "|" + sTID + "|@";
        }
        catch (Exception ex){
            return "6C|Error Formato|";
        }
    }

    public String Tag6CPeajewithouTID(String sEPC) {
        try {

            String sBits = getBinaryFromHex(sEPC, 96);
            String Operador = Integer.parseInt(sBits.substring(16, 26), 2) + "";
            String sFolio = Integer.parseInt(sBits.substring(26, 58), 2) + "";

            return  padLeftZeros(Operador, 3) + padLeftZeros(sFolio, 8) + "|@";
        }
        catch (Exception ex){
            return "6C|Error Formato|";
        }
    }


    public String Tag6BPeaje(String sEPC, String sTID){
        String sFolio = "";
        sTID = getBinaryFromHex(sTID, 128);

        for (int i=0; i< 60 ;i=i+6)
            sFolio += get6BitChar(sTID.substring(i,i+6));

        sFolio += get6BitChar(sTID.substring(64,70));
        sFolio += get6BitChar(sTID.substring(70,76));

        return sFolio+"|"+sEPC+"|@";
//        return sTID+"|"+sEPC+"   "+ sFolio+"|@";
    }


    public String TagTRPeaje(String sEPC){
        String sFolio = "";
        sEPC = getBinaryFromHex(sEPC, 128);

        for (int i=0; i< 60 ;i=i+6)
            sFolio += get6BitChar(sEPC.substring(i,i+6));

        sFolio += get6BitChar(sEPC.substring(64,70));
        sFolio += get6BitChar(sEPC.substring(70,76));

        return "TR|"+sFolio+"|";
    }


    private String getBinaryFromHex(String sHex, int iLength) {
        String sResult = new BigInteger(sHex, 16).toString(2);
        if (sResult.length() <= iLength)
            sResult = padLeftZeros(sResult, iLength);
        return sResult;
    }

    @NonNull
    public static String padLeftZeros(String str, int n) {
        return String.format("%1$" + n + "s", str).replace(' ', '0');
    }


    // Codificación ISO 6 bit
    public char get6BitChar(String str)
    {
        switch (str)
        {
            case "000000":
                return ' ';
            case "000001":
                return '!';
            case "000010":
                return '"';
            case "000011":
                return '#';
            case "000100":
                return '$';
            case "000101":
                return '%';
            case "000110":
                return '&';
            case "000111":
                return '\'';
            case "001000":
                return '(';
            case "001001":
                return ')';
            case "001010":
                return '*';
            case "001011":
                return '+';
            case "001100":
                return ',';
            case "001101":
                return '-';
            case "001110":
                return '.';
            case "001111":
                return '/';
            case "010000":
                return '0';
            case "010001":
                return '1';
            case "010010":
                return '2';
            case "010011":
                return '3';
            case "010100":
                return '4';
            case "010101":
                return '5';
            case "010110":
                return '6';
            case "010111":
                return '7';
            case "011000":
                return '8';
            case "011001":
                return '9';
            case "011010":
                return ':';
            case "011011":
                return ';';
            case "011100":
                return '<';
            case "011101":
                return '=';
            case "011110":
                return '>';
            case "011111":
                return '?';
            case "100000":
                return '@';
            case "100001":
                return 'A';
            case "100010":
                return 'B';
            case "100011":
                return 'C';
            case "100100":
                return 'D';
            case "100101":
                return 'E';
            case "100110":
                return 'F';
            case "100111":
                return 'G';
            case "101000":
                return 'H';
            case "101001":
                return 'I';
            case "101010":
                return 'J';
            case "101011":
                return 'K';
            case "101100":
                return 'L';
            case "101101":
                return 'M';
            case "101110":
                return 'N';
            case "101111":
                return 'O';
            case "110000":
                return 'P';
            case "110001":
                return 'Q';
            case "110010":
                return 'R';
            case "110011":
                return 'S';
            case "110100":
                return 'T';
            case "110101":
                return 'U';
            case "110110":
                return 'V';
            case "110111":
                return 'W';
            case "111000":
                return 'X';
            case "111001":
                return 'Y';
            case "111010":
                return 'Z';
            case "111011":
                return '[';
            case "111100":
                return '\\';
            case "111101":
                return ']';
            case "111110":
                return '^';
            case "111111":
                return '_';
        }
        return '\0';
    }

}