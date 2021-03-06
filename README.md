# LST Toolkit  - THOTCON edition

A collection of one-off unconventional tools created with the goal of overcoming restrictions on data exfiltration vectors placed by enterprise captive portals in the form of screen remote sessions, terminal services and kiosks.

The tools follow a producer/consumer model, whereby the producer is code execute in the captive portal, and the consumer is the code executed on the local machine.

We are operating in digital to analog to digital realm. This is the biggesrt challenge.

# Components

## Exfil::Light
 - PixelTraitor - exfltration on visual spectrum
 - PixelTraitor - exfiltration on colorcodes, with variance
 - DemoQRacy - QR-code exfiltraiton

## Exfil::Sound
 - ToneDeaf
 - DogWhistle (2 modes)

## Infil::Touch
 - KeyBorat
 - Anesthesia

## Model

#### Producer
- must be lightweight and with as little dependencies as possible. OPSec should be tight and should assume as little support as possible in a way of outbound Internet connectivity. The code should be small and easily transferable across adversarial networks without triggering alarms.
- must use situational awareness on the foreign portal. Usually this means using tools mostly available on the default OS image, cross platform. We have made an effort for the consumer to strictly work within the browser.
- must be relatively performant to the limited goals set by the specific scenario, and within common sense technology constraints for the scenarios.

#### Consumer

- must be cross-platform: Linux, Win, MacOS. Not the same binary, just possible to install and run without too much pain.
- may enjoy more dependencies, as the system it will run on is for the most part fully owned by the operator.
- may enjoy the use of a range of technology stacks, as best suited to the job at hand as possible.

## Current Proof of Concepts

Currently exfiltration of data is accomplished with Light, Sound. Infiltration is accomplished with Touch.

## Light

We have concentrated on beaconing data over two distinct means: Pixels and QR codes

### PixelTraitor(FS)

This method employs two attempts to transfer data over beaming pixels.

A. The first attempt is using rgB Blue spectrum to encode data and pulse pixels at XXX ms over browser. 
The Consumer locks on a pixel on the screen that is beaming data, polls it for change in color, and processes subtle differences in Blue spectrum off of that pixel. 
The binary data (uploaded by the Producer) into the DOM is converted to Base64 and individual bytes are beamed. 
The issue with Base64 is that it may have several adjacent bytes that are the same. Since we are polling on a pixel change at predefined intervals, there is an edge condition that does not distinguish duplicate pixel reading from legitimate duplicate bytes beamed by the Producer. 
The code overcomes this by introducing a control byte after each data byte (fencing). This slows down the rate but increase accuracy. The Consumer has an algorithm to remove fencing. Stop and Start control pixels are used to enter and exit the pixel polling.

*Pros*

- No external dependencies for Producer.
- May be made visually undetectable to anyone looking at the screen.
- Works well for browser captive portals that are not overlayed by the remote screen rendering (RDP, VNC). Works OK with Captive VMWare video rendering.

*Cons*

- Serial pulse on one pixel. Not fast.
- Does not work well (rather consistent) with RDP/Citrix because the emitted pixel colors are not calibrated, and we need exact matches.


B. The second attempt is building on the first one in the attempt to overcome issues with the rgB scanner, and recognition of color variation. It is using the full RGB spectrum to encode data and pulse pixels at XXX ms over browser. 
It also has a pixel color adjustment algorithm to compensate for differences in how some remote captive portals generate colors. It has done that with distinct 64 colors to avoid collisions due to color variations. The Consumer locks on a pixel on the screen that is beaming 64 color data and processes data of that pixel.

*Pros*

- Color range matching improves somewhat on detection and decoding of data Consumer side.
- Still one pixel but with multicolor.

*Cons*

- Still higher than normal spread of color discrepancies.
- Speed of serial beaming.

### Usage

#### Producer 

Locally launched `file://` resource of `Light/PixelTraitor/producer/disco.html` in the browser

or 

Locally launched `file://` resource of `Light/PixelTraitor/producer/discoFS.html` in the browser

#### Consumer

- Install NodeJS. (This is not a hard requirement overall, and we will move to Java.)

- install packages via `npm` to satify the following :
```  javascript 
const robot = require("robotjs");
const sleep = require('sleep');
const fs    = require('fs');
const btoa  = require('btoa');
const commandLineArgs = require('command-line-args')
```

- Usage:
```
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
```

### DemoQRacy

This method employs beaming QRcodes. 
To compensate over the issues of color recognition by Light-Pixel implementation, an attempt was made to scan and read a screen pixel matrix on intervals. Instead of one pixel beaming the idea was to read 100x100 area and determine active (transmitting) and passive (dormant) pixels. However the approach was slow in the initial testing as we needed to find a fast way to scan over a large number of pixels while maintaining speed of the Producer beaming data to us. The solution was found with an attempt to encode data into a QRCode and use the Consumer to decode it over a predefined area on the screen. Stop and Start control QR Codes are used to enter and exit the QR polling. Data is currently saved into a file in binary mode.

