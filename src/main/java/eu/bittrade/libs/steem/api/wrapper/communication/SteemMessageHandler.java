package eu.bittrade.libs.steem.api.wrapper.communication;

import javax.websocket.MessageHandler;

/**
 * Simple MessageHandler implementation.
 * 
 * @author http://steemit.com/@dez1337
 */
public class SteemMessageHandler implements MessageHandler.Whole<String> {
    private String message;
    private CommunicationHandler communicationHandlerInstance;

    public SteemMessageHandler(CommunicationHandler communicationHandlerInstance) {
        this.communicationHandlerInstance = communicationHandlerInstance;
    }

    @Override
    public void onMessage(String message) {
        this.message = message;
        communicationHandlerInstance.countDownLetch();
    }

    public String getMessage() {
        return message;
    }
}
