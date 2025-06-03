package common.helper;

import com.alibaba.fastjson2.JSONObject;
import model.shape.Shape;
import server.ServerSocketHandler;

public class BoardcastHelper {

    public static void kickOther(String username){

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Type", "kick");
        jsonObject.put("Username",username);

        ServerSocketHandler.broadcast(jsonObject);
    }

    public static void addUser(String username){

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Type", "addUser");
        jsonObject.put("Username",username);

        ServerSocketHandler.broadcast(jsonObject);

    }

    public static void updateUser(JSONObject obj){

        ServerSocketHandler.broadcast(obj);

    }

    public static void addShape(Shape shape){
        //message
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Type", "addShape");
        jsonObject.put("Shape", shape.toJSONObject());

        //broadcast
        ServerSocketHandler.broadcast(jsonObject);
    }

    public static void addChatMsg(String username,String msg){

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Type", "addChatMsg");
        jsonObject.put("Username", username);
        jsonObject.put("Msg",msg);

        //broadcast
        ServerSocketHandler.broadcast(jsonObject);
    }

    public static void serverQuit(){

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Type", "serverQuit");

        //broadcast
        ServerSocketHandler.broadcast(jsonObject);
    }
}
