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
package org.apache.cxf.configuration.jsse.spring;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamReader;


import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.common.util.PackageUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.TLSClientParametersType;
import org.apache.cxf.jaxb.JAXBContextCache;
import org.apache.cxf.jaxb.JAXBContextCache.CachedContextAndSchemas;
import org.apache.cxf.staxutils.StaxUtils;

/**
 * This class provides the TLSClientParameters that programmatically
 * configure a HTTPConduit. It is initialized with the JAXB
 * type TLSClientParametersType that was used in the Spring configuration
 * of the http-conduit bean.
 */
@NoJSR250Annotations
public final class TLSClientParametersConfig {
    private static Set<Class<?>> classes;
    private static JAXBContext context;
    
    private TLSClientParametersConfig() {
        //not constructed
    }
    
    private static synchronized JAXBContext getContext() throws JAXBException {
        if (context == null || classes == null) {
            Set<Class<?>> c2 = new HashSet<Class<?>>();
            JAXBContextCache.addPackage(c2, 
                                        PackageUtils.getPackageName(TLSClientParametersType.class), 
                                        TLSClientParametersConfig.class.getClassLoader());
            CachedContextAndSchemas ccs 
                = JAXBContextCache.getCachedContextAndSchemas(c2, null, null, null, false);
            classes = ccs.getClasses();
            context = ccs.getContext();
        }
        return context;
    }

    static TLSClientParameters createTLSClientParametersFromType(TLSClientParametersType params) 
        throws GeneralSecurityException,
               IOException {

        TLSClientParameters ret = new TLSClientParameters(); 
        boolean usingDefaults = params.isUseHttpsURLConnectionDefaultSslSocketFactory();
        
        if (params.isDisableCNCheck()) {
            ret.setDisableCNCheck(true);
        }
        if (params.isUseHttpsURLConnectionDefaultHostnameVerifier()) {
            ret.setUseHttpsURLConnectionDefaultHostnameVerifier(true);
        }
        if (params.isUseHttpsURLConnectionDefaultSslSocketFactory()) {
            ret.setUseHttpsURLConnectionDefaultSslSocketFactory(true);
        }
        if (params.isSetSecureSocketProtocol()) {
            ret.setSecureSocketProtocol(params.getSecureSocketProtocol());
        }
        if (params.isSetCipherSuitesFilter()) {
            ret.setCipherSuitesFilter(params.getCipherSuitesFilter());
        }
        if (params.isSetCipherSuites()) {
            ret.setCipherSuites(params.getCipherSuites().getCipherSuite());
        }
        if (params.isSetJsseProvider()) {
            ret.setJsseProvider(params.getJsseProvider());
        }
        if (params.isSetSecureRandomParameters() && !usingDefaults) {
            ret.setSecureRandom(
                TLSParameterJaxBUtils.getSecureRandom(
                        params.getSecureRandomParameters()));
        }
        if (params.isSetKeyManagers() && !usingDefaults) {
            ret.setKeyManagers(
                TLSParameterJaxBUtils.getKeyManagers(params.getKeyManagers()));
        }
        if (params.isSetTrustManagers() && !usingDefaults) {
            ret.setTrustManagers(
                TLSParameterJaxBUtils.getTrustManagers(
                        params.getTrustManagers()));
        }
        if (params.isSetCertConstraints()) {
            ret.setCertConstraints(params.getCertConstraints());
        }
        if (params.isSetSslCacheTimeout()) {
            ret.setSslCacheTimeout(params.getSslCacheTimeout());
        }
        return ret;
    }
    


    public static Object createTLSClientParameters(String s) {
        
        StringReader reader = new StringReader(s);
        XMLStreamReader data = StaxUtils.createXMLStreamReader(reader);
        Unmarshaller u;
        try {
            u = getContext().createUnmarshaller();
            Object obj = u.unmarshal(data, TLSClientParametersType.class);
            if (obj instanceof JAXBElement<?>) {
                JAXBElement<?> el = (JAXBElement<?>)obj;
                obj = el.getValue();

            }
            
            TLSClientParametersType cpt = (TLSClientParametersType)obj;
            return createTLSClientParametersFromType(cpt);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
