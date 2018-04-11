import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

import javax.imageio.ImageIO;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Color;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

/**
 * Capture a portion of the screen, data from QRCode
 */
public class Screen{

    // Read QR from a specific image file
    private static String readQRCode(String filePath,  Map hintMap)
            throws IOException, NotFoundException {
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(
                        ImageIO.read(new FileInputStream(filePath)))));
        Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap,
                hintMap);
        return qrCodeResult.getText();
    }

    // Read QR from Image buffer
    private static String readQRCodeBuffer(BufferedImage image,  Map hintMap){
        Result qrCodeResult = null;
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(
                        image)));
        try{

            qrCodeResult = new QRCodeReader().decode(binaryBitmap, hintMap);
            return qrCodeResult.getText();
        }catch (NotFoundException nfe){
            System.err.println(nfe.getMessage());
            return "Not Found Error";
        }catch (ChecksumException ce){
            System.err.println(ce.getMessage());
            return "Checksum Error";
        }catch (FormatException fe){
            System.err.println(fe.getMessage());
            return "Format Error";
        }

    }

    // Write capture for debug
    private static void writeImgFile(BufferedImage image, int id){
        String format = "png";
        String filePath = String.valueOf(id) +  "Capture.png";
        try{
            ImageIO.write(image, format, new File(filePath));
            System.out.println("Captured " + filePath);
        }catch (IOException ioe){
            System.err.println("IOException" + ioe.getMessage());
        }
    }

    // Write decoded data to file
    private static void writeDataFile(byte[] data, String dataFileName){
        FileOutputStream stream;

        try {
            stream = new FileOutputStream(dataFileName);
            stream.write(data);
            stream.close();
        }catch( FileNotFoundException fnf){
            System.err.println("File not found" + fnf.getMessage());
        }catch ( IOException ioe){
            System.err.println("Error saving dataFile : " + ioe.getMessage());
        }
    }
    public static void main(String[] args) throws NotFoundException, IOException, InterruptedException {

        Boolean Debug=true;
        try {
            Robot robot = new Robot();

            String b64data;             // Clean encoded string
            String b64data_raw = "";    // Read  encoded string
            String b64data_prev = "";   // Buffer encoded string
            StringBuilder sb = new StringBuilder();
            Integer captureHeight = 250;
            Integer captureWidth = 250;
            Integer scannerInterval= 300;
            String recvDataFile = "data";

            if (Debug){
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int resolution = Toolkit.getDefaultToolkit().getScreenResolution();
                GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                int width = gd.getDisplayMode().getWidth();
                int height = gd.getDisplayMode().getHeight();
                System.out.println("ScreenSize : " + screenSize);
                System.out.println("Resolution : " + resolution);
                System.out.println("Graphics Env width:" + width + " height: " + height);
            }


            System.out.println(">>> Place mouse over top left(x,y) of capture. [Enter] when ready");
            System.console().readLine();
            Point tl = MouseInfo.getPointerInfo().getLocation();
            Double tlX = tl.getX();
            Integer tlXi = tlX.intValue();
            Double tlY = tl.getY();
            Integer tlYi = tlY.intValue();
            System.out.println("Mouse [Top Left] read: X:" + tlXi + " Y: " + tlYi);


            System.out.println(">>> Place mouse over control pixel in QR Code. [Enter] when ready");
            System.console().readLine();
            Point cp = MouseInfo.getPointerInfo().getLocation();
            Double cpX = cp.getX();
            Integer cpXi = cpX.intValue();
            Double cpY = cp.getY();
            Integer cpYi = cpY.intValue();
            System.out.println("Mouse [Control] read : X:" + cpXi + " Y: " + cpYi);
            Color ctrlColor = robot.getPixelColor(cpXi,cpYi);
            String ctrlColorRGB = Integer.toHexString(ctrlColor.getRGB());
            ctrlColorRGB = ctrlColorRGB.substring(2, ctrlColorRGB.length());
            System.out.println("Watch for control pixel color (sRGB native) on this computer: #"  + ctrlColorRGB);

            System.out.println(">>> Load File in QR Emitter. [Enter] when ready to read");
            System.out.println("Scanner Interval " + scannerInterval + "ms");
            System.console().readLine();

            int i = 0;
            long startTime = System.currentTimeMillis();
            while (true) {

                Thread.sleep(scannerInterval);

                // Wait for stop and save data to file
                // We cannot rely on HTML colors and native colors be the same. 
                // TODO: calibrate function and check against that value
                if (robot.getPixelColor(cpXi,cpYi).equals(Color.black) ){
                    System.out.println("<<< Stop QR Capture");
                    byte[] decoded = Base64.getDecoder().decode(sb.toString());

                    if (Debug){
                        System.out.println("::: Data received (decoded): "
                                + new String(decoded, "UTF-8"));
                    }

                    System.out.println("::: Saving Data to  " + recvDataFile );
                    writeDataFile(decoded, recvDataFile);
                    break;
                }
                Rectangle captureRect = new Rectangle(tlXi, tlYi, captureHeight, captureWidth); // positions

                // Capture screen area
                BufferedImage screenImage = robot.createScreenCapture(captureRect);
                if (Debug){
                   writeImgFile(screenImage, i++);
                }

                // Guide QR decoder
                Map hintMap = new HashMap();
                hintMap.put(DecodeHintType.CHARACTER_SET, CharacterSetECI.UTF8);
                hintMap.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
                hintMap.put(DecodeHintType.TRY_HARDER, true);

                b64data_raw=readQRCodeBuffer(screenImage, hintMap);

                // Skip "seen" QR codes (scanner is faster that emitter)
                if (b64data_raw.equals(b64data_prev)){
                        assert true;
                }else{
                    // Handler for Stop/Start markers. Skip for now
                    if (b64data_raw.equals("start") || b64data_raw.equals("stop")){
                        System.out.print(".");
                        continue;
                    }
                    b64data = b64data_raw.replaceAll("_", "=");
                    System.out.println("[+] " + b64data);
                    sb.append(b64data);
                    b64data_prev = b64data_raw;
                }
            }

            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            System.out.println("--------------------------");
            System.out.println("Execution time (s): " + elapsedTime/1000);

           } catch (AWTException ex) {
            System.err.println(ex);
        }
    }

}
