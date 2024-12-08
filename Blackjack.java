import java.net.Socket;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

//main class
class Blackjack{
	static DataInputStream dis;
	static DataOutputStream dos;
    static int handtotal = 0; //variable for the main total amount of the hand
	static String[] fullstr; //variable that contains a split array of what is read from the dealer
	static String[] upcard; //split array of the dealer upcard
	static String returnstr = ""; //variable returns the string for what it wants to play(split,double,stand,hit)
    static String upcard1; //the number of the dealer upcard. It is upcard[0]. 
    static Boolean response = false; //boolean used in strategies
    static Socket socket; 
    String var1; //variable used to read the dealers replies

	//Blackjack constructor for IpAddress, IpPort, and socket
	public Blackjack(String s1, String s2) throws IOException {
		String IpAddress = s1;
		String IpPort = s2;
		socket = new Socket(IpAddress, Integer.valueOf(IpPort));
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());

	}

	//main code that plays and checks what the dealer sends and plays the game based on that(calculates betting, calls methods for strategies)
	public static void main(String[] args) throws IOException{
		Blackjack Startup = new Blackjack(args[0], args[1]); 
		String var1 = ""; 
		while(!var1.startsWith("done")){ //while loop with game protocol
			var1 = Startup.read();
			fullstr = var1.split(":");
			if(var1.startsWith("login")){
				Startup.write("Bareclaw:Bereket");
			}
			else if(var1.startsWith("status")){ 
				//do not do anything
			}
			else if(var1.startsWith("bet")){
				int betamount = 1; //the amount that I will be betting 
				int runningcount = 0; //a count for adding / subtracting each card that has been dealth
				int t = 3; //index where the first card starts at when the dealer sends the bet reply
				int decksleft; //amount of card decks left
				int deckhave; //amount of card decks that have been dealt already
				int truecount; //count per deck
				while(t < fullstr.length){  
					runningcount += getbetcount(fullstr[t].charAt(0));
					t++;
				}
				deckhave = ((fullstr.length - 3) / 52); 
        		decksleft = (7 - deckhave);
        		truecount = (runningcount / decksleft);
				if((truecount > 0) && (truecount <= 2)){
					betamount = 2; 
				}
				else if(truecount >= 10){
					betamount = 10; 
				}
				else if((truecount >= 2) && (truecount <= 8)){
					betamount = 6;
				}
				else if(truecount == 1){
					betamount = 5; 
				}
				else if(truecount == 0){
					betamount = 1; 
				}
				else if(truecount == -1){
					betamount = 1; 
				}
				else{
					betamount = 1; 
				}  
				Startup.write("bet:" + betamount);
			}
			else if(var1.startsWith("play")){ 
				upcard = fullstr[2].split(""); 
				upcard1 = upcard[0];	
				int i = 4; 
				String cardval = "";  
	            while(i >= 4 && i < fullstr.length){ //while loop loops through each card and 
	                cardval = fullstr[i];  
	                handtotal += getnum(cardval);
	                i++;
	            }
	            for(int index = 4; index < fullstr.length; index++){
	                if((handtotal > 21) && (fullstr[index].contains("A"))){
	                    handtotal -= 10; 
	                }
            	}

		        Startup.write(play(handtotal, upcard1));
		        handtotal = 0; 
			} 
		} 
		socket.close();
	}
	//method for the card counting part of the betting for each card that is dealt. 
	public static int getbetcount(char card){
        switch(card){
            case '2':
            	return 1;
            case '3':
            	return 1;
            case '4':
            	return 1;
            case '5':
            	return 1;
            case '6':
            	return 1; 

            case '7':
            	return 0;
            case '8':
            	return 0;
            case '9':
            	return 0;

            case '1':
            	return -1;
            case 'J':
            	return -1;
            case 'Q':
            	return -1;
            case 'K':
            	return -1;
            case 'A':
            	return -1; 

        default:
        	return 0; 

        }
    }
	//method for getting the value of the card and adding it to the temphandtotal.
	public static int getnum(String a)throws IOException{
        int temphandtotal = 0; //local variable for the value of the card going through the getnum method
        if(a.charAt(0) == ('A')){
            if(handtotal <= 10){
                temphandtotal += 11;    
            }
        }
        //conditions to set the card string value to its number to add to the total value of the hand
        if(a.charAt(0) == ('J') || a.charAt(0) == ('K') || a.charAt(0) == ('Q')){
            temphandtotal += 10;
        }
        else if(a.charAt(0) == '1'){
            temphandtotal += 10; 
        }
        else if(a.charAt(0) == '2'){
            temphandtotal += 2; 
        }
        else if(a.charAt(0) == '3'){
            temphandtotal += 3; 
        }
        else if(a.charAt(0) == '4'){
            temphandtotal += 4; 
        }
        else if(a.charAt(0) == '5'){
            temphandtotal += 5; 
        }
        else if(a.charAt(0) == '6'){
            temphandtotal += 6; 
        }
        else if(a.charAt(0) == '7'){
            temphandtotal += 7; 
        }
        else if(a.charAt(0) == '8'){
            temphandtotal += 8; 
        }
        else if(a.charAt(0) == '9'){
            temphandtotal += 9; 
        }
    	return temphandtotal; 
    }
    //method that contains the conditions with the strategies to play 
    public static String play(int n, String j)throws IOException{
            handtotal = n; //total value of hand
            upcard1 = j; //dealer upcard
            returnstr = ""; 
            Boolean response = false;
        if((fullstr[4].charAt(0) == fullstr[5].charAt(0)) && fullstr.length == 6){ //big condition for it there are only 2 cards in the hand and they are both equal to eachother.
                if(((fullstr[4].charAt(0) == '8' && fullstr[5].charAt(0) == '8'))){
                    returnstr = "split"; 
                    response = true;
                }
                else if((fullstr[4].charAt(0) == 'K' && fullstr[5].charAt(0) == 'K')){
                	returnstr = "stand"; 
                	response = true;
                }
                else if((fullstr[4].charAt(0) == 'Q' && fullstr[5].charAt(0) == 'Q')){
                	returnstr = "stand";
                	response = true; 
                }
                else if((fullstr[4].charAt(0) == 'J' && fullstr[5].charAt(0) == 'J')){
                	returnstr = "stand"; 
                	response = true;
                }
                else if((fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == 'A')){
                	returnstr = "split"; 
                	response = true;
                }
                else if((fullstr[4].charAt(0) == '1' && fullstr[5].charAt(0) == '1')){
                    returnstr = "stand";
                    response = true; 
                }
                else if((fullstr[4].charAt(0) == '9' && fullstr[5].charAt(0) == '9') && (j.equals("2") || j.equals("3") || j.equals("4") || j.equals("5") || j.equals("6") || j.equals("8") || j.equals("9"))){
                    returnstr = "split";
                    response = true;
                } 
                else if((fullstr[4].charAt(0) == '9' && fullstr[5].charAt(0) == '9') && (j.equals("7") || j.equals("1") || j.equals("J") || j.equals("Q") || j.equals("K") || j.equals("K") || j.equals("A"))){
                    returnstr = "stand"; 
                    response = true;
                }
                else if((fullstr[4].charAt(0) == '7' && fullstr[5].charAt(0) == '7') && (j.equals("2") || j.equals("3") || j.equals("4") || j.equals("5") || j.equals("6") || j.equals("7"))){
                    returnstr = "split";
                    response = true;
                }
                else if((fullstr[4].charAt(0) == '7' && fullstr[5].charAt(0) == '7') && (j.equals("8") || j.equals("9") || j.equals("1") || j.equals("J") || j.equals("Q") || j.equals("K") || j.equals("A"))){
                    returnstr = "hit"; 
                    response = true;
                }
                else if((fullstr[4].charAt(0) == '6' && fullstr[5].charAt(0) == '6') && (j.equals("2") || j.equals("3") || j.equals("4") || j.equals("5") || j.equals("6"))){
                    returnstr ="split";
                    response = true;
                }
                else if((fullstr[4].charAt(0) == '6' && fullstr[5].charAt(0) == '6') && (j.equals("7") || j.equals("8") || j.equals("9") || j.equals("1") || j.equals("J") || j.equals("Q") || j.equals("K") || j.equals("A"))){
                    returnstr ="hit";
                    response = true;
                }
                else if((fullstr[4].charAt(0) == '5' && fullstr[5].charAt(0) == '5') && (j.equals("2") || j.equals("3") || j.equals("4") || j.equals("5") || j.equals("6") || j.equals("7") || j.equals("8") || j.equals("9"))){
                    returnstr = "double"; 
                    response = true;
                }
                else if((fullstr[4].charAt(0) == '5' && fullstr[5].charAt(0) == '5') && (j.equals("1") || j.equals("J") || j.equals("Q") || j.equals("K") || j.equals("A"))){
                    returnstr = "hit"; 
                    response = true;
                }
                else if((fullstr[4].charAt(0) == '4' && fullstr[5].charAt(0) == '4') && (j.equals("5") || j.equals("6"))){
                    returnstr = "split"; 
                    response = true;
                }
                else if((fullstr[4].charAt(0) == '4' && fullstr[5].charAt(0) == '4') && (j.equals("2") || j.equals("3") || j.equals("4") || j.equals("7") || j.equals("8") || j.equals("9") || j.equals("1") || j.equals("J") || j.equals("Q") || j.equals("K") || j.equals("A"))){
                    returnstr = "hit";
                    response = true;
                }
                else if((fullstr[4].charAt(0) == '3' && fullstr[5].charAt(0) == '3') && (j.equals("2") || j.equals("3") || j.equals("4") || j.equals("5") || j.equals("6") || j.equals("7"))){
                    returnstr = "split";
                    response = true;
                }
                else if((fullstr[4].charAt(0) == '3' && fullstr[5].charAt(0) == '3') && (j.equals("8") || j.equals("9") || j.equals("1") || j.equals("J") || j.equals("Q") || j.equals("K") || j.equals("A"))){
                    returnstr = "hit";
                    response = true;
                }
                else if((fullstr[4].charAt(0) == '2' && fullstr[5].charAt(0) == '2') && (j.equals("2") || j.equals("3") || j.equals("4") || j.equals("5") || j.equals("6") || j.equals("7"))){
                    returnstr = "split";
                    response = true;
                }
                else if((fullstr[4].charAt(0) == '2' && fullstr[5].charAt(0) == '2') && (j.equals("8") || j.equals("9") || j.equals("1") || j.equals("J") || j.equals("Q") || j.equals("K") || j.equals("A"))){
                    returnstr = "hit"; 
                    response = true;
                } 
                else if((n == 11)){
                	returnstr = "double"; 
                	response = true;
            	}
        }
        if(fullstr[4].charAt(0) != fullstr[5].charAt(0)){ //big if condition for if the first 2 cards are not equal to eachother.
            if(fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == '9'){
                returnstr = "stand";
                response = true;
            }
            else if((fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == '8') && (j.equals("2") || j.equals("3") || j.equals("4") || j.equals("5") || j.equals("7") || j.equals("8") || j.equals("9") || j.equals("1") || j.equals("J") || j.equals("Q") || j.equals("K") || j.equals("A"))){
                returnstr = "stand";
                response = true;
            }
            else if((fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == '7') && (j.equals("9") || j.equals("1") || j.equals("J") || j.equals("K") || j.equals("Q") || j.equals("A"))){
                returnstr = "hit";
                response = true;
            }
            else if((fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == '7') && (j.equals("7") || j.equals("8"))){
                returnstr = "stand";
                response = true;
            }
            else if((fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == '6') && (j.equals("2") || j.equals("7") || j.equals("8") || j.equals("9") || j.equals("1") || j.equals("J") || j.equals("K") || j.equals("Q") || j.equals("A"))){
                returnstr = "hit";
                response = true;
            }
            else if((fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == '5') && (j.equals("2") || j.equals("3") || j.equals("7") || j.equals("8") || j.equals("9") || j.equals("1") || j.equals("J") || j.equals("K") || j.equals("Q") || j.equals("A"))){
                returnstr = "hit";
                response = true;
            }
            else if((fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == '4') && (j.equals("2") || j.equals("3") || j.equals("7") || j.equals("8") || j.equals("9") || j.equals("1") || j.equals("J") || j.equals("K") || j.equals("Q") || j.equals("A"))){
                returnstr = "hit";
                response = true;

            }
            else if((fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == '3') && (j.equals("2") || j.equals("3") || j.equals("4") || j.equals("7") || j.equals("8") || j.equals("9") || j.equals("1") || j.equals("J") || j.equals("K") || j.equals("Q") || j.equals("A"))){
                returnstr = "hit";
                response = true;
            }
            else if((fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == '2') && (j.equals("2") || j.equals("3") || j.equals("4") || j.equals("7") || j.equals("8") || j.equals("9") || j.equals("1") || j.equals("J") || j.equals("K") || j.equals("Q") || j.equals("A"))){
                returnstr = "hit";
                response = true;
            }
            //if there are no Aces in hand, or if Ace is dealt and it counts as 1 instead of 11 
            else if((n >= 12 && n <= 16) && (j.equals("7") || j.equals("8") || j.equals("9") || j.equals("10") || j.equals("J") || j.equals("K") || j.equals("Q") || j.equals("A"))) {
                returnstr = "hit";
                response = true; 
            }
            else if((n >= 5 && n <= 8) && ((j.equals("2") || j.equals("3") || j.equals("4") || j.equals("5") || j.equals("6") || j.equals("7") || j.equals("8") || j.equals("9") || j.equals("1") || j.equals("J") || j.equals("K") || j.equals("Q") || j.equals("A")))){
                returnstr = "hit";
                response = true; 
            }
            else if((n <= 4) && (j.equals("2") || j.equals("3") || j.equals("4") || j.equals("5") || j.equals("6") || j.equals("7") || j.equals("8") || j.equals("9") || j.equals("10") || j.equals("A"))){
            	returnstr = "hit";
            	response = true;
            }
            else if((n == 16 || n == 15 || n == 14 || n == 13) && (j.equals("2") || j.equals("3") || j.equals("4") || j.equals("5") || j.equals("6"))){
                returnstr = "stand";
                response = true;
            }
            else if (((n >= 12 && n <= 16) && (j.equals("4") || j.equals("5") || j.equals("6")))){
                returnstr = "stand";
                response = true;
            } 
            else if (((n >= 12 && n <= 16) && (j.equals("2") || j.equals("3") || j.equals("7") || j.equals("8") || j.equals("9") || j.equals("1") || j.equals("J") || j.equals("K") || j.equals("Q") || j.equals("K") || j.equals("A")))){
                returnstr = "hit";
                response = true;
            }
            else if ((n >= 17 && n <= 21) && (fullstr[4].charAt(0) != fullstr[5].charAt(0))){
                returnstr = "stand";
                response = true;
            }
            else if((n >= 13 && n <= 16) && (j.equals("2") || j.equals("3") || j.equals("4") || j.equals("5") || j.equals("6"))){/*if both cards are 9 and dealer's card is a 7 or a 10 or A*/
                returnstr = "stand";
                response = true;
            }
            else if((n >= 13 && n <= 16) && (j.equals("7") || j.equals("8") || j.equals("9") || j.equals("1") || j.equals("J") || j.equals("K") || j.equals("Q") || j.equals("A"))){/*if both cards are 9 and dealer's card is a 7 or a 10 or A*/
                returnstr = "hit";
                response = true;
            }
            else if((n == 10) && (j.equals("1") || j.equals("J") || j.equals("K") || j.equals("Q") || j.equals("A"))){
                returnstr = "hit"; 
                response = true;
            }
            else if((n == 9) && (j.equals("2") || j.equals("7") || j.equals("8") || j.equals("9") || j.equals("1") || j.equals("J") || j.equals("K") || j.equals("Q") || j.equals("A"))){
            	returnstr = "hit"; 
            	response = true;
            }
        }
        if((fullstr.length == 6) && (fullstr[4].charAt(0) != fullstr[5].charAt(0))){ //condition that calls the play2 method if the hand is only 2 cards, and if they are not equal to eachother. 
        	returnstr = play2(handtotal, upcard1);
        }
	    else{
	    	if(response == false){
                returnstr = "stand"; 
            }
	    }
        return returnstr; 
    }
    //method for doubling if there are only 2 cards in the hand and they are not equal to eachother.
    public static String play2(int t, String g)throws IOException{
    	handtotal = t; //total value of hand
        upcard1 = g; //dealer upcard
        response = false; 
        	if((fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == '8') && (g.equals("6"))){
                returnstr = "double";
                response = true;
            }
			else if((fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == '7') && (g.equals("2") || g.equals("3") || g.equals("4") || g.equals("5") || g.equals("6"))){
                returnstr = "double";
                response = true;
            }
			else if((fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == '6') && (g.equals("3") || g.equals("4") || g.equals("5") || g.equals("6"))){
                returnstr = "double";
                response = true;
            }
			else if((fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == '5') && (g.equals("4") || g.equals("5") || g.equals("6"))){
                returnstr = "double";
                response = true;
            }
			else if((fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == '4') && (g.equals("4") || g.equals("5") || g.equals("6"))){
                returnstr = "double";
                response = true;
            }
			else if((fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == '3') && (g.equals("5") || g.equals("6"))){
                returnstr = "double";
                response = true;
            }
			else if((fullstr[4].charAt(0) == 'A' && fullstr[5].charAt(0) == '2') && (g.equals("5") || g.equals("6"))){
                returnstr = "double";
                response = true;
            }
			else if((t == 11) && ((g.equals("2") || g.equals("3") || g.equals("4") || g.equals("5") || g.equals("6") || g.equals("7") || g.equals("8") || g.equals("9") || g.equals("1") || g.equals("K") || g.equals("Q") || g.equals("K") || g.equals("J") || g.equals("A")))){
                returnstr = "double";
                response = true;
            }
			else if((t == 10) && ((g.equals("2") || g.equals("3") || g.equals("4") || g.equals("5") || g.equals("6") || g.equals("7") || g.equals("8") || g.equals("9")))){
                returnstr = "double";
                response = true;
            }
            else if((t == 9) && (g.equals("3") || g.equals("4") || g.equals("5") || g.equals("6"))){
            	returnstr = "double";
            	response = true; 
            }
            return returnstr; 
    }

	//method for writing words/string that the dealer will read
	private static void write(String s) throws IOException {
		dos.writeUTF(s);
		dos.flush();
	}

	//method for reading words that the dealer sends
	private static String read() throws IOException {
		String var3 = dis.readUTF();
		System.out.println("reading " + var3);
		return var3;
	}
}
