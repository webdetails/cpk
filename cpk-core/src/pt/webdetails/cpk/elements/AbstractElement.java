/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk.elements;

import java.util.Map;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpk.CpkEngine;

/**
 *
 * @author Pedro Alves<pedro.alves@webdetails.pt>
 */
public class AbstractElement implements IElement {

    private String id;
    private String name;
    private String elementType;
    private String location;
    private ElementInfo elementInfo;
    private  boolean adminOnly;
    private String topLevel;

    @Override
    public String getTopLevel() {
        return topLevel;
    }

    public void setTopLevel(String topLevel) {
        this.topLevel = topLevel;
    }
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getElementType() {
        return elementType;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }
    
    @Override
    public boolean isAdminOnly(){
        return this.adminOnly;
    }
    
    public void setAdminOnly(boolean admin){
        this.adminOnly = admin;
    }
    
    @Override
    public String toString() {
        return "AbstractElement{" + "id=" + getId() + ", name=" + getName() + ", elementType=" + getElementType() + ", location=" + getLocation() + ", adminOnly=" + isAdminOnly() +'}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 11 * hash + (this.elementType != null ? this.elementType.hashCode() : 0);
        hash = 11 * hash + (this.location != null ? this.location.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractElement other = (AbstractElement) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        if ((this.elementType == null) ? (other.elementType != null) : !this.elementType.equals(other.elementType)) {
            return false;
        }
        if ((this.location == null) ? (other.location != null) : !this.location.equals(other.location)) {
            return false;
        }
        return true;
    }

    @Override
    /**
     * Processes the request
     */
    public void processRequest(Map<String, ICommonParameterProvider> parameterProviders) {
        // Get the elementType and process it
        CpkEngine.getInstance().getElementType(this.getElementType()).processRequest(parameterProviders, this);        
    }

    @Override
    public ElementInfo getElementInfo() {
        return elementInfo;
    }

    public void setElementInfo(ElementInfo elementInfo) {
        this.elementInfo = elementInfo;
    }
    
}
