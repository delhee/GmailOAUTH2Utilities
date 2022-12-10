//https://stackoverflow.com/questions/6584635/getelementsbyclassname-doesnt-work-in-old-internet-explorers-like-ie6-ie7-i/25054465#25054465
if (!document.getElementsByClassName) {
	document.getElementsByClassName = function(search) {
		var d = document, elements, pattern, i, results = [];
		if (d.querySelectorAll) { // IE8
			return d.querySelectorAll("." + search);
		}
		if (d.evaluate) { // IE6, IE7
			pattern = ".//*[contains(concat(' ', @class, ' '), ' " + search + " ')]";
			elements = d.evaluate(pattern, d, null, 0, null);
			while ((i = elements.iterateNext())) {
				results.push(i);
			}
		} else {
			elements = d.getElementsByTagName("*");
			pattern = new RegExp("(^|\\s)" + search + "(\\s|$)");
			for (i = 0; i < elements.length; i++) {
				if (pattern.test(elements[i].className)) {
					results.push(elements[i]);
				}
			}
		}
		return results;
	}
}

// https://stackoverflow.com/questions/400212/how-do-i-copy-to-the-clipboard-in-javascript
function fallbackCopyTextToClipboard(text) {
	var textArea = document.createElement("textarea");
	textArea.value = text;

	// Avoid scrolling to bottom
	textArea.style.top = "0";
	textArea.style.left = "0";
	textArea.style.position = "fixed";

	document.body.appendChild(textArea);
	textArea.focus();
	textArea.select();

	var copyResult = "";

	try {
		var successful = document.execCommand('copy');
		var msg = successful ? 'successful' : 'unsuccessful';

		copyResult = "Verification code copied. ";
	} catch (err) {
		copyResult = "Unable to copy the verification code. ";
	}

	document.body.removeChild(textArea);

	alert(copyResult);
}

// https://stackoverflow.com/questions/400212/how-do-i-copy-to-the-clipboard-in-javascript
if (document.querySelector) {
	var copyButton = document.querySelector(".copy-button");

	// console.log(copyButton);

	if (copyButton) {
		if (copyButton.addEventListener) {
			// console.log( "copyButton.addEventListener exists" );

			copyButton.addEventListener('click', function(e) {
				var code = document.querySelector(".code").innerHTML;

				if (navigator.clipboard) {
					navigator.clipboard.writeText(code).then(function() {
						alert("Verification code copied. ");
					}, function(err) {
						alert("Unable to copy the verification code. ");
						console.error('Async: Could not copy text: ', err);
					});
				} else {
					fallbackCopyTextToClipboard(code);
				}
			});
		} else {
			copyButton.onclick = function(e) {
				fallbackCopyTextToClipboard(document.querySelector(".code").innerHTML);
			}
		}
	}
} else {
	// console.log(document.getElementsByClassName("copy-button"));
	if (document.getElementsByClassName("copy-button")) {
		if (document.getElementsByClassName("copy-button")[0]) {
			// console.log( document.getElementsByClassName("copy-button")[0] );

			document.getElementsByClassName("copy-button")[0].onclick = function(e) {
				if (document.getElementsByClassName("code")) {
					if (document.getElementsByClassName("code")[0]) {
						fallbackCopyTextToClipboard(document.getElementsByClassName("code")[0].innerHTML);
					}
				}
			}
		}
	}
}
