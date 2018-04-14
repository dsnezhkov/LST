/*
DiscoFS.js

 Examples:
 // Calibrate: get exact pixel location under mouse
 node color.js  -c
 // Poll from specific coordinates without use of a mouse
 node color.js  -m coords  -x 1179 -y 248
 // Poll from specific coordinates without use of a mouse, and specify poll delay (ms)
 node color.js  -m coords  -x 1179 -y 248 -p 50
 // Poll from coordinates with use of a mouse
 node color.js  -m mouse
 // Poll from coordinates with use of a mouse, and specify poll delay (ms)
 node color.js  -m mouse -p 50
 // Poll and save B64 into log.txt, and then convert into binary into hello.recv
 node color.js  -m coords -x 978 -y 228 -p 10 -l ./log.txt -o hello.recv
 */
const robot = require("robotjs");
const sleep = require('sleep');
const fs    = require('fs');
const btoa  = require('btoa');
const commandLineArgs = require('command-line-args')


// Gets mous coords
function getMouseCoords(){
    var mousecoords = [];
    mouseCoordObj = robot.getMousePos()
    mousecoords.push(mouseCoordObj.x);
    mousecoords.push(mouseCoordObj.y);
    return mousecoords;
}

// Gets color of pixel under coords
function getPixelColor(x,y){
	return robot.getPixelColor(x,y);
}

// Records pixel color in a bag, applying  adjustments on deviation
function recordPixel(hexcolor, x, y){

    var baseHexColor = "";
    var adjusted = false;

    if ( hexcolor in b64colors || hexcolor == control || hexcolor == fence ){
       baseHexColor = hexcolor;
    }else{
       baseHexColor = getSimilarColors(hexcolor);
       adjusted = true;
    }

    if ( (Hexcolor['current'] != baseHexColor) && (hexcolor != fence)){ // skip fence

        Hexcolor['current']=baseHexColor;

        console.log("Recording New Data #" + baseHexColor + (adjusted ? " ~  " +  hexcolor  : "" )+ " at x:" + x + " y:" + y);

        // Skip Start/Stop markers
        if ( baseHexColor != control ){
            var ascii = b64colors[baseHexColor];
            console.log("Ascii: " + ascii);
            //fs.appendFileSync('log.txt', hexcolor + '\n');
            fs.appendFile(log, ascii, function(err) {
                if (err) throw err;
            });
        }else{
            console.log("Start/Stop #" + baseHexColor);
        }

    }else{
        if (hexcolor == fence) {
            Hexcolor['current']='NaN'; // guard reset
            //console.log("Fence #" + hexcolor);
        }

    }

}

// Reads pixel 
function readPixel(polldelay, coords){
    var x = coords[0];
    var  y = coords[1];


    console.log("Will Poll on coordinates =>  x:" + x + " y:" + y +
                " with delay of " + polldelay);

    console.log("Creating Log: " + log)
    fs.closeSync(fs.openSync(log, 'w'));

    var startTrigger=false
    var stopTrigger=false
    var running = false
    while(1){

        // Exit polling
        if (stopTrigger == true) {return}

        sleep.msleep(polldelay);

        var hexcolor = getPixelColor(x,y);

        // Start / Stop polling
        if (hexcolor == control || getSimilarColors(hexcolor) == control ){
            if ( running == true ){
                if (Hexcolor['current'] == hexcolor ) {
                    continue;
                }else{
                    startTrigger = false
                    stopTrigger = true
                    running = false
                    console.log("Stop ...");
                    continue;
                }
            }else{
                Hexcolor['current'] = hexcolor;
                startTrigger = true
                stopTrigger = false
                running = true
                console.log("Start ...");
                continue;
            }
        }

        if( startTrigger == true){
            recordPixel(hexcolor, x, y);
        }

    }

}

// Dumps data to file
function convertFile(){

    fs.readFile(log, 'ascii', function (err,b64data) {
        if (err) {
            return console.log(err);
        }

        data = new Buffer(b64data, 'base64');
        fs.writeFile(out, data, (err) => {
            if (err) throw err;
            console.log('The file has been converted.');
        });
    });
}

// Matching engine for HTML colors
function createB64Colors(ccodes, charset) {
    var arr = {};
    for(var i = 0, ii = ccodes.length; i<ii; i++) {
        arr[ccodes[i]] = charset[i];
    }
    return arr;
}

