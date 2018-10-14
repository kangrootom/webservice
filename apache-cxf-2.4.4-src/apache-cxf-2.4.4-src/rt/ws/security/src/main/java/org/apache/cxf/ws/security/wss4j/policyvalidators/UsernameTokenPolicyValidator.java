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

import java.util.Collection;

import org.apache.cxf.message.Message;
import org.apache.cxf.ws.policy.AssertionInfo;
import org.apache.cxf.ws.policy.AssertionInfoMap;
import org.apache.cxf.ws.security.policy.SP12Constants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.message.token.UsernameToken;

/**
 * Validate a WSSecurityEngineResult corresponding to the processing of a UsernameToken
 * against the appropriate policy.
 */
public class UsernameTokenPolicyValidator extends AbstractTokenPolicyValidator {
    
    private Message message;

    public UsernameTokenPolicyValidator(
        Message message
    ) {
        this.message = message;
    }
    
    public boolean validatePolicy(
        AssertionInfoMap aim,
        WSSecurityEngineResult wser
    ) {
        Collection<AssertionInfo> utAis = aim.get(SP12Constants.USERNAME_TOKEN);
        if (utAis != null && !utAis.isEmpty()) {
            for (AssertionInfo ai : utAis) {
                UsernameToken usernameToken = 
                    (UsernameToken)wser.get(WSSecurityEngineResult.TAG_USERNAME_TOKEN);
                org.apache.cxf.ws.security.policy.model.UsernameToken usernameTokenPolicy = 
                    (org.apache.cxf.ws.security.policy.model.UsernameToken)ai.getAssertion();
                ai.setAsserted(true);
                
                boolean tokenRequired = isTokenRequired(usernameTokenPolicy, message);
                if (tokenRequired && usernameToken == null) {
                    ai.setNotAsserted(
                        "The received token does not match the token inclusion requirement"
                    );
                    return false;
                }
                if (!tokenRequired) {
                    continue;
                }
                
                if (usernameTokenPolicy.isHashPassword() != usernameToken.isHashed()) {
                    ai.setNotAsserted("Password hashing policy not enforced");
                    return false;
                }
                if (usernameTokenPolicy.isNoPassword() && usernameToken.getPassword() != null) {
                    ai.setNotAsserted("Username Token NoPassword policy not enforced");
                    return false;
                }
                if (usernameTokenPolicy.isRequireCreated() 
                    && (usernameToken.getCreated() == null || usernameToken.isHashed())) {
                    ai.setNotAsserted("Username Token Created policy not enforced");
                    return false;
                }
                if (usernameTokenPolicy.isRequireNonce() 
                    && (usernameToken.getNonce() == null || usernameToken.isHashed())) {
                    ai.setNotAsserted("Username Token Nonce policy not enforced");
                    return false;
                }

            }
        }
        return true;
    }
    
}
