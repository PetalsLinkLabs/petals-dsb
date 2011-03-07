/**
 * easySchema - easyWSDL toolbox Platform.
 * Copyright (c) 2008,  eBM Websourcing
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ow2.petals.exchange;

import java.util.logging.Logger;

import org.ow2.petals.exchange.api.Exchange;
import org.ow2.petals.exchange.api.ExchangeException;
import org.ow2.petals.exchange.api.ExchangeReader;
import org.ow2.petals.exchange.api.ExchangeWriter;


/**
 * This abstract class defines a factory API that enables applications to obtain
 * a SchemaFactory capable of producing new Definitions, new SchemaReaders, and
 * new SchemaWriters.
 *
 * Some ideas used here have been shamelessly copied from the wonderful JAXP and
 * Xerces work.
 *
 * @author Nicolas Salatge - eBM WebSourcing
 */
public abstract class ExchangeFactory {

    private static Logger log = Logger.getLogger(ExchangeFactory.class.getName());


    private static ExchangeFactory instance = null;

    public static ExchangeFactory getInstance() throws ExchangeException {
    	if(instance == null) {
    		instance = new ExchangeFactoryImpl();
    	}
        return instance;
    }

    public abstract Exchange newExchange() throws ExchangeException;
    

    public abstract ExchangeReader newExchangeReader() throws ExchangeException;


    public abstract ExchangeWriter newExchangeWriter() throws ExchangeException;

}
