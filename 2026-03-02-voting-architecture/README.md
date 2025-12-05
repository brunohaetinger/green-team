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

>We have to design an architecture for a **realtime voting system** that will handle
>millions of users and high peaks of requests per second. 
>
> **Requirements**: 
>
> * We must ensure a smooth experience to the user when voting
> * Each vote must be unique
> * The user can check realtime results. 
> * It has to be
>   * reliable
>   * scalable
>   * secure
>   * recoverable
>   * auditable


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

#### Cache layer diagram

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

#### 5.3 Websocket, SSE and Polling

##### 5.3.1 Websocket
A full-duplex, persisnt connection where client can push data at any time.

PROS (+)
  * Real-time, bidirectional communication.
  * Minimal overhead after connection is established.
  * High throughput, good for chat apps, multiplayer games, collaborative editors.
  * Works well for many messages per second.

CONS (+)
  * More complex to implement than other.
  * Not ideal for simple one-way updates.
  * Not supported by older proxies without WebSocket upgrades.

##### 5.3.2 Server-Sent Events (SSE)
A single long-lived http connection where server pushes updates.  
Unidirectional (client cannot send messages back over the same channel).

PROS (+)
  * Very simple to implement (just a text stream from server).
  * Auto-reconnect built into the browser EventSource.
  * Uses regular HTTP-proxy-friendly.
  * Lightweight for one-direction real-time feeds.

CONS (+)
  * Not bidirectional.
  * Not ideal for very high-frequency updates.
  * Limited browser support on some older/embedded environments.
  * No binary data (text only unless you encode).

##### 5.3.3 Polling
Client periodically requests new data with repeated HTTP requests.

PROS (+)
  * Easiest to implement.
  * Works everywhere, no special protocols.
  * Good for low-frequency or low-priority updates.

CONS (+)
  * Inefficient: many requests with no data = waste.
  * Higher latency between updates (depends on poll interval).
  * Scales poorly (many clients -> many HTTP requests).

#### 5.4 Cache layer

##### 5.4.1 Redis
PROS (+)
  * Rich Data Structures: Redis supports hashes, sets, sorted sets, bitmaps, and atomic counters, enabling complex real-time operations such as vote counting and user uniqueness checks.
  * Atomic Operations: Operations like INCR, HINCRBY, SETNX, and Lua scripts guarantee correctness under high concurrency, which is essential for voting systems.
  * Persistence Options: Redis offers RDB and AOF persistence, ensuring data durability during crashes.
  * Pub/Sub Support: Redis can push real-time updates through Pub/Sub, enabling instant updates for dashboards and WebSocket-based clients.
  * Replication & Clustering: Redis Cluster provides automatic sharding and replication for high availability and horizontal scalability.

CONS (–)
  * Higher Resource Usage: Rich data structures and persistence add memory overhead and CPU use, making Redis more expensive to operate at scale.
  * More Operational Complexity: Redis clustering, failover, and persistence tuning require deeper operational knowledge.
  * Single-Threaded per Shard: Although extremely fast, operations are serialized per shard, which may limit throughput for some workloads.
  * Overkill for Simple Cache: If you only need GET/SET caching with no atomicity or structures, Redis provides features you don’t need and increases overhead.

##### 5.4.2 Memcached
PROS (+)
  * Extremely Lightweight: Memcached is optimized for pure in-memory key-value caching with very low overhead, giving it high throughput for simple GET/SET.
  * Simple Horizontal Scaling: Memcached nodes are stateless and client-side sharded, making scaling out trivial.
  * Lower Cost: Since it uses less memory overhead and no persistence, Memcached is cheaper to run at large scale.
  * Ideal for Simple Cache Layer: Perfect for caching HTML fragments, sessions, or API responses where atomicity and structure are not needed.

CONS (–)
  * No Persistence: Data is lost on restart or failure, making Memcached unsuitable for scenarios where counts or state must survive crashes.
  * No Complex Data Types: Only supports raw key-value pairs, preventing efficient server-side counters, sets, or hash operations.  
  * No Pub/Sub or Streaming: Cannot support real-time update features, forcing additional components for push-based dashboards.
  * No Replication Built-In: Failures mean immediate data loss unless handled at the application layer.

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

##### 10.1 Redis
###### 10.1.1 Creaating the real-time vote counter
```
# HINCRBY is atomic: safe for concurrent voting.

Key: poll:<poll_id>:counts
Type: HASH
Fields:
  <option_id>:<count>
```
Operations
```
HSET poll:<pull_id>:counts A 0 B 0 C 0 # to create
HINCRBY poll:<pull_id>:counts <OPTION_ID> <QUANTITY_TO_INCREMENT> # to increment into the vote list
HGETALL poll:<pull_id>:counts # to get all options and values from the vote list
HGET poll:<pull_id>:counts "B" # to get a value from specific vote option
```
###### 10.1.2 Ensuring Unique Votes
```
# HINCRBY is atomic: safe for concurrent voting.
Key: poll:<poll_id>:voters
Type: SET
Value: <user_id>
```
Operations
```
SADD poll:<poll_id>:voters "user_001"  # to create
SISMEMBER poll:<poll_id>:voters "user_001" # check if the value exists
```
###### 10.1.3 Pub/Sub for Live Result Updates
```
Channel: poll:<poll_id>:updates
Type: PUBSUB
```
operations
```
PUBLISH poll:<pull_id>:updates '{"option":"A","count":12004}'
SUBSCRIBE poll:<pull_id>:updates
```

### 🖹 11. Technology Stack

Describe your stack, what databases would be used, what servers, what kind of components, mobile/ui approach, general architecture components, frameworks and libs to be used or not be used and why.

- Backend:
- Frontend: 

#### 11.3 Websocket
WebSockets are chosen because they are bidirectional, scalable, secure, reliable, and optimized for real-time systems - all critical requirements for a massive voting platform.

WHY:
  * Bidirecional communication: Clients must send votes, and the server must confirm them.
  * SSE is one-way only (server -> client): WS support full two-way messaging.
  * Scalablity: We need to support 300M users and 250k RPS, SSE uses heavy HTTP connections and does not scale well to millions, Websockets are optimized for millions of concurrent connections.
  * Lower latency and better performance: WS have lighter frames, less overhead, and better throughput, SSE becomes inefficient at very hight RPS.

##### 11.4 Redis
We chose Redis as the caching layer for the voting system due to its strong support for atomic operations, which are essential to guarantee correctness under high concurrency.  
Redis provides native atomic commands, such as `INCR`, `HSET`, and `HINCRBY`, which ensure that vote increments and state transitions occur safely even when millions of users interact simultaneously.

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
