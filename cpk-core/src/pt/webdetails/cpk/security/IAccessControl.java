/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cpk.security;

import java.util.Map;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpk.elements.IElement;



public interface IAccessControl {
    public boolean isAllowed(IElement element);

    public boolean isAdmin();
    

    //Bloody stupid name
    public void throwAccessDenied(Map<String,ICommonParameterProvider> parameterProviders);
    
}
