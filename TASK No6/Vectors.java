import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

class MyVector{
    private ArrayList<Integer> vector;
    public MyVector() throws IllegalStateException, NoSuchElementException {
        System.out.println("Type vector");
        Scanner scanner = new Scanner(System.in);
        String[] providedData = scanner.nextLine().split(" ");
        vector = new ArrayList<>();
        for(String s: providedData){
            if(s.matches("^\\d+")){
                vector.add(Integer.parseInt(s));
            }
        }
    }
    public Integer getLength(){
        return vector.size();
    }
    public void dump(){
        try(BufferedWriter buff = new BufferedWriter(new FileWriter("SumOfVectors.txt"))){
            for(Integer coefficient: vector){
                buff.write(coefficient.toString() + " ");
            }
        }
        catch(IOException io){
            System.out.println("IOError");
        }
    }
    public MyVector Add(MyVector other) throws VectorsOfDistinctLengthException{
        if(this.vector.size() != other.vector.size()){
            throw new VectorsOfDistinctLengthException(this, other);
        }
        MyVector sum = this;
        for(int i = 0; i < sum.vector.size(); ++i){
            sum.vector.set(i, sum.vector.get(i) + other.vector.get(i));
            System.out.println(sum.vector.get(i));
        }
        sum.dump();
        return sum;
    }
}
class VectorsOfDistinctLengthException extends Exception{
    private MyVector vec1, vec2;
    public VectorsOfDistinctLengthException(MyVector vec1, MyVector vec2){
        this.vec1 = vec1;
        this.vec2 = vec2;
    }
    public MyVector[] repair(){
        vec1 = new MyVector();
        vec2 = new MyVector();
        MyVector[] vec = new MyVector[2];
        vec[0] = vec1;
        vec[1] = vec2;
        return vec;
    }
    public void command(){
        System.out.printf("Length of the first vector equals: %d, length of the second vector equals: %d\n", vec1.getLength(), vec2.getLength());
    }
}
class Vectors {
    public static void main(String[] args) {
        MyVector[] vec = new MyVector[2];
        for(int i = 0; i < vec.length; ++i){
            vec[i] = new MyVector();
        }
        while(true) {
            try {
                vec[0].Add(vec[1]);
                break;
            } catch (VectorsOfDistinctLengthException vecEx) {
                vecEx.command();
                vec = vecEx.repair();
            }
        }
    }
}