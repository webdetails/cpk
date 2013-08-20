/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.DocumentException;
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.Router;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.elements.AbstractElement;
import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpk.security.IAccessControl;

/**
 *
 * @author joao
 */
public class CpkCoreService {

    private static final Log logger = LogFactory.getLog(CpkCoreService.class);
    private static final String ENCODING = "UTF-8";
    protected CpkEngine cpkEngine;
    protected ICpkEnvironment cpkEnvironment;

    public CpkCoreService(ICpkEnvironment environment) {
        this.cpkEnvironment = environment;
    }

    private CpkEngine getCpkEngine() {
        if (cpkEngine == null) {
            cpkEngine = CpkEngine.getInstanceWithEnv(cpkEnvironment);
        }

        return cpkEngine;
    }

    public void createContent(Map<String, ICommonParameterProvider> parameterProviders) throws Exception {

        //Make sure the instance is first set so we have pluginUtils
        CpkEngine engine = getCpkEngine();
        IAccessControl accessControl = cpkEnvironment.getAccessControl();

        logger.debug("Creating content");

        // Get the path, remove leading slash
        IPluginUtils pluginUtils = cpkEnvironment.getPluginUtils();
        String path = pluginUtils.getPathParameters(parameterProviders).getStringParameter("path", null);
        IElement element = null;

        if (path == null || path.equals("/")) {
            String url = engine.getDefaultElement().getId().toLowerCase();
            if (path == null) {
                // We need to put the http redirection on the right level
                url = pluginUtils.getPluginName() + "/" + url;
            }
            pluginUtils.redirect(parameterProviders, url);
        }
        if (path != null) {
            element = engine.getElement(path.substring(1).toLowerCase());
        }
        if (element != null) {
            if (accessControl.isAllowed(element)) {
                element.processRequest(parameterProviders);
            } else {
                accessControl.throwAccessDenied(parameterProviders);
            }

        } else {
            logger.debug("Unable to get element: "+path+". This is probably a call to a control CPK operation (reload, status)");
            throw new NoElementException("Unable to get element!");
        }
    }

    // alias to refresh
    public void reload(OutputStream out, Map<String, ICommonParameterProvider> parameterProviders) throws DocumentException, IOException {
        refresh(out, parameterProviders);
    }

    public void refresh(OutputStream out, Map<String, ICommonParameterProvider> parameterProviders) throws DocumentException, IOException {
        IAccessControl accessControl = cpkEnvironment.getAccessControl();
        if (accessControl.isAdmin()) {
            logger.info("Refreshing CPK plugin " + getPluginName());
            getCpkEngine().reload();
            status(out, parameterProviders);
        } else {
            accessControl.throwAccessDenied(parameterProviders);
        }
    }
    
    /**
     * This method runs a Job or Transformation (in this order) if it exists inside the "system" folder of the plugin
     * 
     * @param filename Name of the file without extension
     * @param adminOnly to specify if everyone is supposed to run it
     * @param parameterProviders Required by processRequest of the AbstractElement class
     * @return Returns true if the file exists and there was no problem reading it, false otherwise
     */
    private boolean runSystemKettle(String filename, boolean adminOnly, Map<String, ICommonParameterProvider> parameterProviders){
        boolean success = false;
        AbstractElement element = new AbstractElement();
        element.setAdminOnly(adminOnly);
        element.setElementType("Kettle");
        
        
        try{
            File kettleFile = new File(cpkEnvironment.getPluginUtils().getPluginDirectory().getAbsolutePath()+"/system/"+filename+".kjb");
            
            if(kettleFile.exists()){
                element.setLocation(kettleFile.getAbsolutePath());
            }else{
                kettleFile = new File(cpkEnvironment.getPluginUtils().getPluginDirectory().getAbsolutePath()+"/system/"+filename+".ktr");
                if(kettleFile.exists()){
                    element.setLocation(kettleFile.getAbsolutePath());
                }else{
                    return false;
                }
            }
            element.processRequest(parameterProviders);
            success = true;
        
        }catch(Exception e){}
        
        return success;
    }

    public void status(OutputStream out, Map<String, ICommonParameterProvider> parameterProviders) throws DocumentException, IOException {
        final String key = "status";
        
        boolean success = runSystemKettle(key, false, parameterProviders);
        
        if(!success){
            logger.info("Showing status for CPK plugin " + getPluginName());
            // Only set the headers if we have access to the response (via parameterProviders).
            if (parameterProviders != null) {
                cpkEnvironment.getPluginUtils().setResponseHeaders(parameterProviders, "text/plain");
            }
            writeMessage(out, getCpkEngine().getStatus());
        }
    }
    
    public void statusJson(OutputStream out, Map<String, ICommonParameterProvider> parameterProviders) throws DocumentException, IOException {
        logger.info("Showing status for CPK plugin " + getPluginName());

        // Only set the headers if we have access to the response (via parameterProviders).
        if (parameterProviders != null) {
            cpkEnvironment.getPluginUtils().setResponseHeaders(parameterProviders, "text/plain");
        }
        
        writeMessage(out, getCpkEngine().getStatusJson());
    }

    public boolean hasElement(String elementId) {
        TreeMap<String, IElement> elementsMap = getCpkEngine().getElementsMap();
        return elementsMap.containsKey(elementId.toLowerCase());
    }

    public IElement[] getElements() {
        IElement[] elements = new IElement[]{};
        CpkEngine engine = getCpkEngine();

        if (engine != null) {
            TreeMap<String, IElement> elementsMap = engine.getElementsMap();
            if (elementsMap != null) {
                Collection<IElement> values = elementsMap.values();
                if (values != null) {
                    return values.toArray(elements);
                }
            }
        } else {
            logger.error("cpkEngine is null...");
        }

        return elements;
    }

    public void getElementsList(OutputStream out, Map<String, ICommonParameterProvider> parameterProviders) {
        final String key = "elementsList";
        boolean success = runSystemKettle(key, false, parameterProviders);
        if(!success){
            writeMessage(out, getCpkEngine().getElementsJson());
        }
    }

    public String getPluginName() {
        return cpkEnvironment.getPluginName();
    }

    private void writeMessage(OutputStream out, String message) {
        try {
            out.write(message.getBytes(ENCODING));
        } catch (IOException ex) {
            logger.error("Error writing message", ex);
        }
    }

    public RestRequestHandler getRequestHandler() {
        return Router.getBaseRouter();
    }
}
