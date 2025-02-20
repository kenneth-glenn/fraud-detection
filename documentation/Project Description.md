# Description

You’re building an API that merchants will integrate with to determine in real time if a transaction is fraudulent.  You’ve decided to build a minimal version of the API to start with to track results and gather feedback.

As part of the initial version, you’ll need to implement the API along with a way to run the server and to test the implementation. This initial version will consist of a single API that will take payment  details. The result will be a fraud score with signals. The API specification is detailed below.

# API Specification

## Endpoint
**POST**  https://example.com/api/score-transaction

## Request Body
**Media Type**: application/json

### Notes:
Updated the request body JSON to use `nameOnCard` instead of `cardOnCard` to clarify the name on the card.

Example:
```json
{
    "customerName":"John Smith",
    "ipAddress":"10.4.3.2",
    "location": {
        "city":"Somewhere",
        "state":"MA"
    },
    "paymentDetails": {
        "cardLast4":"4567",
        "nameOnCard":"John Smith", //changed from `cardOnCard` to `nameOnCard` 
        "purchaseAmount":46.56, //changed from `cardAmount` to `purchaseAmount` to clarify the amount purchased with the card
    },
    "transactionDetails": {
        "merchantName":"Some Store",
        "merchantLocation": {
            "city":"Somewhere",
            "state":"MA"
        },
        "purchasedItemCount":2
    },
}
```

## Response Body
**Media Type**: application/json

Example:
```json
{
    "signals": [
        {
            "signal":"location",
            "potentialFraud":true,
            "details":[
                "Location differs from resident location",
                "Location has not been purchased from previously",
            ],
         },
         {
            "signal":"ipAddress",
            "potentialFraud":false,
            "details": [
                "IP Address is not known to be fraudulent or malicious",
            ]
         },
         {
            "signal":"transaction",
            "potentialFraud":false,
            "details": [
                "Transaction details do not look fraudulent",
            ]
         },
         {
            "signal":"cardDetails",
            "potentialFraud":false,
            "details": [
                "Card details do not look fraudulent",
            ]
         },
    ]
}
```

# Goal of this Exercise

This goal of this exercise is not to get a "correct" solution. There isn't a correct solution. The goal is to use an example API from this domain that can allow you to develop a simple API. The main success criteria is how well the application and API is developed. Some things do consider in the implementation:

* Is the code clear?
* Are you using modern language features and practices?
* Are there good tests?
* Are you following good code quality patterns?
* Are you using reasonable abstractions?
* Would someone unfamiliar with the application understand what the different pieces are responsible for?


# Implementation Notes

## Fraud Indicators

Some sample fraud indicators have been added in the response above. These fraud indicators are totally fine to use. However, if you have other ideas, you may use your own fraud indicators. There is no right answer here.

Part of the reason for the openness of the fraud indicators is to allow enough room in the implementation to allow you to develop an application/API in which you can demonstrate your ability to implement APIs and model domain logic.


## Architectural/Design Considerations
When implementing this API, keep in mind that the main purpose of this exercise is to see how you approach designing a a solution and writing code in the real world. While implementing a solution, be sure to use the same architectural and design principals you’d use when you’re write real production code that you’d expect to be maintainable and performant.

We’d like to see good architectural practices and good software design as we’re reading through your implementation. This should include things such as:
* Thoughtful architectural boundaries - is there a reasonable api vs domain model boundary? Are there other boundaries to be considered?
* Are you writing class level or method level code that can be well maintained?
* How much of the codebase would need to be changed for a simple bug fix or feature enhancement?
* Can the code be easily unit and integration tested?
* Can the API be observed via logging or other mechanisms?


## Storage

There is no specific storage required as part of this implementation. However, you might find having some storage useful in order to implement some of the fraud indicators.


# Running Locally

As part of the implementation, you should provide a way to run and test the API locally. You should provide instructions on how to do this as well.