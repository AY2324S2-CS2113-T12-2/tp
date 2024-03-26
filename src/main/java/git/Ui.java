package git;

import java.util.List;
import java.util.Scanner;

import exceptions.GitException;
import exceptions.InvalidCostException;
import grocery.Grocery;


/**
 * Deals with interactions with the user.
 */
public class Ui {
    // ATTRIBUTES
    public static final String DIVIDER = "- - - - -";
    private Scanner in;

    // METHODS

    /**
     * Constructs Ui and initialises Scanner to read input.
     */
    public Ui() {
        in = new Scanner(System.in);
    }

    /**
     * Prints welcome message.
     */
    public void printWelcome() {
        final String GITLOGO =
                "   ______   _  _________\n" +
                " .' ___  | (_)|  _   _  |\n" +
                "/ .'   \\_| __ |_/ | | \\_|\n" +
                "| |   ____[  |    | |\n" +
                "\\ `.___]  || |   _| |_\n" +
                " `._____.'[___] |_____|";

        System.out.println(GITLOGO + System.lineSeparator());
        System.out.println("Hello from GiT");
        System.out.println("What is your name?");
        printLine();
        String userName = in.nextLine();
        printHello(userName);
    }

    /**
     * Prints Hello with user's name.
     *
     * @param userName User's name.
     */
    public void printHello(String userName) {
        System.out.println("Hello " + userName + "!");

        displayHelp();
        System.out.println("Enter command:");

        printLine();
    }

    /**
     * Processes user input into commands and their details.
     *
     * @return Array of the fragments of the commands.
     */
    public String[] processInput() {
        String commandLine = in.nextLine();
        assert !(commandLine.isEmpty()): "User input should be read";
        String[] commandParts = commandLine.strip().split(" ", 2);
        assert commandParts.length > 0 : "Failed to read user input";

        // Return an array of length 2 for executeCommand
        if (commandParts.length == 1) {
            return new String[]{commandParts[0], ""};
        } else {
            return commandParts;
        }
    }

    /**
     * Prompts user for expiration date.
     *
     * @return Formatted expiration date.
     */
    public String promptForExpiration() {
        System.out.println("Please enter the year of expiry (e.g. 2024):");
        String year = in.nextLine().trim();

        System.out.println("Please enter the month of expiry (e.g. July or 07):");
        String month = in.nextLine().trim();
        month = convertMonthToNumber(month);


        System.out.println("Please enter the date of expiry (e.g. 19):");

        String day = in.nextLine().trim();

        String formattedDate = formatExpirationDate(year, month, day);
        return formattedDate;
    }
    /**
     * Prompts user for category
     */
    public String promptForCategory(){
        System.out.println("Please enter the category (e.g. fruit):");
        return in.nextLine().trim();
    }

    public int promptForAmount(){
        System.out.println("Please enter the amount (e.g. 3):");
        try {
            return Integer.parseInt(in.nextLine().trim());
        } catch (NumberFormatException e){
            System.out.println("Please enter a valid integer for the amount (e.g. 3)!");
            return promptForAmount();
        }
    }

    /**
     * Prompts the user to enter the cost of the grocery and format the string.
     * Returns the cost to be set for the grocery.
     *
     * @return Cost of the grocery formatted in 2 decimal points.
     */
    public String promptForCost() throws GitException {
        System.out.println("Please enter the cost (e.g., $1.20):");
        String price = in.nextLine().trim();
        if(price.contains("$")) {
            String formattedPrice = price.replace("$", "");
            try {
                double cost = Double.parseDouble(formattedPrice);
                return String.format("%.2f", cost);//format the money value to 2dp
            } catch (NumberFormatException nfe) {
                throw new InvalidCostException();
            }
        } else {
            throw new InvalidCostException();
        }
    }

    /**
     * Reads expiration date from user input.
     *
     * @param month Month of expiration.
     * @return Month in numerical format.
     */
    private String convertMonthToNumber(String month) {
        // Convert month from name to number (e.g., "July" to "07")
        String[] monthNames = {"January", "February", "March", "April", "May", "June", 
                               "July", "August", "September", "October", "November", "December"};
        String[] monthNumbers = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        for (int i = 0; i < monthNames.length; i++) {
            if (month.equalsIgnoreCase(monthNames[i]) || month.equals(monthNumbers[i])) {
                return monthNumbers[i]; // Found a match, return the month number
            }
        }
        // If no match found or input is already in numeric format, return original input
        // This part can be enhanced to handle invalid months.
        return month;
    }

