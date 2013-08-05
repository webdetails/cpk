/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk.elements;

import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import pt.webdetails.cpf.http.ICommonParameterProvider;

/**
 *
 * @author Pedro Alves<pedro.alves@webdetails.pt>
 */
public interface IElement {
    
    @JsonProperty("id")
    public String getId();
    
    @JsonProperty("name")
    public String getName();
    
    @JsonIgnore
    public String getLocation();
    
    @JsonProperty("type")
    public String getElementType();
 
    public void processRequest(Map<String, ICommonParameterProvider> parameterProviders);
    
    @JsonProperty("adminOnly")
    public boolean isAdminOnly();
    
    @JsonIgnore
    public ElementInfo getElementInfo();
    
    @JsonProperty("location")
    public String getTopLevel();
    
    
}
