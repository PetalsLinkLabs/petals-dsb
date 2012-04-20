# Petals Distributed Service Bus

Petals Distributed Service Bus is a OW2-Petals Entreprise Service Bus extension which targets ease of use, monitoring, management and Internet-Friendly communication protocols.

## Build
* To build the modules, just launch 'mvn install'
* To also build the distributions and the installer, add -Pdistributions to your maven command 'mvn install -Pdistributions'

## Migration notes

### To Petals 3.2

#### Changes in the DSB code

- *LoggingUtil* has been changed from monolog to Java logger. The API has been broken...
- *org.ow2.petals.jbi.messaging.exchange.MessageExchange* has been replaced by *org.ow2.petals.jbi.messaging.exchange.MessageExchangeImpl*... **Need an API**!!! Update of all references to *org.ow2.petals.jbi.messaging.exchange.MessageExchangeImpl*
- Update *XMLUtil*, *XMLHelper*, *Transformers* with EBM commons utils
- *MessageExchange* does not contains the location anymore. So hard to use it in *org.petalslink.dsb.transport.Adapter*
- Transporter API *sendSync* does not return the message exchange anymore but *void*
- NS error for */org.petalslink.dsb.dsb-kernel/src/main/java/org/petalslink/dsb/kernel/ws/api/MasterConnectionService.java*. NS updated with dsb NS but will broke master integration... Looks like now there is a real connector in the ESB kernel with *org.ow2.petals.ws.DragonConnectionServiceImpl*. cf [https://gist.github.com/1885587](https://gist.github.com/1885587) for error details.
- The CDK does not support *onNotificationMessage* on the listener anymore. The DSB CDK must support it (dsb-wsn-jbise component)
- BC SOAP: Add WSAAddressing endpoint, handle null extensions on outgoing listener.

#### Strange stuff, bad design, etc...

- Need to use interfaces in interfaces when available. It is not the case for MessageExchangeImpl!
- Question : ou sont enregistrés les MBeans qui étés dans le PetalsServerIMpl? : MbeanHelper . Il y a eu un update du logger dedans et du router : Pas fait dans le DSB

#### TODOs

- TODO : implement *org.petalslink.dsb.transport.Adapter.createJBIMessageWrapper(MessageExchange)*
- TODO : Do not use MEP of *MessageExchanheImpl*
- TODO : Check launcher which is not use the same way.
- Need to delete */org.petalslink.dsb.dsb-xmlutils/src/main/java/org/petalslink/dsb/xmlutils/XMLHelper.java* and use direclty utils

#### New but not used
- *org.ow2.petals.jbi.messaging.exchange.PersistedMessageExchangeWrapper* in TransporterNIO
- *RouterMonitor* and *Persistance* service are not used for now in the DSB: has been set to optional in the router module. Note that this should be a router module and not a router dependency!

#### Upgrades to do on the ESB side (will be nice)
- API for MessageExchange!
- PetalsServerImpl registers MBeans with Helper : Not accessible!

#### Disappear
*org.ow2.petals.jbi.messaging.endpoint.EndpointPropertiesService*

#### Deprecated in the DSB

And will be cleaned...

- Petals Oldies: org.petalslink.dsb:dsb-petals-oldies
- Old monitoring: org.petalslink.dsb:dsb-monitoring
