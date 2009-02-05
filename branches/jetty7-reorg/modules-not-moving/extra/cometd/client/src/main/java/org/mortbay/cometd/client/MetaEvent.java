package org.mortbay.cometd.client;

import org.cometd.Message;

public interface MetaEvent
{
    void metaConnect(boolean success);
    void metaDisconnect();
    void metaPublishFail(Message[] messages);
    void metaHandshake(boolean success, boolean reestablish);
    
}
