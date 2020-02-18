package com.app.customerservice.thread;


import com.app.model.Customer;
import com.app.model.Level;

import java.util.concurrent.BlockingQueue;

import static com.app.customerservice.CustomerService.employeeHashMap;

public class EmployeeThread implements Runnable {
    private long threadId;
    private final BlockingQueue<Customer> customersDealtByEmployee;
    private final BlockingQueue<Customer> customersDealtBySupervisor;

    public EmployeeThread(BlockingQueue<Customer> customersDealtByEmployee,
                          BlockingQueue<Customer> customersDealtBySupervisor,
                          long threadId) {
        this.customersDealtByEmployee = customersDealtByEmployee;
        this.customersDealtBySupervisor = customersDealtBySupervisor;
        this.threadId = threadId;
    }

    public void run() {
        // if there is no task in the blockingQueue, then keep waiting.
        // if there is a task requiring higher level help, it will run half of the talking time, and throw to the next
        // level staff.
        while (true) {
            try {
                System.out.println("Employee NO. " + threadId + " is ready to help. ");

                // take a customer from the queue if there is at least one in it
                Customer customer = customersDealtByEmployee.take();
                // this employee is handling the phone call and becomes busy now
                employeeHashMap.get(threadId).setHandlingId(customer.getId());
                System.out.println("Employee NO. " + threadId
                        + " accepted the phone call from customer NO." + customer.getId());

                long callDuration = customer.getCallDuration() * 1000;

                // if this customer require further help, talk for half of duration time and
                // put the customer into the supervisor's queue
                if ( customer.getHelpLevel() != Level.EMPLOYEE.ordinal() ) {
                    callDuration = callDuration/2;
                    Thread.sleep(callDuration);
                    System.out.println("After " + callDuration/1000 + " seconds, Employee NO." + threadId
                            + " decided to ask for supervisor's help with Customer NO." + customer.getId());
                    customersDealtBySupervisor.add(customer);
                    // and this employee becomes free
                    employeeHashMap.get(threadId).setHandlingId(-1);
                    continue;
                }

                // handle the phone call
                Thread.sleep(callDuration);
                System.out.println("After " + callDuration/1000 + " seconds, Employee NO." + threadId
                        + " ended the phone call from Customer NO." + customer.getId());
                employeeHashMap.get(threadId).setHandlingId(-1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
