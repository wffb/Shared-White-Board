package client;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import common.ParseHelper;
import lombok.extern.slf4j.Slf4j;
import config.ClientConfig;
import model.gui.Gui;
import model.shape.Shape;
import server.RmiProcessor;

import javax.swing.*;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class ClientProcessor {

    private static ArrayList<Shape> shapeList = new ArrayList<>();
    private static ArrayList<String> usernameList = new ArrayList<>();

    private static RmiProcessor proccessor;

    public static void run()  {

        //socket build
        try {
            ClientSocketHandler.init();

        } catch (Exception e) {
            log.error("client socket init failed: "+e.getMessage());
        }

        //

        //rmi build
        Registry serverRegistry = null;
        try {

            serverRegistry = LocateRegistry.getRegistry(ClientConfig.serverRMIPort);
            proccessor = (RmiProcessor) serverRegistry.lookup(ClientConfig.serverName);

            //get shapes - users
            shapeList.addAll(proccessor.getShapes());
            usernameList.addAll(proccessor.getUsernames());

        } catch (Exception e) {
            log.error("Client RMI initialization failed: "+e.getMessage());
            return;
        }

        //init board
        Gui.init(shapeList,usernameList,proccessor);



    }

    public static class ClientProcessorHandler {

        //the behavior of the client
        public static void parseJson(JSONObject object, SocketChannel channel, SelectionKey key){

            String type = (String) object.get("Type");
            String username;
            switch (type){

                case "addShape":
                    JSONObject shapeObject = (JSONObject) object.get("Shape");
                    Shape newShape = ParseHelper.parseShape(shapeObject);

                    shapeList.add(newShape);
                    Gui.getINSTANCE().repaint();
                    return;

                case "updateUser":
//                    username = (String) object.get("Username");
//                    if(Objects.isNull(username)||username.isEmpty()||usernameList.contains(username))
//                        return;

                    JSONArray jsonArray = object.getJSONArray("Users");

                    ArrayList<String> newList =  jsonArray.stream()
                            .map(Object::toString)
                            .collect(Collectors.toCollection(ArrayList::new));

                    if(Objects.isNull(newList)||newList.isEmpty())
                        return;

                    usernameList = newList ;
                    Gui.setUsers(newList);


                    Gui.refreshUserPanel();
                    log.info(ClientConfig.username+" join server success");
                    return;

                case "addChatMsg":
                    username = (String) object.get("Username");
                    if(Objects.isNull(username)||username.isEmpty()||username.equals(ClientConfig.username))
                        return;

                    Gui.getINSTANCE().addChatMessage(username,(String)object.get("Msg"));
                    return;

                case "denyJoin":
                    String info = (String) object.get("Info");
                    log.error(ClientConfig.username+" join server failed: "+info);
                    closeConnection(channel,key);
                    return;

                case "kick":

                    username = (String) object.get("Username");
                    if(Objects.isNull(username)||username.isEmpty()){
                        log.warn("Cannot operate Null username");
                        return;
                    }

                    usernameList.remove(username);
                    Gui.refreshUserPanel();


                    if(username.equals(ClientConfig.username)){
                        //close connection
                        try {
                            key.cancel();
                            channel.close();
                        } catch (IOException e) {
                            log.error("Connection interruption failed"+e.getMessage());
                        }
                        log.info("Connection has been closed");
                        //show information
                        JOptionPane.showMessageDialog(
                                null,
                                "You have been kicked out by the administrator. You should close the program or try to restart and reconnect",
                                "Kicked Information", JOptionPane.INFORMATION_MESSAGE);

                        System.exit(0);
                    }
                    return;

                case "serverQuit":

                    try {
                        key.cancel();
                        channel.close();
                    } catch (IOException e) {
                        log.error("Connection interruption failed"+e.getMessage());
                    }

                    JOptionPane.showMessageDialog(
                            null,
                            "The server has gone offline. Please quit the client.",
                            "Quit Information", JOptionPane.INFORMATION_MESSAGE);

                    System.exit(0);

                    return;

                default:
                    log.error("Wrong JSON Command!");
            }
        }

    }

    public static void closeConnection(SocketChannel channel, SelectionKey key){
        try {
            channel.close();
        } catch (IOException e) {
            log.error("Connection close failed: "+e.getMessage());
        }
        key.cancel();
    }

    public static void addChatMsg(String username,String msg){

        try {
            proccessor.addChatMsg(username,msg);
        } catch (RemoteException e) {

            log.error("Message add failed: "+e.getMessage());
        }
    }


}
