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

package org.apache.cxf.helpers;

import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XPathUtils {
    private XPath xpath;

    public XPathUtils() {
        xpath = XPathFactory.newInstance().newXPath();
    }

    public XPathUtils(final Map<String, String> ns) {
        this();

        if (ns != null) {
            xpath.setNamespaceContext(new MapNamespaceContext(ns));
        }
    }

    public XPathUtils(final NamespaceContext ctx) {
        this();
        xpath.setNamespaceContext(ctx);
    }

    public Object getValue(String xpathExpression, Node node, QName type) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(xpath.getClass().getClassLoader());
            return xpath.evaluate(xpathExpression, node, type);
        } catch (Exception e) {
            return null;
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
    }
    public NodeList getValueList(String xpathExpression, Node node) {
        return (NodeList)getValue(xpathExpression, node, XPathConstants.NODESET);
    }
    public String getValueString(String xpathExpression, Node node) {
        return (String)getValue(xpathExpression, node, XPathConstants.STRING);
    }
    public Node getValueNode(String xpathExpression, Node node) {
        return (Node)getValue(xpathExpression, node, XPathConstants.NODE);
    }

    public boolean isExist(String xpathExpression, Node node, QName type) {
        return getValue(xpathExpression, node, type) != null;
    }

}
