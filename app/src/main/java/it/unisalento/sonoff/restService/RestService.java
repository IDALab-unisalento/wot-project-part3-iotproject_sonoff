package it.unisalento.sonoff.restService;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;

import it.unisalento.sonoff.R;
import it.unisalento.sonoff.model.Credential;
import it.unisalento.sonoff.model.Event;
import it.unisalento.sonoff.model.User;
import it.unisalento.sonoff.utils.ListViewAdapter;
import it.unisalento.sonoff.utils.ToastRunnable;
import it.unisalento.sonoff.view.DashboardActivity;
import it.unisalento.sonoff.view.EventLogActivity;
import it.unisalento.sonoff.view.LoginActivity;
import it.unisalento.sonoff.view.MainActivity;

@SuppressLint({"HardwareIds", "UseSwitchCompatOrMaterialCode"})
public class RestService {
    String address = "http://192.168.1.177:8080";
    //String address = "http://10.3.141.130:8080";
    String clientId;
    Context context;

    public RestService(Context context) {
        this.context=context;
        AndroidNetworking.initialize(context);
        clientId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void getInitialState(MainActivity activity){
        AndroidNetworking.post(address+"/getStatus/"+clientId)
                .setPriority(Priority.LOW)
                .addApplicationJsonBody(activity.getUser())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.w("Rest(getInitialState():", "stato corrente " + response);
                        try {
                            String status = response.getString("status");
                            String touchSensor = response.getString("touchSensor");
                            String pirSensor = response.getString("pirSensor");
                            activity.getToggleButton().setChecked(status.equals("ON"));
                            activity.getProgressDialog().dismiss();

                            if(touchSensor.equals("ON"))
                                activity.getTouchSensorImage().setImageResource(R.drawable.ic_baseline_circle_green);

                            if(pirSensor.equals("ON"))
                                activity.getPirSensorImage().setImageResource(R.drawable.ic_baseline_circle_green);

                            if(!response.isNull("user")) {
                                JSONObject jsonUser = (JSONObject) response.get("user");
                                activity.getUser().setToken(jsonUser.getString("token"));
                                activity.getUser().setRefreshToken(jsonUser.getString("refreshToken"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("Rest (getStatus()):", anError.toString());
                        Log.e("Rest (getStatus()):", anError.getErrorBody());
                        if(anError.getErrorCode()==401){
                            Intent intent = new Intent(activity, LoginActivity.class);
                            activity.finish();
                            activity.startActivity(intent);
                        }
                    }
                });
    }

    public void getTouchSensorState(MainActivity activity){
        AndroidNetworking.post(address+"/getTouchSensorState/"+clientId)
                .setPriority(Priority.LOW)
                .addApplicationJsonBody(activity.getUser())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.w("Rest (getState()):", "stato corrente " + response);
                        try {
                            String status = response.getString("touchSensor");
                            activity.getTvAccess().setVisibility(View.VISIBLE);
                            if(status.equals("OFF")) {
                                activity.getTvAccess().setText(R.string.access_ok);
                                activity.getTvAccess().setTextColor(Color.GREEN);
                            }
                            else if(status.equals("ON")){
                                activity.getTvAccess().setText(R.string.access_deny);
                                activity.getTvAccess().setTextColor(Color.RED);
                            }

                            if(response.isNull("user")){
                                JSONObject jsonUser = (JSONObject) response.get("user");
                                activity.getUser().setToken(jsonUser.getString("token"));
                                activity.getUser().setRefreshToken(jsonUser.getString("refreshToken"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("Rest (getStatus()):", anError.toString());
                        Log.e("Rest (getStatus()):", anError.getErrorBody());
                        if(anError.getErrorCode()==401){
                            Intent intent = new Intent(activity, LoginActivity.class);
                            activity.finish();
                            activity.startActivity(intent);
                        }
                        else {
                            activity.getTvAccess().setVisibility(View.GONE);
                            new ToastRunnable("Qualcosa è andato storto, riprova", 500, context);
                        }
                    }
                });
    }


    public void changeStatusON(MainActivity activity) {
        AndroidNetworking.post(address+"/changeStatusON/"+clientId)
                .setPriority(Priority.LOW)
                .addApplicationJsonBody(activity.getUser())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Rest (changeStatus()):", "status changed" + response);
                        activity.getTvAccess().setText("");
                        activity.getTvAccess().setVisibility(View.GONE);
                        activity.getToggleButton().setChecked(true);
                        try {
                            if(!response.getString("token").equals("null")) {
                                activity.getUser().setToken(response.getString("token"));
                                activity.getUser().setRefreshToken(response.getString("refreshToken"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("Rest (changeStatus()):", anError.toString());
                        if(anError.getErrorCode()==401){
                            Intent intent = new Intent(activity, LoginActivity.class);
                            activity.finish();
                            activity.startActivity(intent);
                        }
                        else{
                            new ToastRunnable("Qualcosa è andato storto, riprova", 500, context);
                        }
                        activity.getToggleButton().setChecked(false);

                    }
                });
    }


    public void changeStatusOFF(MainActivity activity) {
        AndroidNetworking.post(address+"/changeStatusOFF/"+clientId)
                .setPriority(Priority.LOW)
                .addApplicationJsonBody(activity.getUser())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Rest (changeStatus()):", "status changed" + response);
                        activity.getTvAccess().setText("");
                        activity.getTvAccess().setVisibility(View.GONE);
                        activity.getToggleButton().setChecked(false);
                        try {
                            if(!response.getString("token").equals("null")) {
                                activity.getUser().setToken(response.getString("token"));
                                activity.getUser().setRefreshToken(response.getString("refreshToken"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("Rest (changeStatus()):", anError.toString());
                        if(anError.getErrorCode()==401){
                            Intent intent = new Intent(activity, LoginActivity.class);
                            activity.finish();
                            activity.startActivity(intent);
                        }
                        else{
                            new ToastRunnable("Qualcosa è andato storto, riprova", 500, context);
                        }
                        activity.getToggleButton().setChecked(true);

                    }
                });
    }

    public void authentication(LoginActivity activity){
        Credential credential = new Credential(activity.getEtUsername().getText().toString(), activity.getEtPwd().getText().toString());

        AndroidNetworking.post(address+"/auth")
                .setPriority(Priority.LOW)
                .addApplicationJsonBody(credential)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        User user = new User();
                        try {
                            user.setUsername((String) response.get("username"));
                            user.setRole((String) response.get("role"));
                            user.setToken((String) response.get("token"));
                            user.setRefreshToken((String) response.get("refreshToken"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        activity.getProgressDialog().dismiss();
                        Intent intent = new Intent(activity, MainActivity.class);
                        intent.putExtra("user", user);
                        activity.startActivity(intent);
                        activity.finish();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("autentication err:", "onError: ", anError);
                        activity.getTvErLog().setVisibility(View.VISIBLE);
                        activity.getTvErLog().setTextColor(Color.RED);
                        activity.getTvErLog().setText(R.string.wrongUsernemaOrPassword);
                    }
                });
    }

    public void createUser(String username, String password, String role, ProgressDialog progress, DashboardActivity activity) {
        AndroidNetworking.post(address+"/createUser/"+username+"/"+password+"/"+role)
                .setPriority(Priority.LOW)
                .addApplicationJsonBody(activity.getUser())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progress.dismiss();
                        activity.getEtRole().setText("");
                        activity.getEtNewPwd().setText("");
                        activity.getEtNewEmail().setText("");
                        activity.getTvErDash().setText(R.string.operation_completed);
                        activity.getTvErDash().setVisibility(View.VISIBLE);
                        try {
                            if(!response.getString("token").equals("null")) {
                                activity.getUser().setToken((String) response.get("token"));
                                activity.getUser().setRefreshToken((String) response.get("refreshToken"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        activity.getTvErDash().setText(R.string.error);
                        activity.getTvErDash().setVisibility(View.VISIBLE);
                    }
                });
    }

    public void getEventLog(EventLogActivity activity){
        AndroidNetworking.post(address+"/getEventLog")
                .setPriority(Priority.LOW)
                .addApplicationJsonBody(activity.getUser())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Event event;
                        try {
                            JSONArray jsonArray = response.getJSONArray("eventDtoList");
                            for(int i = 0; i<jsonArray.length(); i++){
                                event = new Event();
                                event.setEventType(jsonArray.getJSONObject(i).getString("event_type"));
                                event.setDate(jsonArray.getJSONObject(i).getString("date"));
                                if(!jsonArray.getJSONObject(i).isNull("userDTO"))
                                    event.setUser(jsonArray.getJSONObject(i).getJSONObject("userDTO").getString("username"));
                                activity.getEventList().add(event);
                            }
                            if(!response.isNull("loggedUser")) {
                                activity.getUser().setToken(response.getJSONObject("loggedUser").getString("token"));
                                activity.getUser().setRefreshToken(response.getJSONObject("loggedUser").getString("refreshToken"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Collections.reverse(activity.getEventList());
                        ListViewAdapter listViewAdapter = new ListViewAdapter(activity.getApplicationContext(), activity.getEventList());
                        activity.getListView().setAdapter(listViewAdapter);
                        activity.getCountTextView().setText("N° eventi :"+activity.getEventList().size());
                        activity.getProgressDialog().dismiss();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("Rest (getStatus()):", anError.toString());
                        Log.e("Rest (getStatus()):", anError.getErrorBody());
                    }
                });
    }
}
