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
package org.apache.cxf.jaxrs.features.clustering;

import java.util.List;

import org.apache.cxf.Bus;
import org.apache.cxf.clustering.AbstractStaticFailoverStrategy;
import org.apache.cxf.clustering.FailoverStrategy;
import org.apache.cxf.clustering.FailoverTargetSelector;
import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.endpoint.ConduitSelector;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.jaxrs.client.ClientConfiguration;

/**
 * This feature may be applied to proxy or HTTP-centric clients
 */
@NoJSR250Annotations
public class FailoverFeature extends org.apache.cxf.clustering.FailoverFeature {

    private FailoverTargetSelector customSelector;
    
    @Override
    public void initialize(InterceptorProvider interceptorProvider, Bus bus) {
        initialize((ClientConfiguration)interceptorProvider, bus);
    }
    
    public void initialize(ClientConfiguration client, Bus bus) {
        ConduitSelector selector = initTargetSelector(client.getConduitSelector().getEndpoint());
        client.setConduitSelector(selector);
    }

    @Override
    protected FailoverTargetSelector getTargetSelector() {
        if (customSelector != null) {
            return customSelector;
        } else {
            return super.getTargetSelector();
        }
    }
    
    public void setTargetSelector(FailoverTargetSelector selector) {
        customSelector = selector;
    }
    
    @Override
    public FailoverStrategy getStrategy()  {
        FailoverStrategy strategy = super.getStrategy();
        
        if (strategy == null) {
            throw new IllegalStateException("Default Strategies are not supported");
        }
        
        if (strategy instanceof AbstractStaticFailoverStrategy) {
            List<String> altAdresses = ((AbstractStaticFailoverStrategy)strategy).getAlternateAddresses(null);
            if (altAdresses == null || altAdresses.isEmpty()) {
                throw new IllegalStateException("Strategy is not initialized");
            }
        }
        return strategy;
    }
}
