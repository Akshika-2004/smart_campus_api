API Design overview
The API follows REST architectural principles with a clear, hierarchical resource structure:

/api/v1                                      → Discovery endpoint (HATEOAS entry point)
/api/v1/rooms                                → Room collection
/api/v1/rooms/{roomId}                       → Individual room
/api/v1/sensors                              → Sensor collection
/api/v1/sensors/{sensorId}                   → Individual sensor
/api/v1/sensors/{sensorId}/readings          → Sensor reading history (sub-resource)

All data is stored in memory using concurrentHashMap and ArrayList. No database is used.


How to build and run 
 Prerequisites
	Java JDK 11 or higher
	Apache maven 3.6+

Steps
1.	Clone the repository:
git clone https://github.com/Akshika-2004/smart_campus_api
cd smart-campus-api
2.	Build the project:
mvn clean package
3.	Run the server:
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
4.	The API will be available at:
http://localhost:8080/api/v1

sample curl commands
1.	Discovery endpoint
                       url -X GET http://localhost:8080/api/v1

2.	Get all rooms
curl -X GET http://localhost:8080/api/v1/rooms

3.	Create a new room
curl -X POST http://localhost:8080/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{"id":"CS-101","name":"Computer Science Lecture Hall","capacity":80}'

4.	Get a specific room
curl -X GET http://localhost:8080/api/v1/rooms/CS-101


5.	Create a sensor linked to a room
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-002","type":"Temperature","status":"ACTIVE","currentValue":21.0,"roomId":"CS-101"}'


6.	Get sensors filtered by type
curl -X GET http://localhost:8080/api/v1/sensors?type=Temperature


7.	Post a sensor reading 
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-002/readings \
  -H "Content-Type: application/json" \
  -d '{"value":23.5}'


8.	Get reading history for a sensor
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-002/readings


9.	Try to delete a room with sensors
curl -X DELETE http://localhost:8080/api/v1/rooms/CS-101


10.	Try creating a sensor with invalid roomId
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"BAD-001","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"FAKE-999"}'



 

Question 01: In your report, explain the default lifecycle of a JAX-RC resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race condition.

By default, JAX-RS creates a new instance of every resource class for each incoming HTTP request (request-scoped lifecycle). This means that instance variable declared inside a resource class are not shared between requests and cannot be used to hold persistent data.
This has a direct impact on how in memory data must be managed. Since each request gets its own resource object, shared state must be stored in static data structures that exist independently of any resource instance. In this project, all data is held in datastore, which uses static final ConcurrentHashMap fields. ConcurrentHashMap is used instead of a regular HashMap because multiple requests can arrive simultaneously, each handled by a different thread. A standard HashMap is not thread safe and concurrent reads/writes can cause data corruption or ConcurrentModificationException.ConcurrentHashMap uses segment level locking internally to allow safe concurrent access without requiring explicit synchronized blocks on every method.


Question 02: Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

HATEOAS (Hypermedia as the engine of Application State) is the principle that API responses should include links that guide the client to related or next step resources, rather than requiring the client to construct URLs from external documentation.
This benefits client developers significantly. With static documentation, if the API changes its URL structure, all clients that hardcode those paths break silently. With HATEOAS, the client follows links embedded in responses, so URL changes are automatically propagated. It also reduces the learning curve, a developer can start at GET /api/v1 and navigate the entire API purely from the links returned, without consulting a separate reference. This self-documenting nature is especially valuable for large or evolving APIs where keeping documentation in sync with the codebase is error prone.


Question 03: when turning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.

Returning only IDs in a list response (eg: ["LIB-301", "LAB-101"]) minimises playload size and network bandwidth, which is beneficial when the client only needs to display a list of names or perform a lookup. However, it forces the client to make N additional HTTP requests to fetch the detail of each item, the classic “N+1 problem”, which increases latency and server load.
Returning full room objects in the list response increases payload size but eliminates the need for follow up requests. This is better when clients need to display multiple fields (name, capacity, sensor count) in a table or dashboard. The optimal approach depends on the use case: summary lists should include enough fields for display, while deeply nested child data (like sensor readings) can remain behind a separate endpoint.


Question 04: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple time.