*Pros*

- Reasonably fast. Block transfer, 64 byte bus.
- Reasonably reliable (QR version 13-21)
- Compact and supportable by scanners.
- No dependency on color calibration. Works over remote screen.
- No images, all DIV based rendering.
- Visually close to static image when beaming. 
- OPSec friendly.

Cons

- 80-90 bps transfer rate so takes time for anything large.
- Jquery small QR code library dependency. Luckily CDN is almost always allowed if any access to Internet is allowed. 
- Minified it can also be brought in the captive environment easily.

### Usage

#### Producer 

Locally launched `file://` resource of 
`Light/DemoQRacy/producer/qrbeamer.html` in the browser

Dependency: Direct access to `https://cdnjs.cloudflare.com/ajax/libs/lrsjng.jquery-qrcode/0.14.0` and JQuery on the live Internet, or adjust for a local copy.

#### Consumer

- Java 1.7+
- Build distribution via `Light/DemoQRacy/consumer/scripts/build.sh`

- Run:
    - Get pixel pointer coordinates: `pointer.sh`.
    - Run `./run.sh -x 40 -y 323 -f /tmp/hello.c -i 35 -v` 

Parameters:
``` 
usage: Screen
 -f,--file <F>       Absolute path to file
 -h,--help           display usage
 -i,--interval <I>   Scanner Interval
 -v,--verbose        verbose run info
 -x,--coordX <X>     X coordinate (pixel offset)
 -y,--coordY <Y>     Y coordinate (pixel offset)
```

## Touch

Use of Mouse and Keyboard events to transfer data.

### Anesthesia 

Infiltration of data (test or binary) is accomplished via producer driving a mouse over a predefined grid of pixels instructing the consumer to record the clicks and convert them into decodable stream of Base64 data. 

*Pros*

- Reasonably fast but sequential.
- Reasonably reliable decoding.
- Avoids keystroke loggers.
- Light weight on the consumer side.

*Cons*

- more dependencies on instrumented image at the consumer side
- locks up input channels

#### Usage
Roles flip. Consumer -> Remote system. Producer -> Local system.

#### Producer

- Java 1.7+ 
- Build distribution via `Touch/Anesthesia/producer/scripts/build.sh`

- Run:
    - Get pixel pointer coordinates: `getPointer.sh`.
    - Run: `runRook.sh`
```
./runRook.sh -f /Users/dimas/Code/LST/data/B64calibrate.txt -x 72 -y 221 -a 5 -A 10 -d 47 -D 47 -w 3000
```

```
usage: Rook
 -a,--delay-aaction <DELAYA>    Delay Between Atomic Mouse Event (ms)
 -A,--delay-caction <DELAYXA>   Delay Between Mouse Clicks (ms)
 -d,--x-drift <XDRIFT>          X coordinate drift tolerance (pixels).
                                Pixels between centers of tiles in image
                                map on X
 -D,--y-drift <YDRIFT>          Y coordinate drift tolerance (pixels).
                                Pixels between centers of tiles in image
                                map on Y
 -f,--file <FILE>               Absolute path to file
 -h,--help                      display usage
 -v,--verbose                   verbose run info
 -w,--window-delay <DELAYW>     Delay Target Window Focus (ms)
 -x,--coordX <X>                X coordinate (pixel offset)
 -y,--coordY <Y>                Y coordinate (pixel offset)
```

#### Consumer
Locally launched `file://` resource of 
`Touch/Anesthesia/consumer/Knight.html` in the browser

Dependency: (or any custom mapped image) 
```
Knight.css	Knight.js	checkmate.jpg
```



### KeyBorat

Infiltration of data (test or binary) is accomplished via producer posting keystrokes into a remote browser (or any other acceptable medium) for future use.

*Pros*
- Reasonably fast but sequential.
- Reasonably reliable decoding.
- Light weight on the consumer side.

*Cons*
- locks up input channels


#### Usage
Roles flip. Consumer -> Remote system. Producer -> Local system.

#### Producer

- Java 1.7+ 
- Build distribution via `Touch/KeyBorat/producer/scripts/build.sh`
- Run: `run.sh -f ../../../../data/first.c -m2`
```

usage: KeyBorat
 -a,--delay-aaction <DELAYA>    Delay Between Atomic Mouse Event (ms)
 -A,--delay-caction <DELAYXA>   Delay Between Mouse Clicks (ms)
 -f,--file <FILE>               Absolute path to file
 -h,--help                      display usage
 -m,--mode <MODE>               Mode of transfer: 
                                    1 - binary(b64)
                                    2 - text(ascii)
 -v,--verbose                   verbose run info
 -w,--window-delay <DELAYW>     Delay Target Window Focus (ms)
```

