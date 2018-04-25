
function clearIResults() {
	incoming.value = '';
}
function clearOResults() {
	original.value = '';
}
function decodeKbd() {
	decodedData = atob(incoming.value);
	original.value = decodedData;
	//console.log(decodedData);
}

var saveData = (function () {
    var a = document.createElement("a");
    document.body.appendChild(a);
    a.style = "display: none";
    return function (byteData, dtype, fileName) {
            var blob = new Blob([byteData], {type: dtype}),
            url = window.URL.createObjectURL(blob);
        a.href = url;
        a.download = fileName;
        a.click();
        window.URL.revokeObjectURL(url);
    };
}());

// Save Decoded in text 
function saveFileA(){

    fileName = makeFName(8) + ".txt";
    saveData(original.value, "text/plain", fileName);
}
// Save Encoded
function saveFileM(){

    fileName = makeFName(8) + ".txt";
    saveData(incoming.value, "text/plain", fileName);
}

// Save Decoded in binary
function saveFileB(){

   fileName = makeFName(8) + ".bin";

   var byteDataNumbers = new Array(decodedData.length);
	for (var i = 0; i < decodedData.length; i++) {
   	byteDataNumbers[i] = decodedData.charCodeAt(i);
	}
   var byteDataBin = new Uint8Array(byteDataNumbers);
	saveData(byteDataBin, "application/octet-stream", fileName);
}

function makeFName(len) {
  var text = "";
  var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

  for (var i = 0; i < len; i++)
    text += possible.charAt(Math.floor(Math.random() * possible.length));

  return text;
}


// Main
var incoming = document.getElementById('incoming');
var original = document.getElementById('original');
incoming.scrollTop = incoming.scrollHeight;

var dbutton = document.getElementById("dbutton"); 

/*
    <button class="button" id="obutton" type="button">Decode B64</button>
    <button class="button" id="sebutton" type="button">Save Encoded (B64)</button>
    <button class="button" id="spbutton" type="button">Save Decoded (Txt)</button>
    <button class="button" id="sbbutton" type="button">Save Decoded (Bin)</button>
*/
/*
    <button class="button" id="cebutton" type="button">Clear Displayed Encoded Contents</button>
    <button class="button" id="cpbutton" type="button">Clear Displayed Plain Contents</button>
*/

var obutton = document.getElementById("obutton"); 
var spbutton = document.getElementById("spbutton"); 
var sebutton = document.getElementById("sebutton"); 
var sbbutton = document.getElementById("sbbutton"); 

var cebutton = document.getElementById("cebutton"); 
var cpbutton = document.getElementById("cpbutton"); 

var decodedData;

// Event Listeners
// Disable Tabindex off of the textarea
incoming.addEventListener('keydown',function(e) {
    if(e.keyCode === 9) { // tab was pressed
        // get caret position/selection
        var start = this.selectionStart;
        var end = this.selectionEnd;

        var target = e.target;
        var value = target.value;

        // set textarea value to: text before caret + tab + text after caret
        target.value = value.substring(0, start)
                    + "\t"
                    + value.substring(end);

        // put caret at right position again (add one for the tab)
        this.selectionStart = this.selectionEnd = start + 1;

        // prevent the focus lose
        e.preventDefault();
    }
},false);



// Decode manual buffer (B64 -> plain)
obutton.addEventListener("click", function(e) { 
    decodeKbd(e.target);
});

// Save decoded data as binary
sbbutton.addEventListener("click", function(e) { 
    saveFileB(e.target);
});

// Save decoded data as text
spbutton.addEventListener("click", function(e) { 
    saveFileA(e.target);
});

// Save encoded data as encoded data
sebutton.addEventListener("click", function(e) { 
    saveFileM(e.target);
});

// Clear encoded data container
cebutton.addEventListener("click", function(e) { 
    clearIResults(e.target);
});

// Clear decoded data container
cpbutton.addEventListener("click", function(e) { 
    clearOResults(e.target);
});

