import java.io.File;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Arrays;
import java.io.FileWriter;
import java.util.Random;
import java.util.Scanner;

public class DSAssignment3 
{
    public static void main(String[] args) 
    {
        File customerTXT = new File("customers.txt");
		File output = new File("output.txt");
		Customer[] customers = CustomerFunctions.readFromTXT(customerTXT);
		FileWriter outputWriter = null;
		Random rand = new Random();


		// Set up output file
		try {
			outputWriter = new FileWriter(output);
		}
		catch (Exception e) {
			System.out.println("An error occurred.");
			System.exit(-1);
		}

		// create queue and enqueue customers
		Queue<Customer> queue = new LinkedList<>();
		for (Customer c : customers) queue.add(c);

		// Time, in minutes, from midnight - 540 would be 9AM
		int time = 540;

		// dequeue customers, generate random time, and call teller() to service each customer
		while (queue.peek() != null) {
			int duration = rand.nextInt(9) + 1; // generate from 0 to 9, add one
			CustomerFunctions.totalTellerWaitTime += duration;
			time += duration;
			time += rand.nextInt(5); // Add 0 to 5 minutes of wait between customers

			Customer c = queue.remove();
			c.arrivalTime = time;
			c.serviceTime = duration;

			String serviceReport = CustomerFunctions.serve(c);
			printAndWrite(serviceReport, outputWriter);
		}

		// Print how many times each teller was seen, average customers seen, and total wait time
		printAndWrite(CustomerFunctions.tellerInfo(), outputWriter);
		printAndWrite("Average customers seen by each teller: "
			+ (CustomerFunctions.tellerCount[0] + CustomerFunctions.tellerCount[1] + CustomerFunctions.tellerCount[2]) / 3 + "\n",
			outputWriter);
		printAndWrite("Total teller wait time: " + CustomerFunctions.totalTellerWaitTime + " minutes", outputWriter);
		// Note that teller number was randomly generated and stored in file,
		//  so the teller number a customer goes to and the teller's totals will be the same each time,
		//  but the wait time will be different since it is random.


		// finish writing to file
		try {
			outputWriter.close();
		}
		catch (Exception e) {
			System.out.println("An error occurred.");
			System.exit(-1);
		}
	}

	// Prints the same text to file and to the console
	static void printAndWrite(String s, FileWriter fw) {
		System.out.print(s);
		try {
			fw.write(s);
			fw.flush();
		}
		catch (Exception e) {
			System.out.println("An error occurred.");
			System.exit(-1);
		}
	}
    }


//used class instead of struct since using Java
class Customer 
{
    public int customerId;
	public int tellerNo;
	public int arrivalTime;
	public int serviceTime;
    public String firstName;
	public String lastName;
	public double transactionAmount;
	public double balance;
	public char transactionType;
	

}

class CustomerFunctions 
{
    public static int[] tellerCount = {0,0,0};
	public static double[] tellerDeposits = {0.0,0.0,0.0};
	public static double[] tellerWithdrawls = {0.0,0.0,0.0};
	public static int totalTellerWaitTime = 0;

    static Customer[] readFromTXT(File file) 
    {
        int lines = 0; 


        try 
        {
            Scanner scan = new Scanner(file);
            while (scan.hasNextLine()) 
            {
                scan.nextLine(); 
                lines++; 
            }
        }
        
        catch (Exception e) 
        {
            System.out.println("Unable to get file length.");
            e.printStackTrace();
            System.exit(-1);
        }

        Customer[] returnArray = new Customer[lines]; 

        try 
        {
            Scanner scan = new Scanner(file); 
            for (int i = 0; i < lines; i++) 
            {
                Customer tempCustomer = new Customer();
                String line = scan.nextLine();
                Scanner lineScan = new Scanner(line); 
                lineScan.useDelimiter(",");

                tempCustomer.customerId = lineScan.nextInt();
                tempCustomer.firstName = lineScan.next();
                tempCustomer.lastName = lineScan.next();
                tempCustomer.transactionAmount = lineScan.nextDouble();
                tempCustomer.transactionType = lineScan.next().charAt(0);
                tempCustomer.balance = lineScan.nextDouble();
                tempCustomer.tellerNo = lineScan.nextInt();

                returnArray[i] = tempCustomer; 

            }
        }

        catch (Exception e) {
			System.out.println(".");
			e.printStackTrace();
			System.exit(-1);
		}


		return returnArray;
	}

	// Used to print out all the info from customer array
	static void testPrintCustomerArray(Customer[] customers) {
		for (Customer c : customers) {
			System.out.println(c.customerId + " "
						   + c.firstName + " "
						   + c.lastName + " "
						   + c.transactionAmount + " "
						   + c.transactionType + " "
						   + c.balance + " "
						   + c.tellerNo);
		}
	}

	// Modify customer balances and teller totals, and return a report of the customer interaction
	static String serve(Customer customer) {
		String serviceReport = String.format(
			"%d: %s %s arrived at %s to %s $%.2f and spoke to teller %d. Current balance: $%.2f",
			customer.customerId,
			customer.firstName,
			customer.lastName,
			CustomerFunctions.getTime(customer.arrivalTime),
			CustomerFunctions.transactionTypeString(customer.transactionType),
			customer.transactionAmount,
			customer.tellerNo,
			customer.balance
			);

		// Perform deposit / withdrawl on customer account
		if (customer.transactionType == 'D') customer.balance += customer.transactionAmount;
		else customer.balance -= customer.transactionAmount;

		serviceReport += String.format(
			", New balance: $%.2f, Wait time: %d minutes%n%n",
			customer.balance,
			customer.serviceTime
			);

		// Add to teller totals
		tellerCount[customer.tellerNo - 1] += 1;
		if (customer.transactionType == 'D') tellerDeposits[customer.tellerNo - 1] += customer.transactionAmount;
		else tellerWithdrawls[customer.tellerNo-1] += customer.transactionAmount;

		return serviceReport;
	}

	// converts integer representing seconds from midnight to formatted string representing time
	static String getTime(int time) {
		int hours = time / 60;
		int minutes = time % 60;

		String ampm;
		if (time >= 720) ampm = "PM";
		else ampm = "AM";

		return (String.format("%d:%02d%s", hours, minutes, ampm));
	}

	// Convert "W" to "withdrawl" and "D" to "deposit"
	static String transactionTypeString(char c) {
		if (c == 'W') return "withdrawl";
		if (c == 'D') return "deposit";
		return ("ERROR");
	}

	// returns a report of the current teller totals
	static String tellerInfo() {
		String tellerReport = new String();
		for (int i=0; i<tellerCount.length;i++) {
			tellerReport += String.format(
				"Teller %d was seen %d times today, deposited $%.2f, and withdrew $%.2f, for a total of $%.2f%n",
				i+1,
				tellerCount[i],
				tellerDeposits[i],
				tellerWithdrawls[i],
				tellerDeposits[i] - tellerWithdrawls[i]);
		}

		return tellerReport;
	}
    }
