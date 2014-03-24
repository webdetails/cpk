/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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


import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JsonKettleOutput extends KettleOutput {

  public JsonKettleOutput( HttpServletResponse response, boolean download ) {
    super( response, download );
  }

  @Override
  public boolean needsRowListener() {
    return true;
  }

  @Override
  public void processResult() {
    ObjectMapper mapper = new ObjectMapper();

    RowsJson rowsJson = new RowsJson( this.getRows(), this.getRowMeta() );

    try {
      mapper.writeValue( this.getOut(), rowsJson );
    } catch ( IOException ex ) {
      this.logger.error( "IO Error processing Json kettle output.", ex );
    }
  }

}
