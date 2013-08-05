/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk.testUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpf.Util;

/**
 *
 * @author Pedro Alves<pedro.alves@webdetails.pt>
 */
public class PluginUtilsForTesting implements IPluginUtils {

    
    protected Log logger = LogFactory.getLog(this.getClass());
    private String pluginName;
    private File pluginDirectory;

    @Override
    public File getPluginDirectory() {
        return pluginDirectory;
    }

    @Override
    public void setPluginDirectory(File pluginDirectory) {
        this.pluginDirectory = pluginDirectory;
    }

    @Override
    public String getPluginName() {
        return pluginName;
    }

    @Override
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public PluginUtilsForTesting() {
        try {
            // init
            initialize();
        } catch (Exception e) {
            logger.error("Can't initialize PluginUtils: " + Util.getExceptionDescription(e));
        }

    }

    @Override
    public void initialize() throws IOException, DocumentException {

        // We need to get the plugin name
       

     
        /*
         * Verify if the index 0 is actually the file we want!
         */
        String url ="file://"+System.getProperty("user.dir")+"/test-resources/cpkSol/plugin.xml";
        
        URL pluginUrl = new URL(url);
        //URL pluginUrl = new File("plugin.xml").toURI().toURL();

        // Parent file holds the name
        File pluginDir = new File(pluginUrl.getFile()).getParentFile();
        setPluginName(pluginDir.getName());
        setPluginDirectory(pluginDir);

        logger.debug("Found resource? " + "?");

    }

    /**
     * Calls out for resources in the plugin, on the specified path
     *
     * @param elementPath Relative to the plugin directory
     * @param recursive Do we want to enable recursivity?
     * @param pattern regular expression to filter the files
     * @return Files found
     */
    @Override
    public Collection<File> getPluginResources(String elementPath, Boolean recursive, String pattern) {

        IOFileFilter fileFilter = TrueFileFilter.TRUE;

        if (pattern != null && !pattern.equals("")) {
            fileFilter = new RegexFileFilter(pattern);


        }

        IOFileFilter dirFilter = recursive.equals(Boolean.TRUE) ? TrueFileFilter.TRUE : null;

        // Get directory name. We need to make sure we're not allowing this to fetch other resources
        String basePath = FilenameUtils.normalize(getPluginDirectory().getAbsolutePath());
        String elementFullPath = FilenameUtils.normalize(basePath + File.separator + elementPath);

        if (!elementFullPath.startsWith(basePath)) {
            logger.warn("PluginUtils.getPluginResources is trying to access a parent path - denied : " + elementFullPath);
            return null;
        }

        File dir = new File(elementFullPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }

        return FileUtils.listFiles(dir, fileFilter, dirFilter);


    }

    /**
     * From a full path, returns the relative path
     *
     * @param fullPath
     * @param includePluginDir
     * @return The relative path
     */
    @Override
    public String getPluginRelativeDirectory(String fullPath, boolean includePluginDir) throws FileNotFoundException {


        // Get directory name. We need to make sure we're not allowing this to fetch other resources
        File pluginDir = getPluginDirectory();
        if (includePluginDir) {
            pluginDir = pluginDir.getParentFile();
        }

        String basePath = FilenameUtils.normalize(pluginDir.getAbsolutePath());
        String elementFullPath = FilenameUtils.getFullPath(FilenameUtils.normalize(fullPath));

        if (elementFullPath.indexOf(basePath) < 0) {
            throw new FileNotFoundException("Can't extract relative path from file " + fullPath);
        }

        return elementFullPath.substring(basePath.length());


    }

    /**
     * Calls out for resources in the plugin, on the specified path
     *
     * @param elementPath Relative to the plugin directory
     * @param recursive Do we want to enable recursivity?
     * @return Files found
     */
    @Override
    public Collection<File> getPluginResources(String elementPath, Boolean recursive) {
        return getPluginResources(elementPath, recursive, null);
    }

