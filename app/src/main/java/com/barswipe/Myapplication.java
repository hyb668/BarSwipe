package com.barswipe;

import android.app.Application;
import android.provider.SyncStateContract;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.parse.Parse;
import com.parse.ParseInstallation;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by SoLi on 2015/12/11.
 */
public class Myapplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, "P8e4kwLZzMHeBl2ncep08igOTtfFDLkkGZuuMFZH", "0aJaQwEphVX7AyJauAb9g31BSBRXyX8etjR5KVWa");
        ParseInstallation.getCurrentInstallation().saveInBackground();
        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
//        Localytics.registerPush(SyncStateContract.Constants.LOCALYTICS_SENDER_ID);

        JSONObject object = new JSONObject();
        object.put("UserToken","sssssdsdwqeewewe");
        object.put("KeyID","list");
        object.put("Total",2);
        object.put("UseLogs", getRecordData(""));
        Log.e("json1", JSON.toJSONString(object));

        object.put("UseLogs", getRecordDatdsa());
        object.put("KeyID", "Array");
        Log.e("json2",JSON.toJSONString(object));
    }

    private  DoorRecordBean[] getRecordDatdsa()
    {
        int length = 2;
        DoorRecordBean[] temp = new DoorRecordBean[length];
        while (length > 0)
        {
            temp[length - 1] = new DoorRecordBean();
            temp[length - 1].setOrderID(15152312);
            temp[length - 1].setDoorID(1526523);
            temp[length - 1].setOpenDoorTime("2015-15-12 12:3" + length);
            length --;
        }
        return temp;
    }

    private List<DoorRecordBean> getRecordData(String data)
    {
        List<DoorRecordBean> temp = new ArrayList<>();
        int length = 3;
        while (length > 0)
        {
            DoorRecordBean child = new DoorRecordBean();
            child.setOrderID(152312);
            child.setDoorID(1526523);
            child.setOpenDoorTime("2015-15-12 12:3" + length);
            temp.add(child);
            length --;
        }
        return temp;
    }
}
