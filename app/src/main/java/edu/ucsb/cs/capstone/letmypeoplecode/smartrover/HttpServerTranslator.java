package edu.ucsb.cs.capstone.letmypeoplecode.smartrover;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by dimberman on 2/26/14.
 */
public class HttpServerTranslator {
    String url;
    HttpParams httpParameters = new BasicHttpParams();
    int timeoutSocket = 5000;
    int timeoutConnection = 3000;
    DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

    public HttpServerTranslator(String url) {
        this.url = url;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

    }

//    public JSONObject createJsonRequest(){
//        JSONObject jason = new JSONObject();
//        jason.put()
//    }


    public void parseJson() throws IOException, JSONException {
        StringBuilder str = new StringBuilder();

        HttpGet request = new HttpGet("http://ucsbsmartserver.no-ip.org/rover?key=letmypeoplecode");

        HttpResponse response = httpClient.execute(request);
        BufferedReader rd = new BufferedReader
                (new InputStreamReader(response.getEntity().getContent()));
        String line = "";
        while ((line = rd.readLine()) != null) {
            str.append(line);
        }
        String s = str.toString();
        s = s.replaceAll("\n", "\\n");
        JSONObject json = null;

        try {
            json = new JSONObject(s);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String message = null;
        try {
            message = json.getString("messages");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (message.equals("yes")) {
            JSONArray actions = json.getJSONArray("actions");
            for (int i = 0; i < actions.length(); i++) {
                String action = (String) actions.get(i);
                if (action.equals("left_for")){
                    if( MainActivity.looper.getPwmDriveLeftVal() <= ( MainActivity.looper.getPWM_MAX_VAL() - MainActivity.looper.getPWM_CHANGE_VAL() ) )
                    {
                        MainActivity.looper.setpwmDriveLeftVal(MainActivity.looper.getPwmDriveLeftVal() + MainActivity.looper.getPWM_CHANGE_VAL());
                    }
                }
                if (action.equals("right_for")) {
                    if (MainActivity.looper.getPwmDriveRightVal() >= (MainActivity.looper.getPWM_MIN_VAL() + MainActivity.looper.getPWM_CHANGE_VAL())) {
                        MainActivity.looper.setPwmDriveRightVal(MainActivity.looper.getPwmDriveRightVal() - MainActivity.looper.getPWM_CHANGE_VAL());
                    }
                }
                if (action.equals("left_rev")){

                    if( MainActivity.looper.getpwmDriveLeftVal() >= ( MainActivity.looper.getPWM_MIN_VAL() + MainActivity.looper.getPWM_CHANGE_VAL() ) )
                    {
                        MainActivity.looper.setpwmDriveLeftVal(MainActivity.looper.getPwmDriveLeftVal() - MainActivity.looper.getPWM_CHANGE_VAL());
                    }

                }
                if (action.equals("right_rev")){
                    if( MainActivity.looper.getpwmDriveRightVal() <= ( MainActivity.looper.getPWM_MAX_VAL() - MainActivity.looper.getPWM_MAX_VAL() ) )
                        {
                            MainActivity.looper.setPwmDriveRightVal(MainActivity.looper.getpwmDriveRightVal() + MainActivity.looper.getPWM_CHANGE_VAL());
                        }
                    }
                if (action.equals("led")) {
                    // on -> off and off -> on
                    MainActivity.toggleButton_.setChecked(MainActivity.toggleButton_.isChecked());
                }
                if (action.equals("fork_up")){
                    if( MainActivity.looper.getPwmForkliftVal() <= ( MainActivity.looper.getPWM_MAX_VAL() - MainActivity.looper.getPWM_CHANGE_VAL() ) )
                    {
                        MainActivity.looper.setPwmForkliftVal(MainActivity.looper.getPwmForkliftVal() + MainActivity.looper.getPWM_CHANGE_VAL());
                    }
                }
                if (action.equals("fork_down"))
                    if( MainActivity.looper.getPwmForkliftVal() >= ( MainActivity.looper.getPWM_MIN_VAL() + MainActivity.looper.getPWM_CHANGE_VAL() ) )
                    {
                        MainActivity.looper.setPwmForkliftVal(MainActivity.looper.getPwmForkliftVal() - MainActivity.looper.getPWM_CHANGE_VAL());
                    }
                if (action.equals("mirror_left")){
                    if( MainActivity.looper.getPwmCameraPanVal() <= ( MainActivity.looper.getPWM_MAX_VAL() - MainActivity.looper.getPWM_CHANGE_VAL()) );
                    {
                        MainActivity.looper.setPwmCameraPanVal(MainActivity.looper.getPwmCameraPanVal() + MainActivity.looper.getPwmCameraPanVal());
                    }
                }
                if (action.equals("mirror_right")){
                    if( MainActivity.looper.getPwmCameraPanVal() >= ( MainActivity.looper.getPWM_MIN_VAL() + MainActivity.looper.getPWM_MIN_VAL() ) )
                    {
                        MainActivity.looper.setPwmCameraPanVal(MainActivity.looper.getPwmCameraPanVal() - MainActivity.looper.getPWM_CHANGE_VAL());
                    }
                }


            }

        }


    }


}
