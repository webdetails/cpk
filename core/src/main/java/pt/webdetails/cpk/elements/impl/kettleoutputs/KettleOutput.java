/*!
* Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company.  All rights reserved.
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

package pt.webdetails.cpk.elements.impl.kettleoutputs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public abstract class KettleOutput implements IKettleOutput {

  public static final class Configuration implements Cloneable {

    // TODO: change to enum
    private String outputType;
    private String mimeType;
    private String attachmentName;
    private boolean sendResultAsAttachment = false;

    /**
     *
     * @return The type of output that is to be returned.
     */
    public String getOutputType() { return outputType; }
    public Configuration setOutputType( String outputType ) {
      this.outputType = outputType;
      return this;
    }

    /**
     *
     * @return If the result is to be sent as an attachment of the response.
     */
    public boolean getSendResultAsAttachment() { return this.sendResultAsAttachment; }
    public Configuration setSendResultAsAttachment( boolean sendResultAsAttachment ) {
      this.sendResultAsAttachment = sendResultAsAttachment;
      return this;
    }

    /**
     *
     * @return The name of the attachment to use when sending the result as an attachment.
     */
    public String getAttachmentName() { return attachmentName; }
    public Configuration setAttachmentName( String attachmentName ) {
      this.attachmentName = attachmentName;
      return this;
    }

    /**
     *
     * @return The mime type to be used when sending result as an attachment.
     */
    public String getMimeType() { return mimeType; }
    public Configuration setMimeType( String mimeType ) {
      this.mimeType = mimeType;
      return this;
    }

    @Override
    public Configuration clone() {
      Configuration clone = new Configuration();
      clone
        .setAttachmentName( this.attachmentName )
        .setMimeType( this.mimeType )
        .setSendResultAsAttachment( this.sendResultAsAttachment )
        .setOutputType( this.outputType );

      return clone;
    }

  }

  protected Log logger = LogFactory.getLog( this.getClass() );
  protected final String ENCODING = "UTF-8";

  private OutputStream out;
  private HttpServletResponse response;
  private Configuration configuration;

  protected OutputStream getOut() { return this.out; }

  public HttpServletResponse getResponse() { return this.response; }
  public KettleOutput setResponse( HttpServletResponse response ) {
    this.response = response;

    try {
      this.out = response.getOutputStream();
    } catch ( IOException ex ) {
      this.logger.error( "Something went wrong setting http response on KettleOutput.", ex );
    }
    return this;
  }

  public Configuration getConfiguration() { return this.configuration; }
  public KettleOutput setConfiguration( Configuration configuration ) {
    this.configuration = configuration;
    return this;
  }

}
