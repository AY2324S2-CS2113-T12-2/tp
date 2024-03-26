package grocery;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GroceryTest {
    @Test
    public void printGrocery_noAmountNoExpiration_leaveEmpty() {

        Grocery grocery = new Grocery("apple", 0, null, "fruit", 0);
        String message = "apple expiration date not set cost not set";

        assertEquals(message, grocery.printGrocery());
    }

    @Test

    public void printGrocery_costWrongFormat_formattedCost() {
        Grocery grocery = new Grocery("chicken", 1, LocalDate.now().plusDays(1), "meat", 1);
        String message = "chicken" + ", amount: 1" + ", expiration: "
                + LocalDate.now().plusDays(1) + ", cost: $1.00";

        assertEquals(message, grocery.printGrocery());
    }

    @Test
    public void printGrocery_correctAmtAndExpAndCost() {
        Grocery grocery = new Grocery("chicken", 1, LocalDate.now().plusDays(1), "meat, 1.20);
        String message = "chicken" + ", amount: 1" + ", expiration: "
                + LocalDate.now().plusDays(1) + ", cost: $1.20";
        assertEquals(message, grocery.printGrocery());
    }

}
