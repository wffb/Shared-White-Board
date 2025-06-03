package client;

import com.alibaba.fastjson2.JSONObject;
import common.ParseHelper;
import config.ClientConfig;
import lombok.extern.slf4j.Slf4j;
import model.gui.Gui;
import model.shape.Shape;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Objects;

@Slf4j
public class ClientSocketHandler {

    private static ClientSocketHandler INSTANCE;

    //定义相关的属性
    private final String HOST = ClientConfig.serverHost;
    private final int PORT = ClientConfig.serverSocketPort;
    private Selector selector;
    private SocketChannel socketChannel;

    public ClientSocketHandler() throws IOException {

        selector = Selector.open();
        socketChannel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);

        //得到 username
        //username = socketChannel.getLocalAddress().toString().substring(1);
        //System.out.println(username + " is ok...");
    }

    public void sendInfo(JSONObject jsonObject) {
        //info = username + " 说：" + info;
        try {

            socketChannel.write(ByteBuffer.wrap(jsonObject.toString().getBytes()));

        } catch (IOException e) {
            log.info("Client message write failed: "+e.getMessage());
        }
    }

    //Read the messages replied from the server side
    public void readInfo() {
        try {
            int readChannels = selector.select();
            if (readChannels > 0) {
                Iterator< SelectionKey > iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isReadable()) {

                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        sc.read(buffer);


                        //read string from buffer
                        String jsonMsg = new String(buffer.array());
                        //jsonTest
                        JSONObject json = JSONObject.parseObject(jsonMsg);
                        //handle info
                        ClientProcessor.ClientProcessorHandler.parseJson(json,sc,key);

                    }
                    iterator.remove();
                }

            } else {
                log.info("no available channel now");
            }
        } catch (Exception e) {
            log.error("something error when read messages: "+e.getMessage());


        }
    }
    public static void init() throws Exception {

        //start up client
        INSTANCE = new ClientSocketHandler();
        //listen to port
        new Thread() {
            public void run() {
                while (true) {
                    INSTANCE.readInfo();
                    try {
                        Thread.currentThread().sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        //sent join message to the server
        JSONObject joinObject = new JSONObject();
        joinObject.put("Type", "join");
        joinObject.put("Username", ClientConfig.username);
        INSTANCE.sendInfo(joinObject);

    }

    public static void clientQuit(){

        if(Objects.isNull(INSTANCE))
            return;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Type","clientQuit");
        jsonObject.put("Username",ClientConfig.username);

        INSTANCE.sendInfo(jsonObject);
    }



}
