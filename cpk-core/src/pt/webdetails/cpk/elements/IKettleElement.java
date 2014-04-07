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

package pt.webdetails.cpk.elements;

import pt.webdetails.cpk.elements.impl.KettleResult;
import java.util.Map;

public interface IKettleElement {

  /**
   * Executes the kettle transformation / job.
   * @param kettleParameters Parameters to be passed into the kettle transformation/job.
   * @param outputStepName The step name from where the result will be fetched.
   * @param bypassCache If true, forces the request to be processed even if a value for it already exists in the cache.
   *                    Bypassing the cache also updates the cache with the new obtained result.
   * @return The result of executing the kettle transformation / job.
   */
  KettleResult processRequest( Map<String, String> kettleParameters, String outputStepName, boolean bypassCache );

}
