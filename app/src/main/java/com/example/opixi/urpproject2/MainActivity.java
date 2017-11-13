package com.example.opixi.urpproject2;


import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;



public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    int num = 0;
    String mode_arr[] = new String[10];
    int mode_arr_count = 0;
    String mode_data[] = new String[2];
    int data_max = 0;
    int data_count3 = 0;
    int data_count2 = 0;
    String arr_string[] = new String[40];
    static final String TAG = "error";
    long result = 0;
    int count=0;
    final int White = -1;
    final int Black = -16777216;
    final String Header = "01110";
    public int index=0;
    private int[] packetArray = new int[10000];
    private Preview preview;
    private ImageView MyCameraPreview = null;
    private Button btnstart,btnstop;
    private TextView temperature;

    public SurfaceView mSurfaceView;
    public SurfaceHolder mHolder;

    //data packet array,string
    private String packets ="";

    //find preamble int array
    private int[] pi = new int[100];

    Handler mHandler = null;
    private TextviewThread textviewThread;

    long startTime=0;
    long endTime=0;
    int err=0;


    private Camera camera;//내가 추가
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);


        mSurfaceView =(SurfaceView)findViewById(R.id.surfaceView);
        mHolder =mSurfaceView.getHolder();

        //mHolder.addCallback(previewCallback);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        /*mSurfaceView.getHolder().addCallback(previewCallback); //내가 추가한 코드*/
        MyCameraPreview = (ImageView) findViewById (R.id.imageView);
        //temperature = (TextView)findViewById(R.id.textView);

        Log.d(TAG, "preview 시작");
        preview = new Preview(MyCameraPreview, this, MainActivity.this);   //이거 활성화 시키면 꺼짐

        Log.d(TAG, "preview 종료");



        btnstart = (Button)findViewById(R.id.button);
        btnstop = (Button)findViewById(R.id.button2);
        btnstart.setOnClickListener(this);
        btnstop.setOnClickListener(this);

        mHandler = new Handler();


    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){

            case R.id.button:
                Log.d(TAG, "button 시작");
                preview.isStart = true;
                textviewThread = new TextviewThread(true);
                textviewThread.start();
                Log.d(TAG, "button 끝");
                break;
            case R.id.button2:


        }

    }

    private void mode(){
        // 하나값 받고 그값 100개랑 비교한다. 같으면 숫자기록.
        //
        int mode_count[] = new int[10];
        String data = "";
        int mode_data_count = 0;
        String mode_max_one = "";
        String mode_max_two = "";
        int mode_max_index = 0;
        int mode_max_index2 = 0;

        for(int i=0; i< mode_arr.length; i++){
            data = mode_arr[i];
            for(int j=0; j< mode_arr.length; j++){
                if(mode_arr[i].equals(mode_arr[j])){
                    //값이 같다면
                    mode_data_count ++;
                }
            }
            mode_count[i] = mode_data_count;
            mode_data_count = 0;
        }

        for(int i=0; i<mode_count.length; i++){
            if(mode_max_index < mode_count[i]){
                mode_max_index = mode_count[i];
            }
            if(mode_max_index != mode_count[i] && mode_max_index < mode_count[i]){
                mode_max_index2 = mode_count[i];
            }
        }

        mode_data[0] = mode_arr[mode_max_index];
        mode_data[1] = mode_arr[mode_max_index2];
    }

    private String packetDecoding(String data){
        String data1 = data.substring(2,10);
        String data2 = data.substring(12,20);
        String data3 = data.substring(22,30);
        String data11 = "";
        String data22 = "";
        String data33 = "";
        String data_a;
        String data_b;
        String data_c;

        for(int i=0; i<data1.length(); i++) {
            if(data1.substring(data1.length()-i-1, data1.length()-i).equals("1")) {
                data11 += "0";
            }else {
                data11 += "1";
            }
            if(data2.substring(data2.length()-i-1, data2.length()-i).equals("1")) {
                data22 += "0";
            }else {
                data22 += "1";
            }
            if(data3.substring(data3.length()-i-1, data3.length()-i).equals("1")) {
                data33 += "0";
            }else {
                data33 += "1";
            }
        }
        data_a = String.valueOf((char) Integer.parseInt(data11, 2));
        data_b = String.valueOf((char) Integer.parseInt(data22, 2));
        data_c = String.valueOf((char) Integer.parseInt(data33, 2));

        return data_a+data_b+data_c;
    }

    private String packetEncoding(String pixel, int previewHeight) {
        int data_count = 0;
        int count = 0;
        int previous = 0;
        int num = 0;
        boolean start = false;
        boolean start_data = false;


        String data = "";
        for (int i = 0; i < 337; i++) {
            if (pixel.substring(i, i + 1).equals("0")) {  // 현재 0 일때
                if (previous == 0) {
                    count++;
                } else {    // 전이 1이일때
                    // 0의 개수를 읽고 나서  전이 1인데 현재가 0     1  -->  0
                    if (count > 30) {
                        if (start == false) {
                            start = true;
                        }

                    } else if (count > 1 && count < 5 & start == true) {
                        data += "1";
                        data_count++;
                    } else {
                        if (start == true && count < 10) {
                            num = (int) ((int) count / 2.5);
                            for (int j = 0; j < num; j++) {
                                data += "1";
                                data_count++;
                            }
                        } else if (start == true) {
                            num = (int) ((int) count / 3);
                            for (int j = 0; j < num; j++) {
                                data += "1";
                                data_count++;
                            }
                        }
                    }
                    count = 1;
                }

                previous = 0;
            } else {                                    // 1 일때
                if (previous == 1) {
                    count++;
                } else {   // 이전이 0이고 지금이 1일때           0   -->   1
                    if (count > 30) {  // 버리는값

                    } else if (count > 1 && count < 6 && start == true && start_data == true) {
                        data += "0";
                        data_count++;
                    } else {
                        if (start == true && start_data == true) {
                            num = (int) ((int) count / 3);
                            for (int j = 0; j < num; j++) {
                                data += "0";
                                data_count++;
                            }
                        }

                    }
                    count = 1;
                    if (start == true) {
                        if (start_data == false) {
                            start_data = true;
                        }
                    }
                }
                previous = 1;
            }


        }
        if (data.length() != 30){
            if (data.length() > 0 && !data.substring(data.length() - 2, data.length()).equals("11")) {
                if (data.substring(data.length() - 2, data.length() - 1).equals("0")) {
                    data += "1";
                } else {
                    data += "11";
                }
            }

            if(data.length() == 29){
                data = "0" + data;
            }
        }


        return data;
    }

    private void printpixel(int[] pixel, int previewWidth, int previewHeight){
        int count=0;
        int prebit=0;
        int temp=0;
        if(preview.centerP != 0){
            for(int i=0; i<previewHeight; i++){
                for(int j = 0 ; j< previewWidth; j++){
                    if(j == (preview.centerP)){
                        temp = pixel[j + i * previewWidth];
                        if(temp == -1){
                            packets = packets + 0;
                        }else if(temp == -16777216){
                            packets = packets + 1;
                        }

                    }
                }
            }
        }

    }



    class TextviewThread extends Thread{
        private boolean isPlay = false;
        int[] temp;
        String max_data[] = new String[2];

        String text="";
        String packets2 = "";
        String last_data = "";


        public TextviewThread(boolean isPlay){
            this.isPlay = isPlay;
        }
        public void stopThread(boolean isPlay){
            this.isPlay = !isPlay;
        }

        @Override

        public void run(){
            //super.run();

            while (isPlay) {
                if(preview.queue.size()>0) {
                    temp = preview.queue.poll();    //앞에서부터 값을 가져옴

                    printpixel(temp, preview.previewWidth, preview.previewHeight);   // 패킷을 만들어줌.

                    packets2 = packetEncoding(packets, preview.previewHeight);

                    System.out.println("packets test data = "+packets2);
                    if(packets2.length() == 30 ){
                        if(packets2.substring(packets2.length()-2,packets2.length()).equals("11")) {
                            System.out.println("packets test data = " + packets2);
                            last_data = packetDecoding(packets2);
                            System.out.println("packets data = "+last_data);

                            if (mode_arr_count < 10) {
                                mode_arr[mode_arr_count] = last_data; //  최빈값구하려고 배열에다 늠.
                                mode_arr_count++;
                            } else {   // 100번 다 채웠다면.
                                mode();
                                mode_arr_count = 0;
                                db_con db = new db_con();
                                db.start(mode_data[0]); // 최빈값
                                db.start(mode_data[1]); // 그다음 값
                            }
                        }

                    }
                    System.out.println("packets test after = " +packets);

                    packets = "";
                }

            }
        }

    }


}