    /**
     * Reads expiration date from user input.
     *
     * @param year Year of expiration.
     * @param month Month of expiration.
     * @param day Day of expiration.
     * @return Formatted expiration date.
     */
    private String formatExpirationDate(String year, String month, String day) {
        // This method can be enhanced to validate the date components
        return year + "-" + month + "-" + day;
    }

    /**
     * Displays help message containing all possible commands.
     */
    public void displayHelp() {
        System.out.println(
                "Here are some ways you can use this app!\n" +
                        "add GROCERY: adds the item GROCERY.\n" +
                        "exp GROCERY d/EXPIRATION_DATE: edits the expiration date for GROCERY.\n" +
                        "amt GROCERY a/AMOUNT: sets the amount of GROCERY.\n" +
                        "use GROCERY a/AMOUNT: updates the total amount after using a GROCERY\n" +
                        "cost GROCERY $PRICE: updates the price of GROCERY.\n" +
                        "del GROCERY: deletes GROCERY.\n" +
                        "list: shows list of all groceries you have.\n" +
                        "listC: shows the list sorted by price.\n" +
                        "exit: exits the program.\n" +
                        "help: view all the possible commands."
        );
    }

    /**
     * Prints output after setting the selected grocery's expiration date.
     *
     * @param grocery The grocery that should be updated.
     */
    public static void printExpSet(Grocery grocery) {
        assert !(grocery.getName().isEmpty()): "grocery name should not be empty";
        System.out.println(grocery.getName() + " will expire on: " + grocery.getExpiration());
    }

    /**
     * Prints output after editing the selected grocery's cost.
     *
     * @param grocery The grocery that should be updated.
     */
    public static void printCostSet(Grocery grocery) {
        assert (grocery.getCost()!= 0): "grocery cost should not be empty";
        double cost = grocery.getCost();
        String price = "$" + String.format("%.2f", cost);
        System.out.println(grocery.getName() + " is now " + price);
    }

    /**
     * Prints output after adding a grocery.
     *
     * @param grocery Grocery added.
     */
    public static void printGroceryAdded(Grocery grocery) {
        assert !(grocery.getName().isEmpty()): "grocery name should not be empty";
        System.out.println(grocery.getName() + " added!");
    }

    /**
     * Prints output after setting the selected grocery's amount.
     *
     * @param grocery The grocery that should be updated.
     */
    public static void printAmtSet(Grocery grocery) {
        // TODO: update amount output according to Grocery subclass
        assert grocery.getAmount() >= 0 : "grocery amount should not be empty";
        System.out.println(grocery.getName() + ": " + grocery.getAmount());
    }

    /**
     * Prints output after a grocery's amount is set to 0.
     *
     * @param grocery The grocery that is depleted.
     */
    public static void printAmtDepleted(Grocery grocery) {
        System.out.println(grocery.getName() + " is now out of stock!");
    }

    /**
     * Prints out when there are no groceries.
     */
    public static void printNoGrocery() {
        System.out.println("There's no groceries!");
    }

    /**
     * Prints all groceries.
     *
     * @param groceries An array list of groceries.
     */
    public static void printGroceryList(List<Grocery> groceries) {
        assert !groceries.isEmpty() : "grocery list should not be empty";
        System.out.println("Here are your groceries!");
        for (Grocery grocery: groceries) {
            System.out.println(" - " + grocery.printGrocery());
        }
    }

    /**
     * Prints output when the selected grocery is removed.
     *
     * @param grocery The grocery that is removed.
     * @param groceries The array list of groceries.
     */
    public static void printGroceryRemoved(Grocery grocery, List<Grocery> groceries) {
        assert grocery!=null : "Grocery does not exist";
        System.out.println("This grocery is removed:");
        System.out.println(grocery.printGrocery());
        System.out.println("You now have " + groceries.size() + " groceries left");
    }

    /**
     * Prints divider for user readability.
     */
    public void printLine() {
        System.out.println(DIVIDER);
    }
}
