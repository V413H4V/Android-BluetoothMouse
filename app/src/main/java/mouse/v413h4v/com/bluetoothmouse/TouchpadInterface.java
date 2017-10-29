package mouse.v413h4v.com.bluetoothmouse;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.nio.charset.Charset;

import static java.security.AccessController.getContext;


public class TouchpadInterface extends AppCompatActivity {
    private static final String TAG = "TouchpadInterface";
    public static float X = 0f;
    public static float Y = 0f;
    String bytesToSend = "0,0|";

    public static TextView statusText;
    public static String connectionStatus = "";

    public static BluetoothServerConnection btServer;

    int screenWidth = 0;
    int screenHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touchpad_interface);

        statusText = (TextView) findViewById(R.id.statusText);

        screenWidth = getScreenWidth();
        screenHeight = getScreenHeight();

        init();
    }

    private void init() {
//        BluetoothServerConnection btServer = new BluetoothServerConnection(this);
        btServer = new BluetoothServerConnection(this);

        /*while(true){
            statusText.setText(connectionStatus);
            try{
                if(connectionStatus.equalsIgnoreCase("CONNECTED")){
                    bytesToSend = String.valueOf(this.X) + "|" + String.valueOf(this.Y);
                    btServer.write(bytesToSend.getBytes(Charset.defaultCharset()));
                    statusText.setText(bytesToSend);
                }

            }catch (Exception e){
                e.printStackTrace();
                break;
            }

        }*/
    }

    public void sendBytesToPC(MotionEvent event){
        this.X = event.getX();
        this.Y = event.getY();
    }

   /* @Override
    public void onResume(){
        super.onResume();
        init();

    }*/

     @Override
     public boolean onTouchEvent(MotionEvent event) {
         // TODO Auto-generated method stub
         Log.d(TAG,"Touch Event detected..calling sendBytesToPC() method.");


             statusText.setText(connectionStatus);
             try{
                 if(connectionStatus.equalsIgnoreCase("CONNECTED")){
                     /*bytesToSend = String.valueOf(event.getX()) + "|" + String.valueOf(event.getY());
                     btServer.write(bytesToSend.getBytes(Charset.defaultCharset()));
                     statusText.setText(bytesToSend);*/

                     double eventX = ((double) event.getX()) / ((double)screenWidth) - 0.5;
                     double eventY = ((double) event.getY()) / ((double)screenHeight) - 0.5;

                     switch(event.getAction()){
                         case MotionEvent.ACTION_DOWN:
                             sendActionDown(eventX, eventY);
                             break;

                         case MotionEvent.ACTION_MOVE:
                             sendActionMove(eventX, eventY);
                             break;

                         case MotionEvent.ACTION_UP:
                             sendActionUp(eventX, eventY);
                             break;
                     }
                     return true;

                 }

             }catch (Exception e){
                 e.printStackTrace();
                 Log.d(TAG,"Error Occured while sending bytes to PC");
             }


         sendBytesToPC(event);
         return super.onTouchEvent(event);
     }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public void sendActionDown(double x, double y){
        Log.d(TAG, "pressed," + x + "," + y);
        btServer.write("actiondown," + String.format("%.3f", x) + "," + String.format("%.3f", y));
    }

    public void sendActionMove(double x, double y){
        Log.d(TAG, "motion," + x + "," + y);
        btServer.write("actionmove," + String.format("%.3f", x) + "," + String.format("%.3f", y));
    }

    public void sendActionUp(double x, double y){
        Log.d(TAG, "release," + x + "," + y);
        btServer.write("actionup," + String.format("%.3f", x) + "," + String.format("%.3f", y));
    }

    public void pageDownButton(View view){
        Log.d(TAG, "pagedown");
        btServer.write("pagedown");
    }

    public void pageUpButton(View view){
        Log.i(TAG, "pageup");
        btServer.write("pageup");
    }

}
