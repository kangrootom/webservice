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

package org.apache.cxf.alarm_retrieval.server;

import javax.xml.ws.Endpoint;

public class Server {

    protected Server() throws Exception {
        System.out.println("Starting Server");
        
        AlarmRetrievalImpl implementor = new AlarmRetrievalImpl();

        implementor.addAlarm(1);
        implementor.addAlarm(2);
        implementor.addAlarm(3);

        String address = "http://localhost:9091/mtosi/v1/AlarmRetrieval";
        Endpoint.publish(address, implementor);
    }
    
    public static void main(String args[]) throws Exception {
        new Server();
        System.out.println("Server ready...");

        Thread.sleep(5 * 60 * 1000);
        System.out.println("Server exiting... finished running for 5 minutes.");
        System.exit(0);
    }
}
