# CustomerService

A big retail company has a customer service call centre with employees, a supervisor, and a manager. There are multiple employees but there can be only one supervisor and one manager.

The employees answer the incoming calls. If an employee cannot handle the call, the call will be forwarded to the supervisor. If the supervisor is either busy or cannot handle the call, the call will be forward to the manager.



- Please implement your design of the call centre.

- Please consider concurrency in your solution.

- Write unit tests for the method findCallHandler.

- Please use Maven as your build manager.

## Design:
There will be fixed number of employees, a supervisor and a manager. For each of them will have a thread in the thread pool. 

When there is a customer phone call coming with its phone call duration and help level(which is the level of help that the customer needs, maybe employee, or supervisor or manager), I will create a customer object in the employee's blockingQueue. One of the employee thread is going to talk to the customer(which I represent it as thread.sleep), if the phone call comes with the higher lvl of help, it will throw the customer to supervisor's blockingQueue. And so on. 

If the blockingQueue is empty, the staff's thread will be waiting there. 

## How to run:
- Clean, Compile, and Build
```bash
mvn clean compile exec:exec
```

- Test:
```bash
mvn test
```

## Input:
We take 4 arguments. Input examples: c 1234567890 20 1
- c stands for a call from a customer
- 1234567890 is the phone number
- 20 stands for this call will be 20 second long
- 1 is the supervisor level of help that the customer may need

We have 3 level of help that the customer may need:
- 0 => Employee Level
- 1 => Supervisor Level
- 2 => Manager Level

We can also check all the staff status or quit:
- H or h: instructions in case you forget the input
- S or s: check all the staff status -- free or handling which customer
- Q or q: safely shut down the server until all the staff finished their call
