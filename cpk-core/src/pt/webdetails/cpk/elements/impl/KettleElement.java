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

package pt.webdetails.cpk.elements.impl;


import pt.webdetails.cpk.cache.EHCache;
import pt.webdetails.cpk.cache.ICache;
import pt.webdetails.cpk.elements.Element;
import pt.webdetails.cpk.elements.impl.kettleoutputs.IKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleoutputs.InferedKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleoutputs.JsonKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleoutputs.ResultFilesKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleoutputs.ResultOnlyKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleoutputs.SingleCellKettleOutput;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public abstract class KettleElement extends Element {

  protected static final String DEFAULT_STEP = "OUTPUT";
  protected static final String KETTLEOUTPUT_CLASSES_NAMESPACE = "pt.webdetails.cpk.elements.impl.kettleOutputs";

  private ICache<ResultKey, KettleResult> cache;

  // TODO: implement serializable to allow disk caching
  protected final class ResultKey {
    private final Map<String, String> kettleParameters;
    private final String outputStepName;

    public String getElementName() {
      return KettleElement.this.getName();
    }

    public String getOutputStepName() {
      return this.outputStepName;
    }

    public Map<String, String> getKettleParameters() {
      return this.kettleParameters;
    }

    public ResultKey( Map<String, String> kettleParameters, String outputStepName ) {
      this.kettleParameters = kettleParameters;
      this.outputStepName = outputStepName;
    }
  }

  // TODO: implement single cache per plugin!
  protected ICache<ResultKey, KettleResult> getCache() throws Exception {
    if ( this.cache == null ) {
      if ( this.getId() == null || this.getId().isEmpty() ) {
        throw new Exception( "Tried to create a cache for kettle element without Id set." );
      }

      this.cache = new EHCache<ResultKey, KettleResult>( "KettleResultsCache_" + this.getId() );
    }
    return this.cache;
  }

  protected IKettleOutput inferResult( Map<String, Map<String, Object>> bloatedMap ) {

        /*
         *  There are a few different types of kettle output processing.
         *  They can be infered or specified from a request parameter: kettleOutput
         *
         *  1. ResultOnly - we'll discard the output and print statistics only
         *  2. ResultFiles - Download the files we have as result filenames
         *  3. Json - Json output of the resultset
         *  4. csv - CSV output of the resultset
         *  5. SingleCell - We'll get the first line, first row
         *  6. Infered - Infering
         *
         *  If nothing specified, the behavior will be:
         *  * Jobs and Transformations with result filenames: ResultFiles
         *  * Without filenames:
         *      * Jobs: ResultOnly
         *      * Transformations:
         *          * Just one cell: SingleCell
         *          * Regular resultset: Json
         *
         *  By complexity:
         *      These don't require rowListener:
         *  1. ResultOnly
         *  2. ResultFiles
         *
         *      These do:
         *  3. SingleCell
         *  4. Json
         *  5. CSV
         *  6. Infered
         */

    //These conditions will treat the different types of kettle operations

    // Get info from bloatedmap
    Map<String, Object> request = bloatedMap.get( "request" );
    String kettleOutputType = (String) request.get( "kettleOutput" );
    // Are we specifying a stepname?
    String stepName = (String) request.get( "stepName" );
    String downloadStr = (String) request.get( "download" );
    boolean download = Boolean.parseBoolean( downloadStr != null ? downloadStr : "false" );
    HttpServletResponse httpResponse = (HttpServletResponse) bloatedMap.get( "path" ).get( "httpresponse" );

    // Get kettleOutput
    return this.inferResult( kettleOutputType, stepName, download, httpResponse );
  }

  protected IKettleOutput inferResult( String kettleOutputType, String stepName, boolean download
    , HttpServletResponse httpResponse ) {

    if ( kettleOutputType == null || kettleOutputType.isEmpty() ) {
      kettleOutputType = "Infered";
    }

    if ( stepName == null || stepName.isEmpty() ) {
      stepName = DEFAULT_STEP;
    }

    IKettleOutput kettleOutput;
    if ( kettleOutputType.equalsIgnoreCase( "Json" ) ) {
      kettleOutput = new JsonKettleOutput( httpResponse, download );
    } else if ( kettleOutputType.equalsIgnoreCase( "ResultFiles" ) ) {
      kettleOutput = new ResultFilesKettleOutput( httpResponse, download );
    } else if ( kettleOutputType.equalsIgnoreCase( "ResultOnly" ) ) {
      kettleOutput = new ResultOnlyKettleOutput( httpResponse, download );
    } else if ( kettleOutputType.equalsIgnoreCase( "SingleCell" ) ) {
      kettleOutput = new SingleCellKettleOutput( httpResponse, download );
    } else {
      kettleOutput = new InferedKettleOutput( httpResponse, download );
    }

    kettleOutput.setOutputStepName( stepName );

    return kettleOutput;
  }


}
