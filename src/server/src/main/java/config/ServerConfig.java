package config;

public class ServerConfig {

    public static String serverName = "WhiteBoardServer";
    public static Integer serverRMIPort = 1099;

    public static Integer serverSocketPort = 6667;
    public static String serverHost = "localhost";

    public static String adminUsername = "admin";


    public static boolean load(String[] args){

        if (args.length == 3) {
            serverHost = args[0];
            serverSocketPort = Integer.parseInt(args[1]);
            adminUsername = args[2];

            return true;
        }
        return false;
    }

}
