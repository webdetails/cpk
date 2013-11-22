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

import java.util.Map;

import pt.webdetails.cpk.CpkEngine;

public class AbstractElement implements IElement {

  private String id;
  private String name;
  private String elementType;
  private String location;
  private ElementInfo elementInfo;
  private boolean adminOnly;
  private String topLevel;

  @Override
  public String getTopLevel() {
    return topLevel;
  }

  public void setTopLevel( String topLevel ) {
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

  public void setLocation( String location ) {
    this.location = location;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public void setElementType( String elementType ) {
    this.elementType = elementType;
  }

  @Override
  public boolean isAdminOnly() {
    return this.adminOnly;
  }

  public void setAdminOnly( boolean admin ) {
    this.adminOnly = admin;
  }

  @Override
  public String toString() {
    return "AbstractElement{" + "id=" + getId() + ", name=" + getName() + ", elementType=" + getElementType()
      + ", location=" + getLocation() + ", adminOnly=" + isAdminOnly() + '}';
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 11 * hash + ( this.id != null ? this.id.hashCode() : 0 );
    hash = 11 * hash + ( this.elementType != null ? this.elementType.hashCode() : 0 );
    hash = 11 * hash + ( this.location != null ? this.location.hashCode() : 0 );
    return hash;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    final AbstractElement other = (AbstractElement) obj;
    if ( ( this.id == null ) ? ( other.id != null ) : !this.id.equals( other.id ) ) {
      return false;
    }
    if ( ( this.elementType == null ) ? ( other.elementType != null ) :
      !this.elementType.equals( other.elementType ) ) {
      return false;
    }
    if ( ( this.location == null ) ? ( other.location != null ) : !this.location.equals( other.location ) ) {
      return false;
    }
    return true;
  }

  @Override
  /**
   * Processes the request
   */
  public void processRequest( Map<String, Map<String, Object>> bloatedMap ) {
    // Get the elementType and process it
    CpkEngine.getInstance().getElementType( this.getElementType() ).processRequest( bloatedMap, this );
  }

  @Override
  public ElementInfo getElementInfo() {
    return elementInfo;
  }

  public void setElementInfo( ElementInfo elementInfo ) {
    this.elementInfo = elementInfo;
  }

}
