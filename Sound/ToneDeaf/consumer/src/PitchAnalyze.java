import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


public class PitchAnalyze {

    private static Map<Integer,Character> fcmapping;
    private static Map<Character,Integer> pitchmap;
    private static FileOutputStream fileOutput;
    private static StringBuilder sb;
    private static boolean recording=false;
    private static boolean VERBOSE = false;

    // Defaults
    private static String dataFile = "./output";
    private static int pitchBarrier = 20;
    private static int startPitchMark = 150;
    private static int stopPitchMark = 5000;
    private static int startPitchMarkBarrier = 10;
    private static int stopPitchMarkBarrier = 120;
    private static int pitchMapSlack = 3;
    private static int startPitchLow = 180;
    private static int startPitchGap = 40;


    public static void main(String[] args){

        // Default Charset
        Character[] charset  = new Character[] {
                'A','B','C','D','E','F','G','H','I','J',
                'K','L','M','N','O','P','Q','R','S','T',
                'U','V','W','X','Y','Z','a','b','c','d',
                'e','f','g','h','i','j','k','l','m','n',
                'o','p','q','r','s','t','u','v','w','x',
                'y','z','0','1','2','3','4','5','6','7',
                '8','9','+','/','=' };

        final int totalPitches = charset.length;

        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addOption("h", "help", false, "display usage");
        options.addOption("v", "verbose", false, "verbose run info");
        options.addOption(
                Option.builder("d")
                        .longOpt( "data-file" )
                        .desc( "Output file for incoming data"  )
                        .hasArg()
                        .argName( "FILE" )
                        .build()
        );
        options.addOption(
                Option.builder("b")
                        .longOpt( "pitch-barrier" )
                        .desc( "+/- data pitch deviation tolerance range (Hz) from base frequency"  )
                        .hasArg()
                        .argName( "NUMBER(Hz)" )
                        .build()
        );
        options.addOption(
                Option.builder("s")
                        .longOpt( "start-pitch-mark" )
                        .desc( "mark start data recording pitch with this frequency"  )
                        .hasArg()
                        .argName( "NUMBER(Hz)" )
                        .build()
        );
        options.addOption(
                Option.builder("S")
                        .longOpt( "stop-pitch-mark" )
                        .desc( "mark stop data recording pitch with this frequency"  )
                        .hasArg()
                        .argName( "NUMBER(Hz)" )
                        .build()
        );
        options.addOption(
                Option.builder("p")
                        .longOpt( "start-mark-barrier" )
                        .desc( "+/- start pitch deviation tolerance range (Hz) from base frequency"  )
                        .hasArg()
                        .argName( "NUMBER(Hz)" )
                        .build()
        );
        options.addOption(
                Option.builder("P")
                        .longOpt( "stop-mark-barrier" )
                        .desc( "+/- stop pitch deviation tolerance range (Hz) from base frequency"  )
                        .hasArg()
                        .argName( "NUMBER(Hz)" )
                        .build()
        );
        options.addOption(
                Option.builder("k")
                        .longOpt( "pitch-slack" )
                        .desc( "data frequency series reading adjustment"  )
                        .hasArg()
                        .argName( "NUMBER" )
                        .build()
        );
        options.addOption(
                Option.builder("l")
                        .longOpt( "start-pitch-low" )
                        .desc( "lowest frequency (Hz) for data analysis"  )
                        .hasArg()
                        .argName( "NUMBER(Hz)" )
                        .build()
        );
        options.addOption(
                Option.builder("g")
                        .longOpt( "pitch-gap" )
                        .desc( "frequency intervals (Hz) to match with charset.\n Correlated with pitch-barrier"  )
                        .hasArg()
                        .argName( "NUMBER(Hz)" )
                        .build()
        );
        try {

            CommandLine cmd = parser.parse( options, args);
            if(cmd.hasOption("h")) {
                System.out.println();
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "PitchAnalyzer", options );
                return;
            }
            if(cmd.hasOption("verbose")) {
                VERBOSE = true;
            }
            if(cmd.hasOption("data-file")) {
                dataFile = cmd.getOptionValue("data-file");
            }
            if(cmd.hasOption("pitch-barrier")) {
                pitchBarrier = Integer.parseInt(cmd.getOptionValue("pitch-barrier"));
            }
            if(cmd.hasOption("pitch-slack")) {
                pitchMapSlack = Integer.parseInt(cmd.getOptionValue("pitch-slack"));
            }
            if(cmd.hasOption("start-pitch-mark")) {
                startPitchMark = Integer.parseInt(cmd.getOptionValue("start-pitch-mark"));
            }
            if(cmd.hasOption("start-mark-barrier")) {
                startPitchMarkBarrier = Integer.parseInt(cmd.getOptionValue("start-mark-barrier"));
            }
            if(cmd.hasOption("stop-pitch-mark")) {
                stopPitchMark = Integer.parseInt(cmd.getOptionValue("stop-pitch-mark"));
            }
            if(cmd.hasOption("stop-mark-barrier")) {
                stopPitchMarkBarrier = Integer.parseInt(cmd.getOptionValue("stop-mark-barrier"));
            }
            if(cmd.hasOption("start-pitch-low")) {
                startPitchLow = Integer.parseInt(cmd.getOptionValue("start-pitch-low"));
            }
            if(cmd.hasOption("start-pitch-gap")) {
                startPitchGap = Integer.parseInt(cmd.getOptionValue("start-pitch-gap"));
            }

        }catch(ParseException pe){
            System.err.println("Unable to parse Options: " + pe.getMessage());
            System.err.println("Run with '-h' for usage");
            System.exit(2);
        }


