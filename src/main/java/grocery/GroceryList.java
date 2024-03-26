package grocery;

import exceptions.InvalidAmountException;
import exceptions.InvalidCostException;
import git.Ui;
import exceptions.GitException;
import exceptions.commands.EmptyGroceryException;
import exceptions.commands.IncompleteCommandException;
import exceptions.commands.NoSuchGroceryException;
import exceptions.commands.WrongFormatException;
import exceptions.CannotUseException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Collections;


/**
 * Stores all the user's groceries.
 */
public class GroceryList {
    private List<Grocery> groceries;
    private Logger logger;

    /**
     * Constructs GroceryList.
     */
    public GroceryList() {
        groceries = new ArrayList<>();
        LoggerGroceryList.setupLogger();
        logger = Logger.getLogger(GroceryList.class.getName());
    }

    /**
     * Adds a grocery.
     *
     * @param grocery Grocery to be added.
     * @throws EmptyGroceryException If grocery name == null.
     */
    public void addGrocery(Grocery grocery) throws EmptyGroceryException {
        if (grocery.getName() == null || grocery.getName().trim().isEmpty()) {
            throw new EmptyGroceryException();
        }

        try {
            groceries.add(grocery);
            Ui.printGroceryAdded(grocery);
            assert groceries.contains(grocery) : "Grocery should be added to the list";
        } catch (NullPointerException e) {
            System.out.println("Failed to add grocery: he groceries collection is null.");
        } catch (Exception e) {
            System.out.println("An unexpected error occurred while adding the grocery: " + e.getMessage());
        }

        logger.log(Level.INFO, "Added " + grocery.printGrocery());
    }

    /**
     * Returns the desired grocery.
     *
     * @param name Name of the grocery.
     * @return The needed grocery.
     * @throws NoSuchGroceryException If the selected grocery does not exist.
     */
    private Grocery getGrocery(String name) throws NoSuchGroceryException {
        int index = -1;
        for (Grocery grocery : groceries) {
            if(grocery.getName().equals(name)) {
                index = groceries.indexOf(grocery);
                break;
            }
        }

        if (index != -1) {
            assert groceries != null : "Found grocery should not be null";
            return groceries.get(index);
        } else {
            throw new NoSuchGroceryException();
        }
    }

