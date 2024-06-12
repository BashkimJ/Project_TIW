/**
 * 
 */
(function(){
	document.getElementById("regbutton").addEventListener('click', (e)=>{
		var form = e.target.closest("form");
		var name = form.username.value;
		var email = form.email.value;
		var pwd = form.password.value;
		var rep = form.reppassword.value;
		if(rep==null || pwd==null || email==null|| name==null ||name==='' || email==='' || pwd==='' || rep==='' ){
			 form.querySelector("p#errorMsg").textContent = "You have to fill all the fields";
		     return;
		}
		if(!email.includes("@")){
			form.querySelector("p#errorMsg").textContent = "Ivalid email";
			return;
		}
		if(pwd!==rep){
			form.querySelector("p#errorMsg").textContent = "Passwords not matching";
			return;
		}
		if(name.length>=40 || pwd.legth >=40 || email.length>=40 ){
			form.querySelector("p#errorMsg").textContent = "Too many characters. Each field must have at most 40.";
			return;
		}
		if(form.checkValidity()){
			makeCall("POST", 'CheckRegistration',e.target.closest("form"),
			function(x){
				if(x.readyState == XMLHttpRequest.DONE){
					var message = x.responseText;
					switch(x.status){
						case 200:
							document.getElementById("Login").hidden = false;
		                    document.getElementById("Registration").hidden = true;
							break;
						case 400:
							form.querySelector("p#errorMsg").textContent = message;
							break;
					    case 500:
							form.querySelector("p#errorMsg").textContent = message;
							break;
					}
				}
			}
			);
		}
		else{
			form.reportValidity();
		}
		
	});
})();