        System.out.println("\n      SETTINGS      ");
        System.out.println("________________________");
        System.out.println("Output file: " + dataFile);
        System.out.println("Data Pitch Barrier: " + pitchBarrier);
        System.out.println("Data Pitch slack: " + pitchMapSlack);
        System.out.println("Start Pitch Mark: " + startPitchMark);
        System.out.println("Start Pitch Mark Barrier: " + startPitchMarkBarrier);
        System.out.println("Stop Pitch Mark: " + stopPitchMark);
        System.out.println("Stop Pitch Mark Barrier: " + stopPitchMarkBarrier);
        System.out.println("Data Pitch Low Frequency: " + startPitchLow);
        System.out.println("Data Pitch Frequency Gap: " + startPitchGap);
        System.out.println("\n");


        // Generate pitch frequency list
        Integer[] freqs = new Integer[totalPitches];
        int current = startPitchLow;
        for ( int count = 0;count < totalPitches; count++ ){
            freqs[count] = current;
            current += startPitchGap;
        }

        if (VERBOSE){
            System.out.print("Frequencies: ");
            for (int f : freqs){
               System.out.print(f + ",");
            }
            System.out.println();
        }

        fcmapping = new HashMap<>();

        // Merge charset with frequency
        for (int i = 0; i < freqs.length; i++){
            fcmapping.put(freqs[i], charset[i]);
        }

        // Make collection unmodifiable
        fcmapping = Collections.unmodifiableMap(fcmapping);


        // Pitch sampling series
        pitchmap = new HashMap<>();

        // Save data into a memory buffer (We cannot write to file fast enough between frequency sampling).
        // Maybe a separate thread here
        sb = new StringBuilder();

