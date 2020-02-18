package com.app.customerservice;

import com.app.customerservice.thread.EmployeeThread;
import com.app.customerservice.thread.ManagerThread;
import com.app.customerservice.thread.SupervisorThread;
import com.app.model.Customer;
import com.app.model.Instruction;
import com.app.model.Level;
import com.app.model.Staff;

import java.util.Scanner;
import java.util.concurrent.*;

public class CustomerService {

    public static int EMPLOYEE_NUMBER = 5;
    public static int SUPERVISOR_NUMBER = 1;
    public static int MANAGER_NUMBER = 1;
    public static ConcurrentHashMap<Long, Staff> employeeHashMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Long, Staff> supervisorHashMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Long, Staff> managerHashMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Customer> customersDealtByEmployee = new LinkedBlockingQueue<>();
        BlockingQueue<Customer> customersDealtBySupervisor = new LinkedBlockingQueue<>();
        BlockingQueue<Customer> customersDealtByManager = new LinkedBlockingQueue<>();
        System.out.println("Server started...");
        System.out.println("There are " + EMPLOYEE_NUMBER + " employees, " + SUPERVISOR_NUMBER +
                        " supervisor and " + MANAGER_NUMBER + " manager on call. ");

        createStaff();

        // create the thread pool
        ExecutorService es = Executors.newFixedThreadPool(EMPLOYEE_NUMBER + SUPERVISOR_NUMBER + MANAGER_NUMBER);

        // one thread for each staff, and put into the thread pool
        employeeHashMap.forEach((key, value) -> es.submit(
                new EmployeeThread(customersDealtByEmployee, customersDealtBySupervisor, key)));
        supervisorHashMap.forEach((key, value) -> es.submit(
                new SupervisorThread(customersDealtBySupervisor, customersDealtByManager, key)));
        managerHashMap.forEach((key, value) -> es.submit(new ManagerThread(customersDealtByManager, key)));

        long customerId = 0;

        // input:
        // only accept: c <phoneNumber> <helpLevel>
        Scanner userInput = new Scanner(System.in);
        while ( true ) {
            System.out.println("Ready to pick up a phone call: (Enter \"h\" for help)");
            String input = userInput.nextLine();
            if ( input.isEmpty() ) continue;

            String argStr[] = input.split("\\s+");
            Instruction instruction = Instruction.UNKNOWN;
            // check input
            switch ( argStr.length ) {
                case 1:
                    if (argStr[0].equals("H") || argStr[0].equals("h")) {
                        // help
                        System.out.println("We take 4 arguments. Input examples: c 1234567890 20 1");
                        System.out.println("    c stands for a call from a customer");
                        System.out.println("    1234567890 is the phone number");
                        System.out.println("    20 stands for this call will be 20 second long");
                        System.out.println("    1 is the supervisor level of help that the customer may need");
                        System.out.println("We have 3 level of help that the customer may need:");
                        System.out.println("    0 => Employee Level");
                        System.out.println("    1 => Supervisor Level");
                        System.out.println("    2 => Manager Level\n");
                        System.out.println("h or H for help");
                        System.out.println("s or S for staff status");
                        System.out.println("q or Q for quit");
                        instruction = Instruction.RESTART;
                    } else if (argStr[0].equals("Q") || argStr[0].equals("q")) {
                        // quit
                        instruction = Instruction.QUIT;
                    } else if (argStr[0].equals("S") || argStr[0].equals("s")) {
                        printStaffStatus();
                        instruction = Instruction.RESTART;
                    }
                    break;
                case 4:
                    System.out.println(argStr.length);
                    if (argStr[0].equals("C") || argStr[0].equals("c")) {
                        // may be able to start
                        // TODO: check the phone number's validation
                        if ( ( argStr[3].equals("0") || argStr[3].equals("1") || argStr[3].equals("2") ) &&
                                ( argStr[2].matches("\\d+") ) ) {
                            instruction = Instruction.START;
                        }
                    }
                    break;
            }

            // base on instruction to decide what to do
            if ( instruction == Instruction.QUIT ) {
                break;
            } else if ( instruction == Instruction.RESTART ) {
                continue;
            } else if ( instruction == Instruction.START ) {
                Customer customer = new Customer(customerId, argStr[1], Long.parseLong(argStr[2]), Long.parseLong(argStr[3]));
                customersDealtByEmployee.add(customer);
                customerId = customerId + 1;
            } else {
                System.out.println("Invalid input, please check and re-enter.");
            }
        }

