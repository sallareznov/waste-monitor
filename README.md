[![Build Status](https://travis-ci.org/sallareznov/waste-monitor.svg?branch=master)](https://travis-ci.org/sallareznov/waste-monitor) [![Codacy Badge](https://api.codacy.com/project/badge/grade/cfc103c241ce409f9c1ee89d9c0b6981)](https://www.codacy.com/app/sallareznov/waste-monitor)

# Waste Monitor

<p align="center">
  <img alt="Logo" src="dist/logo.png">
</p>

Waste Monitor is a RESTful web service enabling the user to monitor the evolution of his waste generation over time.

### Routes

```json
[
  {
    "description": "Attempts to register a new user",
    "route": "/api/register",
    "verb": "POST",
    "headers": "None",
    "urlParameters": "None",
    "queryParameters": "None",
    "body": {
      "username": "johndoe",
      "password": "johndoe"
    },
    "returnCodes": [
      "201 (Created) if the operation proceeded successfully and the user was created",
      "409 (Conflict) if a user with the same username already exists",
      "500 (Internal Server Error) if an error occurred on the server"
    ]
  },
  {
    "description": "Attempts to log a user",
    "route": "/api/login",
    "verb": "POST",
    "headers": "None",
    "urlParameters": "None",
    "queryParameters": "None",
    "body": {
      "username": "johndoe",
      "password": "johndoe"
    },
    "returnCodes": [
      "200 (OK) if the operation proceeded successfully",
      "401 (Unauthorized) if the credentials are invalid",
      "500 (Internal Server Error) if an error occurred on the server"
    ]
  },
  {
    "description": "Lists the registered users",
    "route": "/api/users",
    "verb": "GET",
    "headers": "None",
    "urlParameters": "None",
    "queryParameters": "None",
    "body": "None",
    "returnCodes": [
      "200 (OK) if the operation proceeded successfully",
      "500 (Internal Server Error) if an error occurred on the server"
    ]
  },
  {
    "description": "Shows informations about the authenticated user (his username, the number of trashes he owns and the total waste volume that are in his trashes)",
    "route": "/api/user",
    "verb": "GET",
    "headers": "Authorization: Basic <access_token>",
    "urlParameters": "None",
    "queryParameters": "None",
    "body": "None",
    "returnCodes": [
      "200 (OK) if the operation proceeded successfully",
      "400 (Bad Request) if the authentication token wasn't provided",
      "403 (Forbidden) if the authentication token is invalid or has expired",
      "500 (Internal Server Error) if an error occurred on the server"
    ]
  },
  {
    "description": "Shows informations about the trashes owned by the authenticated user",
    "route": "/api/user/trashes",
    "verb": "GET",
    "headers": "Authorization: Basic <access_token>",
    "urlParameters": "None",
    "queryParameters": "None",
    "body": "None",
    "returnCodes": [
      "200 (OK) if the operation proceeded successfully",
      "400 (Bad Request) if the authentication token wasn't provided",
      "403 (Forbidden) if the authentication token is invalid or has expired",
      "500 (Internal Server Error) if an error occurred on the server"
    ]
  },
  {
    "description": "Creates a new trash for the authenticated user",
    "route": "/api/user/createTrash",
    "verb": "POST",
    "headers": "Authorization: Basic <access_token>",
    "urlParameters": "None",
    "queryParameters": {
      "volume": 5
    },
    "body": "None",
    "returnCodes": [
      "201 (Created) if the operation proceeded successfully and the trash was created",
      "400 (Bad Request) if the authentication token wasn't provided",
      "403 (Forbidden) if the authentication token is invalid or has expired",
      "500 (Internal Server Error) if an error occurred on the server"
    ]
  },
  {
    "description": "Shows informations about the selected trash owned by the authenticated user",
    "route": "/api/user/trash",
    "verb": "GET",
    "headers": "Authorization: Basic <access_token>",
    "urlParameters": {
      "trashId": "the identifier of the trash"
    },
    "queryParameters": "None",
    "body": "None",
    "returnCodes": [
      "201 (Created) if the operation proceeded successfully and the trash was created",
      "400 (Bad Request) if the authentication token wasn't provided",
      "403 (Forbidden) if the authentication token is invalid or has expired",
      "500 (Internal Server Error) if an error occurred on the server"
    ]
  },
  {
    "description": "Empties the specified trash",
    "route": "/api/user/emptyTrash",
    "verb": "PUT",
    "headers": "Authorization: Basic <access_token>",
    "urlParameters": {
      "trashId": "the identifier of the trash"
    },
    "queryParameters": "None",
    "body": "None",
    "returnCodes": [
      "200 (OK) if the operation proceeded successfully",
      "400 (Bad Request) if the authentication token wasn't provided",
      "403 (Forbidden) if the authentication token is invalid or has expired",
      "404 (Not Found) if the trash with the specified id doesn't exist",
      "409 (Conflict) if the trash is already empty",
      "500 (Internal Server Error) if an error occurred on the server"
    ]
  },
  {
    "description": "Fills the specified trash",
    "route": "/api/user/fillTrash",
    "verb": "PUT",
    "headers": "Authorization: Basic <access_token>",
    "urlParameters": {
      "trashId": "the identifier of the trash"
    },
    "queryParameters": "None",
    "body": "None",
    "returnCodes": [
      "200 (OK) if the operation proceeded successfully",
      "400 (Bad Request) if the authentication token wasn't provided",
      "403 (Forbidden) if the authentication token is invalid or has expired",
      "404 (Not Found) if the trash with the specified id doesn't exist",
      "409 (Conflict) if the trash is already empty",
      "500 (Internal Server Error) if an error occurred on the server"
    ]
  },
  {
    "description": "Deletes the specified trash owned by the authenticated user",
    "route": "/api/user/deleteTrash",
    "verb": "DELETE",
    "headers": "Authorization: Basic <access_token>",
    "urlParameters": {
      "trashId": "the identifier of the trash"
    },
    "queryParameters": "None",
    "body": "None",
    "returnCodes": [
      "200 (OK) if the operation proceeded successfully",
      "400 (Bad Request) if the authentication token wasn't provided",
      "403 (Forbidden) if the authentication token is invalid or has expired",
      "404 (Not Found) if the trash with the specified id doesn't exist",
      "500 (Internal Server Error) if an error occurred on the server"
    ]
  },
  {
    "description": "Shows informations about the evolution of the authenticated user's waste",
    "route": "/api/user/evolution",
    "verb": "GET",
    "headers": "Authorization: Basic <access_token>",
    "urlParameters": "None",
    "queryParameters": "None",
    "body": "None",
    "returnCodes": [
      "200 (OK) if the operation proceeded successfully",
      "400 (Bad Request) if the authentication token wasn't provided",
      "403 (Forbidden) if the authentication token is invalid or has expired",
      "500 (Internal Server Error) if an error occurred on the server"
    ]
  }
]
```

### Database model

<p align="center">
  <img alt="Database model" src="dist/sql_model.png">
</p>

### Website mockups

#### Login
![Login](dist/website_mockups/Login.png)

#### Register
![Register](dist/website_mockups/Register.png)

#### Home page
![Register](dist/website_mockups/Home.png)

#### Evolution
![Register](dist/website_mockups/Evolution.png)
