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

// ElementInfo has the relevant information to be passed to the content string.
public class ElementInfo {

  private boolean hasCache = false;
  private int cacheDuration;
  private String mimeType;
  private String attachmentName;

  public ElementInfo() {
  }

  public ElementInfo( String mimeType ) {
    this.mimeType = mimeType;
  }

  public ElementInfo( String mimeType, int cacheDuration ) {

    this.cacheDuration = cacheDuration;
    this.mimeType = mimeType;

    // if cacheDuration > 0, hasCache is true
    setHasCache( cacheDuration > 0 );

  }

  public boolean isHasCache() {
    return hasCache;
  }

  public void setHasCache( boolean hasCache ) {
    this.hasCache = hasCache;
  }

  public int getCacheDuration() {
    return cacheDuration;
  }

  public void setCacheDuration( int cacheDuration ) {
    this.cacheDuration = cacheDuration;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType( String mimeType ) {
    this.mimeType = mimeType;
  }

  public boolean hasAttachment() {
    return getAttachmentName() != null && !getAttachmentName().equals( "" );
  }

  public String getAttachmentName() {
    return attachmentName;
  }

  public void setAttachmentName( String attachmentName ) {
    this.attachmentName = attachmentName;
  }
}
