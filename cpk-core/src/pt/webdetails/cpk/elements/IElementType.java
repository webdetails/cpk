/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cpk.elements;

import java.util.List;
import java.util.Map;
import org.dom4j.Node;

public interface IElementType {

    public String getType();

    public List<IElement> scanElements(Node node);
    
    public IElement registerElement(String elementLocation, Node node);
    
    public void processRequest(Map<String, Map<String, Object>> bloatedMap, IElement element);
        
    public boolean isShowInSitemap();
    
}
