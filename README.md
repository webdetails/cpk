CPK
===

Community Plugin Kick-starter


## Motivation


[Pentaho](http://www.pentaho.com) is very well known for being a very good
Business Analytics software, but is in fact much more than that; Pentaho is a
great platform to build on top of. 


Using an easy analogy, I see Pentaho acting as an operating sytem where people
can build Application on top of


## Objective


The goal of CPK is to provide a simple and easy way to develop pentaho plugins
that behave like packaged applications, simplifying it's structure. 

The UI is built using [CDE](http://cde.webdetails.org), with a simple
methodology to create new dashboards / pages and a sitemap that provides simple
navigation and a default template

There are 3 options for doing server-side code:

1. [Kettle](http://kettle.pentaho.org) transformations / jobs
1. Javascript server side code execution
1. Java classes

The first two are recommended, since it's easier to register new endpoints just
by dropping code in a directory and no compilation is necessary.

With this approach not only we expect to make it easier and faster to develop
plugins, we also hope to lower down the specific technical requirements to build
them. The end goal is that business consultants are able to build new plugins,
not requiring specific java knowledge.

## Kettle Endpoints
Kettle transformations or jobs of a CPK plugin are automatically exposed as rest endpoints **pentaho/plugin/{cpkPluginId}/api/{kettleFileName}**. To execute the corresponding transformation/job simply perform an HTTP GET request to the endpoint. 

If you have need of a private transformation/job, prefix its name with an underscore and it won't be made available in an endpoint. 

### Specifying from where to get results
It is possible to specify from which step/job entry we want to fetch row results. To do so, simply include the _**stepName**_ parameter in the query string of the request. For example, to get the transformation results from the "OUTPUT" step execute the request *pentaho/plugin/{cpkPluginId}/api/{kettleFileName}?stepName=OUTPUT*.

A kettle endpoint may have multiple steps or job entries from where we can fetch results. Because allowing to get results from any given intermediate step/job entry would be a possible security breach, it is necessary to explicitly identify which can be used as output. This is done by setting the name of the step/job entry to start with *"OUTPUT"*. For example, a kettle endpoint for a transformation that has five steps with names "Step1", "Step2", "OutputStep", "OUTPUTStep1" and "OUTPUTStep2" will only allow getting results from "OUTPUTStep1" and "OUTPUTStep2".

If no *stepName* is specified in the HTTP request, or if the *stepName* is an invalid output step/job entry, CPK will use the default value of the parameter **cpk.result.stepName** defined in the transformation/job. 
If no *stepName* is specified in the HTTP request and no default value is set for **cpk.result.stepName** in the transformation/job, CPK will choose one valid output step to get the results from. If there is no valid output step/job entry, the transformation/job will still be executed but no row result information will be returned.


### Passing parameter values into a kettle transformation/job

It is also possible to pass parameter values to a transformation/job at runtime. This is achieved by encoding the parameter names in the query string with a *param* prefix. For example, if a transformation expects a parameter named *foo* and you want to set *bar* as its value, your request should be *pentaho/plugin/{cpkPluginId}/api/{kettleFileName}?paramfoo=bar*.

### Result formatting
The nature of a transformation/job result will vary and as such it is desirable to be able to format it differently. For example, a transformation may produce a tabular result set which we want to feed into a chart or it may select and filter some files which we want to zip and download. 

CPK allows formatting a kettle endpoint result in four different ways:

* **Json**: returns the result rows in a standard *CDA* like result set format (metadata/queryinfo/resultset)
* **ResultFiles**: gets the files that were set in the result
* **SingleCell**: returns the content of the first cell of the first row in the result
* **ResultOnly**: returns status information about the execution 

To select the desired format set the **kettleOutput** query string parameter to the chosen value. For example, *{cpkPluginId}/api/{kettleFileName}?kettleOutput=Json*.

It is possible to specify the default value to be used when the kettleOutput parameter is not defined in the query string. This is achieved by setting the default value of the transformation/job parameter **cpk.response.kettleOutput**.

If no query string or transformation/job parameter is defined for result formatting, CPK uses the following decision logic to determine which one best fits the endpoint result.

* Result has file names?	
	* Yes => **ResultFiles**
	* No  => Result from a kettle Job?
		* Yes => **ResultOnly**
		* No => Result has only one row with a single cell?
			* Yes => **SingleCell**
			* No => **Json**

This logic may also be explicitly triggered by setting *kettleOutput* value to *Infered*.


#### Results as response attachments (file downloads)
Some output formats may trigger the download of a file. 

##### ResultFiles
When **ResultFiles** is used and the result contains multiple files, CPK compresses them into a single Zip file that is returned as an attachment of the response. However, results that have a single file do not trigger a download by default, i.e. the file is not marked as an attachment of the response.

It is possible to change this default behaviour by setting the transformation/job parameter **cpk.response.download** to *true*. Also, the default value is always overrided when the value of the query string parameter **download** is defined in the request. For example, consider a transformation which result has one file and which **cpk.response.download** is set to *true*. If the request *{cpkPluginId}/api/{kettleFileName}?kettleOutput=ResultFiles&download=false* is executed the file will not be marked as an attachment.

When the result has single file, CPK will try to determine the mime type from the file extension. If the mime type is known a priori you can override the default behaviour by setting the transformation/job parameter **cpk.response.mimeType** to the desired value (e.g. application/xml).

It is also possible to define the name of the downloaded file by setting the tranformation/job parameter **cpk.response.attachmentName** to the intended value. 

##### SingleCell
In some cases it is useful to interpret the contents of a cell as the contents of a file. In this way the **SingleCell** output format behaves similarly to **ResultFiles** and as such it is sensible to the parameters **download**, **cpk.response.download**, **cpk.response.mimeType** and **cpk.response.attachmentName**.    

For example, consider a transformation that writes xml to the single cell of the single row of the result with parameters _**cpk.response.mimeType**=application/xml_, _**cpk.response.attachmentName**=myXml.xml_ and _**cpk.response.download**=true_ A request *{cpkPluginId}/api/{transformationKettleFileName}* will download a file named *myXml.xml* which content is the content of the single cell result.


<!-- TODO: There should be a better explanation on the Json and ResultOnly outputs -->

<!-- TODO: Mention that all parameter names that begin with the string "cpk." are reserved. -->


### Cache
Each CPK plugin has a cache that stores the results obtained from the execution of its endpoints. 
Although there is one cache per CPK plugin, this cache can be enabled/disabled per kettle endpoint. By default caching is disabled and to enable it you set the value of the transformation/job parameter **cpk.cache.isEnabled** to true. You can also specify how long a result should be cached by setting the value of the transformation/job parameter **cpk.cache.timeToLiveSeconds**.

If at runtime you wish to bypass an enabled cache use the query string parameter **bypassCache** set to true. This will force the transformation/job to execute and update the previous cached valued.

<!-- TODO: explicitly refer that EHCache is being used for CPK caching and it is possible to use Ehcache.xml configuration to tweak cache settings -->


## Structure

This is the resulting plugin structure. Ideally, no compilation is necessary, so
everything except maybe the lib directory could be stored in a _VCS_ system.

This is the proposed stub configuration

_to be completed_


	CPK_Plugin
	|-- conf
	|-- dashboards
	|-- endpoints
	|-- lib
	`-- plugin.xml


## CPK administrative features


Besides providing the regular templating for creating new plugins, CPK can have
an administrative UI with the following features:


* List existing plugins
* Detect if the plugins are up to date with the latest version of CPK
* Allow the creation of new plugins
* Allow to change plugin metadata
* List and register new endpoints (UI and code)

Here's a list of stretch goals / nice to have

* Import UI from existing dashboards in solution
* Allow editing dashboards from this UI
* Submit marketplace metadata to *Pentaho*
* Generate distribution zip package



## Updates

As the CPK framework or any of it's dependencies gets improved, the plugins
themselves can't stay outdated. There will be a version information attached to
the _CPK plugin version_ so that it's possible to upgrade to the latest version.



## Dependencies

CPK will have as little code as possible, making it as simple as possible to
develop plugins. However, it will need a few dependencies:

* Pentaho
* [CPF](https://github.com/webdetails/cpf) - Community Plugin Framework, with
  the common set of code for the plugins
* [CDE](https://github.com/webdetails/cde) - Community Dashboard Editor
* [CDF](https://github.com/webdetails/cdf) - Community Dashboard Framework
* [CDA](https://github.com/webdetails/cda) - Community Data Access


## Link with Pentaho Marketplace

Once a plugin is developed, and the authors think it's in a state that can be
shared, CPK will be able to generate a packaged plugin and metadata information
so it can be integrated into Pentaho's marketplace. 

Pentaho will then be able to categorize / approve the plugin so that it becomes
available to other users through the marketplace



## License

This project uses [MPLv2](http://www.mozilla.org/MPL/2.0/)
