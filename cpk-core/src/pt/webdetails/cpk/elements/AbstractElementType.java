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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Node;
import pt.webdetails.cpf.utils.IPluginUtils;

public abstract class AbstractElementType implements IElementType {

  protected Log logger = LogFactory.getLog( this.getClass() );

  public abstract String getType();

  protected IPluginUtils pluginUtils;

  public AbstractElementType( IPluginUtils pluginUtils ) {
    this.pluginUtils = pluginUtils;
  }

  /**
   * Scans the location of the directory and returns a list of the content
   *
   * @param node
   */
  @Override
  public List<IElement> scanElements( Node node ) {

    // Initialize container
    ArrayList<IElement> iElements = new ArrayList<IElement>();

    // Get list of files to process

    List<Node> elementLocations = node.selectNodes( "elementLocations/elementLocation" );

    for ( Node elementLocation : elementLocations ) {

      // Get the list of elements. We need to filter only the ones we want

      String elementPath = elementLocation.valueOf( "@path" );
      Boolean isRecursive = Boolean.parseBoolean( elementLocation.valueOf( "@isRecursive" ) );
      String pattern = elementLocation.valueOf( "@pattern" );
      Collection<File> elements = pluginUtils.getPluginResources( elementPath, isRecursive, pattern );

      if ( elements == null ) {
        continue;
      }

      // Found the list we need. Processing it!
      for ( File elementFile : elements ) {

        IElement iElement = this.registerElement( elementFile.getAbsolutePath(), elementLocation );
        if ( iElement != null ) {

          // Now - there are some reserved words for the id

          iElements.add( iElement );
          logger.debug( "Registred element " + iElement.toString() );

        }
      }
    }

    return iElements;

  }

  /**
   * Basic registerElement code. This can be overriden by the implementation classes for the specifics
   *
   * @param elementLocation
   * @return the initialized element
   */
  @Override
  public IElement registerElement( String elementLocation, Node node ) {

    //
    // classes

    AbstractElement element = new AbstractElement();
    initBaseProperties( element, elementLocation, node );

    return element;


  }

  /**
   * Main shared initialization code. This assigns the id, location and name properties
   *
   * @param element
   * @param elementLocation
   * @return
   */
  public void initBaseProperties( AbstractElement element, String elementLocation, Node node ) {

    element.setLocation( elementLocation );
    element.setId( FilenameUtils.getBaseName( elementLocation ) );
    element.setElementType( this.getType() );
    element.setName( element.getId() );
    element.setAdminOnly( Boolean.parseBoolean( node.valueOf( "@adminOnly" ) ) );
    element.setTopLevel( node.valueOf( "@path" ) );

    element.setElementInfo( createElementInfo() );


  }

  @Override
  public abstract void processRequest( Map<String, Map<String, Object>> bloatedMap, IElement element );

  protected abstract ElementInfo createElementInfo();
}