    /**
     * Checks whether details are valid, else throw GitException accordingly.
     *
     * @param details User input.
     * @param command Command word.
     * @param parameter Parameter for the command.
     * @return String array of valid details.
     * @throws GitException Exception thrown depending on error.
     */
    private String[] checkDetails(String details, String command, String parameter) throws GitException {
        if (details.isEmpty()) {
            throw new EmptyGroceryException();
        }

        // Split the input into the grocery name and the detail part.
        String[] detailParts = details.split(parameter, 2);
        if (detailParts.length < 2 || detailParts[1].trim().isEmpty()) {
            throw new WrongFormatException(command + " command is in the wrong format. " + parameter + " is required.");
        }

        // Retrieve the grocery to ensure it exists.
        Grocery grocery = getGrocery(detailParts[0].strip());
        if (grocery == null) {
            throw new NoSuchGroceryException();
        }

        String attribute = detailParts[1].strip();
        // For expiration date updates, validate the date format.
        if (command.equals("exp")) {
            try {
                LocalDate.parse(attribute, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                throw new WrongFormatException("Expiration date is in the wrong format. Please use yyyy-MM-dd.");
            }
        }

        return new String[] {detailParts[0].trim(), attribute};
    }

    /**
     * Adds the expiration date of an existing grocery.
     *
     * @param details A string containing grocery name and details.
     * @throws GitException Exception thrown depending on error.
     */
    public void editExpiration(String details) throws GitException {
        // Assuming the format is "exp GROCERY d/EXPIRATION_DATE"
        String[] expParts = checkDetails(details, "exp", "d/");
        if (expParts.length < 2 || expParts[1].trim().isEmpty()) {
            throw new IncompleteCommandException("Expiration date is missing. Please use the format 'd/YYYY-MM-DD'.");
        }
        Grocery grocery = getGrocery(expParts[0].strip());
        
        // Parse the date string to LocalDate
        LocalDate date;
        try {
            date = LocalDate.parse(expParts[1].strip(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            throw new WrongFormatException("Expiration date is in the wrong format. Please use yyyy-MM-dd.");
        }
    
        // Convert LocalDate back to String to match the setExpiration signature
        String dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        grocery.setExpiration(dateString);
    
        // Verification and UI feedback
        assert grocery.getExpiration().isEqual(date) : "Expiration date should be set correctly";
        Ui.printExpSet(grocery);
    }

    /**
     * Sets the amount of an existing grocery.
     *
     * @param details User input.
     * @param use True to reduce the amount of a grocery, false to set a new amount.
     * @throws GitException Exception thrown depending on error.
     */
    public void editAmount(String details, boolean use) throws GitException {
        // Assuming the format is "amt GROCERY a/AMOUNT"
        String[] amtParts = checkDetails(details, "amt", "a/");
        Grocery grocery = getGrocery(amtParts[0].strip());
        String amountString = amtParts[1].strip();

        int amount = 0;
        try {
            amount = Integer.parseInt(amountString);
        } catch (NumberFormatException e) {
            throw new InvalidAmountException();
        }

        // "use" is not valid if an amount was not previously set
        if (use && grocery.getAmount() == 0) {
            throw new CannotUseException();
        }

        int finalAmount = use ? Math.max(0, grocery.getAmount() - amount) : amount;
        grocery.setAmount(finalAmount);
        if (finalAmount == 0) {
            Ui.printAmtDepleted(grocery);
        } else {
            Ui.printAmtSet(grocery);
        }
    }

    /**
     * Updates the cost of an existing grocery.
     *
     * @param details A string containing grocery name and details.
     * @throws GitException If the input new cost is not numeric.
     */
    public void editCost(String details) throws GitException {
        // Assuming the format is "cost GROCERY $PRICE"
        System.out.println(details);
        String[] costParts = checkDetails(details, "cost", "\\$");
        Grocery grocery = getGrocery(costParts[0].strip());
        String price = costParts[1].strip();

        try {
            double cost = Double.parseDouble(price);
            grocery.setCost(String.format("%.2f", cost));
            Ui.printCostSet(grocery);
        } catch (NumberFormatException e) {
            throw new InvalidCostException();
        }
    }

    /**
     * Lists all the user's groceries.
     */
    public void listGroceries() {
        int size = groceries.size();
        if (size == 0) {
            Ui.printNoGrocery();
        } else {
            Ui.printGroceryList(groceries);
        }
    }

    /**
     * Sorts the groceries by expiration date.
     */
    public void sortByExpiration() {
        Collections.sort(groceries, (g1, g2) -> g1.getExpiration().compareTo(g2.getExpiration()));
    }

    public void sortByCost() {
        int size = groceries.size();
        if (size == 0) {
            Ui.printNoGrocery();
        } else {
            List<Grocery> groceriesByDate = groceries;
            groceriesByDate.sort((g1, g2) -> Double.compare(g1.getCost(), g2.getCost()));
            Collections.reverse(groceriesByDate);
            Ui.printGroceryList(groceriesByDate);
        }
    }

    /**
     * Removes a grocery.
     *
     * @param details User input.
     * @throws GitException If grocery is empty.
     */
    public void removeGrocery(String details) throws GitException {
        assert (!groceries.isEmpty()) : "There is nothing to remove.";
        if (details.isEmpty()) {
            throw new EmptyGroceryException();
        }

        // Assuming the format is "del GROCERY"
        Grocery grocery = getGrocery(details);
        groceries.remove(grocery);
        Ui.printGroceryRemoved(grocery, groceries);
    }
}
