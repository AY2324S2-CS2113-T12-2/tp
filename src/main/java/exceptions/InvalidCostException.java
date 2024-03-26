package exceptions;

/**
 * Represents the exception thrown when the cost inputted by the user is invalid.
 */
public class InvalidCostException extends GitException{
    /**
     * Constructs InvalidCostException.
     */
    public InvalidCostException() {
        message = "Please input a valid cost!";
    }
}
