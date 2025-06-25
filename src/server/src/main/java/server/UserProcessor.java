package server;

import common.entity.User;
import config.ServerConfig;

import java.util.ArrayList;
import java.util.Objects;


public class UserProcessor {

    private final ArrayList<User> userList;
    private final ArrayList<String> usernameList;


    public static UserProcessor INSTANCE = null;

    private UserProcessor(){

        this.userList = new ArrayList<>();
        this.usernameList = new ArrayList<>();

        // add user account
        userList.add(new User(
                ServerConfig.adminUsername,
                null,
                ServerConfig.serverHost+": "+ServerConfig.serverSocketPort,
                null
        ));
        usernameList.add(ServerConfig.adminUsername);
    }

    public static UserProcessor getINSTANCE() {

        if(Objects.isNull(INSTANCE)){
            INSTANCE = new  UserProcessor();
        }

        return INSTANCE;
    }

    public synchronized void addUser(User user){

        if(!usernameList.contains(user.getUsername())){
            usernameList.add(user.getUsername());
            userList.add(user);
        }

    }

    public synchronized void deleteUser(User user){

        if(Objects.isNull(user) || !usernameList.contains(user.getUsername()))
            return;

        userList.remove(user);
        usernameList.remove(user.getUsername());
    }

    public synchronized void deleteUserByUsername(String username){

        if(!usernameList.contains(username))
            return;

        User get = getUserByUsername(username);
        if(!Objects.isNull(get))
            deleteUser(get);
    }


    //Can only be read and cannot be modified
    public synchronized ArrayList<User> getUsers(){
        return  userList;
    }

    public synchronized ArrayList<String> getUsernames(){
        return  usernameList;
    }

    public synchronized User getUserByUsername(String username){

        User get = null;
        for(User user : userList)
            if(user.getUsername().equals(username)){
                get = user;
                break;
            }

        return get;
    }

    public synchronized boolean exists (String username){
        return usernameList.contains(username);
    }


}
