/**
 * Copyright (c) 2012, Oliver Kleine, Institute of Telematics, University of Luebeck
 * All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *  - Redistributions of source messageCode must retain the above copyright notice, this list of conditions and the following
 *    disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *  - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 *    products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uzl.itm.ncoap.application;

import de.uzl.itm.ncoap.communication.codec.CoapMessageDecoder;
import de.uzl.itm.ncoap.communication.codec.CoapMessageEncoder;
import de.uzl.itm.ncoap.communication.reliability.OutboundReliabilityHandler;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Abstract base class for pipeline factories for clients, servers and peers.
 *
 * @author Oliver Kleine
 */
public abstract class CoapChannelPipelineFactory implements ChannelPipelineFactory {

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    /**
     * The name of the {@link org.jboss.netty.handler.execution.ExecutionHandler} instance of a CoAP application
     */
    public static final String EXECUTION_HANDLER = "EH";

    /**
     * The name of the {@link de.uzl.itm.ncoap.communication.codec.CoapMessageEncoder} instance
     * of a CoAP application
     */
    public static final String ENCODER = "ENC";

    /**
     * The name of the {@link de.uzl.itm.ncoap.communication.codec.CoapMessageDecoder} instance
     * of a CoAP application
     */
    public static final String DECODER = "DEC";

    /**
     * The name of the {@link de.uzl.itm.ncoap.communication.reliability.OutboundReliabilityHandler} instance
     * of a CoAP application
     */
    public static String OUTBOUND_RELIABILITY_HANDLER = "OutboundReliabilityHandler";

    private Map<String, ChannelHandler> handler;


    protected CoapChannelPipelineFactory(ScheduledExecutorService executor){
         this.handler = new LinkedHashMap<>();

        addChannelHandler(EXECUTION_HANDLER, new ExecutionHandler(executor));

        addChannelHandler(ENCODER, new CoapMessageEncoder());
        addChannelHandler(DECODER, new CoapMessageDecoder());

        addChannelHandler(OUTBOUND_RELIABILITY_HANDLER, new OutboundReliabilityHandler(executor));
     }


    protected void addChannelHandler(String name, ChannelHandler channelHandler){
        this.handler.put(name, channelHandler);
    }


    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();

        for(String handlerName : handler.keySet()){
            pipeline.addLast(handlerName, handler.get(handlerName));
            log.debug("Added Handler to Pipeline: {}.", handlerName);
        }

        return pipeline;
    }

    /**
     * Returns the {@link org.jboss.netty.channel.ChannelHandler} instance which is part of each pipeline created
     * using this factory (or <code>null</code> if no such handler exists). See static constants for the
     * available names.
     *
     * @param name the name of the {@link org.jboss.netty.channel.ChannelHandler instance to be returned}
     *
     * @return the {@link org.jboss.netty.channel.ChannelHandler} instance which is part of each pipeline created
     * using this factory (or <code>null</code> if no such handler exists).
     */
    public ChannelHandler getChannelHandler(String name){
        return handler.get(name);
    }

}