The DELETE operation in this implementation is idempotent in terms of server state side effects, but it is not strictly idempotent in terms of HTTP response codes. The first DELETE/api/v1/rooms/{roomId} for an existing, empty room removes it and returns 204 no content. Any subsequent identical DELETE request for the same room will return 404 not found, because the room no longer exists.
This is acceptable consistent with REST conversations. The key idempotency guarantee is that no additional state changes occur after the first successful deletion, the resource is simply absent on all subsequent calls. There are no cascading side effects from repeated DELETE calls. This makes DELETE safe to retry unreliable network conditions, as a client that receives no response (due to a time out) can safely retry without risking double deletion side effects.


Question 05: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

The @concumes (mediatype. APPLICATION_JSON) annotation instructs JAX-RS to only invoke this method when the incoming request’s content type header is application/json. If a client sends a request with content type: text/plain or content type: application/xml, JAX-RS cannot find matching method and automatically returns HTTP 415 Unsupported media type. The resourse method is never invoked. This behaviour is handled entirely by the JAX-RS runtime’s content negotiation layer, before any application code runs, providing a clean contract enforcement mechanism without boilerplate validation code in every method.


Question 06: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

Using a query parameter (GET /api/v1/sensors?type=CO2) is the correct RESTful design for filtering a collection for several reasons. Query parameters are optional by nature, the same endpoint works without the parameter to return all sensors, and with it to filter. Path parameters (/api/v1/sensors/type/CO2) imply a distinct, addressable resource, which is semantically incorrect for a filter operation. There is no resource at path /type/CO2; it is a search criterion, not a resource identifier.
Query parameters also compose well. Multiple filters (?type=CO2&status=ACTIVE) are easy to add without changing the URL hierarchy. Path-based filtering does not compose this way without creating awkward nested paths. Additionally, query parameters are the universally understood convention for search and filter operations across web APIs, making the interface intuitive for client developers.


Question 07: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

The Sub-Resource Locator pattern allows a resource class to delegate handling of a sub-path to a separate, dedicated class. Instead of defining GET /sensors/{id}/readings and POST /sensors/{id}/readings directly inside SensorResource, JAX-RS routes any request matching /{sensorId}/readings to a method that returns an instance of SensorReadingResource.
This has significant architectural benefits in large APIs. Each resource class has a single, well-defined responsibility, SensorResource manages sensors, SensorReadingResource manages readings. This separation makes each class easier to test, modify, and understand in isolation. Without this pattern, a single controller class would grow to handle dozens of endpoints, making it difficult to maintain. It also enables the reading resource to receive the sensorId as constructor context, keeping the relationship between parent and child resource explicit and clean.


Question 08: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

HTTP 404 Not Found conventionally means the requested URL resource does not exist. When a client sends POST /api/v1/sensors with a valid URL and a well-formed JSON body, the endpoint itself clearly exists, so 404 is semantically misleading.
HTTP 422 Unprocessable Entity is more accurate because it signals that the server understood the request format and the URL was valid, but the content of the payload failed business logic validation. The issue is not the resource being requested it is that a value inside the payload references a non-existent dependency. This gives the client a precise signal: your request was syntactically correct and reached the right endpoint, but the data inside it is logically invalid. This distinction helps client developers debug integration issues far more quickly than a generic 404 would.


Question 09: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

Exposing raw Java stack traces to external API consumers creates several serious security vulnerabilities. Stack traces reveal the internal package and class names of the application (e.g., com.smartcampus.store.DataStore), which allows an attacker to map the application's internal architecture. They also expose the exact versions of third-party libraries in use,an attacker can cross-reference these versions against public vulnerability databases (CVE) to identify known exploits. Stack traces may include file system paths from the server, revealing the deployment directory structure. They can also expose logic and data flow details for example, a NullPointerException at a specific line number tells an attacker which inputs caused unexpected behaviour, which can be used to craft more targeted attacks. The global ExceptionMapper<Throwable> in this project ensures none of this information is ever sent to clients, all unexpected errors are logged server-side and only a generic message is returned externally.


Question 10: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

Inserting Logger.info() calls manually inside every resource method violates the DRY (Don't Repeat Yourself) principle and mixes infrastructure concerns with business logic. If the logging format needs to change, every resource method must be updated individually. If a new endpoint is added and the developer forgets to add logging, that endpoint goes unobserved silently.
JAX-RS filters handle logging as a cross-cutting concern, a single LoggingFilter class intercepts every request and response automatically, regardless of which resource handles it. This guarantees consistent, complete observability across the entire API with zero risk of omission. It also keeps resource classes clean and focused solely on their business logic, making them easier to read and test.