#### Consumer

Locally launched `file://` resource of 
`Touch/KeyBorat/consumer/KeyBorat.html` in the browser

Dependencies: (local)
```
KeyBorat.css	KeyBorat.html	KeyBorat.js
```


## Sound

### Via Pitch tones

This is an attempt to encode and transfer data bytes in pitch tones. The pitch is the main frequency of the waveform (the 'note' being played or sung). It is expressed as a float in Hz. We attempt to encode data alphabet into various frequencies on the producer side and decode them back on the consumer side.

In general, pitch detection is difficult to achieve for computers. Many algorithms have been designed and experimented, but there is no 'best' algorithm. They all depend on the context and the tradeoffs acceptable in terms of speed and latency. We need to solve harmonics, polyphonic sound and also do this at 100% accuracy, as there are no retransmission and checksums in the exfiltration scenarios we are solving accessible for sound transfer. We cannot take a digital snapshot of a sound data file (wav) and transfer it, we cannot do steganography in sound (also digital). We have to work in the reality of an analog sound. This PoC is what we have come up with using one of the better pitch detection libraries: https://github.com/JorenSix/TarsosDSP

### ToneDeaf 

This method employs beaming tones over a range 64 frequencies to align with Base64 data charset used to encode binary data. Producer generation of tones is happening over WebAudio API driven by the browser's JavaScript engine. The detection of sound with possible correction algorithms (FFT_FASTYIN), is driven by a metronome-like logic, decoding pitch realtime. The tradeoff of speed, high degree of accuracy requirement, sequential nature of transfer/bandwidth constraints on monophonic pitches, allows for ~ 2bps data transfer rate.

*Pros*

- Sound. Go figure a defense for this.
- Edging 98-99% reliable. Need to do more.
- Compact on the client side. Native WebAudio in browser. 
- Very small dependency footprint


*Cons*

- Slow, sequential.
- Errors still creep up (working on it). Might be decomissioned in favor of modulaton based apporach.

#### Installation and Invocation


##### Producer

Locally launched `file://` resource of 
`Sound/ToneDeaf/producer/pitch.html` in the browser

Dependencies (local):

```
webaudio_tools.js
```

##### Consumer

 - Java 1.7+ 
 - Download TarsosDSP JAR or use the bundled version (/libs) - Algos. (https://0110.be/releases/TarsosDSP/TarsosDSP-latest)
 - Download Apache Commons CLI or use the bundled version (/libs) - CLI options
    (http://apache.osuosl.org//commons/cli/binaries/commons-cli-1.4-bin.tar.gz)

 - Build distribution via `Sound/ToneDeaf/consumer/scripts/build.sh`
 - Run : `run.sh`

 ```
      SETTINGS
________________________
Output file: ./output
Data Pitch Barrier: 20
Data Pitch slack: 3
Start Pitch Mark: 150
Start Pitch Mark Barrier: 10
Stop Pitch Mark: 5000
Stop Pitch Mark Barrier: 120
Data Pitch Low Frequency: 180
Data Pitch Frequency Gap: 40

usage: PitchAnalyzer
 -b,--pitch-barrier <NUMBER(Hz)>        +/- data pitch deviation tolerance
                                        range (Hz) from base frequency
 -d,--data-file <FILE>                  Output file for incoming data
 -g,--pitch-gap <NUMBER(Hz)>            frequency intervals (Hz) to match
                                        with charset.
                                        Correlated with pitch-barrier
 -h,--help                              display usage
 -k,--pitch-slack <NUMBER>              data frequency series reading
                                        adjustment
 -l,--start-pitch-low <NUMBER(Hz)>      lowest frequency (Hz) for data
                                        analysis
 -p,--start-mark-barrier <NUMBER(Hz)>   +/- start pitch deviation
                                        tolerance range (Hz) from base
                                        frequency
 -P,--stop-mark-barrier <NUMBER(Hz)>    +/- stop pitch deviation tolerance
                                        range (Hz) from base frequency
 -s,--start-pitch-mark <NUMBER(Hz)>     mark start data recording pitch
                                        with this frequency
 -S,--stop-pitch-mark <NUMBER(Hz)>      mark stop data recording pitch
                                        with this frequency
 -v,--verbose                           verbose run info
 ```

### Via Modulation

This is an attempt to encode and transfer data bytes with QuietJS library - tone modulaton 


### DogWhistle

This method employs 

*Pros*

- Sound. Go figure a defense for this.
- Compact on the client side. Native WebAudio in browser. 
- Small QuietJS dependency footprint
- Fast.
- Can be made inaudible / ultrsonic
- Interference detection, cable based transfer.
- Ability to hook long-range transfers (e.g. BlueTooth)

*Cons*

- Small QuietJS dependency footprint

#### Installation and Invocation

```
TBD
```
