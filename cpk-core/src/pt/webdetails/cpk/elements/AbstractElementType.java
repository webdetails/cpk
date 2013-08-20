/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk.elements;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Node;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpf.utils.IPluginUtils;
/**
 *
 * @author Pedro Alves<pedro.alves@webdetails.pt>
 */
public abstract class AbstractElementType implements IElementType {

    protected Log logger = LogFactory.getLog(this.getClass());
    public abstract String getType();
    protected IPluginUtils pluginUtils;
    
    public AbstractElementType(IPluginUtils pluginUtils){
        this.pluginUtils=pluginUtils;
    }
    /**
     * Scans the location of the directory and returns a list of the content
     *
     * @param node
     */
    @Override
    public List<IElement> scanElements(Node node) {

        // Initialize container
        ArrayList<IElement> iElements = new ArrayList<IElement>();

        // Get list of files to process

        List<Node> elementLocations = node.selectNodes("elementLocations/elementLocation");

        for (Node elementLocation : elementLocations) {

            // Get the list of elements. We need to filter only the ones we want

            String elementPath = elementLocation.valueOf("@path");
            Boolean isRecursive = Boolean.parseBoolean(elementLocation.valueOf("@isRecursive"));
            String pattern = elementLocation.valueOf("@pattern");
            Collection<File> elements = pluginUtils.getPluginResources(elementPath, isRecursive, pattern);

            if (elements == null)
                continue;
            
            // Found the list we need. Processing it!
            for (File elementFile : elements) {

                IElement iElement = this.registerElement(elementFile.getAbsolutePath(), elementLocation);
                if (iElement != null) {

                    // Now - there are some reserved words for the id
                    
                    iElements.add(iElement);
                    logger.debug("Registred element " + iElement.toString());

                }
            }
        }

        return iElements;

    }

    /**
     * Basic registerElement code. This can be overriden by the implementation
     * classes for the specifics
     *
     * @param elementLocation
     * @return the initialized element
     */
    @Override
    public IElement registerElement(String elementLocation, Node node) {

        // 
        // classes

        AbstractElement element = new AbstractElement();
        initBaseProperties(element, elementLocation, node);

        return element;



    }

    /**
     * Main shared initialization code. This assigns the id, location and name
     * properties
     *
     * @param element
     * @param elementLocation
     * @return
     */
    public void initBaseProperties(AbstractElement element, String elementLocation, Node node) {

        element.setLocation(elementLocation);
        element.setId(FilenameUtils.getBaseName(elementLocation));
        element.setElementType(this.getType());
        element.setName(element.getId());
        element.setAdminOnly(Boolean.parseBoolean(node.valueOf("@adminOnly")));
        element.setTopLevel(node.valueOf("@path"));
        
        element.setElementInfo(createElementInfo());
        
        

    }

    @Override
    public abstract void processRequest(Map<String, ICommonParameterProvider> parameterProviders, IElement element);
    
    protected abstract ElementInfo createElementInfo();
}
