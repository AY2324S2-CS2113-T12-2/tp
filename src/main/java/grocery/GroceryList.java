package grocery;

import exceptions.GitException;
import exceptions.invalidinput.InvalidAmountException;
import exceptions.LocalDateWrongFormatException;
import exceptions.PastExpirationDateException;
import exceptions.emptyinput.EmptyInputException;
import exceptions.CannotUseException;
import exceptions.invalidinput.InvalidCostException;
import exceptions.SameLocationException;
import git.Storage;
import git.GroceryUi;
import exceptions.nosuch.NoSuchObjectException;
import exceptions.commands.IncompleteParameterException;
import exceptions.commands.CommandWrongFormatException;
import grocery.location.Location;
import grocery.location.LocationList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Collections;
import java.util.stream.Collectors;


/**
 * Stores all the user's groceries in a main list.
 */
public class GroceryList {
    private List<Grocery> groceries;
    private Logger logger;
    private Storage storage;

    /**
     * Constructs GroceryList.
     */
    public GroceryList() {
        groceries = new ArrayList<>();
        LoggerGroceryList.setupLogger();
        logger = Logger.getLogger(GroceryList.class.getName());
        this.storage = new Storage();
    }

    /**
     * Adds a grocery.
     *
     * @param grocery Grocery to be added.
     */
    public void addGrocery(Grocery grocery) {

        try {
            groceries.add(grocery);
            storage.saveGroceryFile(getGroceries());
            assert groceries.contains(grocery) : "Grocery should be added to the list";
        } catch (NullPointerException e) {
            System.out.println("Failed to add grocery: the grocery is null.");
        } catch (Exception e) {
            System.out.println("An unexpected error occurred while adding the grocery: " + e.getMessage());
        }

        logger.log(Level.INFO, "Added " + grocery.printGrocery());

    }

