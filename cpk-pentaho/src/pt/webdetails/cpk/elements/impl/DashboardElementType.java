
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk.elements.impl;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletRequest;
import org.apache.commons.lang.StringUtils;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpf.utils.PluginUtils;
import pt.webdetails.cpk.elements.AbstractElementType;
import pt.webdetails.cpk.elements.ElementInfo;
import pt.webdetails.cpk.elements.IElement;


import java.io.*;
import java.util.HashMap;
import pt.webdetails.cpf.utils.IPluginUtils;

/**
 *
 * @author Pedro Alves<pedro.alves@webdetails.pt>
 */
public class DashboardElementType extends AbstractElementType {

    private IPluginUtils pluginUtils;
    public DashboardElementType(IPluginUtils plug) {
        super(plug);
        pluginUtils=plug;
    }

    @Override
    public String getType() {
        return "Dashboard";
    }

    @Override
    public void processRequest(Map<String, ICommonParameterProvider> parameterProviders, IElement element) {
        try {
            // element = (DashboardElement) element;
            callCDE(parameterProviders, element);
        } catch (Exception ex) {
            logger.error("Error while calling CDE: "+ Util.getExceptionDescription(ex));
        }    
    }

    protected void callCDE(Map<String, ICommonParameterProvider> parameterProviders, IElement element) throws UnsupportedEncodingException, IOException {

        

        
        String path = pluginUtils.getPluginRelativeDirectory(element.getLocation(), true);
        
        ServletRequest wrapper = ((PluginUtils) pluginUtils).getRequest(parameterProviders);
        OutputStream out = ((PluginUtils) pluginUtils).getResponseOutputStream(parameterProviders);

        String root = wrapper.getScheme() + "://" + wrapper.getServerName() + ":" + wrapper.getServerPort();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("solution", "system");
        params.put("path", path);
        params.put("file", element.getId() + ".wcdf");
        params.put("absolute", "true");
        params.put("inferScheme", "false");
        params.put("root", root);
        ICommonParameterProvider requestParams = pluginUtils.getRequestParameters(parameterProviders);
        PluginUtils.copyParametersFromProvider(params, requestParams);

        if (requestParams.hasParameter("mode") && requestParams.getStringParameter("mode", "Render").equals("edit")) {
            redirectToCdeEditor(parameterProviders, params);
            return;
        }

        InterPluginCall pluginCall = new InterPluginCall(InterPluginCall.CDE, "Render", params);
        pluginCall.setResponse(((PluginUtils)pluginUtils).getResponse(parameterProviders));
        pluginCall.setOutputStream(out);
        pluginCall.run();
    }

    private void redirectToCdeEditor(Map<String, ICommonParameterProvider> parameterProviders, Map<String, Object> params) throws IOException {

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("../pentaho-cdf-dd/edit");
        if (params.size() > 0) {
            urlBuilder.append("?");
        }

        List<String> paramArray = new ArrayList<String>();
        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value instanceof String) {
                paramArray.add(key + "=" + URLEncoder.encode((String) value, "utf-8"));
            }
        }

        urlBuilder.append(StringUtils.join(paramArray, "&"));
        pluginUtils.redirect(parameterProviders, urlBuilder.toString());
    }

    
    @Override
    protected ElementInfo createElementInfo() {
        return new ElementInfo("text/html");
    }

    @Override
    public boolean isShowInSitemap() {
        return true;
    }


    
    
}
