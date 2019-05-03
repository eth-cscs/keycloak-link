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

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordFormFactory;
import org.keycloak.models.KeycloakSession;

public class IdpUsernamePasswordFormFactory extends UsernamePasswordFormFactory {

    public static final String PROVIDER_ID = "idp-username-password-form-cscs";
    public static final UsernamePasswordForm IDP_SINGLETON = new IdpUsernamePasswordForm();

    @Override
    public Authenticator create(KeycloakSession session) {
        return IDP_SINGLETON;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "Validates login and password.";
    }

    @Override
    public String getDisplayType() {
        return "Username Password Form for identity provider reauthentication (CSCS)";
    }
}
