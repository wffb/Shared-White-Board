import config.ServerConfig;
import lombok.extern.slf4j.Slf4j;
import server.ServerProcessor;

@Slf4j
public class ServerBoostrap {

    public static void main(String[] args) {

        if(!ServerConfig.load(args)) {
            log.error("The input parameters are illegal");
            return;
        }

        ServerProcessor.run();
    }
}
