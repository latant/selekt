# selekt

A tool for mapping Kotlin objects to trees, inspired by [GraphQL](https://graphql.org/).

## Purpose

A usual use-case is when we provide different views of the same data for different clients.
One proven solution is to create different [DTO](https://en.wikipedia.org/wiki/Data_transfer_object)-s that model the same business entity, with certain details included of it. Most of the cases, the definitions are duplicated, while the main purpuse of their existence is to ***selekt*** the needed parts of the original data.

### Example

The following classes are the domain model.

```kotlin
data class Person(
    val name: String,
    val birth: Int,
    val moviesActed: List<Movie>,
    val moviesDirected: List<Movie>,
    val moviesProduced: List<Movie>,
    val followers: List<Person>,
)

data class Movie(
    val title: String,
    val tagline: String,
    val released: Int,
    val director: Person,
    val writer: Person,
    val producer: Person,
    val actors: List<Person>,
)
```

We need to map the instances differently for different clients.
Let's create some DTO classes, the mapping to them, and the code that stringifies them to json.

```kotlin
data class PersonDetailsDTO(
    val name: String, 
    val birth: String,
)

data class MovieDetailsDTO(
    val title: String,
    val tagline: String,
    val released: Int,
    val director: PersonDetailsDTO,
    val producerName: String,
    val actors: List<String>,
)

data class ActorDetailsDTO(
    val name: String,
    val birth: Int,
    val movieTitles: List<String>,
    val peopleFollowed: List<PersonDetailsDTO>,
)

fun Person.toPersonDetailsDTO() = PersonDetailsDTO(
    name = name,
    birth = birth
)

fun Person.toActorDetailsDTO() = ActorDetailsDTO(
    name = name,
    birth = birth,
    movieTitles = moviesActed.map { it.title },
    peopleFollowed = peopleFollowed.map { it.toPersonDetailsDTO() }
)

fun Movie.toMovieDetailsDTO() = MovieDetailsDTO(
    title = title,
    tagline = tagline,
    released = released,
    director = director.toPersonDetailsDTO(),
    producerName = producer.name,
    actors = actors.map { it.toPersonDetailsDTO() }
)

fun Person.toDetailsJson() = encoder.stringify(toPersonDetailsDTO())
fun Person.toActorDetailsJson() = encoder.stringify(toActorDetailsDTO())
fun Movie.toDetailsJson() = encoder.stringify(toMovieDetailsDTO())
```

Since we are using a concise language like Kotlin, writing it is not a huge pain, but there are downsides:

- Code duplicates, like properties' names and types make the code harder to maintain on changes.
- The full structure of a nested DTO like MovieDetailsDTO can only be observed by checking the types of the properties, then checking their structure and so on.
- Boilerplate mapping between the types.
- Runtime overhead in storage and memory caused by the mapping.

***selekt*** aims to solve all these problems.

### Solution

***selekt*** generates [type-safe DSL](https://kotlinlang.org/docs/reference/type-safe-builders.html)-s for the classes that are annotated them with `@Queryable` and can be used as follows.

```kotlin
fun Person.toDetailsJson() = query(encoder) {
    name()
    birth()
}

fun Person.toActorDetialsJson() = query(encoder) {
    name()
    birth()
    moviesActed { 
        title()
    }
    peopleFollowed { 
        name()
        birth()
    }
}

fun Movie.toDetailsJson() = query(encoder) {
    title()
    tagline()
    released()
    director { 
        name()
        birth()
    }
    producer { 
        name()
        birth()
    }
    actors { 
        name()
        birth()
    }
}
```

The generated builders provide arbitrary combinations and depth.

```kotlin
fun Person.deeplyNestedData() = query(encoder) {
    name()
    birth()
    followers {
        name()
        peopleFollowed { 
            name()
            moviesActed { 
                actors { 
                    moviesDirected { 
                        title()
                    }
                }
            }
        }
    }
}
```

Advantages:
- The queries are type-safe
- The code is more maintainable. If Person's birth property's type is changed to String, the queries don't break.
- The queries are transparent and explain the structure of the data well.
- There is no need to write additional code, the mapping code generated.
- The encoder can directly serialize the data without storing it first.
