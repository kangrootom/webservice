/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.transport;

import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.annotation.Resource;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.configuration.spring.MapProvider;

@NoJSR250Annotations(unlessNull = "bus")
public final class DestinationFactoryManagerImpl implements DestinationFactoryManager {

    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(DestinationFactoryManager.class);

    Map<String, DestinationFactory> destinationFactories;
    Set<String> failed = new CopyOnWriteArraySet<String>();
    Set<String> loaded = new CopyOnWriteArraySet<String>();
    Properties factoryNamespaceMappings;

    private Bus bus;

    public DestinationFactoryManagerImpl() {
        destinationFactories = new ConcurrentHashMap<String, DestinationFactory>();
    }
    public DestinationFactoryManagerImpl(Bus b) {
        destinationFactories = new ConcurrentHashMap<String, DestinationFactory>();
        setBus(b);
    }

    public DestinationFactoryManagerImpl(Map<String, DestinationFactory> destinationFactories) {
        this.destinationFactories = destinationFactories;
    }
    public DestinationFactoryManagerImpl(Map<String, DestinationFactory> destinationFactories,
                                         Bus b) {
        this.destinationFactories = destinationFactories;
        setBus(b);
    }
    public DestinationFactoryManagerImpl(MapProvider<String, DestinationFactory> destinationFactories) {
        this.destinationFactories = destinationFactories.createMap();
    }

    public void setMapProvider(MapProvider<String, DestinationFactory> mapProvider) {
        this.destinationFactories = mapProvider.createMap();
    }

    @Resource
    public void setBus(Bus b) {
        bus = b;
        if (null != bus) {
            bus.setExtension(this, DestinationFactoryManager.class);
        }
    }



    /*
     * (non-Javadoc)
     *
     * @see org.apache.cxf.bus.DestinationFactoryManager#registerDestinationFactory(java.lang.String,
     *      org.apache.cxf.transports.DestinationFactory)
     */
    public void registerDestinationFactory(String namespace, DestinationFactory factory) {
        destinationFactories.put(namespace, factory);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.cxf.bus.DestinationFactoryManager#deregisterDestinationFactory(java.lang.String)
     */
    public void deregisterDestinationFactory(String namespace) {
        destinationFactories.remove(namespace);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.cxf.bus.DestinationFactoryManager#DestinationFactory(java.lang.String)
     */
    /**
     * Returns the conduit initiator for the given namespace, constructing it
     * (and storing in the cache for future reference) if necessary, using its
     * list of factory classname to namespace mappings.
     *
     * @param namespace the namespace.
     */
    public DestinationFactory getDestinationFactory(String namespace) throws BusException {
        DestinationFactory factory = destinationFactories.get(namespace);
        if (factory == null && !failed.contains(namespace)) {
            factory = new TransportFinder<DestinationFactory>(bus,
                    destinationFactories,
                    loaded,
                    DestinationFactory.class)
                .findTransportForNamespace(namespace);
        }
        if (factory == null) {
            failed.add(namespace);
            throw new BusException(new Message("NO_DEST_FACTORY", BUNDLE, namespace));
        }
        return factory;
    }

    public DestinationFactory getDestinationFactoryForUri(String uri) {       
        DestinationFactory factory = new TransportFinder<DestinationFactory>(bus,
                destinationFactories,
                loaded,
                DestinationFactory.class).findTransportForURI(uri);
        
        return factory;
    }
    
}
