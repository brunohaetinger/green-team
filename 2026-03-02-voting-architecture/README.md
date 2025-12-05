# 🧬 02.MAR.2026 - ARCHITECTURE KATA

## Objective 
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
1. Serverless: it has high latency, cold startup, resources and execution time are limited.
2. MongoDB - Due to its eventual consistency characteristic, reading may not be realtime.
3. On-Premisse and other clouds than AWS: AWS is the chosen cloud as it's more reliable and scalable
4. OpenShift - OpenShift is a proprietary solution, prefer K8s or other opensource microservice solution.
5. Mainframe or Monolith solutions - The system will need to automatically scale, quickly and on-demand.
```

### 📐 3. Principles

Design principles we want to follow:

```
1. Low Coupling: We need to watch for coupling all times.
2. Isolation: Resources and environments should be isolated
3. Reliability: The system should be highly-available(99.9%) mainly during peaks
4. Observability: we should expose all key metrics on main features. Sucess and errors counters need to be exposed.
5. Testability: Load testing, unit, integration and E2E tests should be done by engineers all times.
6. Cache efficiency: Should leverage SSD caches and all forms of caches as much as possible.
```

Recommended Reading: http://diego-pacheco.blogspot.com/2018/01/stability-principles.html

### 🏗️ 4. Overall Diagrams

Here is a bunch of diagrams to understand the solution
[coiso](http://asd)

🗂️ 4.1 [Overall](arch.drawio)    architecture: Show the big picture, relationship between macro components.
🗂️ 4.2 Deployment: Show the infra in a big picture. 
🗂️ 4.3 Use Cases: Make 1 macro use case diagram that list the main capability that needs to be covered. 

Recommended Reading: http://diego-pacheco.blogspot.com/2020/10/uml-hidden-gems.html

### 🧭 5. Trade-offs

List the tradeoffs analysis, comparing pros and cons for each major decision.
Before you need list all your major decisions, them run tradeoffs on than.
example:
Major Decisions: 
```
1. Language 

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
