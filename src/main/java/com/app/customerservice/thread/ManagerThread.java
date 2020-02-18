package com.app.customerservice.thread;

import com.app.model.Customer;

import java.util.concurrent.BlockingQueue;

import static com.app.customerservice.CustomerService.managerHashMap;

public class ManagerThread implements Runnable {
    private long threadId;
    private final BlockingQueue<Customer> customersDealtByManager;

    public ManagerThread(BlockingQueue<Customer> customersDealtByManager, long threadId) {
        this.customersDealtByManager = customersDealtByManager;
        this.threadId = threadId;
    }

    public void run() {
        // if there is no task in the blockingQueue, then keep waiting.
        while (true) {
            try {
                System.out.println("Manager NO. " + threadId + " is ready to help. ");

                // take a customer from the queue if there is at least one in it
                Customer customer = customersDealtByManager.take();
                // this manager is handling the phone call and becomes busy now
                managerHashMap.get(threadId).setHandlingId(customer.getId());
                System.out.println("Manager NO. " + threadId
                        + " accepted the phone call from customer NO." + customer.getId());

                long callDuration = customer.getCallDuration() * 1000;

                // handle the phone call
                Thread.sleep(callDuration);
                System.out.println("After " + callDuration/1000 + " seconds, Manager NO." + threadId
                        + " ended the phone call from Customer NO." + customer.getId());
                managerHashMap.get(threadId).setHandlingId(-1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}