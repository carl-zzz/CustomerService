package com.app.customerservice.thread;

import com.app.model.Customer;
import com.app.model.Level;

import java.util.concurrent.BlockingQueue;

import static com.app.customerservice.CustomerService.supervisorHashMap;

public class SupervisorThread implements Runnable {
    private long threadId;
    private final BlockingQueue<Customer> customersDealtBySupervisor;
    private final BlockingQueue<Customer> customersDealtByManager;

    public SupervisorThread(BlockingQueue<Customer> customersDealtBySupervisor,
                            BlockingQueue<Customer> customersDealtByManager,
                            long threadId) {
        this.customersDealtBySupervisor = customersDealtBySupervisor;
        this.customersDealtByManager = customersDealtByManager;
        this.threadId = threadId;
    }

    public void run() {
        // if there is no task in the blockingQueue, then keep waiting.
        // if there is a task requiring higher level help, it will run half of the talking time, and throw to the next
        // level staff.
        while (true) {
            try {
                System.out.println("Supervisor NO. " + threadId + " is ready to help. ");

                // take a customer from the queue if there is at least one in it
                Customer customer = customersDealtBySupervisor.take();
                // this employee is handling the phone call and becomes busy now
                supervisorHashMap.get(threadId).setHandlingId(customer.getId());
                System.out.println("Supervisor NO. " + threadId
                        + " accepted the phone call from customer NO." + customer.getId());

                long callDuration = customer.getCallDuration() * 1000;

                // if this customer require further help, talk for half of duration time and
                // put the customer into the manager's queue
                if ( customer.getHelpLevel() != Level.SUPERVISOR.ordinal() ) {
                    callDuration = callDuration/2;
                    Thread.sleep(callDuration);
                    System.out.println("After " + callDuration/1000 + " seconds, Supervisor NO." + threadId
                            + " decided to ask for manager's help with Customer NO." + customer.getId());
                    customersDealtByManager.add(customer);
                    // and this supervisor becomes free
                    supervisorHashMap.get(threadId).setHandlingId(-1);
                    continue;
                }

                // handle the phone call
                Thread.sleep(callDuration);
                System.out.println("After " + callDuration/1000 + " seconds, Supervisor NO." + threadId
                        + " ended the phone call from Customer NO." + customer.getId());
                supervisorHashMap.get(threadId).setHandlingId(-1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}