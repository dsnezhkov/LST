import java.awt.MouseInfo;


public class Pointer {

  private static int coordX;
  private static int coordY;

  public static void main(String[] args) throws Exception{

    // Check if we are running from console
    java.io.Console c = System.console();
    if (c == null) {
       System.err.println("Should run from the console.");
       System.exit(3);
    }

    Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() { 
	        System.out.println();
	        System.out.println("-x " + coordX + " -y " + coordY + "\r");
        }
    });

	// Get Current position of center of first tile
    while (true) {
	 //String enter = c.readLine("Point mouse to the pixel, press <Enter> ");	  
	 coordX = (int) MouseInfo.getPointerInfo().getLocation().getX();
	 coordY = (int) MouseInfo.getPointerInfo().getLocation().getY();
	 System.out.print("-x " + coordX + " -y " + coordY + "        \r");
    }
  }
}
    
