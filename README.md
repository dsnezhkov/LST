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

### Installation and Invocation 

```    
TBD
````

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

### Installation and Invocation
```
TBD
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

#### Installation and Invocation

```
TBD
```

### KeyBorat

Infiltration of data (test or binary) is accomplished via producer posting keystrokes into a remote browser (or any other acceptable medium) for future use.

*Pros*
- Reasonably fast but sequential.
- Reasonably reliable decoding.
- Light weight on the consumer side.

*Cons*
- locks up input channels

#### Installation and Invocation

```
TBD
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

```
TBD
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