    /**
     * Checks if a grocery exists.
     *
     * @param name Name of the grocery.
     * @return True if the grocery exists, false otherwise.
     */
    public boolean isGroceryExists(String name) {
        for (Grocery grocery : groceries) {
            if (grocery.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the desired grocery.
     *
     * @param name Name of the grocery.
     * @return The needed grocery.
     * @throws NoSuchObjectException If the selected grocery does not exist.
     */
    public Grocery getGrocery(String name) throws NoSuchObjectException {
        int index = -1;
        for (Grocery grocery : groceries) {
            if(grocery.getName().equalsIgnoreCase(name)) {
                index = groceries.indexOf(grocery);
                break;
            }
        }

        if (index != -1) {
            assert groceries != null : "Found grocery should not be null";
            return groceries.get(index);
        } else {
            throw new NoSuchObjectException("grocery (" + name + ")");
        }
    }
    /**
     * Returns the desired groceries.
     *
     * @return The needed groceries.
     */
    public List<Grocery> getGroceries(){
        return groceries;
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
            throw new EmptyInputException("grocery");
        }

        // Split the input into the grocery name and the detail part.
        String[] detailParts;
        if (command.equals("cost")) {
            detailParts = details.split("\\$", 2);
        } else {
            detailParts = details.split(parameter, 2);
        }

        // Check iin the grocery exists
        if (!isGroceryExists(detailParts[0].strip())) {
            throw new NoSuchObjectException("grocery (" + detailParts[0].strip() + ")");
        }

        // Missing parameter
        if (detailParts.length < 2) {
            throw new CommandWrongFormatException(command, parameter);
        }

        String attribute = detailParts[1].strip();
        if (attribute.isEmpty()) {
            throw new IncompleteParameterException(parameter);
        }

        return new String[] {detailParts[0].strip(), attribute};
    }

    /**
     * Sets the expiration date of an existing grocery.
     *
     * @param details A string containing grocery name and details.
     * @throws GitException Exception thrown depending on error.
     */
    public void editExpiration(String details) throws GitException {
        String[] expParts = checkDetails(details, "exp", "d/");
        Grocery grocery = getGrocery(expParts[0].strip());
        
        // Parse the date string to LocalDate
        LocalDate date;
        try {
            date = LocalDate.parse(expParts[1].strip(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            throw new LocalDateWrongFormatException();
        }
    
        // Convert LocalDate back to String to match the setExpiration signature
        String dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        try {
            grocery.setExpiration(dateString);
        } catch (PastExpirationDateException e) {
            System.out.println(e.getMessage());
        }

        // Verification and UI feedback
        GroceryUi.printExpSet(grocery);
        storage.saveGroceryFile(getGroceries());
    }

    /**
     * Sets the category of an existing grocery.
     *
     * @param details User input.
     * @throws GitException Exception thrown depending on error.
     */
    public void editCategory(String details) throws GitException {
        String[] catParts = checkDetails(details, "cat", "c/");
        Grocery grocery = getGrocery(catParts[0].strip());
        String newCategory = catParts[1].strip();

        grocery.setCategory(newCategory.toUpperCase());
        GroceryUi.printCategorySet(grocery);
        storage.saveGroceryFile(getGroceries());
    }

    /**
     * Checks whether the amount inputted by the user is valid.
     *
     * @param amountString String of amount inputted by the user.
     * @return Valid amount.
     * @throws InvalidAmountException Thrown if the amount is not a valid integer that is greater than 0
     */
    private int checkAmount (String amountString) throws InvalidAmountException {
        int amount;
        try {
            amount = Integer.parseInt(amountString);
        } catch (NumberFormatException e) {
            throw new InvalidAmountException();
        }

        if (amount <= 0) {
            throw new InvalidAmountException();
        }

        return amount;
    }

    /**
     * Sets the amount of an existing grocery.
     *
     * @param details User input.
     * @param isUse True to reduce the amount of a grocery, false to set a new amount.
     * @throws GitException Exception thrown depending on error.
     */
    public void editAmount(String details, boolean isUse) throws GitException {
        String [] amtParts;
        if (isUse) {
            amtParts = checkDetails(details, "use", "a/");
        } else {
            amtParts = checkDetails(details, "amt", "a/");
        }
        Grocery grocery = getGrocery(amtParts[0].strip());
        String amountString = amtParts[1].strip();
        int amount = checkAmount(amountString);

        if (isUse && grocery.getAmount() == 0) {
            throw new CannotUseException();
        } else if (isUse) {
            amount = Math.max(0, grocery.getAmount() - amount);
        }

        grocery.setAmount(amount);
        storage.saveGroceryFile(getGroceries());
        if (amount == 0) {
            GroceryUi.printAmtDepleted(grocery);
        } else if (grocery.isLow()){
            GroceryUi.lowStockAlert(grocery);
        } else {
            GroceryUi.printAmtSet(grocery);
        }
    }

    /**
     * Updates the remark of an existing grocery.
     *
     * @param details A string containing grocery new remark.
     * @throws GitException is input is not valid
     */
    public void editRemark(String details) throws GitException {
        // Assuming the format is "remark GROCERY r/REMARK"
        String[] remarkParts = checkDetails(details, "remark", "r/");
        Grocery grocery = getGrocery(remarkParts[0].strip());
        String remark = remarkParts[1].strip();

        grocery.setRemark(remark);
        if (remark.isEmpty()) {
            throw new EmptyInputException("remark");
        }
        GroceryUi.printRemarkSet(grocery);
        storage.saveGroceryFile(getGroceries());
    }

    /**
     * Updates the cost of an existing grocery.
     *
     * @param details A string containing grocery name and details.
     * @throws GitException If the input new cost is not numeric.
     */
    public void editCost(String details) throws GitException {
        String[] costParts = checkDetails(details, "cost", "$");
        Grocery grocery = getGrocery(costParts[0].strip());
        String price = costParts[1].strip();

        try {
            double cost = Double.parseDouble(price);
            if (cost < 0) {
                throw new InvalidCostException();
            }
            grocery.setCost(cost);
            GroceryUi.printCostSet(grocery);
            storage.saveGroceryFile(getGroceries());
        } catch (NumberFormatException e) {
            throw new InvalidCostException();
        }
    }

    /**
     * Updates the threshold of an existing grocery.
     *
     * @param details A string containing grocery name and details.
     * @throws GitException If the input new cost is not numeric.
     */
    public void editThreshold(String details) throws GitException {
        String [] amtParts = checkDetails(details, "th", "a/");
        Grocery grocery = getGrocery(amtParts[0].strip());
        String thresholdString = amtParts[1].strip();
        int threshold;
        try {
            threshold = Integer.parseInt(thresholdString);
        } catch (NumberFormatException e) {
            throw new InvalidAmountException();
        }
        grocery.setThreshold(threshold);
        GroceryUi.printThresholdSet(grocery);
        storage.saveGroceryFile(getGroceries());
    }

    /**
     * Updates the location of an existing grocery.
     *
     * @param details A string containing grocery name and details.
     * @throws GitException Thrown if given location name is empty.
     */
    public void editLocation(String details) throws GitException {
        String[] locationParts = checkDetails(details, "store", "l/");
        Grocery grocery = getGrocery(locationParts[0].strip());
        String name = locationParts[1].strip();

        Location location;
        try {
            location = LocationList.findLocation(name);
        } catch (NoSuchObjectException e) {
            LocationList.addLocation(name);
            GroceryUi.printLocationAdded(name.strip());
            location = LocationList.findLocation(name);
        }

        Location oldLocation = grocery.getLocation();
        if (oldLocation == location) {
            throw new SameLocationException(grocery.getName(), location.getName());
        } else if (oldLocation != null) {
            oldLocation.removeGrocery(grocery);
        }
        grocery.setLocation(location);
        location.addGrocery(grocery);
        GroceryUi.printLocationSet(grocery);
        storage.saveGroceryFile(getGroceries());
    }

    /**
     * Searches for groceries containing the given keyword.
     */
    public void findGroceries(String key) throws EmptyInputException {
        if (key.isEmpty()) {
            throw new EmptyInputException("keyword");
        }

        List<Grocery> relevantGroceries = new ArrayList<>();
        for (Grocery grocery : groceries) {
            if(grocery.getName().toLowerCase().contains(key.toLowerCase())) {
                relevantGroceries.add(grocery);
            }
        }

        GroceryUi.printGroceriesFound(relevantGroceries, key);
    }

    //@@author SharlynLui
    /**
     * Display all the details of the grocery.
     */
    public void viewGrocery(String grocery) throws EmptyInputException {
        if (grocery.isEmpty()) {
            throw new EmptyInputException("grocery");
        }

        int exists = 0;

        for (Grocery item : groceries) {
            if(item.getName().toLowerCase().equals(grocery.trim())) {
                GroceryUi.printViewGrocery(item);
                exists = 1;
                break;
            }
        }

        if (exists == 0) {
            GroceryUi.printGroceriesNotFound();
        }
    }
    //@@author SharlynLui

    /**
     * Updates the rating and review of an existing grocery.
     * 
     * @param details A string containing grocery name and details.
     * @throws GitException If the input grocery is empty.
     */
    public void editRatingAndReview(String details) throws GitException {
        if (details.isEmpty()) {
            throw new EmptyInputException("grocery");
        }
        Grocery grocery = getGrocery(details);
        GroceryUi.promptForRatingAndReview(grocery);
        storage.saveGroceryFile(getGroceries());
    }

    /**
     * Lists all the user's groceries.
     */
    public void listGroceries() {
        int size = groceries.size();
        if (size == 0) {
            GroceryUi.printNoGrocery();
        } else {
            GroceryUi.printGroceryList(groceries);
        }
    }

    /**
     * Lists all the groceries that are low in stock.
     */
    public void listLowStocks() {
        List<Grocery> lowStockGroceries = new ArrayList<>();
        for (Grocery grocery: groceries) {
            if (grocery.isLow()) {
                lowStockGroceries.add(grocery);
            }
        }
        GroceryUi.printLowStocks(lowStockGroceries);
    }

    /**
     * Sorts the groceries by expiration date. Groceries without an expiration date are sorted to the end.
     */
    public void sortByExpiration() {
        int size = groceries.size();
        if (size == 0) {
            GroceryUi.printNoGrocery();
        } else {
            Collections.sort(groceries, (g1, g2) -> {
                LocalDate exp1 = g1.getExpiration();
                LocalDate exp2 = g2.getExpiration();
                if (exp1 == null && exp2 == null) {
                    // If both groceries have no expiration date, they are equal
                    return 0; 
                } 
                if (exp1 == null) {
                    // If only the first grocery has no expiration date, it is greater
                    return 1; 
                } 
                if (exp2 == null) {
                    // If only the second grocery has no expiration date, it is greater
                    return -1; 
                } 
                return exp1.compareTo(exp2);
            });
            GroceryUi.printGroceryList(groceries);
        }
    }

    /**
     * Gets a list of groceries expiring in the next 3 days.
     *
     * @return A list of groceries expiring within the next 3 days.
     */
    public List<Grocery> getGroceriesExpiringInNext3Days() {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysFromNow = today.plusDays(3);

        return groceries.stream()
                .filter(grocery -> {
                    LocalDate expirationDate = grocery.getExpiration();
                    return !expirationDate.isBefore(today) && !expirationDate.isAfter(threeDaysFromNow);
                })
                .collect(Collectors.toList());
    }

    /**
     * display the groceries that are expiring in the next 3 days.
     */
    public void displayGroceriesExpiringInNext3Days() {
        List<Grocery> groceriesExpiringInNext3Days = getGroceriesExpiringInNext3Days();
        if (groceriesExpiringInNext3Days.isEmpty()) {
            GroceryUi.printNoGrocery();
        } else {
            System.out.println("Here are the groceries expiring in the next 3 days:");
            GroceryUi.printGroceryList(groceriesExpiringInNext3Days);
        }
    }

    /**
     * Sorts the groceries by descending cost.
     */
    public void sortByCost() {
        int size = groceries.size();
        if (size == 0) {
            GroceryUi.printNoGrocery();
        } else {
            List<Grocery> groceriesByCost = groceries;
            groceriesByCost.sort((g1, g2) -> Double.compare(g1.getCost(), g2.getCost()));
            Collections.reverse(groceriesByCost);
            GroceryUi.printGroceryList(groceriesByCost);
        }
    }
    /**
     * Sorts the groceries by category.
     */
    public void sortByCategory(){
        int size = groceries.size();
        if (size == 0) {
            GroceryUi.printNoGrocery();
        } else {
            Collections.sort(groceries, Comparator.comparing(Grocery::getCategory));
            GroceryUi.printGroceryList(groceries);
        }
    }
    /**
     * Removes a grocery.
     *
     * @param name Grocery name from user input.
     * @throws GitException If grocery is empty.
     */
    public void removeGrocery(String name) throws GitException {
        if (name.isEmpty()) {
            throw new EmptyInputException("grocery");
        }

        // Assuming the format is "del GROCERY"
        Grocery grocery = getGrocery(name);
        groceries.remove(grocery);
        Location location = grocery.getLocation();
        if (location != null) {
            location.removeGrocery(grocery);
        }
        GroceryUi.printGroceryRemoved(grocery, groceries);
        storage.saveGroceryFile(getGroceries());
    }
}
