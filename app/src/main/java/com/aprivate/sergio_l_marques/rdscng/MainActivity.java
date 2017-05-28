package com.aprivate.sergio_l_marques.rdscng;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    private boolean readSockThreadRunnableFlag, zoneViewHelpLayoutFlag;
    private static String zone_pgm_labels_filename="rdscng_zone_pgm_labels";
    private static final String LTAG = "rdscng";
    int i, IT100MS=0;
    private View.OnTouchListener bListener;
    private ClientThread sockClientThread;
    private Thread sockThread;
    private static Socket txSocket;
    private Handler handler=null;
    private String hostAddress;
    private String tcPort;
    private String dscPass;
    private String dscUserCode;
    ToneGenerator toneG;

    private TextView lcdTextView;

    LinearLayout zoneMainll;

    private LinearLayout[] zonell = new LinearLayout[2];
    //LinearLayout zonell;
    private int numZones= 0; // number of textviews
    private TextView[] zonesTextViews = new TextView[64]; // create an empty array;
    private static int zoneStatus[]=new int[64];

    private LinearLayout cmdOPll;
    private int numCmdOP= 0;
    private Button[] cmdOPButtons = new Button[4]; // create an empty array;
    private static int cmdOPStatus[]=new int[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LTAG, "onCreate");
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Log.d(LTAG, "Get preferences");
        hostAddress=prefs.getString("haddress", "default.com");
        tcPort=prefs.getString("tcport", "4000");
        dscPass=prefs.getString("dscpass", "00000");
        dscUserCode=prefs.getString("dscUserCode", "1243");

        Toast.makeText(getApplicationContext(), hostAddress, Toast.LENGTH_LONG).show();


        TabHost host = (TabHost)findViewById(R.id.tab_host);
        host.setup();

        TabHost.TabSpec spec = host.newTabSpec("keypad");
        spec.setContent(R.id.tab_one_container);
        spec.setIndicator("keypad");
        host.addTab(spec);

        spec = host.newTabSpec("zones");
        spec.setContent(R.id.tab_two_container);
        spec.setIndicator("zones&pgm");
        host.addTab(spec);

        //set Windows tab as default (zero based)
        host.setCurrentTab(0);

        handler = new Handler();

        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 20);

        for (i=0;i<64;i++) zoneStatus[i]=0;
        for (i=0;i<4;i++) cmdOPStatus[i]=0;

        Log.d(LTAG, "Get layouts");
        cmdOPll   = (LinearLayout) findViewById(R.id.CommandOPll);
        zoneMainll = (LinearLayout) findViewById(R.id.zoneslinearLayout);
        zonell[0] = (LinearLayout) findViewById(R.id.zoneslinearLayoutLeft);
        zonell[1] = (LinearLayout) findViewById(R.id.zoneslinearLayoutRight);
        lcdTextView = (TextView)findViewById(R.id.lcd);

        Log.d(LTAG, "set listenners");
        bListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int id = v.getId();
                Button button= (Button) findViewById(v.getId());

                Log.d("OnTouchListener", "ENTER");

                if(event.getAction() == MotionEvent.ACTION_UP){

                    button.getBackground().setAlpha(255);

                    //Toast.makeText(getApplicationContext(), "keybreak", Toast.LENGTH_LONG).show();
                    switch (id) {
                        case R.id.cmdOPreservedNamed0:
                        case R.id.cmdOPreservedNamed1:
                        case R.id.cmdOPreservedNamed2:
                        case R.id.cmdOPreservedNamed3:
                            break;
                        default:
                            txCmd("070"+ "^");
                    }
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    button.getBackground().setAlpha(45);

                    //Toast.makeText(getApplicationContext(), "key", Toast.LENGTH_LONG).show();
                    switch (id) {
                        case R.id.k0      : txCmd("070"+ "0"); break;
                        case R.id.k1      : txCmd("070"+ "1"); break;
                        case R.id.k2      : txCmd("070"+ "2"); break;
                        case R.id.k3      : txCmd("070"+ "3"); break;
                        case R.id.k4      : txCmd("070"+ "4"); break;
                        case R.id.k5      : txCmd("070"+ "5"); break;
                        case R.id.k6      : txCmd("070"+ "6"); break;
                        case R.id.k7      : txCmd("070"+ "7"); break;
                        case R.id.k8      : txCmd("070"+ "8"); break;
                        case R.id.k9      : txCmd("070"+ "9"); break;
                        case R.id.f1      : txCmd("070"+ "F"); break;
                        case R.id.f2      : txCmd("070"+ "A"); break;
                        case R.id.f3      : txCmd("070"+ "P"); break;
                        case R.id.f4      : txCmd("070"+ "a"); break;
                        case R.id.f5      : txCmd("070"+ "b"); break;
                        case R.id.f6      : txCmd("070"+ "c"); break;
                        case R.id.f7      : txCmd("070"+ "d"); break;
                        case R.id.f8      : txCmd("070"+ "e"); break;
                        case R.id.asterisk: txCmd("070"+ "*"); break;
                        case R.id.cardinal: txCmd("070"+ "#"); break;
                        case R.id.left    : txCmd("070"+ "<"); break;
                        case R.id.right   : txCmd("070"+ ">"); break;

                        case R.id.cmdOPreservedNamed0:  sendCmdOP(0, id); break;
                        case R.id.cmdOPreservedNamed1:  sendCmdOP(1, id); break;
                        case R.id.cmdOPreservedNamed2:  sendCmdOP(2, id); break;
                        case R.id.cmdOPreservedNamed3:  sendCmdOP(3, id); break;
                    }
                }
                return true;
            }
        };


        Log.d(LTAG, "set buttons touchlistener");
        Button button;
        button = (Button) findViewById(R.id.k0      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.k1      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.k2      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.k3      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.k4      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.k5      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.k6      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.k7      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.k8      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.k9      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.f1      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.f2      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.f3      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.f4      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.f5      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.f6      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.f7      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.f8      ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.asterisk); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.cardinal); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.left    ); button.setOnTouchListener(bListener);
        button = (Button) findViewById(R.id.right   ); button.setOnTouchListener(bListener);

        Log.d(LTAG, "set thread policy");
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Log.d(LTAG, "opening rdscng_zone_pgm_labels file for read");
        BufferedReader inputReader = null;
        try {
            inputReader = new BufferedReader(new InputStreamReader(openFileInput(zone_pgm_labels_filename)));
            Log.d(LTAG, "OK");

            Log.d(LTAG, "reading rdscng_zone_pgm_labels file");
            String inputString;
            //Reading data line by line and storing it into the stringbuffer
            try {
                while ((inputString = inputReader.readLine()) != null) {
                    Scanner scanIn= new Scanner(inputString);
                    String entryType=scanIn.next();
                    int entryIdx=scanIn.nextInt();
                    String entryName=scanIn.nextLine();
                    Log.d(LTAG, entryType + String.format(" --> %d ", entryIdx) + entryName + " |");

                    if      (entryType.equals("zone")) newZoneStatusObject(entryIdx, entryName);
                    else if (entryType.equals( "pgm")) newCommandOutputObject(entryIdx, entryName);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        readSockThreadRunnableFlag=true;
        zoneViewHelpLayoutFlag=true;

        Log.d(LTAG, "create thread");
        sockClientThread=new ClientThread(hostAddress);
        sockThread = new Thread(sockClientThread);
        sockThread.start();

    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(LTAG, "onStop");

        readSockThreadRunnableFlag=false;
        try{
            sockThread.join();
        }catch(Exception e){System.out.println(e);}

        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(LTAG, "onDestroy");

        for (i=0;i<64;i++) zoneStatus[i]=0;
        for (i=0;i<4;i++) cmdOPStatus[i]=0;

        zonell[0].removeAllViews();
        zonell[1].removeAllViews();
        cmdOPll.removeAllViews();
    }

//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        Log.d(LTAG, "onWindowFocusChanged");

        Button button;

        button= (Button) findViewById(R.id.f1); button.setBackground(setButtonBackground(button, R.drawable.fire     ));
        button= (Button) findViewById(R.id.f2); button.setBackground(setButtonBackground(button, R.drawable.ambulance));
        button= (Button) findViewById(R.id.f3); button.setBackground(setButtonBackground(button, R.drawable.panic    ));
        button= (Button) findViewById(R.id.f4); button.setBackground(setButtonBackground(button, R.drawable.arm_stay ));
        button= (Button) findViewById(R.id.f5); button.setBackground(setButtonBackground(button, R.drawable.arm_away ));
        button= (Button) findViewById(R.id.f6); button.setBackground(setButtonBackground(button, R.drawable.chime    ));
        button= (Button) findViewById(R.id.f7); button.setBackground(setButtonBackground(button, R.drawable.reset    ));
        button= (Button) findViewById(R.id.f8); button.setBackground(setButtonBackground(button, R.drawable.exit     ));

        button= (Button) findViewById(R.id.k0); button.setBackground(setButtonBackground(button, R.drawable.zero     ));
        button= (Button) findViewById(R.id.k1); button.setBackground(setButtonBackground(button, R.drawable.um       ));
        button= (Button) findViewById(R.id.k2); button.setBackground(setButtonBackground(button, R.drawable.dois     ));
        button= (Button) findViewById(R.id.k3); button.setBackground(setButtonBackground(button, R.drawable.tres     ));
        button= (Button) findViewById(R.id.k4); button.setBackground(setButtonBackground(button, R.drawable.quatro   ));
        button= (Button) findViewById(R.id.k5); button.setBackground(setButtonBackground(button, R.drawable.cinco    ));
        button= (Button) findViewById(R.id.k6); button.setBackground(setButtonBackground(button, R.drawable.seis     ));
        button= (Button) findViewById(R.id.k7); button.setBackground(setButtonBackground(button, R.drawable.sete     ));
        button= (Button) findViewById(R.id.k8); button.setBackground(setButtonBackground(button, R.drawable.oito     ));
        button= (Button) findViewById(R.id.k9); button.setBackground(setButtonBackground(button, R.drawable.nove     ));

        button= (Button) findViewById(R.id.left ); button.setBackground(setButtonBackground(button, R.drawable.menor));
        button= (Button) findViewById(R.id.right); button.setBackground(setButtonBackground(button, R.drawable.maior));

        button= (Button) findViewById(R.id.asterisk); button.setBackground(setButtonBackground(button, R.drawable.aste));
        button= (Button) findViewById(R.id.cardinal); button.setBackground(setButtonBackground(button, R.drawable.card));

    }

    private Drawable setButtonBackground(Button button, int id) {
        BitmapDrawable bmapScaled;

        Log.d(LTAG, "setButtonBackground");

        int height = button.getHeight();
        int width = button.getWidth();

        BitmapDrawable bmap = (BitmapDrawable) this.getResources().getDrawable(id);

        float bmapWidth = bmap.getBitmap().getWidth();
        float bmapHeight = bmap.getBitmap().getHeight();

        float wRatio = width / bmapWidth;
        float hRatio = height / bmapHeight;

        float ratioMultiplier = wRatio;
        // Untested conditional though I expect this might work for landscape mode
        if (hRatio < wRatio) {
            ratioMultiplier = hRatio;
        }

        int newBmapWidth = (int) (bmapWidth*ratioMultiplier);
        int newBmapHeight = (int) (bmapHeight*ratioMultiplier);

        bmapScaled = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bmap.getBitmap(), newBmapWidth, newBmapHeight, false));
        bmapScaled.setGravity(Gravity.CENTER);
        bmapScaled.setBounds(0, 0, button.getWidth(), button.getHeight());

        return bmapScaled;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    Intent intent;
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(this, ViewAndUpdatePreferencesActivity.class);
                //this.startActivity(intent);
                startActivityForResult(intent, 1);
                //super.recreate();
                return true;
            case R.id.dateTime:
                Time now = new Time();
                now.setToNow();
                txCmd("010"+String.format("%02d%02d%02d%02d%02d", now.hour, now.minute, now.month+1, now.monthDay, now.year%100 ));
                return true;
            case R.id.about:
                Toast.makeText(this, "Powered by: Sergio Marques", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.reset:

                File file = new File(this.getFilesDir(), zone_pgm_labels_filename);
                boolean deleted = file.delete();
                super.recreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            //if(resultCode == Activity.RESULT_OK){
            //    String result=data.getStringExtra("result");
            //}
            //if (resultCode == Activity.RESULT_CANCELED) {
            //    //Write your code if there's no result
            //}
            super.recreate();
        }
    }//onActivityResult

    class updateToastMsgThread implements Runnable {
        private String msg;
        public updateToastMsgThread(String str) {
            this.msg = str;
        }
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
    }

    class updateLcdDisplayThread implements Runnable {
        private String msg;
        public updateLcdDisplayThread(String str) {
            this.msg = str;
        }
        @Override
        public void run() {
            Log.d(LTAG, "lcdTextView: " + msg);
            lcdTextView.setText(msg);
        }
    }

    class updateLedStateThread implements Runnable {
        private String msg;
        TextView ledTextView;
        public updateLedStateThread(String str) {
            this.msg = str;
        }
        @Override
        public void run() {
            switch (msg.charAt(3)) {
                case '1': //ledReady
                    ledTextView = (TextView)findViewById(R.id.Ready);
                    if (msg.charAt(4)=='0') ledTextView.setTextColor(Color.parseColor("#808080")); //off
                    else ledTextView.setTextColor(Color.parseColor("#008000")); //on
                    break;
                case '2'://LedArmed
                    ledTextView = (TextView)findViewById(R.id.Armed);
                    if (msg.charAt(4)=='0') ledTextView.setTextColor(Color.parseColor("#808080")); //off
                    else ledTextView.setTextColor(Color.parseColor("#008000")); //on
                    break;
                case '3'://LedMemory
                    ledTextView = (TextView)findViewById(R.id.Memory);
                    if (msg.charAt(4)=='0') ledTextView.setTextColor(Color.parseColor("#808080")); //off
                    else ledTextView.setTextColor(Color.parseColor("#FF0000")); //on
                    break;
                case '4'://LedBypass
                    ledTextView = (TextView)findViewById(R.id.Bypass);
                    if (msg.charAt(4)=='0') ledTextView.setTextColor(Color.parseColor("#808080")); //off
                    else ledTextView.setTextColor(Color.parseColor("#FFFF00")); //on
                    break;
                case '5'://LedTrouble
                    ledTextView = (TextView)findViewById(R.id.Trouble);
                    if (msg.charAt(4)=='0') ledTextView.setTextColor(Color.parseColor("#808080")); //off
                    else ledTextView.setTextColor(Color.parseColor("#FF0000")); //on
                    break;
                case '6'://LedProgram
                    ledTextView = (TextView)findViewById(R.id.Program);
                    if (msg.charAt(4)=='0') ledTextView.setTextColor(Color.parseColor("#808080")); //off
                    else ledTextView.setTextColor(Color.parseColor("#008000")); //on
                    break;
                case '7'://LedFire
                    ledTextView = (TextView)findViewById(R.id.Fire);
                    if (msg.charAt(4)=='0') ledTextView.setTextColor(Color.parseColor("#808080")); //off
                    else ledTextView.setTextColor(Color.parseColor("#FF0000")); //on
                    break;
                case '8':
                    //Toast.makeText(MainActivity.getContextOfApplication(), "backligth " + msg, Toast.LENGTH_LONG).show();
                    //
                    // backligth
                    //
                    break;
                case '9'://LedAC
                    ledTextView = (TextView)findViewById(R.id.AC);
                    if (msg.charAt(4)=='0') ledTextView.setTextColor(Color.parseColor("#808080")); //off
                    else ledTextView.setTextColor(Color.parseColor("#FF0000")); //on
            }
        }
    }

    class updateBeepThread implements Runnable {
        private String msg;
        public updateBeepThread(String str) {
            this.msg = str;
        }
        @Override
        public void run() {
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, Integer.parseInt(String.copyValueOf(msg.toCharArray(), 3, 3))*200);
        }
    }

    int readSockThread(Socket clientSocket) {
        BufferedReader input;

        try {

            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //Labels request
            try {
                int i;
                char CKS;
                String str = "002";
                for (i=0, CKS=0;i<str.length();i++) CKS+=str.charAt(i);
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream())),
                        true);
                out.println(String.format("%s%X%X\r", str, CKS>>4,CKS&0x0F));
                //out.print(String.format("%s%X%X", str, CKS>>4,CKS&0x0F) + "\r\n");
                //out.flush();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //status request
            try {
                int i;
                char CKS;
                String str = "001";
                for (i=0, CKS=0;i<str.length();i++) CKS+=str.charAt(i);
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream())),
                        true);
                out.println(String.format("%s%X%X\r", str, CKS>>4,CKS&0x0F));
                out.flush();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }

        while (!Thread.currentThread().isInterrupted() && readSockThreadRunnableFlag) {
            try {
                String read = input.readLine();
                if (read==null) {
                    Log.e(LTAG, "readSockThread: read==null!!!!");
                    clientSocket.close();
                    handler.post(new tcpClientStart());
                    return 2;
                } else {

                    //Log.d(LTAG, "read = input.readLine()" + read);

                    if (Integer.parseInt(read.substring(0, 2)) < 50 ) {
                        //Temos timestamp-control!
                        if (read.length()>9) read=read.substring(9);
                    }

                    if (read.startsWith("901")) {//LCD update
                        String line1, line2;
                        line1=read.substring(8   , 8+16   );
                        line2=read.substring(8+16, 8+16+16);
                        handler.post(new updateLcdDisplayThread( line1 + "\n" + line2 ));
                    } else if (read.startsWith("5053")) {//requesting password
                        int i; char CKS;
                        Log.d(LTAG, "requesting password: " + dscPass);
                        String str = "005"+ dscPass;
                        for (i=0, CKS=0;i<str.length();i++) CKS+=str.charAt(i);
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(clientSocket.getOutputStream())), true);
                        try {
                            out.println(String.format("%s%X%X\r", str, (CKS >> 4) & 0x0F, CKS & 0x0F));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(LTAG, "FAILED SENDING PASSWD");
                        }
                        out.flush();
                    } else if (read.startsWith("5050")) {//login failure
                        handler.post(new updateToastMsgThread("login failure"));
                    } else if (read.startsWith("5051")) {//login success
                        handler.post(new updateToastMsgThread("login success"));
                    } else if (read.startsWith("5052")) {//login timed out
                        handler.post(new updateToastMsgThread("login timed out"));
                    } else if (read.startsWith("903")) {//led update
                        handler.post(new updateLedStateThread(read));
                    } else if (read.startsWith("904")) {//beep
                        handler.post(new updateBeepThread(read));
                    } else if  (read.startsWith("570")) {//Labels
                        int idx=Integer.parseInt(read.substring(3, 6));
                        if (idx<=64) {
                            handler.post(new newZoneStatus(read));
                        } else if (idx==65) {
                            //Fire Alarm Label
                        } else if (idx==66) {
                            //Failed to arm Alarm Label
                        } else if (idx==67) {
                            //Alarm when armed Label
                        } else if (idx<=100) {
                            //reserved labels
                        } else if (idx<=108) {
                            //partition labels...
                        } else if (idx<=119) {
                            //reserved labels
                        } else if (idx<=123) {
                            //Command Output labels for partition 1

                            handler.post(new newCommandOutput(read));

                        } else if (idx<=151) {
                            //Command Output labels for partition 2..8

                            if  (idx==151) {//ultima label
                                try {
                                    int i;
                                    char CKS;
                                    String str = "001";
                                    for (i=0, CKS=0;i<str.length();i++) CKS+=str.charAt(i);
                                    PrintWriter out = new PrintWriter(new BufferedWriter(
                                            new OutputStreamWriter(clientSocket.getOutputStream())),
                                            true);
                                    out.println(String.format("%s%X%X\r", str, CKS>>4,CKS&0x0F));
                                    out.flush();
                                    //MainActivity.updateDebugText("Sending status request command 001");
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else if  (read.startsWith("609")) {//Zone Alarm
                        handler.post(new updateZoneStatus(read));
                    } else if  (read.startsWith("610")) {//Zone Restore
                        handler.post(new updateZoneStatus(read));
                    } else if  (read.startsWith("900")) {
                        txCmd("200" + dscUserCode);
                    } else {
                        //MainActivity.updateDebugText(read);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    input.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                try {
                    clientSocket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                return 3;
            }
        }
        Log.e(LTAG, "!Thread.currentThread().isInterrupted() && readSockThreadRunnableFlag");
        return 0;
    }

    class tcpClientStart implements Runnable {
        //private String msg;
        public tcpClientStart() {
            //this.msg = str;
        }
        @Override
        public void run() {
            sockClientThread=new ClientThread(hostAddress);
            sockThread = new Thread(sockClientThread);
            sockThread.start();
            Log.d(LTAG, "tcpClientStart");
        }
    }

    class ClientThread implements Runnable {
        private String hostName;
        private Socket socket;


        public ClientThread(String hostName) {
            this.hostName = hostName;
            socket = new Socket();
            txSocket=socket;
        }
        @Override
        public void run() {
            try {

                Log.d(LTAG, "Trying to connect host: " + hostName);

                handler.post(new updateLcdDisplayThread("   Trying to   " + "\n" + "  connect host " ));
                InetAddress serverAddr = InetAddress.getByName(hostName);
                try {
                    socket.connect(new InetSocketAddress(serverAddr, Integer.parseInt(tcPort.replaceAll("[\\D]",""))), 1000);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    handler.post(new updateLcdDisplayThread("   Connection  " + "\n" + "   timed out!  " ));
                    handler.post(new tcpClientStart());
                    //handler.postDelayed(this, 1000);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.post(new updateLcdDisplayThread("   Connection  " + "\n" + "   timed out!  " ));
                    handler.post(new tcpClientStart());
                    //handler.postDelayed(this, 1000);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    return;
                }


                //readSockThread commThread = new readSockThread(socket);
                //new Thread(commThread).start();
                readSockThread(socket);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            handler.post(new tcpClientStart());
        }
    }


    public static void txCmd(String cmd) {

        Log.d(LTAG, "txCmd");
        try {
            int i;
            int CKS;
            for (i=0, CKS=0;i<cmd.length();i++) CKS+=cmd.charAt(i);
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(txSocket.getOutputStream())),
                    true);
            CKS=CKS%256;
            out.println(String.format("%s%X%X\r", cmd, CKS>>4,CKS&0x0F));
            out.flush();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class updateZoneStatus implements Runnable {
        private String cmd;
        private int i;

        public updateZoneStatus(String str) {
            this.cmd = str;
        }
        @Override
        public void run() {

            int idx;
            String name;

            idx=Integer.parseInt(cmd.substring(3, 6))-1;
            if (idx<64) {

                if (zoneStatus[idx]!=0) {
                    if (cmd.startsWith("609")) {//in�cio de alarme
                        zonesTextViews[idx].setTextColor(Color.parseColor("#FF0000"));
                    } else if (cmd.startsWith("610")) {//fim de alarme
                        zonesTextViews[idx].setTextColor(Color.parseColor("#008000"));
                    }
                }
            }
        }
    }

    class newZoneStatus implements Runnable {
        private String cmd;
        private int i;

        public newZoneStatus(String str) {
            this.cmd = str;
        }
        @Override
        public void run() {

            int idx;
            String name;

            idx=Integer.parseInt(cmd.substring(3, 6))-1;
            name=cmd.substring(6, 37);

            newZoneStatusObject(idx, name);
        }
    }


    void newZoneStatusObject(int idx, String name) {
        float weightSum;

        if ((idx < 64) && (name.startsWith("Zone ") == false) && (zoneStatus[idx] == 0)) {

            if (zoneMainll != null) {
                zoneMainll.removeViewInLayout(findViewById(R.id.zoneslinearLayoutHelpTextView));
                zoneMainll = null;
            }

            zonesTextViews[idx] = new TextView(MainActivity.this);
            zoneStatus[idx] = 1;
            zonesTextViews[idx].setGravity(Gravity.CENTER);
            zonesTextViews[idx].setTextColor(Color.parseColor("#808080"));
            zonesTextViews[idx].setText(name.trim());


            zonell[numZones % 2].removeAllViews();

            weightSum = numZones / 2 + 1;
            zonell[numZones % 2].setWeightSum(weightSum);

            for (i = 0; i < 64; i++) {
                if ((zoneStatus[i] != 0) && (i % 2 == numZones % 2)) {
                    LinearLayout.LayoutParams llparam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
                    llparam.width= ViewGroup.LayoutParams.MATCH_PARENT;
                    llparam.height=0;
                    llparam.weight=1.0f;

                    zonesTextViews[i].setLayoutParams(llparam);
                    zonell[numZones % 2].addView(zonesTextViews[i]);
                }
            }

            numZones++;

            //ver se este idx já está em ficheiro
            Log.d(LTAG, "opening rdscng_zone_pgm_labels file for read");
            BufferedReader inputReader = null;
            boolean zoneExist = false;
            try {
                inputReader = new BufferedReader(new InputStreamReader(openFileInput(zone_pgm_labels_filename)));
                Log.d(LTAG, "OK");
                Log.d(LTAG, "reading rdscng_zone_pgm_labels file");
                String inputString;
                //Reading data line by line and storing it into the stringbuffer
                try {
                    while ((inputString = inputReader.readLine()) != null) {
                        //stringBuffer.append(inputString + "\n");
                        //Log.d(LTAG, "--> " + inputString + " |");
                        Scanner scanIn = new Scanner(inputString);
                        String entryType=scanIn.next();
                        int zoneFileIdx = scanIn.nextInt();

                        if ((entryType.equals("zone"))&&(zoneFileIdx == idx)) {
                            zoneExist = true;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (zoneExist == false) {
                Log.d(LTAG, "opening rdscng_zone_pgm_labels file for write ");
                FileOutputStream zone_pgm_labels_fos;
                try {
                    zone_pgm_labels_fos = openFileOutput(zone_pgm_labels_filename, getApplicationContext().MODE_APPEND);
                    Log.d(LTAG, "OK");

                    String str2file = "zone" + String.format(" %d ", idx) + name.trim().toString() + "\n";
                    try {
                        zone_pgm_labels_fos.write(str2file.getBytes());
                        Log.d(LTAG, "writing |" + str2file + "| to rdscng_zone_pgm_labels file");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        zone_pgm_labels_fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class newCommandOutput implements Runnable {
        private String cmd;
        private int i;

        public newCommandOutput(String str) {
            this.cmd = str;
        }
        @Override
        public void run() {

            int idx;
            String name;

            idx=Integer.parseInt(cmd.substring(3, 6))-120;
            name=cmd.substring(6, 37);

            newCommandOutputObject(idx, name);
        }
    }

    void newCommandOutputObject(int idx, String name) {
        float weightSum;

        if ( (idx<4) && (name.startsWith("Command O/P")==false) && (cmdOPStatus[idx]==0) ) {

            cmdOPButtons[idx] = new Button(MainActivity.this);
            cmdOPStatus[idx]=1;

            cmdOPButtons[idx].setTextColor(Color.parseColor("#808080"));
            cmdOPButtons[idx].setText(name.trim());

            cmdOPll.removeAllViews();

            weightSum=numCmdOP+1;
            cmdOPll.setWeightSum(weightSum);

            for (i=0;i<4;i++) {
                if (cmdOPStatus[i]!=0) {
                    LinearLayout.LayoutParams llparam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
                    llparam.width= ViewGroup.LayoutParams.MATCH_PARENT;
                    llparam.height=0;
                    llparam.weight=1.0f;

                    cmdOPButtons[i].setLayoutParams(llparam);
                    cmdOPll.addView(cmdOPButtons[i]);
                    switch (i) {
                        case 0: cmdOPButtons[i].setId(R.id.cmdOPreservedNamed0); break;
                        case 1: cmdOPButtons[i].setId(R.id.cmdOPreservedNamed1); break;
                        case 2: cmdOPButtons[i].setId(R.id.cmdOPreservedNamed2); break;
                        case 3: cmdOPButtons[i].setId(R.id.cmdOPreservedNamed3); break;
                    }
                    cmdOPButtons[i].setOnTouchListener(bListener);
                }
            }

            numCmdOP++;

            //ver se este idx já está em ficheiro
            Log.d(LTAG, "opening rdscng_zone_pgm_labels file for read");
            BufferedReader inputReader = null;
            boolean pgmExist = false;
            try {
                inputReader = new BufferedReader(new InputStreamReader(openFileInput(zone_pgm_labels_filename)));
                Log.d(LTAG, "OK");
                Log.d(LTAG, "reading rdscng_zone_pgm_labels file");
                String inputString;
                //Reading data line by line and storing it into the stringbuffer
                try {
                    while ((inputString = inputReader.readLine()) != null) {
                        //stringBuffer.append(inputString + "\n");
                        //Log.d(LTAG, "--> " + inputString + " |");
                        Scanner scanIn = new Scanner(inputString);
                        String entryType=scanIn.next();
                        int pgmFileIdx = scanIn.nextInt();

                        if ((entryType.equals("pgm"))&&(pgmFileIdx == idx)) {
                            pgmExist = true;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (pgmExist == false) {
                Log.d(LTAG, "opening rdscng_zone_pgm_labels file for write ");
                FileOutputStream zone_pgm_labels_fos;
                try {
                    zone_pgm_labels_fos = openFileOutput(zone_pgm_labels_filename, getApplicationContext().MODE_APPEND);
                    Log.d(LTAG, "OK");

                    String str2file = "pgm" + String.format(" %d ", idx) + name.trim().toString() + "\n";
                    try {
                        zone_pgm_labels_fos.write(str2file.getBytes());
                        Log.d(LTAG, "writing |" + str2file + "| to rdscng_zone_pgm_labels file");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        zone_pgm_labels_fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendCmdOP(int cmdOPidx, int buttonId) {
        Button button= (Button) findViewById(buttonId);


        switch (cmdOPidx) {
            case 0:
                DialogInterface.OnClickListener dialogClickListener0 = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE: // Yes button clicked
                                txCmd("02011");
                                break;
                            case DialogInterface.BUTTON_NEGATIVE: // No button clicked
                                // do nothing
                            case DialogInterface.BUTTON_NEUTRAL: // No button clicked
                                // do nothing
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder0 = new AlertDialog.Builder(MainActivity.this);
                builder0.setMessage(button.getText() + " ?").setPositiveButton("Yes", dialogClickListener0).setNeutralButton("Cancel", dialogClickListener0).show();
                break;
            case 1:
                DialogInterface.OnClickListener dialogClickListener1 = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE: // Yes button clicked
                                txCmd("02012");
                                break;
                            case DialogInterface.BUTTON_NEGATIVE: // No button clicked
                                // do nothing
                            case DialogInterface.BUTTON_NEUTRAL: // No button clicked
                                // do nothing
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setMessage(button.getText() + " ?").setPositiveButton("Yes", dialogClickListener1).setNeutralButton("Cancel", dialogClickListener1).show();
                break;
            case 2:
                DialogInterface.OnClickListener dialogClickListener2 = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE: // Yes button clicked
                                txCmd("02013");
                                break;
                            case DialogInterface.BUTTON_NEGATIVE: // No button clicked
                                // do nothing
                            case DialogInterface.BUTTON_NEUTRAL: // No button clicked
                                // do nothing
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                builder2.setMessage(button.getText() + " ?").setPositiveButton("Yes", dialogClickListener2).setNeutralButton("Cancel", dialogClickListener2).show();
                break;
            case 3:
                DialogInterface.OnClickListener dialogClickListener3 = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE: // Yes button clicked
                                txCmd("02014");
                                break;
                            case DialogInterface.BUTTON_NEGATIVE: // No button clicked
                                // do nothing
                            case DialogInterface.BUTTON_NEUTRAL: // No button clicked
                                // do nothing
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder3 = new AlertDialog.Builder(MainActivity.this);
                builder3.setMessage(button.getText() + " ?").setPositiveButton("Yes", dialogClickListener3).setNeutralButton("Cancel", dialogClickListener3).show();
                break;
        }
    }

}
