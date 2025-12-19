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
```
We have to design an architecture for a realtime voting system that will handle millions of users and high peaks of requests per second. We must ensure a smooth experience to the user when voting, each vote is unique and the user can check realtime results. It has to be reliable, scalable, secure, recoverable and auditable.
```

### 2. 🎯 Goals

```
1. Realtime results - It has to provide realtime results so users can follow it after voting
2. Unique votes - Each user will vote only once
2. Security is a must - we must implement ways of preventing bots, DDoS attacks and secure the web layer
3. Encryption - Encryption at rest and transit
5. Scalability - It has to scale as traffic grows
6. Fraud prevention - There has to be fraud prevention in place preventing bots or internal actors from adding artifitial votes.
7. Auditable - There has to be accessible ways of third-party companies audit the voting results when asked
```

### 3. 🎯 Non-Goals

```
1. Serverless: it has high latency, cold startup and resources and execution time are limited.
2. MongoDB - WHY ?
3. On-Premisse and other clouds than AWS: AWS is the chosen cloud as it's more reliable and scalable
4. OpenShift - OpenShift is a proprietary solution, prefer K8s or other opensource microservice solution.
5. Mainframe or Monolith solutions - The system will need to automatically scale, quickly and on-demand.
```

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

No migration required in this project


### 🖹 8. Testing strategy

Explain the techniques, principles, types of tests and will be performaned, and spesific details how to mock data, stress test it, spesific chaos goals and assumptions.

- What kind of tests are we going to implement ?
- What tests we should have more in our project ?
- When tests are going to run?
- Which tools are we going to use ?
- What are we going to test ? Any KPIs to be defined ?
- Which are the most important features ?


### 🖹 9. Observability strategy

Explain the techniques, principles,types of observability that will be used, key metrics, what would be logged and how to design proper dashboards and alerts.

#### 9.1 Metrics collection and Dashboards

- What metrics to collect ?
- What dashboards to use? which metrics to display ?

#### 9.2 Logging

- What is important to log ?
- How to make it easy to keep track of logs ?

#### 9.3 Alerting and Incident Response

- What will trigger alerts ? What are the boundaries to evaluate ?

#### 9.4 Distributed Tracing

### 🖹 10. Data Store Designs

For each different kind of data store i.e (Postgres, Memcached, Elasticache, S3, Neo4J etc...) describe the schemas, what would be stored there and why, main queries, expectations on performance. Diagrams are welcome but you really need some dictionaries.

- Queries examples, per service?
- Partitioning ?
- Caching ?

### 🖹 11. Technology Stack

Describe your stack, what databases would be used, what servers, what kind of components, mobile/ui approach, general architecture components, frameworks and libs to be used or not be used and why.

- Backend:

**Go** has a lightweight concurrency model, powered by goroutines and channels, that enables massive parallel request handling without the overhead of traditional threading models, serving as a perfect choice for our distributed system. This choice will grant lower latency and smaller memory footprint, which is critical for high-RPS microservices. It also provides excellent built-in networking libraries, simplifying the development of HTTP, WebSocket, and gRPC services. The compiler produces single, statically linked binaries that streamline deployment and enable quick startup times for horizontal scaling. Go also benefits from a mature ecosystem with robust support for distributed systems technologies like Kafka, Redis, CockroachDB, PostgreSQL, and various distributed caches.

- Frontend: 
- Infrastructure:
- Data:


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
