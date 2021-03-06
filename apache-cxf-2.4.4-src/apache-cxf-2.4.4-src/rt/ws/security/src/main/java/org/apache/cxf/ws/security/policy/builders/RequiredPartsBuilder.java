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
package org.apache.cxf.ws.security.policy.builders;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.apache.cxf.ws.security.policy.SP12Constants;
import org.apache.cxf.ws.security.policy.SPConstants;
import org.apache.cxf.ws.security.policy.model.Header;
import org.apache.cxf.ws.security.policy.model.RequiredParts;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.builders.AssertionBuilder;

public class RequiredPartsBuilder implements AssertionBuilder<Element> {
        
    public Assertion build(Element element, AssertionBuilderFactory factory)
        throws IllegalArgumentException {
        RequiredParts requiredParts = new RequiredParts(SP12Constants.INSTANCE);
        
        Node nd = element.getFirstChild();
        while (nd != null) {
            if (nd instanceof Element) {
                processElement((Element)nd, requiredParts);                
            }
            nd = nd.getNextSibling();
        }

        return requiredParts;
    }
       
    public QName[] getKnownElements() {
        return new QName[] {SP12Constants.REQUIRED_PARTS};
    }

    private void processElement(Element element, RequiredParts parent) {
        if ("Header".equals(element.getLocalName())) {

            String nameAttribute = element.getAttribute(SPConstants.NAME);
            if (nameAttribute == null) {
                nameAttribute = "";
            }

            String namespaceAttribute = element.getAttribute(SPConstants.NAMESPACE);
            parent.addHeader(new Header(nameAttribute, namespaceAttribute));
        }
    }
}
