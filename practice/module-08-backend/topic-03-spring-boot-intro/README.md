# Spring Boot: your first app

Start the Spoke API so the rider app has a server to talk to. It needs two endpoints:

- `GET /stations` answers `["Riverside","Old Mill","Harbor","Uptown","Depot"]`, in that order.
- `GET /health` answers `{"status":"UP","stations":5}`, so Theo can ping it.

`SpokeApiApplication` (the app's starting point) and the `Health` record are ready. Your work is in `StationController.java`.

1. **TODO 1:** put `@RestController` above the class, so Spring creates the controller and sends its answers as JSON.
2. **TODO 2:** put `@GetMapping("/stations")` above `stations()`.
3. **TODO 3:** return the `STATIONS` list.
4. **TODO 4:** put `@GetMapping("/health")` above `health()`.
5. **TODO 5:** return `new Health("UP", ...)` with the number of stations in `STATIONS`.

## How to read the tests

Open `StationControllerTest.java`. `@SpringBootTest` starts your whole app inside the test, and `@AutoConfigureMockMvc` adds MockMvc, which sends pretend requests to your controller. No real server or port is used.

- A **404** means Spring found no method for the path. Check the annotations from TODO 1, 2 and 4.
- The tests read the JSON with `JsonPath`, for example `$.status` for the `status` field.
- One test asks for `/bikes`, which does not exist yet, and expects 404 Not Found.

This topic has no Run button, because running the app would start a real server that waits for requests forever. The tests are the way to check your work. If you want to see the real server, run `SpokeApiApplication` from your IDE and open http://localhost:8080/stations, then stop it.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
