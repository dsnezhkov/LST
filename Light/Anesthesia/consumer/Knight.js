
function printMousePos(event) {

	var div = document.getElementById('incoming');
	div.innerHTML += "X: " + event.clientX + " - Y: " + event.clientY + "\n";
}

function renderMap() {
	incoming.value=transitionalDataBag.join("");
}
function pushMap(area) {
   //console.log(area.title);
	transitionalDataBag.push(area.title);	
}
function clearResults() {
	incoming.value = '';
	original.value = '';
}
function decodeMap() {
    console.log(transitionalDataBag);
	decodedData = atob(transitionalDataBag.join(''));
	original.value = decodedData;
	console.log(decodedData);
}
function decodeKbd() {
	decodedData = atob(incoming.value);
	original.value = decodedData;
	console.log(decodedData);
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

// Human Redable
function saveFileA(){

    fileName = makeFName(8) + ".txt";
    saveData(decodedData, "text/plain", fileName);
}
// Manual entry -> txt
function saveFileM(){

    fileName = makeFName(8) + ".txt";
    saveData(incoming.value, "text/plain", fileName);
}

// Binary
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
var map = document.getElementById("map"); 
var dbutton = document.getElementById("dbutton"); 
var obutton = document.getElementById("obutton"); 
var sbutton = document.getElementById("sbutton"); 
var abutton = document.getElementById("abutton"); 
var cbutton = document.getElementById("cbutton"); 
var tbutton = document.getElementById("tbutton"); 
var decodedData;
var transitionalDataBag = [];

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


// Record map clicks into transitional data bg
map.addEventListener("click", function(e) { 
    pushMap(e.target);
});

// Dump B64 data fron transitional data bag
rbutton.addEventListener("click", function(e) { 
    renderMap(e.target);
});

// Decode transitional data bag
dbutton.addEventListener("click", function(e) { 
    decodeMap(e.target);
});

// Decode manual buffer
obutton.addEventListener("click", function(e) { 
    decodeKbd(e.target);
});

// Save decoded data as binary
sbutton.addEventListener("click", function(e) { 
    saveFileB(e.target);
});

// Save decoded data as text
abutton.addEventListener("click", function(e) { 
    saveFileA(e.target);
});

// Save manually entered data as text
abutton.addEventListener("click", function(e) { 
    saveFileM(e.target);
});

// Clear rendered results
cbutton.addEventListener("click", function(e) { 
    clearResults(e.target);
});

// Clear transitional Data bag and reload
tbutton.addEventListener("click", function(e) { 
    location.reload();
});




