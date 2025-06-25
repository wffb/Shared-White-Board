package server;

import com.alibaba.fastjson2.JSONObject;
import common.entity.User;
import common.helper.BoardcastHelper;
import config.ServerConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Objects;

@Slf4j
public class ServerSocketHandler {


    public static ServerSocketHandler INSTANCE;

    private Selector selector;
    private ServerSocketChannel listenChannel;
    private static final int PORT = ServerConfig.serverSocketPort;

    //Initialization
    public ServerSocketHandler() {
        try {

            //get selector
            selector = Selector.open();
            //ServerSocketChannel
            listenChannel = ServerSocketChannel.open();
            //blind port
            listenChannel.socket().bind(new InetSocketAddress(PORT));

            //Set the non-blocking mode
            listenChannel.configureBlocking(false);
            //Register the Channel to the Selector and specify the type of event of interest.
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            //out panel

            log.error("Socket build failed； "+e.getMessage());
        }
    }
    public void listen() {
        try {
            //It is used to wait for the ready events of one or more NIO channels (such as SocketChannel, ServerSocketChannel, DatagramChannel or FileChannel) in a non-blocking manner
            while (true) {

                //This method will block until at least one registered channel is ready, or the current thread is interrupted, or the selector is closed. If the method returns, it will return an integer representing the number of channels that are ready
                int count = selector.select();
                if (count > 0) {
                    //Traverse to obtain the set of SelectionKeys, where selectionkeys represent the ready channels and their events
                    Iterator< SelectionKey > iterator = selector.selectedKeys().iterator();

                    while (iterator.hasNext()) {
                        //SelectionKey is a class in the Java NIO library, which represents the Key of a Channel registered on the Selector. Each SelectionKey is associated with a specific Selector
                        SelectionKey key = iterator.next();
                        //Listen to accept. If the channel is ServerSocketChannel and is ready to receive new connections, return true
                        if (key.isAcceptable()) {
                            //Create a Channel containing the new connection
                            SocketChannel sc = listenChannel.accept();
                            sc.configureBlocking(false);
                            //Register this sc to seletor
                            sc.register(selector, SelectionKey.OP_READ);

                            log.info(sc.getRemoteAddress() + " online");
                        }
                        if (key.isReadable()) {
                            //The channel sends the read event, that is, the channel is in a readable state. If the channel is readable, it returns true.
                            //read Handler
                            readData(key);
                        }
                        //Delete the current key to prevent duplicate processing
                        iterator.remove();
                    }


                } else {
                    log.info("server socket is waiting");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //read data from client
    public void readData(SelectionKey key) {

        SocketChannel channel = null;
        try {
            //get channel
            channel = (SocketChannel) key.channel();
            //create buffer
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            //number of byte get from buffer。
            int count = channel.read(buffer);
            if (count > 0) {
                //read string from buffer
                String jsonMsg = new String(buffer.array());
                //jsonTest
                JSONObject json = JSONObject.parseObject(jsonMsg);

                /**
                 * could be thread pool here
                 */
                //handle info
                handleMsg(json,channel,key);

            }
        } catch (IOException e) {
            try {
                log.info(channel.getRemoteAddress() + " outline");
                //Cancel registration
                key.cancel();
                //Close channel
                channel.close();

            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    private synchronized void handleMsg(JSONObject jsonObject,SocketChannel channel,SelectionKey key) throws IOException {

        JSONObject res = ServerProcessor.ServerProcessorHelper.parseJson(jsonObject,channel,key);
        if(Objects.isNull(res))
            return;



        //feedback
        switch ((String) res.get("Type")){

            case "updateUser":
                //BoardcastHelper.addUser((String) res.get("Username"));
                BoardcastHelper.updateUser(res);
                return;

            case "denyJoin":
                sendInfo(res,channel);
                throw new IOException("Connection denied");

        }

    }


    //Broadcast the message to other channel
    private synchronized void sendInfoToAllClients(JSONObject json) throws IOException {

        log.info("Server is broadcasting");

        for (SelectionKey key: selector.keys()) {
            //Retrieve the corresponding SocketChannel through the key
            Channel targetChannel = key.channel();

            if (targetChannel instanceof SocketChannel) {

                SocketChannel dest = (SocketChannel) targetChannel;
                //Store msg in buffer
                ByteBuffer buffer = ByteBuffer.wrap(json.toString().getBytes());
                //Write the data of the buffer to the channel
                dest.write(buffer);
            }
        }
    }

    /**
     * could be Encrypted here
     */
    private void sendInfo(JSONObject jsonObject,SocketChannel socketChannel) {
        try {

            socketChannel.write(ByteBuffer.wrap(jsonObject.toString().getBytes()));

        } catch (IOException e) {
            log.info("Server message write failed: "+e.getMessage());
        }
    }

    public static void init() {
        //Create a server object
        INSTANCE = new ServerSocketHandler();
        INSTANCE.listen();
    }

    public static void broadcast(JSONObject json)  {

        if(Objects.isNull(INSTANCE))
            return;

        try {
            INSTANCE.sendInfoToAllClients(json);
        } catch (IOException e) {
            log.error("Server broadcast info failed:"+e.getMessage());
        }
    }






}
