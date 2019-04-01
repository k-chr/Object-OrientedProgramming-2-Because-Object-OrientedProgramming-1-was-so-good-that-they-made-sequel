import java.util.*;
import java.util.List;
import java.lang.Exception;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class squareEquation{
    private Integer a;
    private Integer b;
    private Integer c;
    public String Equation;
    public squareEquation(List<Integer> coef){
        if(coef == null){
            return;
        }
        a = coef.get(0);
        b = coef.get(1);
        c = coef.get(2);
        Equation = a.toString() + "x^2 " + (b>0 ? "+":"") + b.toString() + "x " + (c>0 ? "+":"") + c.toString();
    }
    public List<Double> solve(){
        List<Double> out = new ArrayList<>();
        int d = delta();
        if(d == 0){
            out.add((-1*b.doubleValue()/(2*a.doubleValue())));
        }
        if(d > 0){
            out.add(((-1*b.doubleValue() - Math.sqrt(d))/(2*a.doubleValue())));
            out.add(((-1*b.doubleValue() + Math.sqrt(d))/(2*a.doubleValue())));
        }
        return out;
    }
    private Integer delta(){
        return b*b - 4*a*c;
    }
}
class MyException extends Exception{
    void ex_command(){
        System.out.println("error");
    }
}
class cannotSolveAnEquation extends MyException{
    private String Equation;
    public cannotSolveAnEquation(String s){
        Equation = s;
    }
    @Override
    public void ex_command(){
        System.out.println("Equation: " + Equation + " hasn't any real solution");
    }
}
class ArgumentsCountException extends MyException{
    @Override
    public void ex_command(){
        System.out.println("invalid number of arguments passed into main method");
    }
}
class NotValidArgumentsException extends MyException{
    @Override
    public void ex_command() {
        System.out.println("input is not a list of numbers");
    }
}
class EquationSquare {
    public static void main(String[] args) {
        try{
            if(args.length != 3){
                throw new ArgumentsCountException();
            }
            List<String> list = Arrays.asList(args);
            final String regex = "(?=.*\\D+.*$)(^(?!-\\d.*$)|(.+-.+)|.*[a-zA-Z]+.*)";
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            for(String l: list){
                final Matcher matcher = pattern.matcher(l);
                if(matcher.results().count() != 0){
                    throw new NotValidArgumentsException();
                }
            }
            List<Integer> coefs = new ArrayList<>();
            for(String s : list){
                coefs.add(Integer.parseInt(s));
            }
            if(coefs.get(0) == 0){
                throw new NotValidArgumentsException();
            }
            squareEquation eq = new squareEquation(coefs);
            List<Double> d = eq.solve();
            if(d.size() == 0){
                throw new cannotSolveAnEquation(eq.Equation);
            }
            if(d.size() > 1){
                System.out.println("Equation: " + eq.Equation + " has two real solutions: x1 = " + d.get(0).toString() + ", x2 = " + d.get(1).toString());
            }
            else{
                System.out.println("Equation: " + eq.Equation + " has only one real solution: x = " + d.get(0).toString());
            }
        }
        catch(MyException inv) {
            inv.ex_command();
        }
        catch(NumberFormatException num){
            System.out.println("ajsemtibitiooo one two three four," +
                               "parseInt has thrown an exception bro");
        }
    }
}