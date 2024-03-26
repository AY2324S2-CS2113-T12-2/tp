package grocery;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 * Represents a grocery.
 */
public class Grocery {
    private String name;
    private int amount;
    private LocalDate expiration;
    private String category;
    private double cost;


    /**
     * Constructs a Grocery.
     *
     * @param name Name.
     * @param amount Measurement of grocery.
     * @param expiration When grocery expires.
     * @param category Category of grocery.
     */

    public Grocery(String name, int amount, LocalDate expiration, String category, double cost) {
        this.name = name;
        this.amount = amount;
        this.expiration = expiration;
        this.category = category;
        this.cost = cost;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }

    public LocalDate getExpiration() {
        return expiration;
    }

    public double getCost() {
        return this.cost;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(int amount) {
        assert amount >= 0 : "Amount entered is invalid!";
        this.amount = amount;
    }

    /**
     * Formats the expiration date from type string to local date.
     *
     * @param expiration The expiration date of the grocery.
     */
    public void setExpiration(String expiration) {
        assert !(expiration.isEmpty()) : "Expiration date entered is invalid!";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.expiration = LocalDate.parse(expiration, formatter);
    }

    /**
     * Converts the cost from type String to double and store it.
     *
     * @param cost The cost of grocery in String type.
     */
    public void setCost(String cost) {
        assert !(cost.isEmpty()) : "Cost entered is invalid!";
        this.cost = Double.parseDouble(cost);
    }

    /**
     * Returns the name, amount, expiration date and cost of the grocery.
     *
     * @return String representation of the Grocery.
     */
    public String printGrocery() {
        assert !(this.name.isEmpty()) : "Grocery does not exist!!";

        // TODO: update amount output according to Grocery subclass
        // TODO: consider stating amount == 0 now that we track amount ?

        String amountString = (amount == 0) ? "" : ", amount: " + amount;
        String exp = (expiration == null) 
            ? " expiration date not set" 
            : ", expiration: " + expiration.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String price = (cost != 0) ? ", cost: $" + String.format("%.2f", cost): " cost not set";
        String unit = "";
        switch (category.toLowerCase()){
            case "fruit":
                unit = "pieces";
                break;
            case "vegetable":
                unit = "grams";
                break;
            case "meat":
                unit = "grams";
                break;
            case "beverage":
                unit = "ml";
                break;
            default:
                unit = "units";
                break;
        }
        return this.name + " (" + this.category + ") " + amountString + unit + exp + price;
    }
}