        AudioDispatcher dispatcher = null;
        try{
            dispatcher =
                    AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        }catch (javax.sound.sampled.LineUnavailableException lineEx){
            System.err.println("Error: " + lineEx.getMessage());
        }


        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e){
                final float pitchInHz = res.getPitch();
                processPitch(pitchInHz, fcmapping);
            }
        };


        AudioProcessor pitchProcessor = new PitchProcessor(PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        try{

            dispatcher.addAudioProcessor(pitchProcessor);
            dispatcher.run();
            System.out.println("Waiting to record pitch on Frequencies range: "
                    + freqs[0] + "(L)"
                    + " - "
                    + freqs[freqs.length-1] + "(H)"
            );
        }catch (NullPointerException npe){

            System.err.println("Error in adding audio processor " + npe.getMessage());
        }

    }


    private static void writeDataFile(){
        try
        {
            //Open the  out file for the stream
            fileOutput = new FileOutputStream(dataFile);
            System.out.println("Writing B64 string to file: ");
            System.out.println(sb.toString());

            fileOutput.write(sb.toString().getBytes());

        }
        catch (IOException e)
        {
            //Catch the IO error and print out the message
            System.out.println("Error message: " + e.getMessage());
        }
        finally {

            if (fileOutput!= null)
            {
                try{
                    fileOutput.close();
                }catch ( java.io.IOException ioe){
                    System.err.println("Unable to close stream" + ioe.getMessage());
                }
            }

        }

    }

    private static void openIncoming(){
        recording = true; // OK to record
    }
    private static void closeIncoming(){
        System.out.println("Incoming data stream finished");
        recording = false;
        writeDataFile();
        System.exit(0);
    }
    private static void recordPitchData(Character ch){
        /* byte[] decodedB = Base64.getDecoder().decode(ch.toString());
        String decodedC="";
        try {
            decodedC = new String(decodedB, "UTF-8");
        }catch (UnsupportedEncodingException encerr){
            decodedC ="_";
        }
        System.out.print(decodedC); */

        // Byte[] soundBytes = arrays.toArray(new Byte[arrays.size()]);

        System.out.print(ch);
        sb.append(ch);
    }
    private static void processPitch(float pitchInHz, Map<Integer, Character> fcmapping) {


        if (VERBOSE){
            System.out.println("Pitch outside recoding" + pitchInHz);
        }

        if (recording) {

            if (VERBOSE) {
                System.out.println("Pitch inside recording" + pitchInHz);
            }

            if (pitchInHz >= (stopPitchMark - stopPitchMarkBarrier ) && pitchInHz < (stopPitchMark + stopPitchMarkBarrier)) {
                closeIncoming();
                return;
            }

            if(pitchInHz  == -1 ) {
                // reset watcher

                if ( ! pitchmap.isEmpty()){

                    System.out.println("Pitch variation: " + pitchmap.size());

                    Map.Entry<Character, Integer> maxEntry = null;
                    for (Map.Entry<Character, Integer> entry : pitchmap.entrySet())
                    {
                         if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
                         {
                             maxEntry = entry;
                         }
                    }

                    if ( maxEntry.getValue() >= pitchMapSlack ) { // Avoid timing slack (1-2 chars in pitchmap)
                        System.out.println("Recording: " +  maxEntry.getKey() );
                        recordPitchData(maxEntry.getKey());
                    }else{
                        System.out.println("Skipping slack: " + maxEntry.getKey() );
                    }
                    // System.out.println("Nullify PitchMaps");
                    pitchmap.clear();
                }

                return;
            }

            Character mapchar;
            for (Map.Entry<Integer, Character> entry : fcmapping.entrySet()) {
                Integer freq = entry.getKey();
                if (pitchInHz >= (freq - pitchBarrier) && pitchInHz < (freq + pitchBarrier)) {
                    mapchar = entry.getValue();

                    System.out.print(mapchar);

                    if (pitchmap.containsKey(mapchar)) {
                        pitchmap.put(mapchar, pitchmap.get(mapchar) + 1);
                    } else {
                        pitchmap.put(mapchar, 1);
                    }
                    break;
                }
            }
        }else{
            if (pitchInHz >= (startPitchMark - startPitchMarkBarrier ) && pitchInHz < (startPitchMark + startPitchMarkBarrier)) {
                openIncoming();
            }

        }
    }
}
