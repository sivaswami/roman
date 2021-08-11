# Roman Numerals

### Author: Siva Jeganathan

This code uses REST API and Asynchronous calls (Vertx.io) to convert a given number to Roman numeral.

## API calls
This service provides 2 API's.
```
http://localhost:<port>/romannumeral?query=<input>
and
http://localhost:<port>/romannumeral?min=<input>&max=<input>
```
## Swagger Documentation


## Design

## REST API Configuration
The code uses 3 configuration parameters. http.Port, roman.min and roman.max.
- http.port : port is useful to set the http startup port
- roman.min and roman.max: These are the minimum and maximum ranges supported by service.
       represented by config parameter roman.min and roman.max

## How to compile and run the code locally
The code can be cloned from github using the below command

```
git clone <github_url>
cd roman
gradle clean app:run
```
## How to compile and run the code in Docker
