package com.example.opixi.urpproject2;

/**
 * Created by opixi on 2017-09-23.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class db_con {
    public static void start(String data) {
        StringBuilder resultText = new StringBuilder();
        try {

            if(data.substring(0,1).equals("1") && data.length() == 3 ){
                URL url = new URL("http://192.168.1.5/test/Data_insert.php?data="+data.substring(1,3)); // 호출할 url
                url.openStream();
            }else if(data.substring(0,1).equals("2") && data.length() == 3){
                URL url = new URL("http://192.168.1.5/test/Data_insert2.php?data="+data.substring(1,3)); // 호출할 url
                url.openStream();
            }


        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
