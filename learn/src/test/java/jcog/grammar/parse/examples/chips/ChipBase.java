package jcog.grammar.parse.examples.chips;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class contains a small database of chips, customers,
 * and orders.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ChipBase {
	private static Dictionary<Integer, Chip> chip;
	private static Dictionary<Integer, Customer> customer;
	private static Vector<Order> order;

	/**
	 * Adds a chip to the database.
	 *
	 * @param Chip the chip to addAt
	 */
	private static void add(Chip c) {
		chip.put(c.getChipID(), c);
	}

	/**
	 * Adds a customer to the database.
	 *
	 * @param Customer the customer to addAt
	 */
	private static void add(Customer c) {
		customer.put(c.getCustomerID(), c);
	}

	/**
	 * Adds an order to the database.
	 *
	 * @param Order the order to addAt
	 */
	private static void add(Order o) {
		order.add(o);
	}

	/**
	 * Returns a dictionary of chip types.
	 *
	 * @return a dictionary of chip types
	 */
	public static Dictionary<Integer, Chip> chip() {
		if (chip == null) {
			chip = new Hashtable<>();

			add(new Chip(1001, "Carson City Silver Dollars", 8.95, 12.0, "Safflower"));

			add(new Chip(1002, "Coyote Crenellations", 9.95, 12.0, "Coconut"));

			add(new Chip(1003, "Four Corner Crispitos", 8.95, 12.0, "Coconut"));

			add(new Chip(1004, "Jim Bob's Jumbo BBQ", 12.95, 16.0, "Safflower"));

			add(new Chip(1007, "Saddle Horns", 9.95, 10.0, "Sunflower"));
		}
		return chip;
	}

	/**
	 * Return a chip, given its ID.
	 *
	 * @return a chip
	 *
	 * @param ID a chip ID
	 */
	private static Chip chip(int ID) {
		return chip().get(ID);
	}

	/**
	 * Returns a dictionary of customers.
	 *
	 * @returna a dictionary of customers
	 */
	public static Dictionary<Integer, Customer> customer() {
		if (customer == null) {
			customer = new Hashtable<>();
			add(new Customer(11156, "Hasskins", "Hank"));
			add(new Customer(11158, "Shumacher", "Carol"));
			add(new Customer(12116, "Zeldis", "Kim"));
			add(new Customer(12122, "Houston", "Jim"));

		}
		return customer;
	}

	/**
	 * Return a customer, given his or her ID.
	 *
	 * @return a customer
	 *
	 * @param ID a customer ID
	 */
	private static Customer customer(int ID) {
		return customer().get(ID);
	}

	/**
	 * Returns a vector of orders.
	 *
	 * @returna a vector of orders
	 */
	public static Vector<Order> order() {
		if (order == null) {
			order = new Vector<>();
			add(new Order(customer(11156), chip(1001), 2));
			add(new Order(customer(11156), chip(1004), 1));
			add(new Order(customer(11158), chip(1007), 4));
			add(new Order(customer(12116), chip(1002), 2));
			add(new Order(customer(12116), chip(1003), 2));
			add(new Order(customer(12122), chip(1004), 2));
			add(new Order(customer(12122), chip(1007), 2));
		}
		return order;
	}
}