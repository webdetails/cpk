/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cpk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpk.security.IAccessControl;

import java.io.IOException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Status {

  private TreeMap<String, IElement> elementsMap;
  private String defaultElementName;
  private ICpkEnvironment cpkEnv;

  private Status() {
  }

  public Status( TreeMap<String, IElement> elementsMap, String defaultElementName, ICpkEnvironment cpkEnv ) {
    init( elementsMap, defaultElementName, cpkEnv );
  }

  private void init( TreeMap<String, IElement> elementsMap, String defaultElementName, ICpkEnvironment cpkEnv ) {
    setCpkEnv( cpkEnv );
    setDefaultElementName( defaultElementName );
    setElementsMap( elementsMap );
  }

  public void setCpkEnv( ICpkEnvironment cpkEnv ) {
    this.cpkEnv = cpkEnv;
  }

  @JsonIgnore
  public String getStatusJson() {
    ObjectMapper mapper = new ObjectMapper();

    String json;
    try {
      json = mapper.writeValueAsString( this );
    } catch ( IOException ex ) {
      Logger.getLogger( Status.class.getName() ).log( Level.SEVERE, null, ex );
      json = "{\"error\":\"There was a problem creating the Status JSON\"}";
    }

    return json;
  }

  @JsonIgnore
  public String getStatus() {

    IAccessControl accessControl = cpkEnv.getAccessControl();
    StringBuilder out = new StringBuilder();

    out.append( "--------------------------------\n" );
    out.append( "   " ).append( cpkEnv.getPluginName() ).append( " Status\n" );
    out.append( "--------------------------------\n" );
    out.append( "\n" );

    out.append( "End Points\n" );

    for ( String key : elementsMap.keySet() ) {

      IElement myElement = elementsMap.get( key );
      if ( accessControl.isAllowed( myElement ) ) {
        out.append( "   [" ).append( key ).append( "]: \t" ).append( myElement.toString() ).append( "\n\n" );
      }

    }

    return out.toString();
  }

  @JsonProperty( "pluginName" )
  public String getPluginName() {
    return cpkEnv.getPluginName();
  }

  @JsonProperty( "elements" )
  public TreeMap<String, IElement> getElementsMap() {
    return elementsMap;
  }

  @JsonIgnore
  public void setElementsMap( TreeMap<String, IElement> elementsMap ) {
    this.elementsMap = elementsMap;
  }

  @JsonProperty( "elementsCount" )
  public int getNrElements() {
    return getElementsMap().size();
  }

  @JsonProperty( "defaultElement" )
  public String getDefaultElementName() {
    return defaultElementName;
  }

  @JsonIgnore
  public void setDefaultElementName( String defaultElementName ) {
    this.defaultElementName = defaultElementName;
  }

  @JsonIgnore
  @Override
  public String toString() {
    return null; //TODO
  }

}
