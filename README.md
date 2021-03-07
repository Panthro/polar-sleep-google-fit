# polar-gfit-sleep

polar-gfit-sleep is your new project powered by [Ktor](http://ktor.io) framework.

<img src="https://repository-images.githubusercontent.com/40136600/f3f5fd00-c59e-11e9-8284-cb297d193133" alt="Ktor" width="100" style="max-width:20%;">

Company website: rafaelroman.com Ktor Version: 1.5.2 Kotlin Version: 1.4.10
BuildSystem: [Gradle with Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html)

# Ktor Documentation

Ktor is a framework for quickly creating web applications in Kotlin with minimal effort.

* Ktor project's [Github](https://github.com/ktorio/ktor/blob/master/README.md)
* Getting started with [Gradle](http://ktor.io/quickstart/gradle.html)
* Getting started with [Maven](http://ktor.io/quickstart/maven.html)
* Getting started with [IDEA](http://ktor.io/quickstart/intellij-idea.html)

Selected Features:

* [Routing](#routing-documentation-jetbrainshttpswwwjetbrainscom)
* [Locations](#locations-documentation-jetbrainshttpswwwjetbrainscom)
* [CallLogging](#calllogging-documentation-jetbrainshttpswwwjetbrainscom)

## Routing Documentation ([JetBrains](https://www.jetbrains.com))

Allows to define structured routes and associated handlers.

### Description

Routing is a feature that is installed into an Application to simplify and structure page request handling. This page explains the routing feature. Extracting
information about a request, and generating valid responses inside a route, is described on the requests and responses pages.

```application.install(Routing) {
    get("/") {
        call.respondText("Hello, World!")
    }
    get("/bye") {
        call.respondText("Good bye, World!")
    }

```

`get`, `post`, `put`, `delete`, `head` and `options` functions are convenience shortcuts to a flexible and powerful routing system. In particular, get is an
alias to `route(HttpMethod.Get, path) { handle(body) }`, where body is a lambda passed to the get function.

### Usage

## Routing Tree

Routing is organized in a tree with a recursive matching system that is capable of handling quite complex rules for request processing. The Tree is built with
nodes and selectors. The Node contains handlers and interceptors, and the selector is attached to an arc which connects another node. If selector matches
current routing evaluation context, the algorithm goes down to the node associated with that selector.

Routing is built using a DSL in a nested manner:

```kotlin
route("a") { // matches first segment with the value "a"
  route("b") { // matches second segment with the value "b"
     get {…} // matches GET verb, and installs a handler
     post {…} // matches POST verb, and installs a handler
  }
}
```

```kotlin
method(HttpMethod.Get) { // matches GET verb
   route("a") { // matches first segment with the value "a"
      route("b") { // matches second segment with the value "b"
         handle { … } // installs handler
      }
   }
}
```kotlin
route resolution algorithms go through nodes recursively discarding subtrees where selector didn't match.

Builder functions:
* `route(path)` – adds path segments matcher(s), see below about paths
* `method(verb)` – adds HTTP method matcher.
* `param(name, value)` – adds matcher for a specific value of the query parameter
* `param(name)` – adds matcher that checks for the existence of a query parameter and captures its value
* `optionalParam(name)` – adds matcher that captures the value of a query parameter if it exists
* `header(name, value)` – adds matcher that for a specific value of HTTP header, see below about quality

## Path
Building routing tree by hand would be very inconvenient. Thus there is `route` function that covers most of the use cases in a simple way, using path.

`route` function (and respective HTTP verb aliases) receives a `path` as a parameter which is processed to build routing tree. First, it is split into path segments by the `/` delimiter. Each segment generates a nested routing node.

These two variants are equivalent:

```kotlin
route("/foo/bar") { … } // (1)

route("/foo") {
   route("bar") { … } // (2)
}
```

### Parameters

Path can also contain parameters that match specific path segment and capture its value into `parameters` properties of an application call:

```kotlin
get("/user/{login}") {
   val login = call.parameters["login"]
}
```

When user agent requests `/user/john` using `GET` method, this route is matched and `parameters` property will have `"login"` key with value `"john"`.

### Optional, Wildcard, Tailcard

Parameters and path segments can be optional or capture entire remainder of URI.

* `{param?}` –- optional path segment, if it exists it's captured in the parameter
* `*` –- wildcard, any segment will match, but shouldn't be missing
* `{...}` –- tailcard, matches all the rest of the URI, should be last. Can be empty.
* `{param...}` –- captured tailcard, matches all the rest of the URI and puts multiple values for each path segment into `parameters` using `param` as key.
  Use `call.parameters.getAll("param")` to get all values.

Examples:

```kotlin
get("/user/{login}/{fullname?}") { … }
get("/resources/{path...}") { … }
```

## Quality

It is not unlikely that several routes can match to the same HTTP request.

One example is matching on the `Accept` HTTP header which can have multiple values with specified priority (quality).

```kotlin
accept(ContentType.Text.Plain) { … }
accept(ContentType.Text.Html) { … }
```

The routing matching algorithm not only checks if a particular HTTP request matches a specific path in a routing tree, but it also calculates the quality of the
match and selects the routing node with the best quality. Given the routes above, which match on the Accept header, and given the request
header `Accept: text/plain; q=0.5, text/html` will match `text/html` because the quality factor in the HTTP header indicates a lower quality fortext/plain (
default is 1.0).

The Header `Accept: text/plain, text/*` will match `text/plain`. Wildcard matches are considered less specific than direct matches. Therefore the routing
matching algorithm will consider them to have a lower quality.

Another example is making short URLs to named entities, e.g. users, and still being able to prefer specific pages like `"settings"`. An example would be

* `https://twitter.com/kotlin` -– displays user `"kotlin"`
* `https://twitter.com/settings` -- displays settings page

This can be implemented like this:

```kotlin
get("/{user}") { … }
get("/settings") { … }
```

The parameter is considered to have a lower quality than a constant string, so that even if `/settings` matches both, the second route will be selected.

### Options

No options()

## Locations Documentation ([JetBrains](https://www.jetbrains.com))

Allows to define route locations in a typed way

### Description

Ktor provides a mechanism to create routes in a typed way, for both: constructing URLs and reading the parameters.

### Usage

## Installation

The Locations feature doesn't require any special configuration:

```kotlin
install(Locations)
```

## Defining route classes

For each typed route you want to handle, you need to create a class (usually a data class) containing the parameters that you want to handle.

The parameters must be of any type supported by the `Data Conversion` feature. By default, you can use `Int`, `Long`, `Float`, `Double`, `Boolean`, `String`,
enums and `Iterable` as parameters.

### URL parameters

That class must be annotated with `@Location` specifying a path to match with placeholders between curly brackets `{` and `}`. For example: `{propertyName}`.
The names between the curly braces must match the properties of the class.

```kotlin
@Location("/list/{name}/page/{page}")
data class Listing(val name: String, val page: Int)
```

* Will match: `/list/movies/page/10`
* Will construct: `Listing(name = "movies", page = 10)`

### GET parameters

If you provide additional class properties that are not part of the path of the `@Location`, those parameters will be obtained from the `GET`'s query string
or `POST` parameters:

```kotlin
@Location("/list/{name}")
data class Listing(val name: String, val page: Int, val count: Int)
```

* Will match: `/list/movies?page=10&count=20`
* Will construct: `Listing(name = "movies", page = 10, count = 20)`

## Defining route handlers

Once you have defined the classes annotated with `@Location`, this feature artifact exposes new typed methods for defining route handlers: `get`, `options`
, `header`, `post`, `put`, `delete` and `patch`.

```kotlin
routing {
    get<Listing> { listing ->
        call.respondText("Listing ${listing.name}, page ${listing.page}")
    }
}
```

## Building URLs

You can construct URLs to your routes by calling `application.locations.href` with an instance of a class annotated with `@Location`:

```kotlin
val path = application.locations.href(Listing(name = "movies", page = 10, count = 20))
```

So for this class, `path` would be `"/list/movies?page=10&count=20"`.

```kotlin
@Location("/list/{name}") data class Listing(val name: String, val page: Int, val count: Int)
```

If you construct the URLs like this, and you decide to change the format of the URL, you will just have to update the `@Location` path, which is really
convenient.

## Subroutes with parameters

You have to create classes referencing to another class annotated with `@Location` like this, and register them normally:

```kotlin
routing {
    get<Type.Edit> { typeEdit -> // /type/{name}/edit
        // ...
    }
    get<Type.List> { typeList -> // /type/{name}/list/{page}
        // ...
    }
}
```

To obtain parameters defined in the superior locations, you just have to include those property names in your classes for the internal routes. For example:

```kotlin
@Location("/type/{name}") data class Type(val name: String) {
	// In these classes we have to include the `name` property matching the parent.
	@Location("/edit") data class Edit(val parent: Type)
	@Location("/list/{page}") data class List(val parent: Type, val page: Int)
}
```

### Options

No options()

## CallLogging Documentation ([JetBrains](https://www.jetbrains.com))

Logs client requests

### Description

You might want to log client requests: and the Call Logging feature does just that. It uses
the `ApplicationEnvironment.log(LoggerFactory.getLogger("Application"))` that uses `slf4j` so you can easily configure the output. For more information on
logging in Ktor, please check the logging in the ktor page.

### Usage

## Basic usage

The basic unconfigured feature logs every request using the level `TRACE`:

```kotlin
install(CallLogging)

```

## Configuring

This feature allows you to configure the log level and filtering the requests that are being logged:

```kotlin
install(CallLogging) {
    level = Level.INFO
    filter { call -> call.request.path().startsWith("/section1") }
    filter { call -> call.request.path().startsWith("/section2") }
    // ...
}

```

The filter method keeps an allow list of filters. If no filters are defined, everything is logged. And if there are filters, if any of them returns true, the
call will be logged.

In the example, it will log both: `/section1/*` and `/section2/*` requests.

## MDC

The `CallLogging` feature supports `MDC` (Mapped Diagnostic Context) from `slf4j` to associate information as part of the request.

When installing the `CallLogging`, you can configure a parameter to associate to the request with the mdc method. This method requires a key name, and a
function provider. The context would be associated (and the providers will be called) as part of the `Monitoring` pipeline phase.

```kotlin
install(CallLogging) {
    mdc(name) { // call: ApplicationCall ->
        "value"
    }
    // ...
}

```

### Options

No options()

# Reporting Issues / Support

Please use [our issue tracker](https://youtrack.jetbrains.com/issues/KTOR) for filing feature requests and bugs. If you'd like to ask a question, we
recommmend [StackOverflow](https://stackoverflow.com/questions/tagged/ktor) where members of the team monitor frequently.

There is also community support on the [Kotlin Slack Ktor channel](https://app.slack.com/client/T09229ZC6/C0A974TJ9)

# Reporting Security Vulnerabilities

If you find a security vulnerability in Ktor, we kindly request that you reach out to the JetBrains security team via
our [responsible disclosure process](https://www.jetbrains.com/legal/terms/responsible-disclosure.html).

# Contributing

Please see [the contribution guide](CONTRIBUTING.md) and the [Code of conduct](CODE_OF_CONDUCT.md) before contributing.

TODO: contribution of features guide (link)
