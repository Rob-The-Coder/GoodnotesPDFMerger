package it.rob.Exceptions;

public class ArgumentException extends IllegalArgumentException{
    public ArgumentException() {
        super();
    }

    @Override
    public String getMessage() {
        return "An error has occurred, the arguments you used are wrong in form or in number.\nPlease use -h to learn how to use this program.";
    }
}
