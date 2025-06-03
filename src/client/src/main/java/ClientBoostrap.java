import client.ClientProcessor;
import config.ClientConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientBoostrap {

    public static void main(String[] args) {

        if(!ClientConfig.load(args)) {
            log.error("The input parameters are illegal");
            return;
        }

        ClientProcessor.run();
    }
}
