import java.util.Scanner;
import java.util.Random;
import java.util.InputMismatchException;

class Game{
    private Random generator;
    private Integer catchMeIfYouCan;
    private Scanner scan;
    private Integer attempts;
    public Game(Integer att){
        generator = new Random();
        scan = new Scanner(System.in);
        attempts = att;
    }
    public void gameStart(){
        System.out.printf("Hi IT nutcase, I bet you won't guess a number I picked randomly.\nyou've %d attempts left!!!?!?!??!?!?!??!?!\n", attempts);
        catchMeIfYouCan = generator.nextInt(101);
        Integer gameState = 0;
        for(; gameState < attempts; ++gameState){
            System.out.println("Enter a number: ");
            try {
                Integer yourTry = Integer.parseInt(scan.next());
                if(yourTry > catchMeIfYouCan){
                    System.out.printf("The number is bigger than the one I picked, try again. you've %d attempts left\n", 3-(gameState+1));
                }
                else if(yourTry < catchMeIfYouCan){
                    System.out.printf("The number is lower than the one I picked, try again. you've %d attempts left\n", 3-(gameState+1));
                }
                else{
                    break;
                }
            }
            catch (NumberFormatException inp){
                System.out.println("Not a number, you've lost chance, you've " + (3-gameState-1) + " attempts left");
            }
        }
        endGame(gameState);
    }
    private void endGame(Integer whatCanIDo){
        if(whatCanIDo < attempts){
            System.out.printf("You guessed a number in %d. attempt!\n", whatCanIDo + 1);
        }
        else{
            System.out.println("You lost a game!?!?!?!?!?!!!!!!!!, the correct number was: " + catchMeIfYouCan);
        }
        System.out.println("Do you wish to play again? (Y/N)");
        while(true){
            try {
                String input = scan.next().trim();

                if(input.length() != 1){
                    throw new InputMismatchException();
                }
                switch (Character.toUpperCase(input.charAt(0))) {
                    case 'Y':
                        gameStart();
                        break;
                    case 'N':
                        return;
                    default:
                        System.out.println("Please, provide valid character");
                        break;
                }
            }
            catch(InputMismatchException inp){
                System.out.println("Please, provide valid character");
            }
        }
    }
}
class GuessNumber {
    public static void main(String[] args) {
        Game game = new Game(3);
        game.gameStart();
    }
}