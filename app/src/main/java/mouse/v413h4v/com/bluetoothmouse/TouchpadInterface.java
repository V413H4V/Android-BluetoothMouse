package mouse.v413h4v.com.bluetoothmouse;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import java.nio.charset.Charset;


public class TouchpadInterface extends AppCompatActivity {
    private static final String TAG = "TouchpadInterface";
    public static float X = 0f;
    public static float Y = 0f;
    String bytesToSend = "0|0";

    public static TextView statusText;
    public static String connectionStatus = "";

    public static BluetoothServerConnection btServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touchpad_interface);

        statusText = (TextView) findViewById(R.id.statusText);
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
                     bytesToSend = String.valueOf(event.getX()) + "|" + String.valueOf(event.getY());
                     btServer.write(bytesToSend.getBytes(Charset.defaultCharset()));
                     statusText.setText(bytesToSend);
                 }

             }catch (Exception e){
                 e.printStackTrace();
                 Log.d(TAG,"Error Occured while sending bytes to PC");
             }


         sendBytesToPC(event);
         return super.onTouchEvent(event);
     }

}
