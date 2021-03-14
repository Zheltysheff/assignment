## Usage

Start service with sbt:

```
sbt run
```
or
```
sbt ~reStart
```

**With the service up, you can start sending HTTP requests:**

Submit job
```
curl -X POST -H 'Content-Type: application/json' http://localhost:8080/submit -d '{"jobId": "1", "priority": 1, "config": {"divined": 1, "divisor": 1}}'

201 Created
```
Get job status by id
```
curl http://localhost:8080/status/1
{
  "status":"succeeded",
  "name":"divisionOfTwoNumbers",
  "result":"{
    "result":1.0
  }"
}
```
Get job config by id
```
curl http://localhost:8080/config/1
{
  "status":"succeeded",
  "name":"divisionOfTwoNumbers",
  "config":"{
    "divined":1,
    "divisor":1
  }"
}
```
Get job summary
```
curl http://localhost:8080/summary
{
  "pending":0,
  "running":0,
  "succeeded":1,
  "failed":0
}
```

## Testing

Execute tests using test command:
```
sbt test
```
