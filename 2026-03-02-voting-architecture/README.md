# 🧬 02.MAR.2026 - ARCHITECTURE KATA

Template: https://github.com/diegopacheco/tech-resources/blob/master/arch-doc-template.md

You must design a Realtime voting system with the following requirements:
    1. Never loose Data
    2. Be secure and prevent bots and bad actors
    3. Handle 300M users 
    4. Handle peak of 250k RPS
    5. Must ensure users vote only once
    6. Should be Realtime
    
Restrictions:
    • Serverless
    • MongoDB
    • On-Premise, Google Cloude, Azure
    • OpenShift
    • Mainframes
    • Monolith Solutions

## 🏛️ Structure

### 1. 🎯 Problem Statement and Context

What is the problem? What is the context of the problem?
Example:
```
The problem is to sell shoes online, the main issue with buying shoes online is 
how we will make our users buy shoes if they cannot make them fit? We would need
to have a huge selectio and find ways to people find they perpect show at the 
same time market teams would need to change campains all the time, we need to
have way to make things fast and dynamic.
```
Recomended Reading: http://diego-pacheco.blogspot.com/2021/10/breaking-problems-down.html

### 2. 🎯 Goals

List in form of bullets what goals do have. Here it's great to have 5-10 lines.
Example:
```
1. Solution needs to be fast! Performance for all operations bellow ~1 ms.
2. Security is non-negociable! Security at-rest, transite, threat analysis and review for by at least 3 different people.
3. Composable solution. Users should be able to mix and match components instead of building all for scratch. ie: map component can be reused on counters component.
4. Work offline: Re-consiliation, CRDTs are a must.
5. Cloud-Native: All backend must be 100% cloud native, using open-source and should and should be cloud-agnostic, avoid propretaty apis.
```
Recommended Learning: http://diego-pacheco.blogspot.com/2020/05/education-vs-learning.html

### 3. 🎯 Non-Goals

List in form of bullets what non-goals do have. Here it's great to have 5-10 lines.
Example:
```
1. Be perfect: There will be mistakes, we dont want have automatic-rollback for everything.
2. DynamoDB: Dynamo is expensive, we want be away from the DB.
3. Serverless: Serverless has high latency, we do not want to use it.
4. Mobile-native: We want have one single codebase, therefore we will not have 2 mobile code bases(ios and android) thefore be native is not a goal.
5. ...
```
Recommended Reading: http://diego-pacheco.blogspot.com/2021/01/requirements-are-dangerous.html

### 📐 3. Principles

List in form of bullets what design principles you want to be followed, it's great to have 5-10 lines.
Example:
```
1. Low Coupling: We need to watch for coupling all times.
2. Flexibility: Users should be able to customize behavior without leaking the internals of the system. Leverage interfaces.
3. Observability: we should expose all key metrics on main features. Sucess and errors counters need to be exposed.
4. Testability: Chaos engineering is a must and property testing. Testing should be done by engineers all times.
5. Cache efficiency: Should leverage SSD caches and all forms of caches as much as possible.
```
Recommended Reading: http://diego-pacheco.blogspot.com/2018/01/stability-principles.html

### 🏗️ 4. Overall Diagrams

Here there will be a bunch of diagrams, to understand the solution.
```
🗂️ 4.1 Overall architecture: Show the big picture, relationship between macro components.
🗂️ 4.2 Deployment: Show the infra in a big picture. 
🗂️ 4.3 Use Cases: Make 1 macro use case diagram that list the main capability that needs to be covered. 
```
Recommended Reading: http://diego-pacheco.blogspot.com/2020/10/uml-hidden-gems.html

### 🧭 5. Trade-offs

List the tradeoffs analysis, comparing pros and cons for each major decision.
Before you need list all your major decisions, them run tradeoffs on than.
example:
Major Decisions: 
```
1. One mobile code base - should be (...)
2. Reusable capability and low latency backends should be (...)
3. Cache efficiency therefore should do (...)
```
Tradeoffs:
```
1. React Native vs (Flutter and Native)
2. Serverless vs Microservices
3. Redis vs Enbeded Caches
```
Each tradeoff line need to be:
```
PROS (+) 
  * Benefit: Explanation that justify why the benefit is true.
CONS (+)
  * Problem: Explanation that justify why the problem is true.
```
PS: Be careful to not confuse problem with explanation. 
<BR/>Recommended reading: http://diego-pacheco.blogspot.com/2023/07/tradeoffs.html