    /**
     * Calls out for resources in the plugin, on the specified path. Not
     * recursive
     *
     * @param elementPath Relative to the plugin directory
     * @param pattern regular expression to filter the files
     * @return Files found
     */
    @Override
    public Collection<File> getPluginResources(String elementPath, String pattern) {
        return getPluginResources(elementPath, false, pattern);
    }

    @Override
    public void setResponseHeaders(Map<String, ICommonParameterProvider> parameterProviders, final String mimeType) {
        setResponseHeaders(parameterProviders, mimeType, 0, null, 0);
    }

    public void setResponseHeaders(Map<String, ICommonParameterProvider> parameterProviders, final String mimeType, final String attachmentName) {
        setResponseHeaders(parameterProviders, mimeType, 0, attachmentName, 0);
    }

    @Override
    public void setResponseHeaders(Map<String, ICommonParameterProvider> parameterProviders, final String mimeType, final String attachmentName, long attachmentSize) {
        setResponseHeaders(parameterProviders, mimeType, 0, attachmentName, attachmentSize);

    }

    public void setResponseHeaders(Map<String, ICommonParameterProvider> parameterProviders, final String mimeType, final int cacheDuration, final String attachmentName, long attachmentSize) {
        // Make sure we have the correct mime type

        /* 
         * This code is part of the content generator. Since we want to simplify,
         * I'll remove this from here and directly set the Content-Type header on the response
         * 
        final IMimeTypeListener mimeTypeListener = outputHandler.getMimeTypeListener();
        if (mimeTypeListener != null) {
        mimeTypeListener.setMimeType(mimeType);
        }
         */


        final HttpServletResponse response = getResponse(parameterProviders);

        if (response == null) {
            logger.warn("Parameter 'httpresponse' not found!");
            return;
        }

        if (mimeType != null) {
            response.setHeader("Content-Type", mimeType);
        }

        if (attachmentName != null) {
            response.setHeader("content-disposition", "attachment; filename=" + attachmentName);
        } // Cache?

        if (attachmentSize > 0) {
            response.setHeader("Content-Length", String.valueOf(attachmentSize));
        }

        if (cacheDuration > 0) {
            response.setHeader("Cache-Control", "max-age=" + cacheDuration);
        } else {
            response.setHeader("Cache-Control", "max-age=0, no-store");
        }
    }

    /**
     * Copies the parameters from the ICommonParameterProvider to a Map
     *
     * @param params
     * @param provider
     */
    
    public void copyParametersFromProvider(Map<String, Object> params, ICommonParameterProvider provider) {
        @SuppressWarnings("unchecked")
        Iterator<String> paramNames = provider.getParameterNames();
        while (paramNames.hasNext()) {
            String paramName = paramNames.next();
            params.put(paramName, provider.getParameter(paramName));
        }
    }

    @Override
    public void redirect(Map<String, ICommonParameterProvider> parameterProviders, String url) {

        final HttpServletResponse response = getResponse(parameterProviders);

        if (response == null) {
            logger.error("response not found");
            return;
        }
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            logger.error("could not redirect", e);
        }
    }


    public HttpServletRequest getRequest(Map<String, ICommonParameterProvider> parameterProviders) {
        return (HttpServletRequest) parameterProviders.get("path").getParameter("httprequest");
    }

    public HttpServletResponse getResponse(Map<String, ICommonParameterProvider> parameterProviders) {
        return (HttpServletResponse) parameterProviders.get("path").getParameter("httpresponse");
    }

    @Override
    public ICommonParameterProvider getRequestParameters(Map<String, ICommonParameterProvider> parameterProviders) {
        return parameterProviders.get("request");
    }

    @Override
    public ICommonParameterProvider getPathParameters(Map<String, ICommonParameterProvider> parameterProviders) {
        return parameterProviders.get("path");
    }

    public OutputStream getResponseOutputStream(Map<String, ICommonParameterProvider> parameterProviders) throws IOException {

        return getResponse(parameterProviders).getOutputStream();
    }
    
    @Override
    public OutputStream getOutputStream(Map<String, ICommonParameterProvider> parameterProviders){
        
        return (OutputStream) parameterProviders.get("path").getParameter("outputstream");
    }
}