// Logic to figure out similar (adjacent) colors
function getSimilarColors (color) {

    //Convert to RGB, then R, G, B
    var color_rgb = hex2rgb(color);
    var color_r = color_rgb.split(',')[0];
    var color_g = color_rgb.split(',')[1];
    var color_b = color_rgb.split(',')[2];

    //Create an emtyp array for the difference betwwen the colors
    var differenceArray=[];

    //Function to find the smallest value in an array
    Array.min = function( array ){
        return Math.min.apply( Math, array );
    };


    //Convert the HEX color in the array to RGB colors, split them up to R-G-B, then find out the difference between the "color" and the colors in the array
    for (var value of ccodes) {
        var base_color_rgb = hex2rgb(value);
        var base_colors_r = base_color_rgb.split(',')[0];
        var base_colors_g = base_color_rgb.split(',')[1];
        var base_colors_b = base_color_rgb.split(',')[2];

        //Add the difference to the differenceArray
        differenceArray.push( 
          Math.sqrt(
(color_r-base_colors_r) * (color_r-base_colors_r) + (color_g-base_colors_g) * (color_g-base_colors_g) + (color_b-base_colors_b) * ( color_b - base_colors_b)
          )
        );
    }

    //Get the lowest number from the differenceArray
    var lowest = Array.min(differenceArray);

    //Get the index for that lowest number
    var index = differenceArray.indexOf(lowest);

    //Function to convert HEX to RGB
    function hex2rgb( color ) {
        var r,g,b;
        if ( color.charAt(0) == '#' ) {
            color = color.substr(1);
        }

        r = color.charAt(0) + color.charAt(1);
        g = color.charAt(2) + color.charAt(3);
        b = color.charAt(4) + color.charAt(5);

        r = parseInt( r,16 );
        g = parseInt( g,16 );
        b = parseInt( b ,16);
        return r+','+g+','+b;
    }

    //Return the HEX code
    return ccodes[index];
}
const optionDefinitions = [
    { name: 'calibrate', alias: 'c', type: Boolean },
    { name: 'method', alias: 'm', type: String, multiple: false },
    { name: 'lat', alias: 'x', type: Number },
    { name: 'long', alias: 'y', type: Number },
    { name: 'polldelay', alias: 'p', type: Number },
    { name: 'log', alias: 'l', type: String, multiple: false },
    { name: 'out', alias: 'o', type: String, multiple: false }
]

const options = commandLineArgs(optionDefinitions)

var Hexcolor = {};
var control='ffffff';
var fence='000000';
var polldelay=20 // default poll delay
var log='./log.txt' // default log file
var out='./a.out' // default output file

var charset = [
    'A','B','C','D','E','F','G','H','I','J',
    'K','L','M','N','O','P','Q','R','S','T',
    'U','V','W','X','Y','Z','a','b','c','d',
    'e','f','g','h','i','j','k','l','m','n',
    'o','p','q','r','s','t','u','v','w','x',
    'y','z','0','1','2','3','4','5','6','7',
    '8','9','+','/','='
];

var ccodes = [
    '0000ff', '00ff00', '00ffff', 'ff0000', 'ff00ff', 'ffff00', '0000aa', '00ffaa', '00aa00',
    '00aaff', '00aaaa', 'ff00aa', 'ffffaa', 'ffaa00', 'ffaaff', 'ffaaaa', 'aa0000', 'aa00ff', 'aa00aa',
    'aaff00', 'aaffff', 'aaffaa', 'aaaa00', 'aaaaff', 'aaaaaa', '000055', '00ff55', '00aa55', '005500',
    '0055ff', '0055aa', '005555', 'ff0055', 'ffff55', 'ffaa55', 'ff5500', 'ff55ff', 'ff55aa', 'ff5555',
    'aa0055', 'aaff55', 'aaaa55', 'aa5500', 'aa55ff', 'aa55aa', 'aa5555', '550000', '5500ff', '5500aa',
    '550055', '55ff00', '55ffff', '55ffaa', '55ff55', '55aa00', '55aaff', '55aaaa', '55aa55', '555500',
    '5555ff', '5555aa', '555555', '666666', '777777', '999999'
];
var b64colors = createB64Colors(ccodes, charset);
//console.log(b64colors);


if ( options.calibrate === true) {
    while(1) {
        var coords = getMouseCoords();
        console.log("MC:    -x " + coords[0] + " -y " + coords[1]);
        sleep.msleep(100);
    }
    return
}
if ( 'polldelay' in options ){
   polldelay=options.polldelay;
}
if ( 'out' in options ){ out = options.out }
if ( 'log' in options ){ log = options.log }

switch(options.method) {
    case 'mouse':
        readPixel(polldelay,getMouseCoords())
        break;
    case 'coords':
        if (( 'long' in options)  && ('lat' in options )){
            readPixel(polldelay,[options.lat, options.long])
            convertFile();
        }else{
            console.log("Logitude and Lattitude need to be specified");
        }
        break;
    default:
  default:
        var usage = `

 USAGE Examples:

 // Calibrate: get exact pixel location under mouse
 node discoFS.js  -c
 // Poll from specific coordinates without use of a mouse
 node discoFS.js  -m coords  -x 1179 -y 248
 // Poll from specific coordinates without use of a mouse, and specify poll delay (ms)
 node discoFS.js  -m coords  -x 1179 -y 248 -p 50
 // Poll from coordinates with use of a mouse
 node discoFS.js  -m mouse
 // Poll from coordinates with use of a mouse, and specify poll delay (ms)
 node discoFS.js  -m mouse -p 50
 // Poll and save B64 into log.txt, and then convert into binary into hello.recv
 node discoFS.js  -m coords -x 978 -y 228 -p 10 -l ./log.txt -o hello.recv
`
        console.log(`${usage}`);
        return
}


