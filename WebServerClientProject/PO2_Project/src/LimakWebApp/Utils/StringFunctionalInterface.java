package LimakWebApp.Utils;

/**
 * <h1>StringFunctionalInterface</h1>
 * This interface provides method to get text
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   11.07.2019
 */
public interface StringFunctionalInterface {
    /**
     * This method returns String built by provided {@link StringBuilder}
     * @param stringBuilder Reference to builder
     * @return String
     */
    String getText(StringBuilder stringBuilder);
}
