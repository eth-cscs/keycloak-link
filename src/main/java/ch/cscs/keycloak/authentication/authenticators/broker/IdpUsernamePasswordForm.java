/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.cscs.keycloak.authentication.authenticators.broker;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.keycloak.forms.login.freemarker.model.LoginBean;

/**
 * Same like classic username+password form, but for use in IdP linking.
 * 
 * IdP/social buttons are hidden, and linking confirmation message is shown.
 * User identity is optionally established e.g. by preceding idp-create-user-if-unique execution.
 * If no identity had been established, the user will be prompted to enter login name.
 */
public class IdpUsernamePasswordForm extends UsernamePasswordForm {

    private static final String FEDERATED_IDENTITY_CONFIRM_REAUTHENTICATE_NO_USER_MESSAGE = "federatedIdentityConfirmReauthenticateNoUserMessage";
    
    @Override
    protected Response challenge(AuthenticationFlowContext context, String error) {
        LoginFormsProvider form = context.form();
        if (error != null) {
            form.setError(error);
        }

        return form.createForm("login-idp.ftl");
    }

    @Override
    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        
        UserModel existingUser = null;
                
        try {
            existingUser = AbstractIdpAuthenticator.getExistingUser(context.getSession(), context.getRealm(), context.getAuthenticationSession());
        } catch(AuthenticationFlowException ex) {
            log.debugf(ex, "No existing user in authSession");
        }

        return setupForm(context, formData, existingUser)
                .setStatus(Response.Status.OK)
                .createForm("login-idp.ftl");
        
    }

    @Override
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        try {
            UserModel existingUser = AbstractIdpAuthenticator.getExistingUser(context.getSession(), context.getRealm(), context.getAuthenticationSession());
            context.setUser(existingUser);

            // Restore formData for the case of error
            setupForm(context, formData, existingUser);
            
            return validatePassword(context, existingUser, formData);
        } catch (AuthenticationFlowException ex) {
            return validateUserAndPassword(context, formData);
        }
    }

    protected LoginFormsProvider setupForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData, UserModel existingUser) {
        SerializedBrokeredIdentityContext serializedCtx = SerializedBrokeredIdentityContext.readFromAuthenticationSession(context.getAuthenticationSession(), AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);
        if (serializedCtx == null) {
            throw new AuthenticationFlowException("Not found serialized context in clientSession", AuthenticationFlowError.IDENTITY_PROVIDER_ERROR);
        }
        
        String message;
        Object[] args;
        
        if (existingUser != null) {
            System.out.println(existingUser.getUsername());
            formData.add(AuthenticationManager.FORM_USERNAME, existingUser.getUsername());
            System.out.println(formData.get(AuthenticationManager.FORM_USERNAME));
            message = Messages.FEDERATED_IDENTITY_CONFIRM_REAUTHENTICATE_MESSAGE;
            args = new Object[] { existingUser.getUsername(), serializedCtx.getIdentityProviderId() };
        } else {  
            message = FEDERATED_IDENTITY_CONFIRM_REAUTHENTICATE_NO_USER_MESSAGE;
            args = new Object[] { serializedCtx.getIdentityProviderId() };
        }

        LoginFormsProvider form = context.form()
                .setFormData(formData)
                .setAttribute("login", new LoginBean(formData))
                .setInfo(message, args);
        
        if (existingUser != null)
            form.setAttribute(LoginFormsProvider.USERNAME_EDIT_DISABLED, true);
        
        return form;
        
    }

}
