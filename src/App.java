// Program Description: This program is a java implementation of the LZ77 compression algorithm
// Last Modification Date: 30/10/2023
// First author - ID : Salma Mohammed Mahmoud / 20210161
// Second author - ID : Ahmed Reda El-Sayed/ 20210018
// Under The Supervision of: Dr. Tawfik

import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        Scanner s=new Scanner(System.in);
        while(true){
            System.out.println("1- Enter 1 to compress");
            System.out.println("2- Enter 2 to decompress");
            System.out.println("3- Enter 0 to exit");
            int choice=s.nextInt();
            LZ77 lz=new LZ77();
            if(choice==1){
                System.out.println("Enter the file name");
                String filename=s.next();
                filename=filename+".txt";
                lz.compress(filename);
            }
            else if(choice==2){
                System.out.println("Enter the file name");
                String filename=s.next();
                filename=filename+".txt";
                lz.decompress(filename);
            }
            else if(choice==0){
                break;
            }
            else{
                System.out.println("Enter a valid choice");
            }
        }
        s.close();
    }
}