
function validarIngresoSoloNumeros(evt){
    var charCode = (evt.which) ? evt.which : event.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57)){
       return false;
    }
    return true;
 }




function validarIngresoNumeroDecimal(txt, event) {
	var charCode = (event.which) ? event.which : event.keyCode;
	if(charCode== 37 || charCode== 39 || charCode== 8){
		return true;
	}
	if (charCode == 46) {
		if (txt.value.indexOf(".") < 0)
			return true;
		else
			return false;
	}

	if (txt.value.indexOf(".") > 0) {
		var txtlen = txt.value.length;
		var dotpos = txt.value.indexOf(".");
		if ((txtlen - dotpos) > 2)
			return false;
	}

	if (charCode > 31 && (charCode < 48 || charCode > 57)){
		return false;
	}

	return true;
}

function validarIngresoNumeroDecimal4(txt, event) {
	var charCode = (event.which) ? event.which : event.keyCode;
	if(charCode== 37 || charCode== 39 || charCode== 8){
		return true;
	}
	if (charCode == 46) {
		if (txt.value.indexOf(".") < 0)
			return true;
		else
			return false;
	}

	if (txt.value.indexOf(".") > 0) {
		var txtlen = txt.value.length;
		var dotpos = txt.value.indexOf(".");
		if ((txtlen - dotpos) > 4)
			return false;
	}

	if (charCode > 31 && (charCode < 48 || charCode > 57)){
		return false;
	}

	return true;
}
