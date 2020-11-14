<div align="center">
  <img alt="logo" src="https://tesfy.s3.us-west-2.amazonaws.com/images/logo.png" width="220">
</div>

<p align="center">
  A lightweight A/B Testing and Feature Flag Kotlin library focused on performance ⚡️
</p>

<div align="center">
    <a href="https://github.com/tesfy/tesfy-kotlin/blob/master/LICENSE">    
        <img alt="license badge" src="https://img.shields.io/badge/license-MIT-blue.svg">    
    </a>
    <a href="https://github.com/tesfy/tesfy-kotlin/actions">    
        <img alt="tests badge" src="https://github.com/tesfy/tesfy-kotlin/workflows/CI/badge.svg">   
    </a>
    <a href="https://search.maven.org/artifact/io.tesfy/tesfy-kotlin/1.0.0/jar">    
        <img alt="maven central" src="https://maven-badges.herokuapp.com/maven-central/io.tesfy/tesfy-kotlin/badge.svg">
    </a>
</div>

[Tesfy](https://github.com/andresz1/tesfy) provides a simple but complete solution to develop A/B Tests and Feature Flags on both server and client side without relying in any storage layer. The main features of this library are:
- Lightweight and focused on performance
- Experiments
- Feature Flags
- Audience definition using [jsonLogic](http://jsonlogic.com/)
- Traffic Allocation
- Sticky Bucketing

## Usage

### Installation
#### Gradle
```
implementation("io.tesfy:tesfy-kotlin")
```

### Initialization
Render the provider with a datafile. A datafile is a `json` that defines the experiments and features avaliable. Ideally this file should be hosted somewhere outside your application (for example in [S3](https://aws.amazon.com/s3/)), so it could be fetched during boostrap or every certain time. This will allow you to make changes to the file without deploying the application.

The only needed class you will need to call to get a result from is called `Engine` in this class you will find all the logic to check if your experiment or your feature flag is enabled or disabled for the parameters passed.

```kt
val datafile = Datafile(
  mapOf(
    "experiment-1" to Experiment(
      "experiment-1",
      100,
      listOf(
        Variation(
          "0", 50
        ),
        Variation(
          "1", 50
        )
      ),
      mapOf(
        "==" to listOf(mapOf(
          "var" to "countryCode"
        ), "us")
      )
    )
  ),
  mapOf()
)
val userId = "4qz936x2-62ex"
val attributes = mapOf("countryCode" to "us")
val engine = Engine(datafile, userId, attributes, storage)

val variationId = engine.getVariationId("experiment-1", userId, attributes)
```

As you can see, the audience (JsonLogic) was defined as a kotlin object but if you want, you can also replace the JsonLogic with a well formatted string containing the JsonLogic needed.

For a concrete example of how to use the library you could [check our integration test file here](https://github.com/tesfy/tesfy-kotlin/blob/master/src/test/kotlin/io/tesfy/itest/ITestEngine.kt).


### Experiments

Check which variation of an experiment is assigned to an user.

```kt
val userId = "676380e0-7793-44d6-9189-eb5868e17a86"
val experimentId = "experiment-1"

tesfyEngine.getVariationId(experimentId, userId) // "1"
```

### Feature Flags

Check if a feature is enabled for a user.

```kt
val userId = "676380e0-7793-44d6-9169-eb4208e17a86"
val featureId = "feature-1"

tesfyEngine.isFeatureEnabled(featureId, userId) // true
```

### Audience attributes

```kt
val userId = "676380e0-7793-44d6-9169-eb4208e17a86"
val featureId = "feature-1"

tesfyEngine.getVariationId(featureId, userId, mapOf("countryCode" to "ve")) // "0"
tesfyEngine.getVariationId(featureId, userId, mapOf("countryCode" to "es")) // null
```

### Sticky bucketing

Optionally you could add a storage layer when instantiating the tesfy engine. This layer could be whatever you would like (memory cache, 3rd party integrations, databases, etc). In this way even if allocation or attributes changes, the users defined will stick with the same variation.

```kt
val datafile = Datafile(
  mapOf(
    "experiment-1" to Experiment(
      "experiment-1",
      100,
      listOf(
        Variation(
          "0", 50
        ),
        Variation(
          "1", 50
        )
      ),
      mapOf(
        "==" to listOf(mapOf(
          "var" to "countryCode"
        ), "us")
      )
    )
  ),
  mapOf()
)
val userId = "4qz936x2-62ex"
val attributes = mapOf("countryCode" to "us")
val engine = Engine(datafile, userId, attributes, storage)
val storage: Storage = StorageImpl()

val variationId = engine.getVariationId("experiment-1", userId, attributes, storage) //  "0"
```

### Considerations

This version of the library is using Unsigned Integers from Kotlin and currently is in Beta,
for more information please read the [following link](https://kotlinlang.org/docs/reference/basic-types.html#beta-status-of-unsigned-integers).

## Feedback

Pull requests, feature ideas, and bug reports are welcome. We highly appreciate any feedback.