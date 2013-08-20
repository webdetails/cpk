/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cpk;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpk.elements.IElementType;
import pt.webdetails.cpk.security.IAccessControl;

/**
 *
 * @author Luis Paulo Silva<luis.silva@webdetails.pt>
 */
public class Status {
    
    private TreeMap<String,IElement> elementsMap;
    private HashMap<String, IElementType> elementTypesMap;
    private String defaultElementName;
    private List reservedWords;
    private ICpkEnvironment cpkEnv;
    
    private Status(){}
    
    public Status(TreeMap<String,IElement> elementsMap,HashMap<String, IElementType> elementTypesMap, String defaultElementName, List reservedWords, ICpkEnvironment cpkEnv){
        init(elementsMap, elementTypesMap, defaultElementName, reservedWords, cpkEnv);
    }
    
    private void init(TreeMap<String,IElement> elementsMap,HashMap<String, IElementType> elementTypesMap, String defaultElementName, List reservedWords, ICpkEnvironment cpkEnv){
     setCpkEnv(cpkEnv);
     setDefaultElementName(defaultElementName);
     setElementTypesMap(elementTypesMap);
     setElementsMap(elementsMap);
     setReservedWords(reservedWords); 
    }

    public void setCpkEnv(ICpkEnvironment cpkEnv) {
        this.cpkEnv = cpkEnv;
    }
    
    @JsonIgnore
    public String getStatusJson(){
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString(this);
        } catch (IOException ex) {
            Logger.getLogger(Status.class.getName()).log(Level.SEVERE, null, ex);
            json = "{\"error\":\"There was a problem creating the Status JSON\"}";
        }
        
        return json;
    }
    
    @JsonIgnore
    public String getStatus(){
        
        IAccessControl accessControl = cpkEnv.getAccessControl();
        StringBuilder out = new StringBuilder();

        out.append("--------------------------------\n");
        out.append("   ").append(cpkEnv.getPluginName()).append(" Status\n");
        out.append("--------------------------------\n");
        out.append("\n");

        // Show the different entities

        out.append(elementTypesMap.size()).append(" registered entity types\nDefault element: [").append(defaultElementName).append("]\n");
        out.append("\n");
        out.append("End Points\n");

        for (String key : elementsMap.keySet()) {

            IElement myElement = elementsMap.get(key);
            if (accessControl.isAllowed(myElement)) {
                out.append("   [").append(key).append("]: \t").append(myElement.toString()).append("\n\n");
            }

        }

        return out.toString();
    }

    @JsonProperty("pluginName")
    public String getPluginName(){
        return cpkEnv.getPluginName();
    }
    
    @JsonProperty("elements")
    public TreeMap<String, IElement> getElementsMap() {
        return elementsMap;
    }

    @JsonIgnore
    public void setElementsMap(TreeMap<String, IElement> elementsMap) {
        this.elementsMap = elementsMap;
    }

    @JsonProperty("registeredElementTypes")
    public HashMap<String, IElementType> getElementTypesMap() {
        return elementTypesMap;
    }
    
    @JsonProperty("typesCount")
    public int getNrRegisteredTypes(){
        return getElementTypesMap().size();
    }
    
    @JsonProperty("elementsCount")
    public int getNrElements(){
        return getElementsMap().size();
    }

    @JsonIgnore
    public void setElementTypesMap(HashMap<String, IElementType> elementTypesMap) {
        this.elementTypesMap = elementTypesMap;
    }

    @JsonProperty("defaultElement")
    public String getDefaultElementName() {
        return defaultElementName;
    }

    @JsonIgnore
    public void setDefaultElementName(String defaultElementName) {
        this.defaultElementName = defaultElementName;
    }

    @JsonProperty("reservedWords")
    public List getReservedWords() {
        return reservedWords;
    }

    @JsonIgnore
    public void setReservedWords(List reservedWords) {
        this.reservedWords = reservedWords;
    }
    
    
    
    
    @JsonIgnore
    @Override
    public String toString(){
        return null; //TODO
    }
    
}
