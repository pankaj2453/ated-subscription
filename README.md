ated-subscription
=================

Annual Tax for Enveloped Dwellings Subscription microservice

[![Build Status](https://travis-ci.org/hmrc/ated-subscription.svg)](https://travis-ci.org/hmrc/ated-subscription) [ ![Download](https://api.bintray.com/packages/hmrc/releases/ated-subscription/images/download.svg) ](https://bintray.com/hmrc/releases/ated-subscription/_latestVersion)

This service provides the ability for uk-based or non-UK based ated clients to subscribe to ATED service.

###Subscribe to ATED

The request must be a valid json for below uri
- POST    /org/:org/subscribe: Subscribe to ated
- POST    /agent/:ac/subscribe: Subscribe a NON-UK based client to ated via agent

Where:

| Parameter | Message    |
|:--------:|-------------|
|   org    | Org-Id      |
|   ac     | AgentCode   |

####Example of usage for individual or Agent

 POST /org/123456789/subscribe
 POST /agent/123456789/subscribe

 **Request body**
 
  ```json
{
  "acknowledgmentReference": "12345678901234567890123456789012",
  "safeId": "XE0001234567890",
  "address": [
    {
      "name1": "Joe",
      "name2": "bloggs",
      "addressDetails": {
        "addressType": "Correspondence",
        "addressLine1": "address-line-1",
        "addressLine2": "address-line-2",
        "addressLine3": "address-line-3",
        "addressLine4": "Newcastle",
        "postalCode": "AB1 4CD",
        "countryCode": "GB"
      },
      "contactDetails": {
        "telephone": "01234567890",
        "eMailAddress": "aa@aa.com"
      }
    }
  ]
}
  ```
  **Response body**
 
  ```json
 {
   "processingDate":"2001-12-17T09:30:47Z",
   "atedRefNumber": "XY1234567890123",
   "formBundleNumber": "123456789012345"
 }
  ```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
