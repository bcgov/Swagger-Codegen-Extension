Swagger Codegen Extension 
====================

# Introduction #
----------------
The Swagger Codegen Extension contains 3 code generator extensions that transform an OpenAPI specification into code for:
- .NET Core MVC
- Java Fuse
- Python Django Rest API

All 3 code generators are intended to be used with applications deployed to OpenShift, however they are also compatible with applications deployed to traditional hosting environments.

The codegen extension is being developed as an open source solution.

Prerequisites
-------------
- Java 7+ JDK  https://java.com
- Maven https://maven.apache.org
- Git https://git-scm.com/
- Optional - Nexus server available in the environment.  A local Nexus server will greatly reduce build times.  http://www.sonatype.org/nexus/ 

Installation
------------
Download the source
Use `mvn package` to compile the jar file
The resulting jar file can then be used with the Swagger.io Codegen utility.

# Usage #
-----

## Django ##

The Django generator is meant to be used to generate the skeleton for an application that uses the Django REST API libraries.  It will generate the following files:

- admin.py : Used to cause models to become database tables
- models folder : Contains definitions for database models and view models
- serializers.py : Contains serializers for the generated models
- fakedata.py : Contains basic data for testing models
- test_api_simple : Contains tests for CRUD operations on simple structures (those only containing primitive attributes)
- test_api_complex : Contains tests for CRUD operations on complex structures (those containing child objects that are not primitive).  This file will have to be edited to create child objects in the correct order.
- test_api_custom : Contains stubs for non CRUD operations.  This file will have to be edited to to implement tests.
- urls.py : Django URL routing
- views.py : Basic generated views for CRUD operations
- views_custom.py : Non CRUD views.  This file will have to be edited.

The following files can be safely copied into the target project folder after generation, as they are not intended to be edited:
- admin.py
- serializers.py
- models
- test_api_simple
- urls.py
- views.py

## ASP .NET Core ##

The .net core generate is intended to be used to build a .net core 1.1 MVC application.

It generates the following:
- test skeletons for the model and paths
- Application source for the following:

  - Models
  - View Models
  - Controllers
  - Service Interfaces
  - Service Implementation Skeleton (these files are intended to be copied once and then edited by hand)
  - Database context definition, which is also causes models to become database tables
  - Supporting files

The .NET Core generator does not automatically implement CRUD operations.

## Java Fuse ##

The Java fuse generator creates a CXF / CDI FUSE skeleton that is suitable for creating a back end service or microservice.  The skeleton will run on its own however is intended to be edited to implement functionality.

The Java fuse generator creates the following:
- supporting files for a full API server
- API implementation skeleton
- Model definitions

The Java Fuse generator does not create database objects at this time.

# Development #
-----------
The extension is composed of a set of Java classes, one for each programming language implemented

Each programming language implemented also has a set of Mustache templates which are used to render the code.

# Contribution #
------------

Please report any [issues](https://github.com/bcgov/Swagger-Codegen-Extension/issues).

[Pull requests](https://github.com/bcgov/Swagger-Codegen-Extension/pulls) are always welcome.

If you would like to contribute, please see our [contributing](CONTRIBUTING.md) guidelines.

Please note that this project is released with a [Contributor Code of Conduct](CODE_OF_CONDUCT.md). By participating in this project you agree to abide by its terms.

License
-------

    Copyright 2017 Province of British Columbia

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at 

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

Maintenance
-----------

This repository is maintained by [BC Ministry of Transportation](http://www.th.gov.bc.ca/).
Click [here](https://github.com/orgs/bcgov/teams/tran/repositories) for a complete list of our repositories on GitHub.