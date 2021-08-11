# Roman Numerals

### Author: Siva Jeganathan

This code uses REST API and Asynchronous calls (Vertx.io) to convert a given number to Roman numeral.

## Design

![Design](https://github.com/sivaswami/roman/blob/master/design/romanServiceDesign.jpg)



## How to Install

### Clone repository
The code can be cloned from github using the below command
```
git clone https://github.com/sivaswami/roman
cd roman
```

### Proxy setup

Fill in the fields of gradle.properties to include any proxies. If no proxies, remove the file.


### Compile and run the code locally

Run the code with
```
export HTTP_PORT=8080 # or custom port
export ROMAN_MAX=255 # for 255 or 3999

gradle clean app:run

```
### Compile and run the code in Docker

Ensure docker is available locally.

Compile docker image using

```
gradle jibDockerBuild
```
change HTTP_PORT & ROMAN_MAX environment variable in docker-compose.yaml to change server port and roman number maximum in docker file.

``` 
docker-compose up
```

## API calls

This service provides 2 API's.
```
http://localhost:<port>/romannumeral?query=<input>
and
http://localhost:<port>/romannumeral?min=<input>&max=<input>
```
## REST API Configuration
The code uses 3 configuration parameters. http.Port, roman.min and roman.max.
- http.port : port is useful to set the http startup port
- roman.min and roman.max: These are the minimum and maximum ranges supported by service.
       represented by config parameter roman.min and roman.max
The code can also take HTTP_PORT environment variable for docker container support.

## Swagger Documentation
![Swagger](https://github.com/sivaswami/roman/blob/master/design/swagger_screenshot.png)


## Health and Metrics
![Health](https://github.com/sivaswami/roman/blob/master/design/HealthCheck_screenshot.png)
![Metrics](https://github.com/sivaswami/roman/blob/master/design/metrics_screenshot.png)

