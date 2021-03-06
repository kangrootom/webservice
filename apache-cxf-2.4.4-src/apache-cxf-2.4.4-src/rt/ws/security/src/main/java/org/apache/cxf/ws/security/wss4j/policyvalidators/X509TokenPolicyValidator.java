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

package org.apache.cxf.ws.security.wss4j.policyvalidators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.cxf.message.Message;
import org.apache.cxf.ws.policy.AssertionInfo;
import org.apache.cxf.ws.policy.AssertionInfoMap;
import org.apache.cxf.ws.security.policy.SP12Constants;
import org.apache.cxf.ws.security.policy.SPConstants;
import org.apache.cxf.ws.security.policy.model.X509Token;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.util.WSSecurityUtil;

/**
 * Validate a WSSecurityEngineResult corresponding to the processing of an X.509 Token
 * against the appropriate policy.
 */
public class X509TokenPolicyValidator extends AbstractTokenPolicyValidator {
    
    private static final String X509_V3_VALUETYPE = WSConstants.X509TOKEN_NS + "#X509v3";
    private static final String PKI_VALUETYPE = WSConstants.X509TOKEN_NS + "#X509PKIPathv1";
    
    private List<WSSecurityEngineResult> bstResults;
    private Message message;

    public X509TokenPolicyValidator(
        Message message,
        List<WSSecurityEngineResult> results
    ) {
        this.message = message;
        bstResults = new ArrayList<WSSecurityEngineResult>();
        WSSecurityUtil.fetchAllActionResults(results, WSConstants.BST, bstResults);
    }
    
    public boolean validatePolicy(
        AssertionInfoMap aim
    ) {
        Collection<AssertionInfo> x509Ais = aim.get(SP12Constants.X509_TOKEN);
        if (x509Ais != null && !x509Ais.isEmpty()) {
            for (AssertionInfo ai : x509Ais) {
                X509Token x509TokenPolicy = (X509Token)ai.getAssertion();
                ai.setAsserted(true);
                
                if (!isTokenRequired(x509TokenPolicy, message)) {
                    continue;
                }
                
                if (bstResults.isEmpty()) {
                    ai.setNotAsserted(
                        "The received token does not match the token inclusion requirement"
                    );
                    return false;
                }
                
                if (!checkTokenType(x509TokenPolicy.getTokenVersionAndType())) {
                    ai.setNotAsserted("An incorrect X.509 Token Type is detected");
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean checkTokenType(String requiredVersionAndType) {
        if (!bstResults.isEmpty()) {
            String requiredType = X509_V3_VALUETYPE;
            if (SPConstants.WSS_X509_PKI_PATH_V1_TOKEN10.equals(requiredType)
                || SPConstants.WSS_X509_PKI_PATH_V1_TOKEN11.equals(requiredType)) {
                requiredType = PKI_VALUETYPE;
            }
            
            for (WSSecurityEngineResult result : bstResults) {
                BinarySecurity binarySecurityToken = 
                    (BinarySecurity)result.get(WSSecurityEngineResult.TAG_BINARY_SECURITY_TOKEN);
                if (binarySecurityToken != null) {
                    String type = binarySecurityToken.getValueType();
                    if (requiredType.equals(type)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
