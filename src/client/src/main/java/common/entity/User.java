package common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@AllArgsConstructor
@Getter
public class User {

    private final String username;
    private final SocketChannel channel;
    private final String RemoteAddress;
    private final SelectionKey key;

}
