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

package org.apache.cxf.ws.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyContainingAssertion;

/**
 * 
 */
public class EffectivePolicyImpl implements EffectivePolicy {
    
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(EffectivePolicyImpl.class); 
    private static final Logger LOG = LogUtils.getL7dLogger(EffectivePolicyImpl.class);
    
    protected Policy policy;     
    protected Collection<Assertion> chosenAlternative;
    protected List<Interceptor<? extends org.apache.cxf.message.Message>> interceptors;
    
    public Policy getPolicy() {
        return policy;        
    }
    
    public List<Interceptor<? extends org.apache.cxf.message.Message>> getInterceptors() {
        return interceptors;
    }
    
    public Collection<Assertion> getChosenAlternative() {
        return chosenAlternative;
    }
    
    
    void initialise(EndpointPolicyImpl epi, PolicyEngineImpl engine, boolean inbound) {
        policy = epi.getPolicy();
        chosenAlternative = epi.getChosenAlternative();
        if (chosenAlternative == null) {
            chooseAlternative(engine, null);
        }
        initialiseInterceptors(engine, inbound);  
    }
    
    void initialise(EndpointInfo ei, 
                    BindingOperationInfo boi, 
                    PolicyEngineImpl engine, 
                    Assertor assertor,
                    boolean requestor, boolean request) {
        initialisePolicy(ei, boi, engine, requestor, request, assertor);
        chooseAlternative(engine, assertor);
        initialiseInterceptors(engine, false);  
    }
    void initialise(EndpointInfo ei, 
                    BindingOperationInfo boi, 
                    PolicyEngineImpl engine, 
                    boolean requestor, boolean request) {
        Assertor assertor = initialisePolicy(ei, boi, engine, requestor, request, null);
        chooseAlternative(engine, assertor);
        initialiseInterceptors(engine, requestor);  
    }
    
    void initialise(EndpointInfo ei, 
                    BindingOperationInfo boi, 
                    BindingFaultInfo bfi, 
                    PolicyEngineImpl engine, 
                    Assertor assertor) {
        initialisePolicy(ei, boi, bfi, engine);
        chooseAlternative(engine, assertor);
        initialiseInterceptors(engine, false);  
    }
     
    Assertor initialisePolicy(EndpointInfo ei,
                          BindingOperationInfo boi,  
                          PolicyEngineImpl engine, 
                          boolean requestor, boolean request,
                          Assertor assertor) {
        
        if (boi.isUnwrapped()) {
            boi = boi.getUnwrappedOperation();
        }
        
        BindingMessageInfo bmi = request ? boi.getInput() : boi.getOutput();
        EndpointPolicy ep;
        if (requestor) {
            ep = engine.getClientEndpointPolicy(ei, (Conduit)assertor);
        } else {
            ep = engine.getServerEndpointPolicy(ei, (Destination)assertor);
        }
        policy = ep.getPolicy();
        if (ep instanceof EndpointPolicyImpl) {
            assertor = ((EndpointPolicyImpl)ep).getAssertor();
        }
        
        policy = policy.merge(engine.getAggregatedOperationPolicy(boi));
        if (null != bmi) {
            policy = policy.merge(engine.getAggregatedMessagePolicy(bmi));
        }
        policy = (Policy)policy.normalize(engine.getRegistry(), true);
        return assertor;
    }
    
    void initialisePolicy(EndpointInfo ei, BindingOperationInfo boi,
                          BindingFaultInfo bfi, PolicyEngineImpl engine) {
        policy = engine.getServerEndpointPolicy(ei, (Destination)null).getPolicy();         
        policy = policy.merge(engine.getAggregatedOperationPolicy(boi));
        if (bfi != null) {
            policy = policy.merge(engine.getAggregatedFaultPolicy(bfi));
        }
        policy = (Policy)policy.normalize(engine.getRegistry(), true);
    }

    void chooseAlternative(PolicyEngineImpl engine, Assertor assertor) {
        Collection<Assertion> alternative = engine.getAlternativeSelector()
            .selectAlternative(policy, engine, assertor);
        if (null == alternative) {
            PolicyUtils.logPolicy(LOG, Level.FINE, "No alternative supported.", getPolicy());
            throw new PolicyException(new Message("NO_ALTERNATIVE_EXC", BUNDLE));
        } else {
            setChosenAlternative(alternative);
        }   
    }

    void initialiseInterceptors(PolicyEngineImpl engine) {
        initialiseInterceptors(engine, false);
    }
    void initialiseInterceptors(PolicyEngineImpl engine, boolean useIn) {
        if (engine.getBus() != null) {
            PolicyInterceptorProviderRegistry reg 
                = engine.getBus().getExtension(PolicyInterceptorProviderRegistry.class);
            Set<Interceptor<? extends org.apache.cxf.message.Message>> out 
                = new LinkedHashSet<Interceptor<? extends org.apache.cxf.message.Message>>();
            for (Assertion a : getChosenAlternative()) {
                initialiseInterceptors(reg, engine, out, a, useIn);
            }        
            setInterceptors(new ArrayList<Interceptor<? extends  org.apache.cxf.message.Message>>(out));
        }
    }
    
    
    protected Collection<Assertion> getSupportedAlternatives(PolicyEngineImpl engine,
                                                                   Policy p) {
        Collection<Assertion> alternatives = new ArrayList<Assertion>();

        for (Iterator<List<Assertion>> it = p.getAlternatives(); it.hasNext();) {
            List<Assertion> alternative = it.next();
            if (engine.supportsAlternative(alternative, null)) {
                alternatives.addAll(alternative);
            }
        }
        return alternatives;
    }

    void initialiseInterceptors(PolicyInterceptorProviderRegistry reg,
                                PolicyEngineImpl engine,
                                Set<Interceptor<? extends org.apache.cxf.message.Message>> out,
                                Assertion a,
                                boolean usIn) {
        QName qn = a.getName();
        PolicyInterceptorProvider pp = reg.get(qn);
        if (null != pp) {
            out.addAll(usIn ? pp.getInInterceptors() : pp.getOutInterceptors());
        }
        if (a instanceof PolicyContainingAssertion) {
            Policy p = ((PolicyContainingAssertion)a).getPolicy();
            if (p != null) {
                for (Assertion a2 : getSupportedAlternatives(engine, p)) {
                    initialiseInterceptors(reg, engine, out, a2, usIn);
                }
            }
        }
    }
    
    // for tests
    
    void setPolicy(Policy ep) {
        policy = ep;
    }
    
    void setChosenAlternative(Collection<Assertion> c) {
        chosenAlternative = c;
    }
    
    void setInterceptors(List<Interceptor<? extends org.apache.cxf.message.Message>> out) {
        interceptors = out;
    }
   
}