### 5.1 Backend

## Go (Golang)

**Pros**: Fast execution and compilation, simple and efficient concurrency through goroutines and channels, mature ecosystem with extensive libraries, easy deployment via compiled binaries, excellent tooling and IDE support.
**Cons**: Garbage collector can introduce occasional microsecond-level pauses under heavy load.

## Rust

**Pros**: Maximum performance with zero-cost abstractions, memory safety without garbage collection, deterministic performance for ultra-low-latency requirements, strong type system catches errors at compile time, no runtime overhead.
**Cons**: Longer compile times compared to Go. Smaller ecosystem compared to established languages.

## Java / Kotlin

**Pros:** Mature ecosystem, gret integration for Kafka Streams (unmatched for stateful stream processing), robust JVM with advanced JIT compilation, Spring framework for quick development.
**Cons**: GC tuning complexity at scale, higher memory footprint, slower startup times, unpredictable latency spikes during GC pauses, which is unacceptable for real-time voting where every millisecond matters.

### 🌏 6. For each key major component

What is a majore component? A service, a lambda, a important ui, a generalized approach for all uis, a generazid approach for computing a workload, etc...
```
6.1 - Class Diagram              : classic uml diagram with attributes and methods
6.2 - Contract Documentation     : Operations, Inputs and Outputs
6.3 - Persistence Model          : Diagrams, Table structure, partiotioning, main queries.
6.4 - Algorithms/Data Structures : Spesific algos that need to be used, along size with spesific data structures.
```

Exemplos of other components: Batch jobs, Events, 3rd Party Integrations, Streaming, ML Models, ChatBots, etc... 

Recommended Reading: http://diego-pacheco.blogspot.com/2018/05/internal-system-design-forgotten.html

### 🖹 7. Migrations

IF Migrations are required describe the migrations strategy with proper diagrams, text and tradeoffs.

### 🖹 8. Testing strategy

Explain the techniques, principles, types of tests and will be performaned, and spesific details how to mock data, stress test it, spesific chaos goals and assumptions.

### 🖹 9. Observability strategy

Explain the techniques, principles,types of observability that will be used, key metrics, what would be logged and how to design proper dashboards and alerts.

### 🖹 10. Data Store Designs

For each different kind of data store i.e (Postgres, Memcached, Elasticache, S3, Neo4J etc...) describe the schemas, what would be stored there and why, main queries, expectations on performance. Diagrams are welcome but you really need some dictionaries.

### 🖹 11. Technology Stack

Describe your stack, what databases would be used, what servers, what kind of components, mobile/ui approach, general architecture components, frameworks and libs to be used or not be used and why.

- Backend:

**Go** has a lightweight concurrency model, powered by goroutines and channels, that enables massive parallel request handling without the overhead of traditional threading models, serving as a perfect choice for our distributed system. This choice will grant lower latency and smaller memory footprint, which is critical for high-RPS microservices. It also provides excellent built-in networking libraries, simplifying the development of HTTP, WebSocket, and gRPC services. The compiler produces single, statically linked binaries that streamline deployment and enable quick startup times for horizontal scaling. Go also benefits from a mature ecosystem with robust support for distributed systems technologies like Kafka, Redis, CockroachDB, PostgreSQL, and various distributed caches.

- Frontend: 


### 🖹 12. References

* Architecture Anti-Patterns: https://architecture-antipatterns.tech/
* EIP https://www.enterpriseintegrationpatterns.com/
* SOA Patterns https://patterns.arcitura.com/soa-patterns
* API Patterns https://microservice-api-patterns.org/
* Anti-Patterns https://sourcemaking.com/antipatterns/software-development-antipatterns
* Refactoring Patterns https://sourcemaking.com/refactoring/refactorings
* Database Refactoring Patterns https://databaserefactoring.com/
* Data Modelling Redis https://redis.com/blog/nosql-data-modeling/
* Cloud Patterns https://docs.aws.amazon.com/prescriptive-guidance/latest/cloud-design-patterns/introduction.html
* 12 Factors App https://12factor.net/
* Relational DB Patterns https://www.geeksforgeeks.org/design-patterns-for-relational-databases/
* Rendering Patterns https://www.patterns.dev/vanilla/rendering-patterns/
* REST API Design https://blog.stoplight.io/api-design-patterns-for-rest-web-services