        System.out.println("Server is going to shutdown...");
        System.out.println("Wait until all staff have finished their phone call...");
        boolean finished = true;
        while ( true ) {
            for ( long staffId : employeeHashMap.keySet() ) {
                if ( employeeHashMap.get(staffId).getHandlingId() != -1 ) {
                    finished = false;
                }
            }
            for ( long staffId : supervisorHashMap.keySet() ) {
                if ( supervisorHashMap.get(staffId).getHandlingId() != -1 ) {
                    finished = false;
                }
            }
            for ( long staffId : managerHashMap.keySet() ) {
                if ( managerHashMap.get(staffId).getHandlingId() != -1 ) {
                    finished = false;
                }
            }
            if ( finished ) break;
            finished = true;
        }
        System.out.println("Done.");
        System.out.println("This time we have handled " + customerId + " customers' phone call. ");
        System.out.println("Bye.");
        System.exit(0);
    }

    private static void printStaffStatus() {
        long handlingId = -1;
        for ( long staffId : employeeHashMap.keySet() ) {
            handlingId = employeeHashMap.get(staffId).getHandlingId();
            if ( handlingId == -1 ) {
                System.out.println("Employee NO." + staffId + ": FREE");
            } else {
                System.out.println("Employee NO." + staffId + ": Customer NO." + handlingId);
            }
        }
        for ( long staffId : supervisorHashMap.keySet() ) {
            handlingId = supervisorHashMap.get(staffId).getHandlingId();
            if ( handlingId == -1 ) {
                System.out.println("Supervisor NO." + staffId + ": FREE");
            } else {
                System.out.println("Supervisor NO." + staffId + ": Customer NO." + handlingId);
            }
        }
        for ( long staffId : managerHashMap.keySet() ) {
            handlingId = managerHashMap.get(staffId).getHandlingId();
            if ( handlingId == -1 ) {
                System.out.println("Manager NO." + staffId + ": FREE");
            } else {
                System.out.println("Manager NO." + staffId + ": Customer NO." + handlingId);
            }
        }
    }

    // Create virtual staff including all levels
    private static void createStaff() {

        for (long i = 0; i<EMPLOYEE_NUMBER; i++ ) {
            final Staff staff = new Staff(i, Level.EMPLOYEE);
            employeeHashMap.put(i, staff);
        }
        for (long i = 0; i<SUPERVISOR_NUMBER; i++ ) {
            final Staff staff = new Staff(i, Level.SUPERVISOR);
            supervisorHashMap.put(i, staff);
        }
        for (long i = 0; i<MANAGER_NUMBER; i++ ) {
            final Staff staff = new Staff(i, Level.MANAGER);
            managerHashMap.put(i, staff);
        }
        System.out.println("Staff deployed. ");
    }

    // findCallHandler is used for unit tests, use customer's id to find the customer's handler and return
    public Staff findCallHandler(long id) {
        for ( long staffId : employeeHashMap.keySet() ) {
            if ( employeeHashMap.get(staffId).getHandlingId() == id ) {
                return employeeHashMap.get(staffId);
            }
        }
        for ( long staffId : supervisorHashMap.keySet() ) {
            if ( supervisorHashMap.get(staffId).getHandlingId() == id ) {
                return supervisorHashMap.get(staffId);
            }
        }
        for ( long staffId : managerHashMap.keySet() ) {
            if ( managerHashMap.get(staffId).getHandlingId() == id ) {
                return managerHashMap.get(staffId);
            }
        }
        return null;
    }

}
