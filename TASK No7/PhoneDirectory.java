import java.util.Iterator;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map;

class Person extends Entry {
    private PhoneNumber phoneNumber;
    private String name;
    private String lastName;
    private String address;
    @Override
    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }
    public Person(Integer code, Long number, String address, String name, String lastName){
        phoneNumber = new PhoneNumber(code, number);
        this.address = address;
        this.name = name;
        this.lastName = lastName;
    }
    @Override
    public void description(){
        System.out.println("----------------------------------------");
        System.out.println("Name and last name: " + name + " " + lastName);
        System.out.println("Address : " + address);
        System.out.println("Phone number: (+" + phoneNumber.getDialingCode().toString() + ")" + phoneNumber.getPhoneNumber().toString());
        System.out.println("----------------------------------------");
    }
}
class Company extends Entry{
    private PhoneNumber phoneNumber;
    private String address;
    private String companyName;
    @Override
    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }
    public Company(Integer code, Long number, String address, String companyName){
        phoneNumber = new PhoneNumber(code, number);
        this.address = address;
        this.companyName = companyName;
    }
    @Override
    public void description(){
        System.out.println("----------------------------------------");
        System.out.println("Name of Company: " + companyName);
        System.out.println("Address of Company: " + address);
        System.out.println("Phone number: (+" + phoneNumber.getDialingCode().toString() + ")" + phoneNumber.getPhoneNumber().toString());
        System.out.println("----------------------------------------");
    }
}
class PhoneNumber implements Comparable{
    private Integer dialingCode;
    private Long phoneNumber;
    public Integer getDialingCode(){
        return dialingCode;
    }
    public Long getPhoneNumber(){
        return phoneNumber;
    }
    public PhoneNumber(Integer code, Long number){
        dialingCode = code;
        phoneNumber = number;
    }
    @Override
    public int compareTo(Object o) {
        return o instanceof PhoneNumber ? (((PhoneNumber)o).getDialingCode().compareTo(this.getDialingCode()) == 0 ? ((PhoneNumber) o).getPhoneNumber().compareTo(this.getPhoneNumber()) : ((PhoneNumber)o).getDialingCode().compareTo(this.getDialingCode())) : 1;
    }
}
abstract class Entry{
    abstract public void description();
    abstract public PhoneNumber getPhoneNumber();
}
class Book{
    private TreeMap<PhoneNumber, Entry> phoneDirectory;
    public Book(ArrayList<Entry> arr){
        phoneDirectory = new TreeMap<>();
        for(Entry e : arr){
            phoneDirectory.put(e.getPhoneNumber(), e);
        }
    }
    public void printBook(){
        Iterator<Map.Entry<PhoneNumber, Entry>> iterator = phoneDirectory.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<PhoneNumber, Entry> nextNode = iterator.next();
            System.out.println("This key: (+" + nextNode.getKey().getDialingCode().toString() + ")" + nextNode.getKey().getPhoneNumber().toString()+ " has an entry in book:");
            nextNode.getValue().description();
        }
    }
}
class PhoneDirectory {
    public static void main(String[] args) {
        ArrayList<Entry> listOfPersonAndCompanies = new ArrayList<>();
        listOfPersonAndCompanies.add(new Person(48, 911666777L, "Mdla 4, Lodz, Poland", "Limak", "Ikswotsurhc"));
        listOfPersonAndCompanies.add(new Company(49, 71140040990L, "Robert-Bosch-Platz 1, Gerlingen, Germany", "Robert Bosch GmbH"));
        listOfPersonAndCompanies.add(new Person(48, 213701488L, "Dziady 44, Zelazko, Poland", "Juliusz", "Slowacki"));
        listOfPersonAndCompanies.add(new Company(48, 126461000L, "Jana Pawla II 39a, Krakow, Poland", "Comarch SA"));
        Book phoneDirectory = new Book(listOfPersonAndCompanies);
        phoneDirectory.printBook();
    }
}