Sling REST Service Framework
===================================

## Background

AEM does not ship with any means of supporting a standard Jersey style REST service. Jersey and other popular REST frameworks don't appear to support the combination of Sling and OSGi. The main issue is that Sling manages all requests and there is no simple way of adding Jersey to a Sling controlled environment with digging into the deep internals of both Sling and Jersey.

## Overview

This bundle provides framework for building out JSON based REST services. Services will register an OSGi service that implement the SlingRestService and mark individual methods with the SlingRestFunction annotation.

## Documentaton

Please see Javadoc for SlingRestService and SlingRestFunction

## Example Function

```
#!java

@SlingRestFunction("GET:/api/v1/user/{id}")
public RestResponse userInfo(SlingHttpServletRequest request, SlingHttpServletResponse response) throws SlingRestServiceException {
   String id = (String) request.getAttribute("ws.id");
   try  {
      ...
      String result = gson.toJson(returnObject);
      return new RestResponse.Builder(result).create();
   } catch (ApplicationException e) {
      throw new SlingRestServiceException.Builder(SlingRestServiceException.CATEGORY_ERROR, "Public message for end user")
      .setException(e)
      .setInternalMessage("Internal message that will be in logs for " + id)
      .create();
   }
}
```

Example URL: http://localhost:4502/api/v1/user/123
