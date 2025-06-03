package server;

import com.alibaba.fastjson2.JSONObject;
import common.entity.User;
import common.helper.BoardcastHelper;
import lombok.extern.slf4j.Slf4j;
import config.ServerConfig;
import model.gui.Gui;
import model.shape.Shape;


import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Objects;

@Slf4j
public class ServerProcessor {

    private static  ArrayList<Shape> shapeList;
    private static RmiProcessor processor;

    private static ArrayList<User> userList;
    private static ArrayList<String> usernameList;

    public static void run() {

        shapeList = new ArrayList<>();
        userList = new ArrayList<>();
        usernameList = new ArrayList<>();


        //rmi build

        try {

            processor = new RimProcessorImp(shapeList,userList,usernameList);
            //rmi registry
            Registry serverRegistry = LocateRegistry.createRegistry(ServerConfig.serverRMIPort);
            serverRegistry.bind(ServerConfig.serverName, processor);

        }catch (Exception re){
            log.error("Server RMI initialization failed: "+ re.getMessage());
            return;
        }


        //board run
        //manager account create
        userList.add(new User(
                ServerConfig.adminUsername,
                null,
                ServerConfig.serverHost+": "+ServerConfig.serverSocketPort,
                null
                ));
        usernameList.add(ServerConfig.adminUsername);

        Gui.init(shapeList,processor,userList,usernameList);


        //socket build
        ServerSocketHandler.init();

    }

    public static class ServerProcessorHelper {

        //the behavior of the server
        public static JSONObject parseJson(JSONObject object, SocketChannel channel, SelectionKey key){

            String type = (String) object.get("Type");

            switch (type){

                case "join":
                    return judgeJoin(object,channel,key);

                case "clientQuit":
                    String username = (String) object.get("Username");
                    if(Objects.isNull(username) || username.isEmpty())
                        return null;

                    Gui.removeUserByUsername(username);
                    return null;


                default:
                    log.error("Wrong JSON Command!");
                    return null;


            }
        }



        public static JSONObject judgeJoin(JSONObject object,SocketChannel channel,SelectionKey key)  {

            JSONObject obj = new JSONObject();

            String username = (String) object.get("Username");
            //username can not be empty
            if(Objects.isNull(username) || username.isEmpty()){
                obj.put("Type","denyJoin");
                obj.put("Info","username dont exist");
                log.info("User:"+username+" join failed for: "+"username dont exist");
                return obj;
            }
            //judge existed
            if(usernameList.contains(username)){
                obj.put("Type","denyJoin");
                obj.put("Info","username already existed");
                log.info("User:"+username+" join failed for: "+"username already existed");
                return obj;
            }


            // add user
            User userAdded;
            try {
                userAdded = new User(
                        username,
                        channel,
                        channel.getRemoteAddress().toString(),
                        key
                );
            } catch (IOException e) {
                log.error("User address reading failed");

                obj.put("Type","denyJoin");
                obj.put("Info","User address reading failed");
                log.info("User:"+username+" join failed for: "+"User address reading failed");
                return obj;
            }

            //manager judge
            if(!Gui.getINSTANCE().managerJudgeJoin(userAdded.getUsername(),userAdded.getRemoteAddress())){
                obj.put("Type","denyJoin");
                obj.put("Info","The manager rejected your request to join");
                log.info("User:"+username+" join failed for: "+"User address reading failed");
                return obj;
            }

            //accept
            userList.add(userAdded);
            usernameList.add(username);

            //add to user list
            Gui.refreshUserPanel();


            /**
             * problem solved
             */
            obj.put("Type","updateUser");
            obj.put("Users",usernameList);
            log.info("User:"+username+" join success!");

            return obj;


        }


    }

}
