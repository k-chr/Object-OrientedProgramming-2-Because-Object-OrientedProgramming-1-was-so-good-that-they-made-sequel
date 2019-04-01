import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;

class myException extends Exception{
    void myCommand(){
        System.out.println("error");
    }
}
class notEnoughMainArguments extends myException{
    void myCommand(){
        System.out.println("User'd provided less than three arguments into main()");
    }
}
class invalidIndexAccess extends myException{
    void myCommand(){
        System.out.println("One of provided indices wasn't valid");
    }
}
class notEnoughIndicesProvided extends myException{
    void myCommand(){
        System.out.println("User'd provided less than two indices");
    }
}
class StringOperation {
    public static void main(String[] args) {
        try{
            List<Integer> coefs = new ArrayList<>();
            List<String> text = new ArrayList<>();
            if(args.length < 3){
                throw new notEnoughMainArguments();
            }
            for(String s : args){
                if(s.matches("^\\d+")){
                    coefs.add(Integer.parseInt(s));
                }
                else{
                    text.add(s);
                }
            }
            if(coefs.size() != 2){
                throw new notEnoughIndicesProvided();
            }
            StringBuilder sb = new StringBuilder();
            for(String s : text){
                sb.append(s);
            }
            String out = sb.toString();
            int x = coefs.get(0);
            int y = coefs.get(1);
            if(x >= y || x >= out.length() || y >= out.length()){
                throw new invalidIndexAccess();
            }
            System.out.println(out.substring(coefs.get(0), coefs.get(1)));
        }
        catch(myException ex){
            ex.myCommand();
        }
        catch (IndexOutOfBoundsException ind){
            ind.getMessage();
        }
    }
}