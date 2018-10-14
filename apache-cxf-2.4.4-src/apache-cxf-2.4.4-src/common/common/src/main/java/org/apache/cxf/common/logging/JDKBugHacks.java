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

package org.apache.cxf.common.logging;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * This is called from LogUtils as LogUtils is almost always one of the VERY
 * first classes loaded in CXF so we can try and register to hacks/workarounds
 * for various bugs in the JDK.
 * 
 * Much of this is taken from work the Tomcat folks have done to find
 * places where memory leaks and jars are locked and such.
 * See:
 * http://svn.apache.org/viewvc/tomcat/trunk/java/org/apache/catalina/
 * core/JreMemoryLeakPreventionListener.java
 * 
 */
final class JDKBugHacks {
    private JDKBugHacks() {
        //not constructed
    }
    
    public static void doHacks() {
        try {
            ClassLoader orig = Thread.currentThread().getContextClassLoader();
            try {
                // Use the system classloader as the victim for all this
                // ClassLoader pinning we're about to do.
                Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
                
                try {
                    //Trigger a call to sun.awt.AppContext.getAppContext()
                    ImageIO.getCacheDirectory();
                } catch (Throwable t) {
                    //ignore
                }
                try {
                    // Several components end up opening JarURLConnections without first
                    // disabling caching. This effectively locks the file.
                    // JAXB does this and thus affects us pretty badly.
                    // Doesn't matter that this JAR doesn't exist - just as long as
                    // the URL is well-formed
                    URL url = new URL("jar:file://dummy.jar!/");
                    URLConnection uConn = new URLConnection(url) {
                        @Override
                        public void connect() throws IOException {
                            // NOOP
                        }
                    };
                    uConn.setDefaultUseCaches(false);
                } catch (Throwable e) {
                    //ignore
                }
                try {
                    //DocumentBuilderFactory seems to SOMETIMES pin the classloader
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.newDocumentBuilder();
                } catch (Throwable e) {
                    //ignore
                }
                // Several components end up calling:
                // sun.misc.GC.requestLatency(long)
                //
                // Those libraries / components known to trigger memory leaks due to
                // eventual calls to requestLatency(long) are:
                // - javax.management.remote.rmi.RMIConnectorServer.start()
                try {
                    Class<?> clazz = Class.forName("sun.misc.GC");
                    Method method = clazz.getDeclaredMethod("requestLatency",
                            new Class[] {Long.TYPE});
                    method.invoke(null, Long.valueOf(3600000));
                } catch (Throwable e) {
                    //ignore
                }
                
                // Calling getPolicy retains a static reference to the context 
                // class loader.
                try {
                    // Policy.getPolicy();
                    Class<?> policyClass = Class
                        .forName("javax.security.auth.Policy");
                    Method method = policyClass.getMethod("getPolicy");
                    method.invoke(null);
                } catch (Throwable e) {
                    // ignore
                }
                try {
                    // Initializing javax.security.auth.login.Configuration retains a static reference 
                    // to the context class loader.
                    Class.forName("javax.security.auth.login.Configuration", true, 
                                  ClassLoader.getSystemClassLoader());
                } catch (Throwable e) {
                    // Ignore
                }
                // Creating a MessageDigest during web application startup
                // initializes the Java Cryptography Architecture. Under certain
                // conditions this starts a Token poller thread with TCCL equal
                // to the web application class loader.
                java.security.Security.getProviders();
            } finally {
                Thread.currentThread().setContextClassLoader(orig);
            }
        } catch (Throwable t) {
            //ignore
        }
    }

}
