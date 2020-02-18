import com.app.customerservice.CustomerService;
import com.app.customerservice.thread.EmployeeThread;
import com.app.customerservice.thread.ManagerThread;
import com.app.customerservice.thread.SupervisorThread;
import com.app.model.Customer;
import com.app.model.Level;
import com.app.model.Staff;
import org.junit.jupiter.api.Test;

import static com.app.customerservice.CustomerService.employeeHashMap;
import static com.app.customerservice.CustomerService.supervisorHashMap;
import static com.app.customerservice.CustomerService.managerHashMap;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;

class FindCallHandlerTest {
    private static final int EMPLOYEE_NUMBER = 5;
    private static final int SUPERVISOR_NUMBER = 1;
    private static final int MANAGER_NUMBER = 1;

    @Test
    void test() throws InterruptedException {

        System.out.println("FindCallHandler Unit Test: ");

        CustomerService cs = new CustomerService();
        employeeHashMap = new ConcurrentHashMap<>();
        supervisorHashMap = new ConcurrentHashMap<>();
        managerHashMap = new ConcurrentHashMap<>();

        BlockingQueue<Customer> customersDealtByEmployee = new LinkedBlockingQueue<>();
        BlockingQueue<Customer> customersDealtBySupervisor = new LinkedBlockingQueue<>();
        BlockingQueue<Customer> customersDealtByManager = new LinkedBlockingQueue<>();
        ExecutorService es = Executors.newFixedThreadPool(EMPLOYEE_NUMBER + SUPERVISOR_NUMBER + MANAGER_NUMBER);

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

        // one thread for each staff, and put into the thread pool
        employeeHashMap.forEach((key, value) -> es.submit(
                new EmployeeThread(customersDealtByEmployee, customersDealtBySupervisor, key)));
        supervisorHashMap.forEach((key, value) -> es.submit(
                new SupervisorThread(customersDealtBySupervisor, customersDealtByManager, key)));
        managerHashMap.forEach((key, value) -> es.submit(new ManagerThread(customersDealtByManager, key)));

        // create customers
        Customer customer = new Customer(0, "1234567890", 4, 0);
        customersDealtByEmployee.add(customer);
        customer = new Customer(1, "1234567890", 4, 0);
        customersDealtByEmployee.add(customer);
        customer = new Customer(2, "1234567890", 4, 0);
        customersDealtByEmployee.add(customer);
        customer = new Customer(3, "1234567890", 4, 0);
        customersDealtByEmployee.add(customer);
        customer = new Customer(4, "1234567890", 4, 0);
        customersDealtByEmployee.add(customer);
        customer = new Customer(5, "1234567890", 4, 1);
        customersDealtByEmployee.add(customer);
        customer = new Customer(6, "1234567890", 4, 2);
        customersDealtByEmployee.add(customer);

        // let the main thread sleep for a bit
        Thread.sleep(1000);
        assertNotEquals(null, cs.findCallHandler(0));
        assertNotEquals(null, cs.findCallHandler(1));
        assertNotEquals(null, cs.findCallHandler(2));
        assertNotEquals(null, cs.findCallHandler(3));
        assertNotEquals(null, cs.findCallHandler(4));
        assertNull(cs.findCallHandler(5));
        assertNull(cs.findCallHandler(6));

        Thread.sleep(4000);
        assertNotEquals(null, cs.findCallHandler(5));
        assertNotEquals(null, cs.findCallHandler(6));

        Thread.sleep(2000 + 4000);
        // let the main thread sleep for another bit
        Thread.sleep(1000);
        assertNull(cs.findCallHandler(5));
        assertNotEquals(null, cs.findCallHandler(6));

        Thread.sleep(2000 + 4000);
        assertNull(cs.findCallHandler(6));

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
        System.out.println("This time we have handled " + 7 + " customers' phone call. ");
    }

}
